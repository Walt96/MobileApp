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
import android.util.Log;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    // Riferimento a Firebase, utilizzato per effettuare query al database
    FirebaseFirestore db = StaticInstance.getInstance();

    // Campi relativi ai dati dell'utente.
    EditText username;
    EditText password;

    // Bottone utilizzato per effettuare il login.
    Button login;

    // Oggetto utilizzato per memorizzare informazioni di cui si vuole tenere traccia anche alla chiusura dell'applicazione
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;

    // Oggetti relativi forniti dall'API di Facebook
    private CallbackManager callbackManager; // Oggetto che gestisce le risposte di eventuali fragment o activity relativi a Facebook.
    private LoginButton loginButton; // Oggetto che rappresenta il Login Button proprio di Facebook.
    private ProfileTracker mProfileTracker; // Oggetto utilizzato per tracciare le informazioni relative al profilo attuale.
                                            // può essere ad esempio utilizzato per gestire eventuali cambiamenti del profilo.



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();

        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        // Registro un evento relativo al Login Button di Facebook.
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>(){

            @Override
            public void onSuccess(LoginResult loginResult) {
                // Se l'operazione di login ha avuto successo richiedo i dati dell'utente.
                final AccessToken token = loginResult.getAccessToken(); // Token generato al momento del login, permette di richiedere informazioni relative all'utente.
                loginButton.setVisibility(View.GONE);

                if(Profile.getCurrentProfile() == null) {
                    // Se non ho ancora memorizzato il profilo corrente, aggiorno il profile tracker.
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            makeGraphRequest(token);
                            mProfileTracker.stopTracking();
                        }
                    };

                } else {
                    // Se ho già un profilo memorizzato, allora vado ad effettuare la query al Graph API
                    makeGraphRequest(token);
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

        checkFBLogged(); // Controllo che l'utente abbia già effettuare l'accesso a facebook o meno.
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


    private void makeGraphRequest(AccessToken token) {
        // Dopo essermi assicurato di avere registrato un profilo, effettuo una richiesta al Graph API, in modo da ottenere
        // la mail dell'utente.
        GraphRequest request = GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
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

        // Definisco i parametri che voglio ottenere dal Graph API.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    // Funzione utilizzata per verificare se l'utente sia loggato o meno a Facebook.
    private void checkFBLogged() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        if(AccessToken.getCurrentAccessToken()!= null) {
            makeGraphRequest(token);
        }
    }

    // Funzione utilizzata per verificare che l'utente sia già registrato all'applicazione con l'email
    // utilizzata per l'account di Facebook.
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

    @Override
    protected void onResume() {
        super.onResume();
        StaticInstance.currentActivity = this;
    }

    public void goToSignin(View v){
        Intent intent = new Intent(this,SigninActivity.class);
        startActivity(intent);
    }

    // funzione utilizzata per verificare se esiste un utente con le credenziali immesse.
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

    // Funzione utilizzata per effettuare il login vero e proprio.
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

}
