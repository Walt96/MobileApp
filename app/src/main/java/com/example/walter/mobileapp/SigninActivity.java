package com.example.walter.mobileapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SigninActivity extends AppCompatActivity {

    // per scrivere sul db
    FirebaseFirestore db = StaticInstance.getInstance();
    Button nextBtn;
    Fragment actualFragment;
    Boolean isUser;
    String username;
    static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_signin);
        setContentView(R.layout.choose_account_type);
        nextBtn = findViewById(R.id.nextbtn);
        isUser = false;
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.chooseGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                nextBtn.setVisibility(View.VISIBLE);
            }
        });



        /*Spinner dropdown = findViewById(R.id.selectrole);
        String[] items = new String[]{"Attaccante","Centrocampista","Difensore","Portiere"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinneritem, items);
        dropdown.setAdapter(adapter);*/

    }

    public void displaySignIn(View v) {
        RadioButton footballerButton = findViewById(R.id.footballerRadio);
        if(footballerButton.isChecked()) {
            nextBtn.setVisibility(View.INVISIBLE);
            Fragment footBallerSignInFrag = new FootballerSignIn();
            replaceFragment(footBallerSignInFrag);
            isUser = true;
        } else {
            nextBtn.setVisibility(View.INVISIBLE);
            Fragment managerSignInFrag = new ManagerSignIn();
            replaceFragment(managerSignInFrag);
            isUser = false;
        }
    }

    public void replaceFragment(Fragment fragment) {
        actualFragment = fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.relativeContainer, fragment);
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    public void validateFields(View v) {

        final EditText usernameEdit = (EditText) actualFragment.getView().findViewById(R.id.usernameSignin);
        EditText passwordEdit = (EditText) actualFragment.getView().findViewById(R.id.passwordSignin);
        final EditText passwordConfirm = (EditText) actualFragment.getView().findViewById(R.id.confirmSignin);
        String userRole = "no role";
        EditText emailText = (EditText) actualFragment.getView().findViewById(R.id.mailAddress);
        EditText phoneNumberText = (EditText) actualFragment.getView().findViewById(R.id.phoneNumber);
        final String email = emailText.getText().toString();
        final String phoneNumber = phoneNumberText.getText().toString();


        if(isUser) {
            userRole = ((Spinner) actualFragment.getView().findViewById(R.id.selectrole)).getSelectedItem().toString();
        }
        final String role = userRole;
        boolean firstControl = true;
        username = usernameEdit.getText().toString();
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
        if(!validate(email)) {
            emailText.setError("Please, insert a valid email");
            firstControl = false;
        }

        if(phoneNumber.length() != 10) {
            phoneNumberText.setError("Please, insert a valid phone number");
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
                                    String message = "Your profile is: \nUsername " + usernameEdit.getText().toString() + "\n";
                                    if(isUser) {
                                        message += "Role " + role;
                                    }
                                    builder.setMessage(message)
                                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Map<String,Object> user = new HashMap<>();
                                                    user.put("username",usernameEdit.getText().toString());
                                                    user.put("password",passwordConfirm.getText().toString());
                                                    if(isUser) {
                                                        user.put("role", role);
                                                        user.put("rates", new ArrayList<>());
                                                        user.put("preferences", new ArrayList<>());
                                                    }
                                                    user.put("player", isUser);
                                                    user.put("email", email);
                                                    user.put("phone", phoneNumber);
                                                    Log.e("", "Called");
                                                    db.collection("users").document(user.get("username").toString())
                                                            .set(user)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void documentReference) {
                                                                    if(isUser){
                                                                        savePhoto();
                                                                    }
                                                                    AlertDialog.Builder builder_ = new AlertDialog.Builder(getActivity());
                                                                    builder_.setMessage("Your account was created successfully");
                                                                    builder_.create().show();
                                                                    startActivity(new Intent(getActivity(),LoginActivity.class));
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

    private void savePhoto() {
        StorageReference ref = StaticInstance.mStorageRef.child("users/"+username);
        final FootballerSignIn fragment = ((FootballerSignIn)actualFragment);
        String path = fragment.getPath();
        if(path!=null) {
            ref.putFile(Uri.parse(path))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fragment.setPath(null);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            fragment.setPath(null);
                        }
                    });
        }
    }

    /*
    public void validateFields(View v) {

        final EditText usernameEdit = (EditText) findViewById(R.id.usernameSignin);
        EditText passwordEdit = (EditText) findViewById(R.id.passwordSignin);
        final EditText passwordConfirm = (EditText) findViewById(R.id.confirmSignin);
        final String role = ((Spinner) findViewById(R.id.selectrole)).getSelectedItem().toString();
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
    */
    private Context getActivity() {
        return this;
    }

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }


}

