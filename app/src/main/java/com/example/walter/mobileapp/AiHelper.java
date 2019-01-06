package com.example.walter.mobileapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.walter.mobileapp.aiobject.ChoosedResult;
import com.example.walter.mobileapp.aiobject.NotAvailable;
import com.example.walter.mobileapp.aiobject.Pitch;
import com.example.walter.mobileapp.aiobject.Time;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import it.unical.mat.embasp.base.Callback;
import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.languages.asp.ASPInputProgram;
import it.unical.mat.embasp.languages.asp.AnswerSet;
import it.unical.mat.embasp.languages.asp.AnswerSets;
import it.unical.mat.embasp.platforms.android.AndroidHandler;
import it.unical.mat.embasp.specializations.dlv.android.DLVAndroidService;

public class AiHelper extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_helper);
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        computeBestMatch();
    }

    private void computeBestMatch() {
        collectData();
    }

    private void collectData() {
        final InputProgram inputProgram = new ASPInputProgram();
        StaticInstance.db.collection("users").document(StaticInstance.username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<HashMap> preferences = new ArrayList<>();
                    preferences = (ArrayList<HashMap>) task.getResult().get("preferences");

                    ArrayList<Pitch> pitch = new ArrayList<>();
                    ArrayList<Time> time = new ArrayList<>();
                    for(int i = 8;i<=22;i++){
                        time.add(new Time(String.valueOf(i),0,0));
                    }

                    //fare una query per prendere indirizzo e calcolare distanza e prendere il prezzo o portare tutti i dati in preferences
                    for(HashMap rate:preferences){
                        int index = pitch.indexOf(new Pitch(rate.get("pitch").toString(),0,0,0,0));
                        if(index != -1) {
                            Pitch current = pitch.get(index);
                            current.setNumMatchInPitch(current.getNumMatchInPitch()+1);
                            current.setRate(current.getRate()+Integer.valueOf(String.valueOf((Long)(rate.get("rate")))));
                        }else{
                            Pitch toAdd = new Pitch(rate.get("pitch").toString(),0,0,Integer.valueOf(String.valueOf((Long)(rate.get("rate")))),1);
                            pitch.add(toAdd);
                        }

                        Time currentTime = time.get(Integer.valueOf(rate.get("time").toString())-8);
                        currentTime.setNumTimesAtThisTime(currentTime.getNumTimesAtThisTime()+1);
                        currentTime.setRate(currentTime.getRate()+Integer.valueOf(String.valueOf((Long)(rate.get("rate")))));
                    }

                    for(Pitch pitch_: pitch) {
                        pitch_.setRate(pitch_.getRate() / pitch_.getNumMatchInPitch());
                        try {
                            inputProgram.addObjectInput(pitch_);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                    for(Time time_: time) {
                        if(time_.getNumTimesAtThisTime()!=0)
                            time_.setRate(time_.getRate() / time_.getNumTimesAtThisTime());
                        try {
                            inputProgram.addObjectInput(time_);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                    StaticInstance.db.collection("matches").whereEqualTo("date",dateFormat.format(new Date())).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot result : task.getResult()){
                                    try {
                                        inputProgram.addObjectInput(new NotAvailable(result.get("pitchcode").toString(),result.get("time").toString()));
                                    } catch (Exception e) {
                                    }
                                }

                                //dlv bug altrimenti non restituisce output
                                try {
                                    inputProgram.addObjectInput(new ChoosedResult("-1","-1"));
                                } catch (Exception e) {

                                }
                                startReasoning(inputProgram);
                            }
                        }
                    });
                    }
            }
        });
    }

    private void startReasoning(InputProgram inputProgram) {
        Handler handler = new AndroidHandler(getApplicationContext(), DLVAndroidService.class);
        handler.addProgram(inputProgram);
        handler.addProgram(new InputProgram("\n" +
                "choosePitch(Code) | notChoosePitch(Code) :- pitch(Code,_,_,_,_).\n" +
                ":- #count{Code:choosePitch(Code)}>1.\n" +
                ":- #count{Code:choosePitch(Code)}<1.\n" +
                "\n" +
                "chooseTime(Time) | notChooseTime(Time) :- time(Time,_,_).\n" +
                ":- #count{Time:chooseTime(Time)}>1.\n" +
                ":- #count{Time:chooseTime(Time)}<1.\n" +
                "\n" +
                ":~ choosePitch(Pitch),pitch(Pitch,Distance,Price,Rate,_), ConsiderRate=20-Rate, ConsiderAll = ConsiderRate+Distance. [ConsiderAll : 3]\n" +
                ":~ choosePitch(Pitch),pitch(Pitch,Distance,Price,Rate,_), ConsiderPrice = 10 - Price. [ConsiderPrice : 2]\n" +
                "\n" +
                ":~ chooseTime(Time),time(Time,Rate,_), ConsiderRate = 10 - Rate. [ConsiderRate : 4]\n" +
                "\n" +
                "choosedResult(X,Y) :- choosePitch(X),chooseTime(Y).\n" +
                ":- choosedResult(Pitch,Time),notAvailable(Pitch,Time).\n" +
                " \n" +
                "\n"));

        handler.startAsync(new Callback() {
            @Override
            public void callback(Output output) {
                if(!(output instanceof AnswerSets))
                    return;
                AnswerSets answerSets=(AnswerSets)output;
                if(answerSets.getAnswersets().isEmpty())
                    return;
                AnswerSet as = answerSets.getAnswersets().get(0);
                try {
                    for(Object obj:as.getAtoms()) {
                        if(obj instanceof  ChoosedResult) {
                            ChoosedResult choosedResult = (ChoosedResult) obj;
                            if(!choosedResult.getMatchtime().equals("\"-1\""))
                                ((TextView)findViewById(R.id.result)).setText(choosedResult.getPitchcode()+" "+choosedResult.getMatchtime());
                        }
                    }
                } catch (Exception e) {
                    // Handle Exception
                }
            }
        });
    }
}
