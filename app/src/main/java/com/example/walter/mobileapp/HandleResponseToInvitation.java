package com.example.walter.mobileapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//classe usata per gestire la risposta agli inviti
public class HandleResponseToInvitation extends AppCompatActivity {
    private int CALENDAR_CODE = 10;
    Lock lock = new ReentrantLock();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StaticInstance.currentActivity = this;
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        setContentView(R.layout.activity_yes_notify);

        final String match = getIntent().getStringExtra("match");
        boolean accepted = getIntent().getBooleanExtra("accept", false);
        final String document = getIntent().getStringExtra("document");
        final String username = getIntent().getStringExtra("username");

        //controllo se l'invito è stato accettato o meno
        if (!accepted)
            StaticInstance.db.collection("invite").document(document).update("accept", "no");
        else {

            StaticInstance.db.collection("matches").document(match).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(((ArrayList)(task.getResult().get("partecipants"))).size()==10){
                            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(StaticInstance.currentActivity);
                            builder.setMessage("The match is already completed. You are late.");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    StaticInstance.db.collection("invite").document(document).update("accept", "no");
                                    startActivity(new Intent(getApplicationContext(), MyMatchesList.class));
                                }
                            });
                            builder.create().show();

                        }
                        else{
                            StaticInstance.db.collection("invite").document(document).update("accept", "yes");
                            HashMap player = new HashMap();
                            player.put("user", username);
                            player.put("role", getIntent().getStringExtra("role"));
                            player.put("team", getIntent().getStringExtra("team"));
                            StaticInstance.db.collection("matches").document(match).update("partecipants", FieldValue.arrayUnion(username));
                            StaticInstance.db.collection("matches").document(match).update("registered", FieldValue.arrayUnion(player));
                            StaticInstance.username = username;
                            StaticInstance.role = getIntent().getStringExtra("role");
                            addEventToCalendar(getIntent());
                            startActivity(new Intent(getApplicationContext(), MyMatchesList.class));
                        }

                    }
                }
            });

        }

    }

    private void addEventToCalendar(Intent intent) {

        boolean hasPermission = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_CALENDAR)
                    == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, CALENDAR_CODE);
            }
        }
        if (hasPermission) {
            AddEventToCalendar task = new AddEventToCalendar(intent);
            task.execute();
        }

    }


    //task per aggiungere in modo asincrono l'evento al calendario
     private class AddEventToCalendar extends AsyncTask<Void, Integer, Long> {

         Intent intent;

         public AddEventToCalendar(Intent saveMyMatch) {
             this.intent = saveMyMatch;
         }

         @Override
         protected Long doInBackground(Void... voids) {

             long startMillis = 0;
             long endMillis = 0;
             Calendar beginTime = Calendar.getInstance();
             String date[] = intent.getStringExtra("date").split("/");
             beginTime.set(Integer.valueOf(date[2]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[0]), Integer.valueOf(intent.getStringExtra("time")), 0);
             startMillis = beginTime.getTimeInMillis();
             Calendar endTime = Calendar.getInstance();
             endTime.set(Integer.valueOf(date[2]), Integer.valueOf(date[1]) - 1, Integer.valueOf(date[0]), Integer.valueOf(intent.getStringExtra("time")), 50);
             endMillis = endTime.getTimeInMillis();


             ContentResolver cr = getContentResolver();
             ContentValues values = new ContentValues();
             values.put(CalendarContract.Events.DTSTART, startMillis);
             values.put(CalendarContract.Events.DTEND, endMillis);
             values.put(CalendarContract.Events.TITLE, "Football match");
             String is = "is";
             if ((boolean) intent.getBooleanExtra("covered", false))
                 is = "is not";
             values.put(CalendarContract.Events.DESCRIPTION, "You have been invited by " + intent.getStringExtra("manager") + ". The pitch " + is + " covered.");
             values.put(CalendarContract.Events.CALENDAR_ID, 1);
             values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().getTimeZone().getID());
             values.put(CalendarContract.Events.EVENT_LOCATION, intent.getStringExtra("address"));
             values.put(CalendarContract.Events.HAS_ALARM, true);
             values.put(CalendarContract.Events.ALL_DAY, 0);
             cr.insert(CalendarContract.Events.CONTENT_URI, values);
             return Long.valueOf(0);
         }

     }
}


