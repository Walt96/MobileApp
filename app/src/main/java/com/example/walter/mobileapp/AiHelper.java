package com.example.walter.mobileapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.walter.mobileapp.aiobject.Pitch;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import it.unical.mat.embasp.base.Callback;
import it.unical.mat.embasp.base.Handler;
import it.unical.mat.embasp.base.InputProgram;
import it.unical.mat.embasp.base.Output;
import it.unical.mat.embasp.languages.asp.ASPInputProgram;
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
                    }

                    for(Pitch pitch_: pitch) {
                        pitch_.setRate(pitch_.getRate() / pitch_.getNumMatchInPitch());
                        try {
                            inputProgram.addObjectInput(new Pitch(pitch_.getCode(),pitch_.getDistance(),pitch_.getPrice(),pitch_.getRate(),pitch_.getNumMatchInPitch()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    startReasoning(inputProgram);
                }
            }
        });
    }

    private void startReasoning(InputProgram inputProgram) {
        Handler handler = new AndroidHandler(getApplicationContext(), DLVAndroidService.class);
        handler.addProgram(inputProgram);
        Log.e("program",inputProgram.getPrograms());
        handler.startAsync(new Callback() {
            @Override
            public void callback(Output output) {
                //codice
            }
        });
        Log.e("cccc","cc");
    }
}
