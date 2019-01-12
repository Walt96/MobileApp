package com.example.walter.mobileapp.utility;

import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

//usata per accedere staticamente alle connessioni e ai dati usati in tutta l'applicazione
public class StaticInstance {

    public static FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static Context currentActivity;
    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static String username;
    public static String role;
    public static String email;
    public static boolean fblogged = false;
    public static StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();


    public static FirebaseFirestore getInstance() {
        return db;
    }

    public static FirebaseDatabase getDatabase() {
        return database;
    }
}
