package com.example.walter.mobileapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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
    FirebaseFirestore db = StaticDbInstance.getInstance();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pitch);

        addressEditText = findViewById(R.id.address);
        cityEditText = findViewById(R.id.city);
        priceEditText = findViewById(R.id.price);
        coveredPitch = findViewById(R.id.coveredPitch);
        progressDialog = new ProgressDialog(this);
    }
    public void validateFields(View v) {
        //questo dato arriverà nell'intent che verra passato dal menu del creatore del campo
        String username = "ciao";
        String address = addressEditText.getText().toString();
        String city = cityEditText.getText().toString();
        float price = 0;
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
        else
            price = Float.valueOf(priceEditText.getText().toString());

        if (validField) {
            progressDialog.setMessage("Adding your pitch...");
            progressDialog.show();
            Map<String, Object> pitch = new HashMap<>();
            pitch.put("owner", username);
            pitch.put("address", address);
            pitch.put("city", city);
            pitch.put("price", price);
            pitch.put("covered",isCovered);
            db.collection("pitch")
                    .add(pitch)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage("Your pitch was created successfully");
                            builder.create().show();
                            progressDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage("An error occured, please try again!");
                            builder.create().show();
                            progressDialog.dismiss();
                        }
                    });
        }

    }

    private Context getActivity() {
        return this;
    }
}
