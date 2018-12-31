package com.example.walter.mobileapp;

import android.Manifest;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class YesNotify extends AppCompatActivity {
    private int CALENDAR_CODE = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        setContentView(R.layout.activity_yes_notify);

        String match = getIntent().getStringExtra("match");
        boolean accepted = getIntent().getBooleanExtra("accept", false);
        String document = getIntent().getStringExtra("document");
        String username = getIntent().getStringExtra("username");

        if (!accepted)
            StaticInstance.db.collection("invite").document(document).update("accept", "no");
        else {
            StaticInstance.db.collection("invite").document(document).update("accept", "yes");
            StaticInstance.db.collection("matches").document(match).update("partecipants", FieldValue.arrayUnion(username));
            HashMap player = new HashMap();
            player.put("user", username);
            player.put("role", getIntent().getStringExtra("role"));
            player.put("team", getIntent().getStringExtra("team"));
            StaticInstance.db.collection("matches").document(match).update("partecipants", FieldValue.arrayUnion(username));
            StaticInstance.db.collection("matches").document(match).update("registered", FieldValue.arrayUnion(player));
            StaticInstance.username = username;
            StaticInstance.role = getIntent().getStringExtra("role");
            addEventToCalendar(getIntent());
            startActivity(new Intent(this, MyMatchesList.class));

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
             values.put(CalendarContract.Events.DESCRIPTION, "The match was organized by " + intent.getStringExtra("manager") + ". The pitch " + is + " covered.");
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


