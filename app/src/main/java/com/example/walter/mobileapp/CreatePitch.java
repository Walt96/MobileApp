package com.example.walter.mobileapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreatePitch extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_CODE = 2 ;
    EditText addressEditText;
    EditText cityEditText;
    EditText priceEditText;
    RadioButton coveredPitch;
    FirebaseFirestore db = StaticInstance.getInstance();
    ProgressDialog progressDialog;
    String username;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String path;
    StorageReference mStorageRef;
    Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pitch);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        username = getIntent().getStringExtra("username");
        addressEditText = findViewById(R.id.pitchAddress);
        cityEditText = findViewById(R.id.city);
        priceEditText = findViewById(R.id.price);
        coveredPitch = findViewById(R.id.coveredPitch);
        progressDialog = new ProgressDialog(this);
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
            final String code = String.valueOf(Calendar.getInstance().getTimeInMillis());
            progressDialog.setMessage("Adding your pitch...");
            progressDialog.show();
            Map<String, Object> pitch = new HashMap<>();
            pitch.put("owner", username);
            pitch.put("address", address);
            pitch.put("city", city.toLowerCase());
            pitch.put("price", price);
            pitch.put("covered",isCovered);
            pitch.put("code",code);
            db.collection("pitch")
                    .add(pitch)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            StorageReference ref = mStorageRef.child("pitch/"+username+code);
                            ref.putFile(Uri.parse(path))
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                        }
                                    });

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

    public void takePhoto(View v){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            photo = (Bitmap) extras.get("data");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    setPathPhoto();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_CODE);
                }
            }else{
                setPathPhoto();
            }
        }
    }

    public void setPathPhoto(){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        path = MediaStore.Images.Media.insertImage(this.getContentResolver(), photo, "Title", null);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == WRITE_EXTERNAL_CODE)
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setPathPhoto();
            }
    }

    private Context getActivity() {
        return this;
    }
}
