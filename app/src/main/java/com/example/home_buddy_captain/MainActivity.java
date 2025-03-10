package com.example.home_buddy_captain;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class MainActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private EditText etName, etEmail, etPassword, etGender, etDob, etMobile, etWorkingDays, etCharges, etExperience;
    private Button btnNext1, btnNext2, btnNext3, btnNext4, btnSubmit;

    private String name, email, password, gender, dob, mobile, workingDays, charges, experience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        btnNext2 = findViewById(R.id.btnNext2);
        btnNext3 = findViewById(R.id.btnNext3);
        btnNext4 = findViewById(R.id.btnNext4);
        btnSubmit = findViewById(R.id.btnNext5);

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

        // Second Next Button
        btnNext3.setOnClickListener(v -> {
            gender = etGender.getText().toString();
            dob = etDob.getText().toString();
            mobile = etMobile.getText().toString();
            if (gender.isEmpty() || dob.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                viewFlipper.showNext(); // Move to next CardView
            }
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
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
