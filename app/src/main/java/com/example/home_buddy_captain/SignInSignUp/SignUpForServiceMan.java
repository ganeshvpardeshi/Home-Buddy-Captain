package com.example.home_buddy_captain.SignInSignUp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.home_buddy_captain.DashboardActivity;
import com.example.home_buddy_captain.R;

public class SignUpForServiceMan extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private EditText etName, etEmail, etPassword, etGender, etDob, etMobile, etWorkingDays, etCharges, etExperience;
    private Button btnNext1, btnPrev1, btnNext2, btnPrev2, btnSubmit;

    private String name, email, password, gender, dob, mobile, workingDays, charges, experience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_for_service_man);

        // Initialize ViewFlipper and UI elements
        viewFlipper = findViewById(R.id.viewFlipper);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etGender = findViewById(R.id.etGender);
        etDob = findViewById(R.id.etDob);
        etMobile = findViewById(R.id.etMobile);
        etWorkingDays = findViewById(R.id.etWorkingDays);
        etCharges = findViewById(R.id.etCharges);
        etExperience = findViewById(R.id.etExperience);

        btnNext1 = findViewById(R.id.btnNext1);
        btnNext2 = findViewById(R.id.btnNext3);
        btnSubmit = findViewById(R.id.btnNext5);
        btnPrev1 = findViewById(R.id.btnNext2);
        btnPrev2= findViewById(R.id.btnNext4);

        // First Next Button
        btnNext1.setOnClickListener(v -> {
            name = etName.getText().toString();
            email = etEmail.getText().toString();
            password = etPassword.getText().toString();
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                viewFlipper.showNext(); // Move to next CardView
            }
        });
        btnPrev1.setOnClickListener(view -> {
            viewFlipper.showPrevious(); // Move to previous CardView
        });
        // Second Next Button
        btnNext2.setOnClickListener(v -> {
            gender = etGender.getText().toString();
            dob = etDob.getText().toString();
            mobile = etMobile.getText().toString();
            if (gender.isEmpty() || dob.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                viewFlipper.showNext(); // Move to next CardView
            }
        });
        btnPrev2.setOnClickListener(view -> {
            viewFlipper.showPrevious(); // Move to previous CardView
        });
        // Submit Button
        btnSubmit.setOnClickListener(v -> {
            workingDays = etWorkingDays.getText().toString();
            charges = etCharges.getText().toString();
            experience = etExperience.getText().toString();

            if (workingDays.isEmpty() || charges.isEmpty() || experience.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Display all data in a Toast
                String userDetails = "Name: " + name + "\nEmail: " + email + "\nGender: " + gender +
                        "\nDOB: " + dob + "\nMobile: " + mobile + "\nWorking Days: " + workingDays +
                        "\nCharges: " + charges + "\nExperience: " + experience;

                Toast.makeText(this, userDetails, Toast.LENGTH_LONG).show();

                // Navigate to Dashboard
                Intent intent = new Intent(SignUpForServiceMan.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
