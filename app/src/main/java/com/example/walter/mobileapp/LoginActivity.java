package com.example.walter.mobileapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    FirebaseFirestore db = StaticInstance.getInstance();
    EditText username;
    EditText password;
    Button login;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private ProfileTracker mProfileTracker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //controlla se qualche utente è gia loggato
        callbackManager = CallbackManager.Factory.create();

        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>(){

            @Override
            public void onSuccess(LoginResult loginResult) {
                final AccessToken token = loginResult.getAccessToken();
                Log.e("TAG", "Logged");
                loginButton.setVisibility(View.GONE);
                if(Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            Profile profile = Profile.getCurrentProfile();
                            Log.v("facebook - profile", currentProfile.getFirstName());
                            Log.v("facebook - profile", profile.getFirstName());
                            Log.e("TAG", profile.getFirstName() + " " + profile.getLastName());

                            /*String firstName = profile.getFirstName();
                            String middleName = profile.getMiddleName();
                            String lastName = profile.getLastName();
                            final String[] email = new String[1];
                            final String[] id = new String[1];*/
                            GraphRequest request = GraphRequest.newMeRequest(
                                    token,
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(JSONObject object, GraphResponse response) {
                                            Log.v("LoginActivity", response.toString());

                                            // Application code
                                            try {
                                                String id = object.getString("id");
                                                String email = "";
                                                String name = "";
                                                if(object.has("email")) {
                                                    email = object.getString("email");
                                                }

                                                if(object.has("name")) {
                                                    name = object.getString("name");
                                                }
                                                Log.e("TAG", id + " - " + email + " - " + name);
                                                checkFBAccount(email);

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,email");
                            request.setParameters(parameters);
                            request.executeAsync();
                            mProfileTracker.stopTracking();
                        }
                    };

                } else {
                    Profile profile = Profile.getCurrentProfile();
                    Log.v("facebook - profile", profile.getFirstName());
                    Log.e("TAG", profile.getFirstName() + " " + profile.getLastName());
                    GraphRequest request = GraphRequest.newMeRequest(
                            token,
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    Log.v("LoginActivity", response.toString());

                                    // Application code
                                    try {
                                        //String id = object.getString("id");
                                        String email = "";
                                       // String name = "";
                                        if(object.has("email")) {
                                            email = object.getString("email");
                                        }

                                        /*if(object.has("name")) {
                                            name = object.getString("name");
                                        }*/
                                        //Log.e("TAG", id + " - " + email + " - " + name);
                                        checkFBAccount(email);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "email");
                    request.setParameters(parameters);
                    request.executeAsync();
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("TAG", error.toString());
            }
        });

        sharedPref  = getSharedPreferences("logged user", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        String user = sharedPref.getString("user","");

        checkFBLogged();
        if(!user.equals("")) {
            String role = sharedPref.getString("role", "");
            String email = sharedPref.getString("email", "");
            if (role.equals(""))
                doLogin(user, "", false, email);
            else {
                doLogin(user, role, true, email);
            }

        }

        //prendo gli oggetti della ui
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        progressDialog = new ProgressDialog(this);


    }


    private void checkFBLogged() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        if(AccessToken.getCurrentAccessToken()!= null) {
           // final AccessToken token = A
            GraphRequest request = GraphRequest.newMeRequest(
                    token,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            // Application code
                            try {
                                String email = "";
                                if(object.has("email")) {
                                    email = object.getString("email");
                                }
                                checkFBAccount(email);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "email");
            request.setParameters(parameters);
            request.executeAsync();
        }
    }

    private void checkFBAccount(String email) {
        Task<QuerySnapshot> querySnapshotTask = db.collection("users").whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            if (task.getResult().isEmpty()) {
                                Toast.makeText(LoginActivity.this, "Please, sign in to our app first", Toast.LENGTH_SHORT).show();
                                LoginManager.getInstance().logOut();
                                loginButton.setVisibility(View.VISIBLE);
                            } else {
                                DocumentSnapshot user_doc = task.getResult().getDocuments().get(0);
                                String user = user_doc.get("username").toString();
                                editor.putString("user", user);
                                boolean isAPlayer = (boolean)user_doc.get("player");
                                String role="";
                                if(isAPlayer){
                                    role = user_doc.get("role").toString();
                                    editor.putString("role", role);
                                }
                                String email = user_doc.get("email").toString();
                                editor.putString("email",email);
                                editor.commit();
                                StaticInstance.fblogged = true;
                                doLogin(user,role,isAPlayer,email);
                            }
                        }

                    }
                });
    }

    // TODO Eliminare, utilizzato solo per testare il login con facebook
    public void testLogin(View w) {
        Intent fbLoginIntent = new Intent(this, FacebookLogin.class);
        startActivity(fbLoginIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        StaticInstance.currentActivity = this;
    }

    public void goToSignin(View v){
        Intent intent = new Intent(this,SigninActivity.class);
        startActivity(intent);
    }

    public void checkLogin(View v){
        if(!CheckConnection.isConnected(this)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You don't have internet connection, please check it!")
                    .setTitle("An error occurred");
            builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(getApplicationContext(), UserHome.class));
                }
            }).setPositiveButton("Check now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                }
            });
            builder.create().show();
        }else {
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
                                    boolean isAPlayer = (boolean) user_doc.get("player");
                                    String role = "";
                                    if (isAPlayer) {
                                        role = user_doc.get("role").toString();
                                        editor.putString("role", role);
                                    }
                                    String email = user_doc.get("email").toString();
                                    editor.putString("email", email);
                                    editor.commit();
                                    doLogin(user, role, isAPlayer, email);
                                }
                            }

                        }
                    });
        }
    }
    public void doLogin(String user, String role, boolean isAPlayer, String email){
        Intent intent;
        if(isAPlayer) {
            intent = new Intent(this, UserHome.class);
        }
        else{
            intent = new Intent(this,OwnerHome.class);
        }
        StaticInstance.username = user;
        StaticInstance.role = role;
        StaticInstance.email = email;
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Context getActivity() {
        return this;
    }

}
