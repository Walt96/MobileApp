package com.example.walter.mobileapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

// Activity utilizzata per visualizzare la mappa
public class ShowMap extends FragmentActivity implements OnMapReadyCallback {


    // Riferimento a Firebase, utilizzato per effettuare query sul database
    FirebaseFirestore db = StaticInstance.getInstance();

    // Variabili ottenute dall'API di Google
    private GoogleMap mMap; // Rappresenta la mappa che verrà visualizzata.
    private GeoDataClient mGeoDataClient; // Variabile utilizzata per accedere al database di google contenente i luoghi.
    private PlaceDetectionClient mPlaceDetectionClient; // Utilizzato per poter accedere alla posizione locale del dispositivo.
    private FusedLocationProviderClient mFusedLocationProviderClient; // Altro oggetto utilizzato per poter determinare informazioni relative alla posizione.

    private static final int DEFAULT_ZOOM = 15; // Zoom di default della mappa.
    private boolean mLocationPermissionGranted; // Booleana utilizzata per assicurarsi che l'utente abbia fornito i peressi per poter accedere alla propria posizione.
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085); // Coordinate della posizione di default, da utilizzare nel caso in cui non sia
                                                                                         // sia possibile accedere alla posizione corrente.
    private Location mLastKnownLocation;    // Rappresenta l'ultima posizione nota.

    private ArrayList<String> cities;  // Array contenente le città dove sono presenti dei campetti.
    private HashMap<LatLng, Pitch> fieldsMap; //Mappa contenente per ogni coordinata il campo corrispondente.

    // Oggetti del layout.
    private Spinner cityPicker;
    private String selectedCity;
    private String selectedDate;
    private Dialog markerDialog;


    private String manager = StaticInstance.username;
    ListenerRegistration listener; // Listener in ascolti di eventuali modifiche effettuate al campetto corrente.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);

        selectedCity = "";
        fieldsMap = new HashMap<>();
        markerDialog = new Dialog(this);

        // Listener in ascolto della chiusura della dialog contenente le informazioni del campetto.
        markerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                listener.remove();
            }
        });

        mGeoDataClient = Places.getGeoDataClient(this);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        selectedDate = getIntent().getStringExtra("date");
        cities = getIntent().getStringArrayListExtra("cities");
        cityPicker = findViewById(R.id.cityPicker);
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.spinneritem, cities);
        cityPicker.setAdapter(adapter);
        cityPicker.setSelection(0);
        cityPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCity = cityPicker.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedCity = "";
            }

        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); // Si richiede l'avvio della mappa.
    }

    private void getNearFields(String city) {
        Log.e("TAG", "adding tags");
        final String locality = city;
        db.collection("pitch")
                .whereEqualTo("city", locality)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.e("TAG", "Query completed " + task.getResult().size() + " " + locality);

                            //Pitch(String id, String address,double price,boolean covered, String city, String owner){
                            boolean locFound = false;
                            Double tmpLat = -1.0;
                            Double tmpLng = -1.0;
                           // fields = new ArrayList<>();
                            int i = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                locFound = true;
                                String id = document.getId();
                                String address = document.get("address").toString();
                                Double price = document.getDouble("price");
                                boolean covered = document.getBoolean("covered");
                                String owner = document.get("owner").toString();
                                String ownerMail = (String) document.get("ownermail");
                                Double lat = document.getDouble("latitude");
                                Double lon = document.getDouble("longitude");
                                tmpLat = lat;
                                tmpLng = lon;
                                final Pitch currentPitch = new Pitch(id, address, price, covered,  locality, owner, ownerMail);

                                StaticInstance.mStorageRef.child("pitch/" + document.get("owner") + document.get("code")).getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Log.e("il mio uri è", uri.toString());
                                                currentPitch.setUri(uri);

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                            }
                                        });

                                //fields.add(currentPitch);

                                Log.e("TAG","Adding in " + lat + " - " + lon);
                                LatLng markerLatLng = new LatLng(lat, lon);
                                MarkerOptions marker = new MarkerOptions().position(markerLatLng).title(address);
                                fieldsMap.put(new LatLng(lat,lon), currentPitch);
                                mMap.addMarker(marker);
                            }

                            if(locFound) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(tmpLat, tmpLng), DEFAULT_ZOOM));
                            }

                        } else {
                            Log.w("", "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    // Funzione di callback richiamata al caricamento della mappa.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        updateLocationUI();

        getDeviceLocation();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

               // markerPopup.show
                LatLng currentLatLng = marker.getPosition();
                final Pitch selectedPitch = fieldsMap.get(currentLatLng);
                initPitchAvailableTime(selectedPitch);
                addListenerForNewMatches(selectedPitch);
                markerDialog.setContentView(R.layout.custom_item_list);

                TextView pitchAddress = markerDialog.findViewById(R.id.pitchAddress);
                TextView pitchPrice = markerDialog.findViewById(R.id.pricePitch);
                TextView pitchCover = markerDialog.findViewById(R.id.pitchCover);
                Spinner availableTime = markerDialog.findViewById(R.id.pitchTime);
                try {
                    Field popup = Spinner.class.getDeclaredField("mPopup");
                    popup.setAccessible(true);
                    // Get private mPopup member variable and try cast to ListPopupWindow
                    android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(availableTime);
                    // Set popupWindow height to 500px
                    popupWindow.setHeight(350);
                } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
                }
                availableTime.setAdapter(selectedPitch.getAvailableTime());
                pitchPrice.setText("Price: " + String.valueOf(selectedPitch.getPrice()) + "€");
                pitchAddress.setText("Address: " + selectedPitch.getAddress());
                ImageView pitchImage = markerDialog.findViewById(R.id.pitchImage);
                Uri imageUri = selectedPitch.getUri();
                if (imageUri != null) {
                    Glide.with(markerDialog.getLayoutInflater().getContext())
                            .load(imageUri)
                            .into(pitchImage);
                } else {
                    Glide.with(markerDialog.getLayoutInflater().getContext())
                            .load(Uri.parse("android.resource://com.example.walter.mobileapp/" + R.drawable.login))
                            .into(pitchImage);
                }
                pitchCover.setText("Covered: Yes");
                if (!selectedPitch.isCovered())
                    pitchCover.setText("Covered: No");

                final Spinner time = markerDialog.findViewById(R.id.pitchTime);

                Button book = markerDialog.findViewById(R.id.bookPitch);
                book.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("TAG", "Book");
                       bookPitch(selectedPitch.getId(), time.getSelectedItem().toString().split(":")[0], selectedPitch.getAddress(), selectedPitch.getOwner(),selectedPitch.getOwnermail(), selectedPitch.isCovered());
                    }
                });

                markerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                markerDialog.show();
                //Toast.makeText(getActivity(), "Marker + " + title + " Clicked", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private Context getActivity() {
        return this;
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            try {
                                mLastKnownLocation = (Location) task.getResult();
                                String city = "";
                                if(mLastKnownLocation != null) {

                                    Double longitude = mLastKnownLocation.getLongitude();
                                    Double latitude = mLastKnownLocation.getLatitude();

                                    Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
                                    List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
                                    if (addresses.size() > 0) {
                                        city = addresses.get(0).getLocality();
                                        getNearFields(city);
                                    }
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(mLastKnownLocation.getLatitude(),
                                                    mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                } else {
                                    Log.e("", "NULL");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    // Funzione utilizzata per verificare se l'utente ha concesso o meno i permessi. Nel caso in cui i permessi
    // sono stati concessi, si procede alla visualizzazione della mappa, altrimenti si richiedono i permessi di localizzazione.
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    // Funzione utilizzata per richiedere i permessi di localizzazione, in modo da poter accedere
    // alle coordinate della posizione corrente.
    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    // Funzione utilizzata per verificare se il campetto è stato prenotato per un certo orario.
    private void initPitchAvailableTime(final Pitch p) {
            db.collection("matches").whereEqualTo("pitchcode", p.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    ArrayList notAvailable = new ArrayList();
                    for (DocumentSnapshot document : task.getResult()) {
                        Log.e("trovati", document.toString());
                        if (document.get("date").toString().equals(selectedDate))
                            notAvailable.add(document.get("time").toString());
                    }
                    p.initWithoutThese(notAvailable);
                }
            });
    }

    // Funzione utilizzata per visualizzare i markers relativi ai campetti sulla mappa.
    public void loadMarkers(View w) {
        if(selectedCity != "") {
            getNearFields(selectedCity);
        }
    }

    // Funzione utilizzata per poter creare un listener, dato un campetto, che permette di catturare l'evento
    // di modifica del campetto, in modo da segnalare che questo non è più disponibile dato un certo orario.
    private void addListenerForNewMatches(final Pitch p) {
        if (listener != null)
            listener.remove();
        listener = StaticInstance.db.collection("matches").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                for (DocumentSnapshot document : snapshot) {
                    Log.e("datacercata", selectedDate);
                    String bookedDate = document.get("date").toString();
                    String bookedTime = document.get("time").toString();
                    String idBookedPitch = document.get("pitchcode").toString();
                    if (selectedDate.equals(bookedDate))
                        if (p.getId().equals(idBookedPitch)) {
                            Log.e("rimuovo", "tolgo il " + bookedTime);
                            p.removeTime(Integer.valueOf(bookedTime));
                        }
                }
            }
        });

    }

    // Funzione utilizzata per poter prenotare il campetto.
    public void bookPitch(final String pitchId, final String time, final String address, final String owner, final String ownermail, boolean isCovered) {

        final boolean selectedCovered = isCovered;
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // Verifico che il dispositivo sia connesso o meno a internet.
        if (!isConnected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
            // Se connesso procedo alla prenotazione del campetto.
            if (time.equals("OCCUPATO")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("The pitch you selected is already booked at this time, sorry!")
                        .setTitle("An error occurred");
                builder.create().show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Address: " + address + "\n" +
                        "Date: " + selectedDate + "\n" +
                        "Time: " + time + ":00")
                        .setTitle("This will be your match:");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final String matchCode = String.valueOf(new Date().getTime()) + manager;
                        HashMap saveMyMatch = new HashMap();
                        HashMap myProfile = new HashMap();
                        ArrayList firstRegistered = new ArrayList();
                        ArrayList myName = new ArrayList();

                        myName.add(manager);

                        myProfile.put("user", manager);
                        myProfile.put("role", StaticInstance.role);
                        myProfile.put("team", "A");

                        firstRegistered.add(myProfile);


                        //aggiungo la nuova partita alla collezione matches nel documento data corrente + manager in modo da renderlo univoco con la concorrenza
                        saveMyMatch.put("date", selectedDate);
                        saveMyMatch.put("time", time);
                        saveMyMatch.put("manager", manager);
                        saveMyMatch.put("pitchcode", pitchId);
                        saveMyMatch.put("address", address);
                        saveMyMatch.put("covered", selectedCovered);
                        saveMyMatch.put("code", matchCode);
                        saveMyMatch.put("pitchmanager", owner);
                        saveMyMatch.put("managermail", ownermail);
                        saveMyMatch.put("registered", firstRegistered);
                        saveMyMatch.put("partecipants", myName);
                        saveMyMatch.put("finished", false);
                        saveMyMatch.put("confirmed", myName);

                        saveMatch(saveMyMatch);


                    }
                }).setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();
            }

        }

    }

    // Funzione utilizzata per memorizzare, tramite query, la nuova partita all'interno del database.
    private void saveMatch(final HashMap saveMyMatch) {
        StaticInstance.db.collection("matches").document(saveMyMatch.get("code").toString())
                .set(saveMyMatch)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendMailToOwnerPitch(saveMyMatch);
                        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.tableSnack), "Pitch booked successfully!", Snackbar.LENGTH_LONG);
                        mySnackbar.setAction("View all matches", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), MyMatchesList.class);
                                startActivity(intent);
                            }
                        });
                        mySnackbar.show();
                    }
                });
    }


    // Funzione utilizzata per poter inviare una mail al proprietario del campetto.
    // Si crea un intento volto ad avviare l'applicazione responsabile per le mail.
    private void sendMailToOwnerPitch(HashMap saveMyMatch) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{saveMyMatch.get("managermail").toString()});
        i.putExtra(Intent.EXTRA_SUBJECT, "Booking pitch");
        i.putExtra(Intent.EXTRA_TEXT   , "Hi, I have booked your pitch located in "+saveMyMatch.get("address")+" for the day "+saveMyMatch.get("date")+" at the "+saveMyMatch.get("time")+ ":00.");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    // Funzione utilizzata per richiedere la nuova posizione quando vengono concessi i permessi di localizzazione.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

}
