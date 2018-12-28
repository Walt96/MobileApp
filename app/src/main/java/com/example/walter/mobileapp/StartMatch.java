package com.example.walter.mobileapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

public class StartMatch extends AppCompatActivity {

    Chronometer match;
    Chronometer change;
    long stop_match;
    long stop_change;

    Button pause;
    Button end;
    Button start;
    Button resume;
    Button change_;

    MediaPlayer mp;
    boolean started;

    String matchcode;
    String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_match);
        mp =  MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        started = false;
        change = findViewById(R.id.change);
        match = findViewById(R.id.match);

        pause = findViewById(R.id.pause);
        end = findViewById(R.id.end);
        start = findViewById(R.id.start);
        resume = findViewById(R.id.resume);
        change_ = findViewById(R.id.change_);

        change.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
               if(SystemClock.elapsedRealtime()-chronometer.getBase()>1000*60) {
                   if(!started) {
                       started = true;
                       mp.start();
                   }
               }
            }
        });

        username = StaticInstance.username;
        matchcode = getIntent().getStringExtra("matchcode");
    }

    public void start(View view) {
        change.setBase(SystemClock.elapsedRealtime()); // set base time for a chronometer
        change.start();
        match.setBase(SystemClock.elapsedRealtime()); // set base time for a chronometer
        match.start();

        end.setVisibility(View.INVISIBLE);
        pause.setVisibility(View.VISIBLE);
    }

    public void pause(View view) {
        stop_match = SystemClock.elapsedRealtime();
        stop_change = SystemClock.elapsedRealtime();

        match.stop();
        change.stop();


        end.setVisibility(View.VISIBLE);
        pause.setVisibility(View.INVISIBLE);
        resume.setVisibility(View.VISIBLE);
        start.setVisibility(View.INVISIBLE);

    }

    public void resume(View view){
        match.setBase(match.getBase()+SystemClock.elapsedRealtime()-stop_match);
        match.start();

        change.setBase(change.getBase()+SystemClock.elapsedRealtime()-stop_change);
        change.start();
        end.setVisibility(View.INVISIBLE);
        pause.setVisibility(View.VISIBLE);
        resume.setVisibility(View.INVISIBLE);
        start.setVisibility(View.VISIBLE);

    }

    public void change(View view){
        change.setBase(SystemClock.elapsedRealtime());
        stop_change=SystemClock.elapsedRealtime();

        if(started){
            started = false;
            mp.stop();
        }
    }

    public void end(View view){
        Intent intent = new Intent(this,RatePlayer.class);
        intent.putExtra("matchcode",matchcode);
        startActivity(intent);
    }
}
