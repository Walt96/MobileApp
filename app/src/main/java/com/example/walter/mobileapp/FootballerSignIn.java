package com.example.walter.mobileapp;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class FootballerSignIn extends Fragment {

    FirebaseFirestore db = StaticInstance.getInstance();
    View fragmentView;

    public FootballerSignIn() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView =  inflater.inflate(R.layout.activity_signin, container, false);
        Spinner dropdown = fragmentView.findViewById(R.id.selectrole);
        String[] items = new String[]{"Attaccante","Centrocampista","Difensore","Portiere"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(), R.layout.spinneritem, items);
        dropdown.setAdapter(adapter);
        return fragmentView;
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
