package com.example.walter.mobileapp;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class ManagerSignIn extends Fragment {

    FirebaseFirestore db = StaticInstance.getInstance();
    View fragmentView;

    public ManagerSignIn() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView =  inflater.inflate(R.layout.fragment_manager_sign_in, container, false);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manager_sign_in, container, false);
    }

    public void validateFields(View v) {

        final EditText usernameEdit = (EditText) fragmentView.findViewById(R.id.usernameSignin);
        EditText passwordEdit = (EditText) fragmentView.findViewById(R.id.passwordSignin);
        final EditText passwordConfirm = (EditText) fragmentView.findViewById(R.id.confirmSignin);
        final String role = ((Spinner) fragmentView.findViewById(R.id.selectrole)).getSelectedItem().toString();
        boolean firstControl = true;
        if (usernameEdit.getText().length() < 5) {
            usernameEdit.setError("Please choose an username with at least 5 characters");
            firstControl = false;
        }
        if (passwordEdit.getText().length() < 5) {
            passwordEdit.setError("Please choose an username with at least 5 characters");
            firstControl = false;
        }

        if (!passwordEdit.getText().toString().equals(passwordConfirm.getText().toString())) {
            passwordConfirm.setError("Passwords aren't equals");
            firstControl = false;
        }



        if (firstControl) {

            final Task<QuerySnapshot> querySnapshotTask = db.collection("users")
                    .whereEqualTo("username", usernameEdit.getText().toString())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().size() != 0)
                                    usernameEdit.setError("This username is already used");
                                else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Your profile is: \nUsername " + usernameEdit.getText().toString() + "\n" + "Role " + role)
                                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Map<String,Object> user = new HashMap<>();
                                                    user.put("username",usernameEdit.getText().toString());
                                                    user.put("password",passwordConfirm.getText().toString());
                                                    user.put("role",role);
                                                    db.collection("users")
                                                            .add(user)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    AlertDialog.Builder builder_ = new AlertDialog.Builder(getActivity());
                                                                    builder_.setMessage("Your account was created successfully");
                                                                    builder_.create().show();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    AlertDialog.Builder builder_ = new AlertDialog.Builder(getActivity());
                                                                    builder_.setMessage("An error occurred, please try again!");
                                                                    builder_.create().show();
                                                                }
                                                            });
                                                }
                                            })
                                            .setNegativeButton("Decline",new DialogInterface.OnClickListener(){
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });


                                    // Create the AlertDialog object and return it
                                    builder.create().show();
                                }
                            }
                        }
                    });

        }
    }

}
