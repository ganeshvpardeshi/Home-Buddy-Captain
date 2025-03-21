package com.example.home_buddy_captain.fragment_page;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;
import static androidx.core.app.ActivityCompat.startActivityForResult;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.home_buddy_captain.MyProfile_Activities.Add_Service_Areas_Activity;
import com.example.home_buddy_captain.MyProfile_Activities.Change_Password_Activity;
import com.example.home_buddy_captain.MyProfile_Activities.Edit_Profile_Activity;
import com.example.home_buddy_captain.R;
import com.example.home_buddy_captain.SignInSignUpActivities.GetStartedActivity;
import com.example.home_buddy_captain.initial_connection.LocationConnection;
import com.example.home_buddy_captain.model.NewServiceManModel;
import com.example.home_buddy_captain.SignInSignUpActivities.SignUpSignInActivity;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private Context context;
    // for profile image
    private static final int PICK_IMAGE_REQUEST = 1;
    private CircleImageView imageButton;
    private ImageView imageViewUploadButton;

    private String base64Image;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser firebaseuser;

    private CardView editProfile, changePassword, subscription, addServiceAreas;
    Button logOut;

    private LocationConnection locationConnection;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<IntentSenderRequest> gpsLauncher;
    TextView locationView, phoneView;

    private TextView userName;
    private String name, phone, serviceCat;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment_profile = inflater.inflate(R.layout.fragment_profile, container, false);

        // for profile image
        imageButton = fragment_profile.findViewById(R.id.imgProfile);
        imageViewUploadButton = fragment_profile.findViewById(R.id.upload_img_imageView);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseuser = auth.getCurrentUser();
        userName = fragment_profile.findViewById(R.id.txtName);
        phoneView = fragment_profile.findViewById(R.id.txtPhone);
        fetchUserDetails(firebaseuser);

        loadProfileImage();     // function to load existing profile image

        // Click on ImageButton to open file picker
        imageViewUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImageToFirestore();
            }
        });

        editProfile = fragment_profile.findViewById(R.id.cardView_EditProfile);
        changePassword = fragment_profile.findViewById(R.id.cardView_ChangePassword);
        subscription = fragment_profile.findViewById(R.id.cardView_Subscription);
        addServiceAreas = fragment_profile.findViewById(R.id.cardView_add_service_areas);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Edit_Profile_Activity.class);
                startActivity(intent);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Change_Password_Activity.class);
                startActivity(intent);
            }
        });

        addServiceAreas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Add_Service_Areas_Activity.class);
                startActivity(intent);
            }
        });

        logOut = fragment_profile.findViewById(R.id.logOutbtn);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), GetStartedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // Initialize launchers
        initializeLaunchers();
        locationView = fragment_profile.findViewById(R.id.txtLocation);
        locationConnection = new LocationConnection(getActivity(), permissionLauncher, gpsLauncher);
        locationConnection.setLocationListener(new LocationConnection.LocationListener() {
            @Override
            public void onLocationReceived(String pinCode, String areaName, String cityName) {
                locationView.setText("City: " + cityName + "-" + pinCode);
            }
        });

        // Request location every time fragment starts
        locationConnection.requestLocation();


        return fragment_profile;
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
                        displayUsername(firebaseUser);
                    } else {
                        Toast.makeText(context, "Service-Man not found in database!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(context, "Service Man doesn't exist.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Something went wrong in getting data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void displayUsername(FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            Toast.makeText(getActivity(), "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Service Man");
        reference.child(serviceCat).child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    NewServiceManModel readUserDetails = snapshot.getValue(NewServiceManModel.class);
                    if (readUserDetails != null && readUserDetails.getName() != null) {
                        name = readUserDetails.getName();
                        userName.setText(name);
                        phone = readUserDetails.getMobile();
                        phoneView.setText("+91-" + phone);
                    } else {
                        Toast.makeText(context, "Username not found in database!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "User data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to fetch username: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


// Location Fetching Section Starts Here

    private void initializeLaunchers() {
        // Permission launcher for Fragment
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        locationConnection.requestLocation();
                    } else {
                        Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // GPS enable launcher for Fragment
        gpsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        locationConnection.requestLocation(); // Retry getting location
                    } else {
                        Toast.makeText(getContext(), "GPS is required!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


// Location Section Ends Here


// Profile Picture Section Starts Here

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ProfileFragment.PICK_IMAGE_REQUEST && resultCode == Edit_Profile_Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            convertImageToBase64(imageUri);
        }
    }

    // Convert Image to Base64
    private void convertImageToBase64(Uri imageUri) {
        try {
            InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream); // Compress image but image format should be JPEG (check it for all the formats of image)
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Set Image to ImageButton
            imageButton.setImageBitmap(bitmap);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    // Upload Image to Firestore
    private void uploadImageToFirestore() {
        if (base64Image == null) {
            Toast.makeText(getContext(), "No image selected!", Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentReference userRef = firestore.collection("Users").document(firebaseuser.getUid());
        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImage", base64Image);
        userRef.set(updates, com.google.firebase.firestore.SetOptions.merge()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to upload image!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load Profile Image from Firestore
    private void loadProfileImage() {
        DocumentReference userRef = firestore.collection("Users").document(firebaseuser.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("profileImage")) {
                String imageString = documentSnapshot.getString("profileImage");
                if (imageString != null && !imageString.isEmpty()) {
                    byte[] decodedBytes = Base64.decode(imageString, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    imageButton.setImageBitmap(decodedBitmap);
                }
            }
        });
    }

// Profile Picture Section Ends Here
}