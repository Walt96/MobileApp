package com.example.walter.mobileapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreatePitch extends AppCompatActivity {

    EditText addressEditText;
    EditText cityEditText;
    EditText priceEditText;
    RadioButton coveredPitch;
    FirebaseFirestore db = StaticInstance.getInstance();
    ProgressDialog progressDialog;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pitch);

        addressEditText = findViewById(R.id.pitchAddress);
        cityEditText = findViewById(R.id.city);
        priceEditText = findViewById(R.id.price);
        coveredPitch = findViewById(R.id.coveredPitch);
        progressDialog = new ProgressDialog(this);

        username = getIntent().getStringExtra("username");
    }

    @Override
    protected void onResume() {
        super.onResume();
        StaticInstance.currentActivity = this;
    }

    public void validateFields(View v) {

        String address = addressEditText.getText().toString();
        String city = cityEditText.getText().toString();
        double price = 0;
        boolean isCovered = coveredPitch.isChecked();
        boolean validField = true;

        if (!address.matches("((via)|(piazza)|(contrada)|(corso)).*n(\\d)+")) {
            addressEditText.setError("Please check your address: it's not valid");
            validField = false;
        }
        if (city.isEmpty()) {
            cityEditText.setError("Please insert a city");
            validField = false;
        }
        if (priceEditText.getText().length() == 0) {
            priceEditText.setError("Do you really want to make your pitch free? :)");
            validField = false;
        }
        else {
            String value_price = priceEditText.getText().toString();
            price = Double.valueOf(value_price);
        }
        if (validField) {

            Toast toast = Toast.makeText(getApplicationContext(), "Adding your pitch", Toast.LENGTH_SHORT);
            toast.show();

            final Map<String, Object> pitch = new HashMap<>();
            pitch.put("owner", username);
            pitch.put("address", address);
            pitch.put("city", city);
            pitch.put("price", price);
            pitch.put("covered",isCovered);
            pitch.put("owner",username);

            db.collection("pitch")
                    .add(pitch)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String current_user = getSharedPreferences("logged user", Context.MODE_PRIVATE).getString("user","");
                            //mostro l'esito dell'aggiunta solo se il proprietario è il
                            if(pitch.get("owner").equals(current_user)){
                                //se intanto ha cambiato activity da errore
                                AlertDialog.Builder builder = new AlertDialog.Builder(StaticInstance.currentActivity);
                                builder.setMessage(current_user+", your pitch was created successfully");
                                builder.create().show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String current_user = getSharedPreferences("logged user", Context.MODE_PRIVATE).getString("user","");
                            if(pitch.get("owner").equals(current_user)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(StaticInstance.currentActivity);
                                builder.setMessage("An error occured, please try again!");
                                builder.create().show();
                            }
                        }
                    });

        }

    }

    private Context getActivity() {
        return this;
    }






}
