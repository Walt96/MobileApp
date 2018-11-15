package com.example.walter.mobileapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SigninActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        Spinner dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"Attaccante","Centrocampista","Difensore","Portiere"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinneritem, items);
        dropdown.setAdapter(adapter);
    }
}
