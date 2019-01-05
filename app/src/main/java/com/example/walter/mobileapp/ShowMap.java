package com.example.walter.mobileapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShowMap extends FragmentActivity implements OnMapReadyCallback {


    FirebaseFirestore db = StaticInstance.getInstance();
    ArrayList<Pitch> fields;
    private GoogleMap mMap;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int DEFAULT_ZOOM = 15;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location mLastKnownLocation;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private ArrayList<String> cities;
    private Spinner cityPicker;
    private String selectedCity;
    private boolean otherCity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedCity = "";
        otherCity = false;
        setContentView(R.layout.activity_show_map);

        mGeoDataClient = Places.getGeoDataClient(this);

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

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
        mapFragment.getMapAsync(this);
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
                            fields = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                locFound = true;
                                String id = document.getId();
                                String address = document.get("address").toString();
                                Double price = document.getDouble("price");
                                boolean covered = document.getBoolean("covered");
                                String owner = document.get("owner").toString();
                                Double lat = document.getDouble("latitude");
                                Double lon = document.getDouble("longitude");
                                tmpLat = lat;
                                tmpLng = lon;

                                Pitch currentPitch = new Pitch(id, address, price, covered,  locality, owner);
                                fields.add(currentPitch);

                                Log.e("TAG","Adding in " + lat + " - " + lon);
                                LatLng marker = new LatLng(lat, lon);
                                mMap.addMarker(new MarkerOptions().position(marker).title(address));
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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Do other setup activities here too, as described elsewhere in this tutorial.

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        /*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            try {
                                // Set the map's camera position to the current location of the device.
                                mLastKnownLocation = (Location) task.getResult();
                                String city = "";
                                if(mLastKnownLocation != null) {
                                    Log.e("", "not null!");
                                    Double longitude = mLastKnownLocation.getLongitude();
                                    Double latitude = mLastKnownLocation.getLatitude();
                                    Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
                                    List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
                                    if (addresses.size() > 0) {
                                        Log.e("TAG", addresses.get(0).toString());
                                        city = addresses.get(0).getLocality();
                                        getNearFields(city);
                                    }
                                } else {
                                    Log.e("", "NULL");
                                }
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d("TAG", "Current location is null. Using defaults.");
                            Log.e("TAG", "Exception: %s", task.getException());
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

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
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

    //TODO Selezionare i campi tramite query su firebase oppure utilizzando quelli già caricati precedentemente?
    public void loadMarkers(View w) {
        otherCity = false;
        if(selectedCity != "") {
            getNearFields(selectedCity);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    public Context getContext() {
        return this;
    }

}
