package com.example.walter.mobileapp;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;

public class StaticInstance {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static Context currentActivity;


    public static FirebaseFirestore getInstance(){
        return db;
    }
}
