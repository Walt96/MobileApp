package com.example.walter.mobileapp.activity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.walter.mobileapp.R;
import com.example.walter.mobileapp.utility.StaticInstance;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;


/**
 * A simple {@link Fragment} subclass.
 */

// Frammento avviato nel caso in cui l'utente richieda di essere registrato come giocatore.
public class FootballerSignIn extends Fragment {

    // Riferimento a firebase, utilizzato per effettuare le query.
    FirebaseFirestore db = StaticInstance.getInstance();

    View fragmentView; // View relativi al frammento attuale, utilizzato per ottenere gli elementi
    Bitmap photo; // Foto relativa al giocatore.
    String path; // Path della foto.

    // Campi utilizzati per individuare la risposta fornita da una activity.
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int WRITE_EXTERNAL_CODE = 2;


    public FootballerSignIn() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_signin, container, false);
        ImageButton takePhoto = fragmentView.findViewById(R.id.takePhoto);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        path = null;
        Spinner dropdown = fragmentView.findViewById(R.id.selectrole);
        String[] items = new String[]{"Attaccante", "Centrocampista", "Difensore", "Portiere"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinneritem, items);
        dropdown.setAdapter(adapter);
        return fragmentView;
    }

    // Funzione utilizzata per scattare una foto dal dispositivo. Viene creato un intento
    // con il quale si richiede l'avvio della fotocamera.
    private void takePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    // Funzione di callback con la quale si gestisce il result delle activity.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            photo = (Bitmap) extras.get("data");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    setPathPhoto();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_CODE);
                }
            } else {
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

    public void setPathPhoto() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), photo, "Title", null);

    }
}
