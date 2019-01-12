package com.example.walter.mobileapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.example.walter.mobileapp.utility.CheckConnection;
import com.example.walter.mobileapp.R;
import com.example.walter.mobileapp.utility.StaticInstance;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// Activity utilizzata per poter effettuare la modifica di un campetto.
public class ModifyPitch extends AppCompatActivity {

    // Riferimento a Firebase, utilizzato per poter effettuare query sul database.
    FirebaseFirestore db = StaticInstance.getInstance();

    // Campi relativi al campetto che si sta per modificare.
    String username;
    String path;
    String id;
    double price;
    boolean covered;

    EditText priceEditText;
    RadioButton coveredPitch;
    RadioButton uncoveredPitch;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_pitch);

        // Si ottengono i parametri passati tramite intento.
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        price = Double.parseDouble(intent.getStringExtra("price"));
        covered = Boolean.valueOf(intent.getStringExtra("covered"));


        username = StaticInstance.username;
        priceEditText = findViewById(R.id.price);
        coveredPitch = findViewById(R.id.coveredPitch);
        uncoveredPitch = findViewById(R.id.uncoveredPitch);
        progressDialog = new ProgressDialog(this);
        path = null;

        priceEditText.setText(String.valueOf(price));

        if (covered) {
            coveredPitch.toggle();
        } else {
            uncoveredPitch.toggle();
        }

    }

    // Funzione utilizzata per verificare che i campi immessi siano corretti.
    public void validateFields(View v) {

        double actualPrice = 0;
        boolean isCovered = coveredPitch.isChecked();
        boolean validField = true;

        if (priceEditText.getText().length() == 0) {
            priceEditText.setError("Do you really want to make your pitch free? :)");
            validField = false;
        } else {
            String value_price = priceEditText.getText().toString();
            actualPrice = Double.valueOf(value_price);
        }

        if (validField) {
            if (!CheckConnection.isConnected(this)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

                // Se il dispositivo è correttamente connessio, allora inoltro la query per effettuare
                // l'aggiornamento del campetto.
                boolean makeUpdate = false;

                progressDialog.setMessage("Modifying your pitch...");
                progressDialog.show();

                Map<String, Object> updates = new HashMap<>();

                if (isCovered != covered) {
                    makeUpdate = true;
                    updates.put("covered", isCovered);
                }

                if (price != actualPrice) {
                    makeUpdate = true;
                    updates.put("price", actualPrice);
                }

                if (makeUpdate) {
                    StaticInstance.db.collection("pitch").document(id).update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void avoid) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage("Your pitch was updated successfully");
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
        }

    }

    private Context getActivity() {
        return this;
    }
}
