package com.example.walter.mobileapp;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;
import static com.example.walter.mobileapp.CreatePitch.REQUEST_ADDRESS_INFO;


/**
 * A simple {@link Fragment} subclass.
 */
public class FootballerSignIn extends Fragment {

    FirebaseFirestore db = StaticInstance.getInstance();
    View fragmentView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap photo;
    String path;
    private static final int WRITE_EXTERNAL_CODE = 2 ;


    public FootballerSignIn() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView =  inflater.inflate(R.layout.activity_signin, container, false);
        ImageButton takePhoto = fragmentView.findViewById(R.id.takePhoto);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        path = null;
        Spinner dropdown = fragmentView.findViewById(R.id.selectrole);
        String[] items = new String[]{"Attaccante","Centrocampista","Difensore","Portiere"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinneritem, items);
        dropdown.setAdapter(adapter);
        return fragmentView;
    }

    private void takePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("Request Code", requestCode+"");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.e("TAG","REQUEST_IMAGE_CAPTURE");
            Bundle extras = data.getExtras();
            photo = (Bitmap) extras.get("data");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(getActivity(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    setPathPhoto();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_CODE);
                }
            }else{
                setPathPhoto();
            }
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setPathPhoto(){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), photo, "Title", null);

    }

    /*
    String getUsername(){
        EditText usernameEdit = (EditText) fragmentView.findViewById(R.id.usernameSignin);
        return usernameEdit.getText().toString();
    }

    String getPassword() {
        EditText passwordEdit = (EditText) fragmentView.findViewById(R.id.passwordSignin);
        return passwordEdit.getText().toString();
    }

    String getPasswordConfirm() {
        EditText passwordConfirm = (EditText) fragmentView.findViewById(R.id.confirmSignin);
        return passwordConfirm.getText().toString();
    }

    String getRole() {
        String role = ((Spinner) fragmentView.findViewById(R.id.selectrole)).getSelectedItem().toString();
        return role;
    }
    */
}
