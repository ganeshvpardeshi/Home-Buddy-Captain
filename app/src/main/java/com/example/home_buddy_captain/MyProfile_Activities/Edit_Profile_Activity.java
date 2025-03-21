package com.example.home_buddy_captain.MyProfile_Activities;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.home_buddy_captain.R;
import com.example.home_buddy_captain.model.NewServiceManModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Edit_Profile_Activity extends AppCompatActivity {

    private Button updateBtn, clearAll;
    private EditText updateUserName, updateEmail, updateMobile, updateBio;
    private ImageView back_img;

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private String serviceCat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views
        back_img = findViewById(R.id.back_arrow);
        updateBtn = findViewById(R.id.update_btn);
        updateUserName = findViewById(R.id.update_username);
        updateEmail = findViewById(R.id.update_Email);
        updateMobile = findViewById(R.id.update_Mobile);
        updateBio = findViewById(R.id.update_Bio);

        // Initialize Firebase
        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();
//        creating the database reference of realtime db. of "Registered Service Man".
        databaseReference = FirebaseDatabase.getInstance().getReference("Registered Service Man");
        // Back button click listener
        back_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Finish the activity to return to the previous fragment
                finish();
            }
        });

        // Update button click listener
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(firebaseUser);
            }
        });

        // Load and display the current user's profile data
        fetchUserDetails(firebaseUser);
    }

    private void fetchUserDetails(FirebaseUser firebaseUser){
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
//                        passing the service category of service man to displayUserName function for further operations.
                        showProfile(firebaseUser);
                    } else {
                        Toast.makeText(Edit_Profile_Activity.this, "Service-Man not found in database!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(Edit_Profile_Activity.this, "Service Man doesn't exist.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Edit_Profile_Activity.this, "Something went wrong in getting data.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Method to load and display the current user's profile data
    private void showProfile(FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseUser.getUid();
        databaseReference.child(serviceCat).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    NewServiceManModel readUserDetails = snapshot.getValue(NewServiceManModel.class);
                    if (readUserDetails != null) {
                        // Set the current username and email in the EditText fields
                        updateUserName.setText(readUserDetails.getName());
                        updateEmail.setText(readUserDetails.getEmail());
                        updateMobile.setText(readUserDetails.getMobile());
                        if(readUserDetails.getBio() != null){
                            updateBio.setText(readUserDetails.getBio());
                        }else {
                            updateBio.setText("Nothing! Add Your Bio to show on your profile.");
                        }
                    }
                } else {
                    Toast.makeText(Edit_Profile_Activity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Edit_Profile_Activity.this, "Failed to fetch user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to update the user's profile data
    private void updateProfile(FirebaseUser firebaseUser) {
        String textName = updateUserName.getText().toString().trim();
        String textEmail = updateEmail.getText().toString().trim();
        String textMobile = updateMobile.getText().toString().trim();
        String textBio = updateBio.getText().toString().trim();
        // Validate inputs
        if (textName.isEmpty()) {
            updateUserName.setError("Username is required");
            updateUserName.requestFocus();
        } else if (textEmail.isEmpty()) {
            updateEmail.setError("Email is required");
            updateEmail.requestFocus();
        } else if (textMobile.isEmpty()) {
            updateMobile.setError("Mobile is required");
            updateMobile.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
            updateEmail.requestFocus();
            updateEmail.setError("Invalid Email");
        } else {
            // Update only the username and email fields using a Map
            String userId = firebaseUser.getUid();
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", textName);
            updates.put("email", textEmail);
            updates.put("mobile", textMobile);
            updates.put("bio", textBio);

            // Update the user's profile data in Firebase Realtime Database
            databaseReference.child(serviceCat).child(userId).updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Edit_Profile_Activity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    // Finish the activity to return to the previous fragment
                    finish();
                } else {
                    Toast.makeText(Edit_Profile_Activity.this, "Failed to update profile!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}