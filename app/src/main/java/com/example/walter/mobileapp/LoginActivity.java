package com.example.walter.mobileapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText usernameEdit = (EditText) findViewById(R.id.username);
        final EditText passwordEdit = (EditText) findViewById(R.id.password);
        final boolean[] firstClick = { true , true};

        passwordEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (firstClick[0]) {
                    passwordEdit.setText("");
                    firstClick[0] = false;
                    passwordEdit.setTransformationMethod(new PasswordTransformationMethod());
                }
                return false;
            }
        });

        usernameEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (firstClick[1]) {
                    usernameEdit.setText("");
                    firstClick[1] = false;

                }
                return false;
            }
        });

    }

    public void goToSignin(View v){
        Intent intent = new Intent(this,SigninActivity.class);
        startActivity(intent);
    }
}
