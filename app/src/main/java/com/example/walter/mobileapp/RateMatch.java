package com.example.walter.mobileapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;

public class RateMatch extends AppCompatActivity {

    int rate;
    String matchcode;
    String time;
    boolean infoRetrieved;
    String pitch;
    float lat;
    float lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rate = -1;
        setContentView(R.layout.activity_rate_match);
        infoRetrieved = false;
        matchcode = getIntent().getStringExtra("matchcode");

        //cerco nel db le informazioni relative alla partita che l'utente sta votando
        StaticInstance.db.collection("matches").document(matchcode).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    lat = Float.valueOf(String.valueOf(task.getResult().get("lat")));
                    lon = Float.valueOf(String.valueOf(task.getResult().get("lon")));

                    time = task.getResult().get("time").toString();
                    pitch = task.getResult().get("pitchcode").toString();
                    infoRetrieved = true;
                }
            }

        });
        final TextView rateDescription = findViewById(R.id.rateDescription);
        ((RatingBar) findViewById(R.id.ratingBar)).setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rate = (int)(rating*2);
                if(rate<=4)
                    rateDescription.setText("Bad");
                else if(rate<=6)
                    rateDescription.setText("Good");
                else if(rate<=8)
                    rateDescription.setText("Perfect");
                else
                    rateDescription.setText("Excellent");
            }
        });
    }

    //aggiungo nel db il voto dell'utente
    public void confirmRate(View v) {
        if (rate == -1)
            ((Button) v).setError("Please, rate this match!");
        else{
            if(infoRetrieved){
                HashMap rate_ = new HashMap();
                rate_.put("pitch",pitch);
                rate_.put("time",time);
                rate_.put("rate",rate);
                rate_.put("lat",lat);
                rate_.put("lon",lon);

                StaticInstance.db.collection("users").document(StaticInstance.username).update("preferences", FieldValue.arrayUnion(rate_));
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Thanks!");
                builder.setMessage("We saved your rate and we'll use it for advice you!");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(),UserHome.class));
                    }
                });
                builder.create().show();
            }
        }
    }
}
