package com.example.weatherapp;

import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {


    private EditText userMail, userPassword;
    private Button btnLogin;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private Intent MainActivity;
    private ImageView loginPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userMail = findViewById(R.id.login_mail);
        userPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.login_btn);
        loginProgress = findViewById(R.id.login_progress);
        mAuth = FirebaseAuth.getInstance();
        MainActivity = new Intent(this, com.example.weatherapp.MainActivity.class);
        loginPhoto = findViewById(R.id.login_photo);
        loginPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent registerActivity = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(registerActivity);
                finish();

            }
        });


        loginProgress.setVisibility(View.INVISIBLE);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProgress.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.INVISIBLE);

                final String mail = userMail.getText().toString();
                final String password = userPassword.getText().toString();

                if(mail.isEmpty() || password.isEmpty()){
                    showMessage("Please verify all field");
                    btnLogin.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.VISIBLE);
                }
                else{
                    signIn(mail,password);
                }
            }
        });


    }

    private void signIn(String mail, String password) {

        mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

               if(task.isSuccessful()){
                   loginProgress.setVisibility(View.INVISIBLE);
                   btnLogin.setVisibility(View.VISIBLE);
                   uptateUI();
               }
               else{
                   btnLogin.setVisibility(View.VISIBLE);
                   loginProgress.setVisibility(View.VISIBLE);
                   showMessage(task.getException().getMessage());
               }

            }
        });

    }

    private void uptateUI() {
        startActivity(MainActivity);
        finish();
    }

    private void showMessage(String text) {

        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            uptateUI();
        }
    }
}
