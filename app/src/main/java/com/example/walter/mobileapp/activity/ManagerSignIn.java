package com.example.walter.mobileapp.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.walter.mobileapp.R;
import com.example.walter.mobileapp.utility.StaticInstance;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * A simple {@link Fragment} subclass.
 */

// Fragment utilizzato per effettuare la registrazione di un gestore.
public class ManagerSignIn extends Fragment {

    FirebaseFirestore db = StaticInstance.getInstance();
    View fragmentView;

    public ManagerSignIn() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_manager_sign_in, container, false);
        return inflater.inflate(R.layout.fragment_manager_sign_in, container, false);
    }
}
