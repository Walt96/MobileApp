package com.example.walter.mobileapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ModifyPitch extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_CODE = 2 ;

    EditText priceEditText;
    RadioButton coveredPitch;
    RadioButton uncoveredPitch;
    FirebaseFirestore db = StaticInstance.getInstance();
    ProgressDialog progressDialog;
    String username;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String path;
    StorageReference mStorageRef;
    Bitmap photo;
    String address;
    String city;
    String id;
    double price;
    boolean covered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("","onCreate");

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        address = intent.getStringExtra("address");
        price = Double.parseDouble(intent.getStringExtra("price"));
        covered = Boolean.valueOf(intent.getStringExtra("covered"));
        city = intent.getStringExtra("city");

        setContentView(R.layout.activity_modify_pitch);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        username = StaticInstance.username;

        priceEditText = findViewById(R.id.price);
        coveredPitch = findViewById(R.id.coveredPitch);
        uncoveredPitch = findViewById(R.id.uncoveredPitch);
        progressDialog = new ProgressDialog(this);
        path=null;

        priceEditText.setText(String.valueOf(price));

        if(covered) {
            coveredPitch.toggle();
        } else {
            uncoveredPitch.toggle();
        }

    }
    public void validateFields(View v) {


        double actualPrice = 0;
        boolean isCovered = coveredPitch.isChecked();
        boolean validField = true;

        if (priceEditText.getText().length() == 0) {
            priceEditText.setError("Do you really want to make your pitch free? :)");
            validField = false;
        }
        else {
            String value_price = priceEditText.getText().toString();
            actualPrice = Double.valueOf(value_price);
        }
        if (validField) {
            if(!CheckConnection.isConnected(this)){
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
            }else {
            boolean makeUpdate = false;
            final String code = String.valueOf(Calendar.getInstance().getTimeInMillis());
            progressDialog.setMessage("Modifying your pitch...");
            progressDialog.show();

            Map<String, Object> updates = new HashMap<>();

            if(isCovered != covered) {
                makeUpdate = true;
                updates.put("covered", isCovered);
               Log.e("TAG", "Updating covered " + isCovered);
            }

            if(price != actualPrice) {
                makeUpdate = true;
                updates.put("price", actualPrice);
              Log.e("TAG", "Updating price " + actualPrice);
            }

            if(makeUpdate) {
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
                            Log.e("TAG", e.toString());
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage("An error occured, please try again!");
                            builder.create().show();
                            progressDialog.dismiss();
                        }
                    });
            }

            //TODO Gestire la foto

            }
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
