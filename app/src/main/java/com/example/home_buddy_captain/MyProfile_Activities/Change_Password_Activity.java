package com.example.home_buddy_captain.MyProfile_Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.home_buddy_captain.DashboardActivity;
import com.example.home_buddy_captain.ProgressHandler;
import com.example.home_buddy_captain.R;
import com.example.home_buddy_captain.initial_connection.PasswordHashingSecurity;
import com.example.home_buddy_captain.model.NewServiceManModel;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

public class Change_Password_Activity extends AppCompatActivity {

    private EditText current_pass, new_pass, con_new_pass;
    private FirebaseAuth authProfile;
    private TextView authenticated;
    private Button authenticate_btn, change_pass_btn;
    private ProgressBar progress_bar;
    private String user_current_pass;
    private ImageView back_img;
    private String serviceCat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize UI elements
        back_img = findViewById(R.id.back_arrow);
        current_pass = findViewById(R.id.update_password);
        new_pass = findViewById(R.id.new_password);
        con_new_pass = findViewById(R.id.confirm_new_password);
        authenticated = findViewById(R.id.text);
        progress_bar = findViewById(R.id.progress_bar);
        authenticate_btn = findViewById(R.id.authenticate_btn);
        change_pass_btn = findViewById(R.id.change_pass_btn);

        // Disable new password fields initially
        new_pass.setEnabled(false);
        con_new_pass.setEnabled(false);
        change_pass_btn.setEnabled(false);

        // Firebase authentication instance
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(Change_Password_Activity.this, "User details not available", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Change_Password_Activity.this, DashboardActivity.class));
            finish();
        } else {
            fetchUserRole(firebaseUser);
        }

        back_img.setOnClickListener(view -> finish());  // Navigate back
    }

    private void fetchUserRole(FirebaseUser firebaseUser) {
//        fetching the role of service man from another db -> "Registered ServiceMan User"
        DatabaseReference shortReference = FirebaseDatabase.getInstance().getReference("Registered ServiceMan User").child(firebaseUser.getUid());
        shortReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    NewServiceManModel readUserDetails = snapshot.getValue(NewServiceManModel.class);
                    if (readUserDetails != null && readUserDetails.getServiceCat() != null) {
                        serviceCat = readUserDetails.getServiceCat();
                        System.out.println(serviceCat + " Service Category");
                        reAuthenticateUser(firebaseUser);
//                        passing the service category of service man to displayUserName function for further operations.
                    } else {
                        Toast.makeText(Change_Password_Activity.this, "Service-Man not found in database!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Change_Password_Activity.this, "Service Man doesn't exist.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Change_Password_Activity.this, "Something went wrong in getting data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        authenticate_btn.setOnClickListener(view -> {

            final ProgressHandler progressHandler = new ProgressHandler(Change_Password_Activity.this);
            user_current_pass = current_pass.getText().toString();

            if (TextUtils.isEmpty(user_current_pass)) {
                current_pass.setError("Please enter your current password");
                current_pass.requestFocus();
                return;
            }

            progressHandler.show();
            AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), user_current_pass);

            firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
                progressHandler.dismiss();
                if (task.isSuccessful()) {
                    // Authentication successful, enable new password fields
                    current_pass.setEnabled(false);
                    new_pass.setEnabled(true);
                    con_new_pass.setEnabled(true);
                    change_pass_btn.setEnabled(true);
                    authenticate_btn.setEnabled(false);

                    authenticated.setText("Authentication successful! You can change your password now.");
                    authenticated.setTextColor(ContextCompat.getColorStateList(Change_Password_Activity.this, R.color.green));
                    change_pass_btn.setBackgroundTintList(ContextCompat.getColorStateList(Change_Password_Activity.this, R.color.green));

                    change_pass_btn.setOnClickListener(view1 -> changePassword(firebaseUser));

                } else {
                    current_pass.setError("Invalid password");
                    current_pass.requestFocus();
                    Toast.makeText(Change_Password_Activity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void changePassword(FirebaseUser firebaseUser) {

        String userPassNew = new_pass.getText().toString().trim();
        String userPassCon = con_new_pass.getText().toString().trim();

        final ProgressHandler progressHandler = new ProgressHandler(Change_Password_Activity.this);

        // Password validation
        if (TextUtils.isEmpty(userPassNew)) {
            new_pass.setError("Please enter the new password");
            new_pass.requestFocus();
        } else if (!userPassNew.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+$).{7,}$")) {
            new_pass.setError("Must have 1 uppercase, 1 digit, 1 special character, and be at least 7 characters.");
            new_pass.requestFocus();
        } else if (TextUtils.isEmpty(userPassCon)) {
            con_new_pass.setError("Please confirm your new password");
            con_new_pass.requestFocus();
        } else if (!userPassNew.equals(userPassCon)) {
            con_new_pass.setError("Passwords do not match");
            con_new_pass.requestFocus();
        } else if (userPassNew.equals(user_current_pass)) {
            new_pass.setError("New password cannot be the same as the old password.");
            new_pass.requestFocus();
        } else {
            progressHandler.show();

            // **🔹 Hash the password using BCrypt**
            String hashedPassword = PasswordHashingSecurity.hashPassword(userPassNew);

            // **🔹 Step 1: Update password in Firebase Authentication**
            firebaseUser.updatePassword(userPassNew).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    DatabaseReference shortReference = FirebaseDatabase.getInstance().getReference("Registered ServiceMan User");
                    shortReference.child(firebaseUser.getUid()).child("password").setValue(hashedPassword)
                            .addOnSuccessListener(aVoid -> {
                                System.out.println("Password changed successfully!");
                            })
                            .addOnFailureListener(e -> {
                                System.out.println("Error updating hashed password in database!");
                            });

                    // **🔹 Step 2: Update the hashed password in Realtime Database**
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Service Man");
                    databaseReference.child(serviceCat).child(firebaseUser.getUid()).child("password").setValue(hashedPassword)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(Change_Password_Activity.this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                                progressHandler.dismiss();
                                startActivity(new Intent(Change_Password_Activity.this, Successful_Password_Changed.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(Change_Password_Activity.this, "Error updating hashed password in database!", Toast.LENGTH_SHORT).show();
                                progressHandler.dismiss();
                            });
                } else {
                    Toast.makeText(Change_Password_Activity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressHandler.dismiss();
                }
            });
        }
    }

}
