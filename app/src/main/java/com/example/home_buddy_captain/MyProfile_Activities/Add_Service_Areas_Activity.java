package com.example.home_buddy_captain.MyProfile_Activities;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.home_buddy_captain.R;
import com.example.home_buddy_captain.SignInSignUpActivities.SignUpForServiceMan;
import com.example.home_buddy_captain.initial_connection.LocationConnection;
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

public class Add_Service_Areas_Activity extends AppCompatActivity {

    private TextView selectedServiceLocation;
    private String cityLocality;
    private Button updateBtn, clearAllBtn;
    private String locationFetchFromDB;

    //    VARIABLE DECLARATION FOR LOCATION CONNECTION
    private LocationConnection locationConnection;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<IntentSenderRequest> gpsLauncher;
    private AutoCompleteTextView autoCompleteSubLocality;
    private ArrayAdapter<String>  adapterItemsSubLocality;
    private String serviceCat;
    private DatabaseReference databaseReference;

    private void gettingLocation() {
        initializeLaunchers();
        locationConnection = new LocationConnection(this, permissionLauncher, gpsLauncher);
        locationConnection.setLocationListener(new LocationConnection.LocationListener() {
            @Override
            public void onLocationReceived(String pinCode, String areaName, String cityName) {
                cityLocality = cityName;  // Set locality when received
                // Fetch sub-localities once the locality is available
                fetchSubLocalities(cityLocality);
            }
        });
        locationConnection.requestLocation();  // Request location, callback will handle fetching sub-localities
    }

    // Fetch sub-localities after locality is available
    private void fetchSubLocalities(String locality) {
        if (locality == null || locality.isEmpty()) {
            Toast.makeText(Add_Service_Areas_Activity.this, "Locality not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] subLocalities = locationConnection.getSubLocalitiesForCity(locality);

        if (subLocalities == null || subLocalities.length == 0) {
            Toast.makeText(Add_Service_Areas_Activity.this, "No sub-localities found for " + locality, Toast.LENGTH_SHORT).show();
        } else {
            autoCompleteSubLocality = findViewById(R.id.etAddServiceArea);
            adapterItemsSubLocality = new ArrayAdapter<>(this, R.layout.sub_locality_dropdown, subLocalities);
            autoCompleteSubLocality.setAdapter(adapterItemsSubLocality);
            autoCompleteSubLocality.setOnItemClickListener((parent, view, position, id) -> {
                String subLocalityArea = parent.getItemAtPosition(position).toString();
                // Handle the sub-locality selection here
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service_areas);
        selectedServiceLocation = findViewById(R.id.selected_service_location);

        updateBtn = findViewById(R.id.update_btn);
        clearAllBtn = findViewById(R.id.clear_all);

        FirebaseAuth authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
//        creating the database reference of realtime db. of "Registered Service Man".
        databaseReference = FirebaseDatabase.getInstance().getReference("Registered Service Man");
        fetchUserRole(firebaseUser);

//        CALLING GETTINGLOCATION FUNCTION WHICH FETCH THE LOCATION AND ALSO RETRIEVE ALL SUB-LOCALITIES(AREA NAME) FROM IT.
        gettingLocation();

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(firebaseUser);
            }
        });
        clearAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllLocation(firebaseUser);
            }
        });

    }

    private void clearAllLocation(FirebaseUser firebaseUser) {

        String textArea = locationFetchFromDB.split(",")[0];
        selectedServiceLocation.setText("Selected Location : " + textArea);

        if((locationFetchFromDB.split(",")).length < 2){
            Toast.makeText(this, "Location Cannot be Update, At least 1 Location has to be needed.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (textArea.isEmpty()) {
            autoCompleteSubLocality.setError("Username is required");
            autoCompleteSubLocality.requestFocus();
        } else {
            // Update only the username and email fields using a Map
            String userId = firebaseUser.getUid();
            Map<String, Object> updates = new HashMap<>();
            updates.put("sub_locality", textArea);

            // Update the user's profile data in Firebase Realtime Database
            databaseReference.child(serviceCat).child(userId).updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Add_Service_Areas_Activity.this, "Location Updated successfully!", Toast.LENGTH_SHORT).show();
                    // Finish the activity to return to the previous fragment
                    finish();
                } else {
                    Toast.makeText(Add_Service_Areas_Activity.this, "Failed to update profile!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateProfile(FirebaseUser firebaseUser) {
        String textArea =  autoCompleteSubLocality.getText().toString().trim();
        locationFetchFromDB = locationFetchFromDB + " , " + textArea;
        selectedServiceLocation.setText("Selected Location : " + locationFetchFromDB);
        // Validate inputs
        if (textArea.isEmpty()) {
            autoCompleteSubLocality.setError("Username is required");
            autoCompleteSubLocality.requestFocus();
        } else {
            // Update only the username and email fields using a Map
            String userId = firebaseUser.getUid();
            Map<String, Object> updates = new HashMap<>();
            updates.put("sub_locality", locationFetchFromDB);

            // Update the user's profile data in Firebase Realtime Database
            databaseReference.child(serviceCat).child(userId).updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Add_Service_Areas_Activity.this, "Location Updated successfully!", Toast.LENGTH_SHORT).show();
                    // Finish the activity to return to the previous fragment
                    finish();
                } else {
                    Toast.makeText(Add_Service_Areas_Activity.this, "Failed to update profile!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void fetchUserRole(FirebaseUser firebaseUser){
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
                        fetchDBLocation(firebaseUser);
                    } else {
                        Toast.makeText(Add_Service_Areas_Activity.this, "Service-Man not found in database!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(Add_Service_Areas_Activity.this, "Service Man doesn't exist.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Add_Service_Areas_Activity.this, "Something went wrong in getting data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchDBLocation(FirebaseUser firebaseUser){
        if (firebaseUser == null) {
            Toast.makeText(Add_Service_Areas_Activity.this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Service Man");
        reference.child(serviceCat).child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    NewServiceManModel readUserDetails = snapshot.getValue(NewServiceManModel.class);
                    if (readUserDetails != null && readUserDetails.getSub_locality() != null) {
                        locationFetchFromDB = readUserDetails.getSub_locality();
                        selectedServiceLocation.setText("Selected Location : " + locationFetchFromDB);
                    } else {
                        Toast.makeText(Add_Service_Areas_Activity.this, "Username not found in database!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Add_Service_Areas_Activity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Add_Service_Areas_Activity.this, "Failed to fetch username: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
//    location method
private void initializeLaunchers() {
    // Permission launcher for Fragment
    permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    locationConnection.requestLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // GPS enable launcher for Fragment
    gpsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == this.RESULT_OK) {
                    locationConnection.requestLocation(); // Retry getting location
                } else {
                    Toast.makeText(this, "GPS is required!", Toast.LENGTH_SHORT).show();
                }
            }
    );
}

}