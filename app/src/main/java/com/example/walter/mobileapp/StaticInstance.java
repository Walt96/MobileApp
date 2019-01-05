package com.example.walter.mobileapp;

import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StaticInstance {

    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static Context currentActivity;
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static String username;
    static String role;
    static String email;
    static StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();




    public static FirebaseFirestore getInstance(){
        return db;
    }
    public static FirebaseDatabase getDatabase(){return  database;}
}
