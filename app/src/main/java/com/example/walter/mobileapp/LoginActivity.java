package com.example.walter.mobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    FirebaseFirestore db = StaticDbInstance.getInstance();
    EditText username;
    EditText password;
    Button login;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPref  = getSharedPreferences("logged user", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        Log.e("oncreate","oncreate");
        if(!sharedPref.getString("user","").equals(""))
            doLogin(sharedPref.getString("user",""),sharedPref.getString("role",""));
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);


    }
    

    public void goToSignin(View v){
        Intent intent = new Intent(this,CreatePitch.class);
        startActivity(intent);
    }

    public void checkLogin(View v){
         Task<QuerySnapshot> querySnapshotTask = db.collection("users").whereEqualTo("username", username.getText().toString()).whereEqualTo("password", password.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty())
                                password.setError("Please check your credentials");
                            else {
                                String user = task.getResult().getDocuments().get(0).get("username").toString();
                                String role = task.getResult().getDocuments().get(0).get("role").toString();
                                editor.putString("user", user);
                                editor.putString("role", role);
                                editor.commit();
                                doLogin(user,role);
                            }
                        }

                    }
                });

    }
    public void doLogin(String user, String role){
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("username",user);
        intent.putExtra("role",role);
        startActivity(intent);
    }

    private Context getActivity() {
        return this;
    }

}
