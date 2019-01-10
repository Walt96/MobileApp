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

public class CreatePitch extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_CODE = 2;
    //EditText addressEditText;
    EditText cityEditText;
    EditText priceEditText;
    RadioButton coveredPitch;
    FirebaseFirestore db = StaticInstance.getInstance();
    ProgressDialog progressDialog;
    String username;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_ADDRESS_INFO = 3;
    String path;
    Bitmap photo;
    private Double latitude;
    private Double longitude;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pitch);
        username = StaticInstance.username;
        //addressEditText = findViewById(R.id.pitchAddress);
        cityEditText = findViewById(R.id.city);
        priceEditText = findViewById(R.id.price);
        coveredPitch = findViewById(R.id.coveredPitch);
        progressDialog = new ProgressDialog(this);
        path = null;

        // Creo la barra di ricerca dei luoghi.
        // TODO Vale la pena prendere la città?
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                try {
                    latitude = place.getLatLng().latitude;
                    longitude = place.getLatLng().longitude;
                    Log.i("", "Place: " + latitude + " " + longitude);
                    Geocoder mGeocoder = new Geocoder(getActivity(), Locale.getDefault());
                    List<Address> addresses = null;
                    addresses = mGeocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        Address selectedAddress = addresses.get(0);
                        String streetName = selectedAddress.getThoroughfare();
                        String number = selectedAddress.getSubThoroughfare();
                        address = streetName;
                        if(number != null) {
                            address += " " + number;
                        }
                        String city = selectedAddress.getLocality();
                        Log.e("TAG", address + " " + city);
                        cityEditText.setText(city);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("", "An error occurred: " + status);
            }
        });

        ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextColor(Color.WHITE);

    }

    public void validateFields(View v) {

        //String address = addressEditText.getText().toString();
        String city = cityEditText.getText().toString();
        double price = 0;
        boolean isCovered = coveredPitch.isChecked();
        boolean validField = true;

        /*if (!address.matches("((via)|(piazza)|(contrada)|(corso)).*n(\\d)+")) {
            addressEditText.setError("Please check your address: it's not valid");
            validField = false;
        }
        */
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
                final String code = String.valueOf(Calendar.getInstance().getTimeInMillis());
                progressDialog.setMessage("Adding your pitch...");
                progressDialog.show();
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
                db.collection("pitch").document(code)
                        .set(pitch)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void takePhoto(View v) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("Request Code", requestCode + "");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.e("TAG", "REQUEST_IMAGE_CAPTURE");
            Bundle extras = data.getExtras();
            photo = (Bitmap) extras.get("data");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    setPathPhoto();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_CODE);
                }
            } else {
                setPathPhoto();
            }
        } else if (requestCode == REQUEST_ADDRESS_INFO && resultCode == RESULT_OK) {
            try {
                Double longitude = data.getDoubleExtra("longitude", 0);
                Double latitude = data.getDoubleExtra("latitude", 0);
                Address add = getAddress(latitude, longitude);
                String address = add.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = add.getLocality();
                Log.e("TAG", "City " + city);
                //addressEditText.setText(address);
                cityEditText.clearFocus();
                cityEditText.setText(city);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*public void searchLocation(View v) {
        String address = addressEditText.getText().toString();
        String city = cityEditText.getText().toString();
        String searchString = address + " " + city;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try
        {
            List<Address> addresses = geocoder.getFromLocationName(searchString, 5);
            Log.d("SIZE", String.valueOf(addresses.size()));
            if (addresses.size() == 1)
            {
                Double lat = (double) (addresses.get(0).getLatitude());
                Double lon = (double) (addresses.get(0).getLongitude());

                Log.d("lat-long", "" + lat + "......." + lon);
                //final LatLng user = new LatLng(lat, lon);
                /*used marker for show the location */
                /*Marker hamburg = map.addMarker(new MarkerOptions()
                        .position(user)
                        .title(adderess)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.marker)));
                // Move the camera instantly to hamburg with a zoom of 15.
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 15));

                // Zoom in, animating the camera.
                map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);*/
            /*} else {
                Log.d("More", "More elements");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }*/


    private Address getAddress(Double latitude, Double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        return addresses.get(0);
    }

    public void setPathPhoto() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        path = MediaStore.Images.Media.insertImage(this.getContentResolver(), photo, "Title", null);

    }

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
