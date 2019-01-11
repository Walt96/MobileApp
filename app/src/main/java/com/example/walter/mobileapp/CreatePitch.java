package com.example.walter.mobileapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/*
    Classe utilizzata per la creazione di un nuovo campetto da parte di un gestore.
 */
public class CreatePitch extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_CODE = 2;

    // Elementi del Layout
    EditText cityEditText;
    EditText priceEditText;
    RadioButton coveredPitch;

    // Riferimento a Firebase, utilizzato per effettuare le query
    FirebaseFirestore db = StaticInstance.getInstance();

    // Progress Dialog, utilizzato per fornire un feedback riguardo lo stato delle operazioni
    ProgressDialog progressDialog;

    // Dati relativi al campo che si sta aggiungendo.
    String username; // Username del gestore
    String path; // Path dell'immagine associata al campetto
    Bitmap photo; // Bitmap rappresentate la foto del campetto
    // Coordinate del campetto, utilizzate per localizzarlo sulla mappa.
    private Double latitude;
    private Double longitude;
    private String address; // Indirizzo del campetto

    // Valori utilizzati per identificare una richiesta effettuata ad un'altra activity
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_ADDRESS_INFO = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pitch);

        username = StaticInstance.username;
        cityEditText = findViewById(R.id.city);
        priceEditText = findViewById(R.id.price);
        coveredPitch = findViewById(R.id.coveredPitch);
        progressDialog = new ProgressDialog(this);
        path = null;

        // Inizializzazione della barra di ricerca dei luoghi di google map.
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Dichiaro una funzione di Callback, utilizzata ogni qual volta un luogo viene selezionato
        // dalla barra di ricerca.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                try {

                    // Ricavo le coordinate ottenute dal luogo selezionato tramite barra.
                    latitude = place.getLatLng().latitude;
                    longitude = place.getLatLng().longitude;

                    // Inizializzo un geocoder, per ottenere ulteriori informazioni riguardanti
                    // il luogo selezionato mediante le coordinate ottenute.
                    Geocoder mGeocoder = new Geocoder(getActivity(), Locale.getDefault());
                    List<Address> addresses = null;
                    addresses = mGeocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && addresses.size() > 0) {

                        // Ottengo informazioni quali via, città o numero civico del luogo ricercato.
                        Address selectedAddress = addresses.get(0);
                        String streetName = selectedAddress.getThoroughfare();
                        String number = selectedAddress.getSubThoroughfare();
                        address = streetName;
                        if(number != null) {
                            address += " " + number;
                        }
                        String city = selectedAddress.getLocality();
                        cityEditText.setText(city);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Status status) {
                Log.i("", "An error occurred: " + status);
            }
        });

        ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextColor(Color.WHITE);

    }

    // Funzione utilizzata per verificare che tutti i campi siano presenti e corretti.
    public void validateFields(View v) {

        String city = cityEditText.getText().toString();
        double price = 0;
        boolean isCovered = coveredPitch.isChecked();
        boolean validField = true;


        if (city.isEmpty()) {
            cityEditText.setError("Please insert a city");
            validField = false;
        }
        if (priceEditText.getText().length() == 0) {
            priceEditText.setError("Do you really want to make your pitch free? :)");
            validField = false;
        } else {
            String value_price = priceEditText.getText().toString();
            price = Double.valueOf(value_price);
        }

        // Se tutti i campi sono presenti e corretti effettuo la query per memorizzare il nuovo
        // campetto nel database, verificando prima che il dispositivo sia connesso o meno a internet.
        if (validField) {

            if (!CheckConnection.isConnected(this)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("You don't have internet connection, please check it!")
                        .setTitle("An error occurred");

                // Se non il dispositivo non sia connesso ad internet, l'utente può
                // richiedere di essere riportato alle impostazioni del dispositivo per
                // poter effettuare la connessione.
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
                final String code = String.valueOf(Calendar.getInstance().getTimeInMillis());
                progressDialog.setMessage("Adding your pitch...");
                progressDialog.show();

                // Creo un HashMap contenente tutti i valori relativi al campetto.
                Map<String, Object> pitch = new HashMap<>();
                pitch.put("owner", username);
                pitch.put("address", address);
                pitch.put("city", city);
                pitch.put("ownermail", StaticInstance.email);
                pitch.put("price", price);
                pitch.put("covered", isCovered);
                pitch.put("code", code);
                pitch.put("latitude", latitude);
                pitch.put("longitude", longitude);

                // Effettuo la query al database, mediante l'oggetto db, passando come oggetto da
                // memorizzare l'HashMap creato precedentemente.
                db.collection("pitch").document(code)
                        .set(pitch)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            // Se l'operazione è andata a buon fine memorizzo nello storage l'immagine
                            // relativa al campetto, nel caso in cui il gestore ne abbia scelto una.
                            @Override
                            public void onSuccess(Void avoid) {
                                StorageReference ref = StaticInstance.mStorageRef.child("pitch/" + username + code);
                                if (path != null) {
                                    ref.putFile(Uri.parse(path))
                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    path = null;
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    path = null;
                                                }
                                            });
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage("Your pitch was created successfully");
                                builder.create().show();
                                progressDialog.dismiss();
                            }
                        })

                        // Se l'operazione di memorizzazione non è invece andata a buon fine,
                        // comunico l'esito all'utente.
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

    // Funzione utilizzata per scattare una foto dal dispositivo. Viene creato un intento
    // con il quale si richiede l'avvio della fotocamera.
    public void takePhoto(View v) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    // Funzione di callback, utilizzata per gestire eventuali risposte relative ad altre attività.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Se la risposta è relativa all'intento utilizzato per avviare la fotocamera, allora
            // si richiede la foto scattata.
            Bundle extras = data.getExtras();
            photo = (Bitmap) extras.get("data");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Si verifica che l'utente abbia fornito i permessi necessari.
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    setPathPhoto();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_CODE);
                }
            } else {
                setPathPhoto();
            }

        }
    }

    public void setPathPhoto() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        path = MediaStore.Images.Media.insertImage(this.getContentResolver(), photo, "Title", null);

    }

    // Funzione di callback, utilizzata per verificare il permessi forniti dall'utente.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_CODE)
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setPathPhoto();
            }
    }

    private Context getActivity() {
        return this;
    }



}
