package com.example.home_buddy_captain;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.home_buddy_captain.SignInSignUp.LoginScreenForServiceMan;
import com.example.home_buddy_captain.SignInSignUp.SignUpForServiceMan;
import com.google.firebase.auth.FirebaseAuth;

public class Splash_Screen extends AppCompatActivity {

    private static int SPLASH_TIMEOUT = 2000;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
    }

    protected void onStart() {
        super.onStart();

        // Splash Screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (authProfile.getCurrentUser() != null){
                    startActivity(new Intent(Splash_Screen.this, LoginScreenForServiceMan.class));
                    finish();
                } else{
                    Intent intent2 = new Intent(Splash_Screen.this, SignUpForServiceMan.class);
                    startActivity(intent2);
                    finish();
                }
            }
        },SPLASH_TIMEOUT);
    }
}