package com.example.home_buddy_captain.SignInSignUpActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.home_buddy_captain.DashboardActivity;
import com.example.home_buddy_captain.ProgressHandler;
import com.example.home_buddy_captain.R;
import com.example.home_buddy_captain.initial_connection.PasswordHashingSecurity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginScreenForServiceMan extends AppCompatActivity {

    private EditText email_login, pass_login;
    private Button sign_in_btn;
    //    private TextView pass_recover;
    private ProgressBar progressBar;

    private static final String TAG = "login_activity";

    private ImageView googleBtn;
    private ImageView backarrow;

    int RC_SIGN_IN = 20;

    String serviceManRole, serviceManEmail, serviceManHashedPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen_for_service_man);

        final ProgressHandler progressHandler = new ProgressHandler(LoginScreenForServiceMan.this);


        backarrow = (ImageView) findViewById(R.id.back_arrow);
        progressBar = findViewById(R.id.progress_bar);
        email_login = findViewById(R.id.login_email);
        pass_login = findViewById(R.id.login_password);
        sign_in_btn = findViewById(R.id.signin_btn);

        googleBtn = (ImageView) findViewById(R.id.google_btn);


        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginScreenForServiceMan.this, GetStartedActivity.class);
                startActivity(intent);
            }
        });

        sign_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Obtain/Get Entered Data
                String text_email = email_login.getText().toString();
                String text_pass = pass_login.getText().toString();

                // Validation
                if (text_email.isEmpty()) {
                    email_login.requestFocus();
                    email_login.setError("Field Cannot Be Empty");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(text_email).matches()) {
                    email_login.requestFocus();
                    email_login.setError("Invalid Email");
                } else if (text_pass.isEmpty()) {
                    pass_login.requestFocus();
                    pass_login.setError("Enter the Password");
                } else {
                    loginUser(text_email, text_pass);
                }
            }
        });
        progressHandler.dismiss();

    }

    private void loginUser(String text_email, String text_pass) {
        final ProgressHandler progressHandler = new ProgressHandler(LoginScreenForServiceMan.this);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithEmailAndPassword(text_email, text_pass)
                .addOnCompleteListener(LoginScreenForServiceMan.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressHandler.show();

                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser(); // Get authenticated user

                            if (firebaseUser != null) {
                                // Successfully signed in
                                String userUID = firebaseUser.getUid(); // Get the UID of the signed-in user
                                fetchUserData(userUID, text_email, text_pass);  // Fetch user data from database
                            } else {
                                Toast.makeText(LoginScreenForServiceMan.this, "Something went wrong! Try again later.", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                email_login.setError("User not registered, Please SignUp");
                                email_login.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                email_login.setError("Invalid Credentials. Kindly check and re-enter");
                                email_login.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(LoginScreenForServiceMan.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            progressHandler.dismiss();
                        }
                    }
                });
    }

    private void fetchUserData(String userUID, String text_email, String text_pass) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Registered ServiceMan User").child(userUID);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch email, hashed password, and role from the database
                    serviceManEmail = snapshot.child("email").getValue(String.class);
                    serviceManHashedPassword = snapshot.child("password").getValue(String.class);
                    serviceManRole = snapshot.child("serviceCat").getValue(String.class);

                    // If credentials match, log the user in (you can also check password here)
                    if (PasswordHashingSecurity.checkPassword(text_pass, serviceManHashedPassword)) {
                        // Move to Dashboard or next screen
                        Intent intent = new Intent(LoginScreenForServiceMan.this, DashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginScreenForServiceMan.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(LoginScreenForServiceMan.this, "User not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginScreenForServiceMan.this, "Database error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
