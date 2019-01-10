package com.example.walter.mobileapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.walter.mobileapp.aiobject.ChoosedResult;
import com.example.walter.mobileapp.aiobject.NotAvailable;
import com.example.walter.mobileapp.aiobject.Pitch;
import com.example.walter.mobileapp.aiobject.Time;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    CustomAdapter adapter;
    ArrayList<ChoosedResult> matchSelectedByAI;
    HashMap<String,com.example.walter.mobileapp.Pitch> pitchesSelectedByAI;
    private int CALENDAR_CODE = 10;
    final HashMap<String, Object> saveMyMatch = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_helper);
        matchSelectedByAI = new ArrayList<>();
        pitchesSelectedByAI = new HashMap<>();
        ListView listView = findViewById(R.id.matchList);
        listView.setAdapter(adapter = new CustomAdapter());
        if(!CheckConnection.isConnected(this)){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("You don't have internet connection, please check it!")
                        .setTitle("An error occurred");
                builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), UserHome.class));
                    }
                }).setPositiveButton("Check now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    }
                });
                builder.create().show();
        }else {
            computeBestMatch();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private void loadPitchValueFromDB(final String code) {
        Log.e("codicee_",code.substring(1,code.length()-1));
        StaticInstance.db.collection("pitch").document(code.substring(1,code.length()-1)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot query = task.getResult();
                    String address = query.get("address").toString();
                    String city = query.get("city").toString();
                    float price = Float.valueOf(query.get("price").toString());
                    boolean covered = (boolean)query.get("covered");
                    final com.example.walter.mobileapp.Pitch toAdd = new com.example.walter.mobileapp.Pitch(address+", "+city,price,covered,"");
                    toAdd.setOwner(query.get("owner").toString());
                    toAdd.setOwnermail(query.get("ownermail").toString());

                    pitchesSelectedByAI.put(code, toAdd);
                    adapter.notifyDataSetChanged();
                    StaticInstance.mStorageRef.child("pitch/" + query.get("owner").toString() + code.substring(1,code.length()-1)).getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    toAdd.setUri(uri);
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
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
                Log.e("outputDLV",output.toString());
                if (!(output instanceof AnswerSets))
                    return;
                AnswerSets answerSets = (AnswerSets) output;
                if (answerSets.getAnswersets().isEmpty())
                    return;

                for (AnswerSet as : answerSets.getAnswersets()) {
                    try {
                        for (Object obj : as.getAtoms()) {
                            if (obj instanceof ChoosedResult) {
                                ChoosedResult choosedResult = (ChoosedResult) obj;

                                if (!choosedResult.getMatchtime().equals("\"-1\"")) {
                                    Log.e("almeno uno","");
                                    matchSelectedByAI.add(choosedResult);
                                    if(!pitchesSelectedByAI.containsKey(choosedResult.getPitchcode()))
                                       loadPitchValueFromDB(choosedResult.getPitchcode());
                                    //else
                                      //  adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Handle Exception
                    }
                }
            }

        });
    }

    class CustomAdapter extends BaseAdapter {

        final HashMap<String, Object> saveMyMatch = new HashMap<>();

        @Override
        public int getCount() {
            return matchSelectedByAI.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            convertView = inflater.inflate(R.layout.info_match, parent, false);

            final com.example.walter.mobileapp.Pitch currentPitch = pitchesSelectedByAI.get(matchSelectedByAI.get(position).getPitchcode());

            //perche nel layout originale c'è booking che ora non serve
            ((TextView) (convertView.findViewById(R.id.bookingby_))).setText("Price:");
            ((TextView) (convertView.findViewById(R.id.registered_))).setVisibility(View.INVISIBLE);
            ((TextView) (convertView.findViewById(R.id.registered__))).setVisibility(View.INVISIBLE);

            TextView pitchAddress = convertView.findViewById(R.id.address_view);
            pitchAddress.setText(currentPitch.getAddress());

            final TextView date = convertView.findViewById(R.id.date);
            date.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

            final TextView time = convertView.findViewById(R.id.time);
            time.setText(matchSelectedByAI.get(position).getMatchtime());

            TextView covered = convertView.findViewById(R.id.covered);
            covered.setText("Yes");
            if (!currentPitch.isCovered())
                covered.setText("No");

            //nel layout originale c'era booking ma ora lo uso per il prezzo
            TextView price = convertView.findViewById(R.id.bookingby);
            price.setText(String.valueOf(currentPitch.getPrice()) + " euro");

            ImageView image = convertView.findViewById(R.id.pitchImage);
            Uri imageUri = currentPitch.getUri();
            if (imageUri != null) {
                Glide.with(convertView)
                        .load(imageUri)
                        .into(image);
            } else {
                Glide.with(convertView)
                        .load(Uri.parse("android.resource://com.example.walter.mobileapp/" + R.drawable.login))
                        .into(image);
            }

            ((Button) convertView.findViewById(R.id.confirm)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bookPitch(matchSelectedByAI.get(position).getPitchcode().substring(1,matchSelectedByAI.get(position).getPitchcode().length()-1), currentPitch, String.valueOf(date.getText()), String.valueOf(time.getText()));
                }
            });

            return convertView;

        }

        private void bookPitch(String pitchcode, com.example.walter.mobileapp.Pitch current, String date, String time) {
            ConnectivityManager cm =
                    (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            if (!isConnected) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setMessage("You don't have internet connection, please check it!")
                        .setTitle("An error occurred");
                builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("Check now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    }
                });
                builder.create().show();


            } else {
                //inserire nel if il controllo per vedere se è stato prenotato
                if (false) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                    builder.setMessage("The pitch you selected is already booked at this time, sorry!")
                            .setTitle("An error occurred");
                    builder.create().show();
                } else {
                    final String matchCode = String.valueOf(new Date().getTime()) + StaticInstance.username;

                    //aggiungo la nuova partita alla collezione matches nel documento data corrente + manager in modo da renderlo univoco con la concorrenza
                    saveMyMatch.put("date", date);
                    saveMyMatch.put("time", time.substring(1,time.length()-1));
                    saveMyMatch.put("manager", StaticInstance.username);
                    saveMyMatch.put("pitchcode", pitchcode);
                    saveMyMatch.put("address", current.getAddress());
                    saveMyMatch.put("covered", current.isCovered());
                    saveMyMatch.put("code", matchCode);
                    saveMyMatch.put("pitchmanager", current.getOwner());
                    saveMyMatch.put("managermail", current.getOwnermail());


                    addCalendarEvent(saveMyMatch);

                }
            }

        }
    }


    private long addCalendarEvent(HashMap<String, Object> saveMyMatch) {

        boolean hasPermission = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true;
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, CALENDAR_CODE);
            }
        }
        if (hasPermission) {
            AiHelper.AddEventToCalendar task = new AiHelper.AddEventToCalendar(saveMyMatch);
            task.execute();
        }
        return -1;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CALENDAR_CODE)
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addCalendarEvent(saveMyMatch);
            }
    }


    private Activity getActivity() {
        return this;
    }

    private class AddEventToCalendar extends AsyncTask<Void, Integer, Long> {

            HashMap saveMyMatch;
            ProgressDialog progressDialog;

            public AddEventToCalendar(HashMap saveMyMatch) {
                this.saveMyMatch = saveMyMatch;
            }

            @Override
            protected Long doInBackground(Void... voids) {

                long startMillis = 0;
                long endMillis = 0;
                Calendar beginTime = Calendar.getInstance();
                String date[] = saveMyMatch.get("date").toString().split("/");
                beginTime.set(Integer.valueOf(date[2]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[0]), Integer.valueOf(saveMyMatch.get("time").toString()), 0);
                startMillis = beginTime.getTimeInMillis();
                Calendar endTime = Calendar.getInstance();
                endTime.set(Integer.valueOf(date[2]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[0]), Integer.valueOf(saveMyMatch.get("time").toString()), 50);
                endMillis = endTime.getTimeInMillis();


                ContentResolver cr = getContentResolver();
                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.DTSTART, startMillis);
                values.put(CalendarContract.Events.DTEND, endMillis);
                values.put(CalendarContract.Events.TITLE, "Football match");
                String is = "is";
                if ((boolean) saveMyMatch.get("covered"))
                    is = "is not";
                values.put(CalendarContract.Events.DESCRIPTION, "The match was organized by you. The pitch " + is + " covered.");
                values.put(CalendarContract.Events.CALENDAR_ID, 1);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().getTimeZone().getID());
                values.put(CalendarContract.Events.EVENT_LOCATION, saveMyMatch.get("address").toString());
                values.put(CalendarContract.Events.HAS_ALARM, true);
                values.put(CalendarContract.Events.ALL_DAY, 0);
                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                return Long.parseLong(uri.getLastPathSegment());
            }

            protected void onPostExecute(Long result) {
                saveMyMatch.put("calendarid", result);
                HashMap myProfile = new HashMap();
                myProfile.put("user", StaticInstance.username);
                myProfile.put("role", StaticInstance.role);
                myProfile.put("team", "A");
                ArrayList firstRegistered = new ArrayList();
                firstRegistered.add(myProfile);
                saveMyMatch.put("registered", firstRegistered);

                ArrayList myName = new ArrayList();
                myName.add(StaticInstance.username);
                saveMyMatch.put("partecipants", myName);

                saveMyMatch.put("finished", false);
                ArrayList confirmed = new ArrayList();
                confirmed.add(StaticInstance.username);
                saveMyMatch.put("confirmed", confirmed);

                StaticInstance.db.collection("matches").document(saveMyMatch.get("code").toString())
                        .set(saveMyMatch)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                sendMailToOwnerPitch();
                                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.view), "Pitch booked successfully!", Snackbar.LENGTH_LONG);
                                mySnackbar.setAction("View all matches", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getApplicationContext(), MyMatchesList.class);
                                        startActivity(intent);
                                    }
                                });
                                mySnackbar.show();
                            }
                        });


            }

            private void sendMailToOwnerPitch() {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{saveMyMatch.get("managermail").toString()});
                i.putExtra(Intent.EXTRA_SUBJECT, "Booking pitch");
                i.putExtra(Intent.EXTRA_TEXT   , "Hi, I have booked your pitch located in "+saveMyMatch.get("address")+" for the day "+saveMyMatch.get("date")+" at the "+saveMyMatch.get("time")+ ":00.");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

