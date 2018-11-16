package com.example.walter.mobileapp;

import com.google.firebase.firestore.FirebaseFirestore;

public class StaticDbInstance {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();


    public static FirebaseFirestore getInstance(){
        return db;
    }
}
