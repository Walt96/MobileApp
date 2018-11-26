package com.example.walter.mobileapp;

import android.app.ProgressDialog;
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
import com.google.firebase.firestore.DocumentSnapshot;
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
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //controlla se qualche utente è gia loggato
        sharedPref  = getSharedPreferences("logged user", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        String user = sharedPref.getString("user","");
        if(!user.equals("")) {
            String role = sharedPref.getString("role", "");
            if (role.equals(""))
                doLogin(user, "", false);
            else {
                doLogin(user, role, true);
            }

        }

        //prendo gli oggetti della ui
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        progressDialog = new ProgressDialog(this);

    }


    public void goToSignin(View v){
        Intent intent = new Intent(this,SigninActivity.class);
        startActivity(intent);
    }

    public void checkLogin(View v){
        progressDialog.setMessage("Checking your credentials...");
        progressDialog.show();
         Task<QuerySnapshot> querySnapshotTask = db.collection("users").whereEqualTo("username", username.getText().toString()).whereEqualTo("password", password.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            if (task.getResult().isEmpty())
                                password.setError("Please check your credentials");
                            else {

                                //salvo l'utente loggato nelle pref
                                DocumentSnapshot user_doc = task.getResult().getDocuments().get(0);
                                String user = user_doc.get("username").toString();
                                editor.putString("user", user);
                                boolean isAPlayer = (boolean)user_doc.get("player");
                                String role="";
                                if(isAPlayer){
                                   role = user_doc.get("role").toString();
                                   editor.putString("role", role);
                                }
                                editor.commit();
                                doLogin(user,role,isAPlayer);
                            }
                        }

                    }
                });

    }
    public void doLogin(String user, String role, boolean isAPlayer){
        Intent intent;
        if(isAPlayer) {
            intent = new Intent(this, UserHome.class);
            intent.putExtra("role", role);
        }
        else{
            intent = new Intent(this,OwnerHome.class);
        }
        intent.putExtra("username",user);
        startActivity(intent);
    }

    private Context getActivity() {
        return this;
    }

}
