package com.example.walter.mobileapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;

public class YesNotify extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yes_notify);

        String match = getIntent().getStringExtra("match");
        boolean accepted = getIntent().getBooleanExtra("accept",false);
        String document = getIntent().getStringExtra("document");
        String username = getIntent().getStringExtra("username");

        if(!accepted)
            StaticInstance.db.collection("invite").document(document).update("accept","no");
        else{
            StaticInstance.db.collection("invite").document(document).update("accept","yes");
            StaticInstance.db.collection("matches").document(match).update("partecipants",FieldValue.arrayUnion(username));
            HashMap player = new HashMap();
            player.put("user",username);
            player.put("role",getIntent().getStringExtra("role"));
            player.put("team",getIntent().getStringExtra("team"));
            StaticInstance.db.collection("matches").document(match).update("partecipants",FieldValue.arrayUnion(username));
            StaticInstance.db.collection("matches").document(match).update("registered",FieldValue.arrayUnion(player));
        }


    }
}
