package com.example.home_buddy_captain.SignInSignUpActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.home_buddy_captain.DashboardActivity;
import com.example.home_buddy_captain.ProgressHandler;
import com.example.home_buddy_captain.R;
import com.example.home_buddy_captain.initial_connection.LocationConnection;
import com.example.home_buddy_captain.initial_connection.PasswordHashingSecurity;
import com.example.home_buddy_captain.model.NewServiceManModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpForServiceMan extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private RelativeLayout gen_relative_layout;
    private EditText etName, etEmail, etPassword, etAge, etMobile;
    private ImageView backarrow;
    private EditText etServiceCategory, etCharges, etExperience, etWorkingDays, etSubLocality;
    private TextView etLocality;
    private RadioGroup radioGroupGender;
    private Button btnNext1, btnPrev1, btnSubmit;
    private CircleImageView right_up_corner, left_up_corner, right_down_corner, left_down_corner;
    private String name, email, password, gender, age, mobile, serviceCat, workingDays, charges, experience;
    private String hashedPassword;
    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+$).{7,}$";
    private boolean is1stCardValid = false, is2ndCardValid = false;
    private String cityLocality;

    private final String[] serviceCategory = {"Electrician", "Plumber", "BroadBand Connection", "Cleaner", "Appliance Technician"};
    private final String[] workingDaysCategory = {"Mon-Fri 10AM to 6PM", "Mon-Sat 10AM to 6PM", "Mon-Fri 12AM to 8PM", "Mon-Sat 12AM to 8PM"};
    AutoCompleteTextView autoCompleteTxtServiceCategory, autoCompleteSubLocality, autoCompleteWorkingDays;
    ArrayAdapter<String> adapterItemsServiceCategory, adapterItemsSubLocality, adapterItemsWorkingDays;

    //    VARIABLE DECLARATION FOR LOCATION CONNECTION
    private LocationConnection locationConnection;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<IntentSenderRequest> gpsLauncher;

    private void gettingLocation() {
        initializeLaunchers();
        locationConnection = new LocationConnection(this, permissionLauncher, gpsLauncher);
        locationConnection.setLocationListener(new LocationConnection.LocationListener() {
            @Override
            public void onLocationReceived(String pinCode, String areaName, String cityName) {
                cityLocality = cityName;  // Set locality when received
                etLocality.setText(cityLocality);
                // Fetch sub-localities once the locality is available
                fetchSubLocalities(cityLocality);
            }
        });
        locationConnection.requestLocation();  // Request location, callback will handle fetching sub-localities
    }

    // Fetch sub-localities after locality is available
    private void fetchSubLocalities(String locality) {
        if (locality == null || locality.isEmpty()) {
            Toast.makeText(SignUpForServiceMan.this, "Locality not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] subLocalities = locationConnection.getSubLocalitiesForCity(locality);

        if (subLocalities == null || subLocalities.length == 0) {
            Toast.makeText(SignUpForServiceMan.this, "No sub-localities found for " + locality, Toast.LENGTH_SHORT).show();
        } else {
            autoCompleteSubLocality = findViewById(R.id.etSubLocality);
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
        setContentView(R.layout.activity_signup_for_service_man);

        // Initialize ViewFlipper and UI elements
        viewFlipper = findViewById(R.id.viewFlipper);

//      1st Card-View elements :
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etAge = findViewById(R.id.etAge);
        etMobile = findViewById(R.id.etPhone);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        gen_relative_layout = findViewById(R.id.gen_relative_layout);
//      2nd Card-View elements :
//        etWorkingDays = findViewById(R.id.etWorkingDays);
        etCharges = findViewById(R.id.etPricing);
        etExperience = findViewById(R.id.etExperience);
        etLocality = findViewById(R.id.etLocality);
        etSubLocality = findViewById(R.id.etSubLocality);
        etServiceCategory = findViewById(R.id.etServiceCategory);
        etWorkingDays = findViewById(R.id.etWorkingDays);
        btnNext1 = findViewById(R.id.btnNext1);
        btnSubmit = findViewById(R.id.btnNext3);
        btnPrev1 = findViewById(R.id.btnNext2);

        right_up_corner = findViewById(R.id.right_up_corner);
        left_up_corner = findViewById(R.id.left_up_corner);
        right_down_corner = findViewById(R.id.right_down_corner);
        left_down_corner = findViewById(R.id.left_down_corner);

//        hiding the left up and right down corner imageview
        left_up_corner.setVisibility(View.INVISIBLE);
        right_down_corner.setVisibility(View.INVISIBLE);
//        showing the left up and right down corner imageview
        right_up_corner.setVisibility(View.VISIBLE);
        left_down_corner.setVisibility(View.VISIBLE);

        backarrow = (ImageView) findViewById(R.id.back_arrow);


        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpForServiceMan.this, GetStartedActivity.class);
                startActivity(intent);
            }
        });

        // First Next Button
        btnNext1.setOnClickListener(v -> {

            name = etName.getText().toString();
            email = etEmail.getText().toString();
            password = etPassword.getText().toString();
            age = etAge.getText().toString();
            mobile = etMobile.getText().toString();


            if (!(isCardView_1_validate(name, email, password, age, mobile))) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {

                viewFlipper.showNext();
//              HIDING ONLY LEFT-DOWN & RIGHT-UP IMAGE-VIEW.
                left_down_corner.setVisibility(View.INVISIBLE);
                right_up_corner.setVisibility(View.INVISIBLE);

//              SHOWING ONLY LEFT-DOWN & RIGHT-UP IMAGE-VIEW.
                left_up_corner.setVisibility(View.VISIBLE);
                right_down_corner.setVisibility(View.VISIBLE);
            }
        });

        btnPrev1.setOnClickListener(view -> {
            viewFlipper.showPrevious(); // Move to previous CardView

//          SHOWING ONLY LEFT-DOWN & RIGHT-UP IMAGE-VIEW.
            left_down_corner.setVisibility(View.VISIBLE);
            right_up_corner.setVisibility(View.VISIBLE);

//          HIDING ONLY LEFT-DOWN & RIGHT-UP IMAGE-VIEW.
            left_up_corner.setVisibility(View.INVISIBLE);
            right_down_corner.setVisibility(View.INVISIBLE);
        });


        autoCompleteTxtServiceCategory = findViewById(R.id.etServiceCategory);
        adapterItemsServiceCategory = new ArrayAdapter<String>(this, R.layout.service_cat_dropdown, serviceCategory);
        autoCompleteTxtServiceCategory.setAdapter(adapterItemsServiceCategory);
        autoCompleteTxtServiceCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                serviceCat = parent.getItemAtPosition(position).toString();
            }
        });


        autoCompleteWorkingDays = findViewById(R.id.etWorkingDays);
        adapterItemsWorkingDays = new ArrayAdapter<String>(this, R.layout.service_cat_dropdown, workingDaysCategory);
        autoCompleteWorkingDays.setAdapter(adapterItemsWorkingDays);
        autoCompleteWorkingDays.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                workingDays = parent.getItemAtPosition(position).toString();
            }
        });

//        CALLING GETTINGLOCATION FUNCTION WHICH FETCH THE LOCATION AND ALSO RETRIEVE ALL SUB-LOCALITIES(AREA NAME) FROM IT.
        gettingLocation();

        // Second Next Button
        btnSubmit.setOnClickListener(v -> {
//            gender = etGender.getText().toString();


            experience = etExperience.getText().toString();
            serviceCat = autoCompleteTxtServiceCategory.getText().toString();
            charges = etCharges.getText().toString();
            String sub_locality = autoCompleteSubLocality.getText().toString();
            workingDays = autoCompleteWorkingDays.getText().toString();


            if (!(isCardView_2_validate(serviceCat, sub_locality, experience, charges, workingDays))) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
//              HIDING ONLY LEFT-DOWN & RIGHT-UP IMAGE-VIEW.
                left_down_corner.setVisibility(View.INVISIBLE);
                right_up_corner.setVisibility(View.INVISIBLE);

//              SHOWING ONLY LEFT-DOWN & RIGHT-UP IMAGE-VIEW.
                left_up_corner.setVisibility(View.VISIBLE);
                right_down_corner.setVisibility(View.VISIBLE);

                registerServiceMan(name, email, password, gender, age, mobile, serviceCat, experience, charges, workingDays, sub_locality);
            }
        });


    }

    //    VALIDATION FOR 2ND CARD-VIEW
    private boolean isCardView_2_validate(String serviceCat, String sub_locality, String experience, String charges, String working_days) {

        if (serviceCat.isEmpty()) {
            etServiceCategory.requestFocus();
            etServiceCategory.setError("Field Cannot Be Empty");
            is2ndCardValid = false;
        } else if (sub_locality.isEmpty()) {
            etSubLocality.requestFocus();
            etSubLocality.setError("Field Cannot Be Empty");
            is2ndCardValid = false;
        } else if (experience.isEmpty()) {
            etExperience.requestFocus();
            etExperience.setError("Field Cannot Be Empty");
            is2ndCardValid = false;
        } else if ((Integer.parseInt(experience)) > 60 || (Integer.parseInt(experience)) < -1) {
            etAge.requestFocus();
            etAge.setError("Invalid Experience");
            is2ndCardValid = false;
        } else if (charges.isEmpty() || charges.length() > 3) {
            etCharges.requestFocus();
            etCharges.setError("Field Cannot Be Empty");
            is2ndCardValid = false;
        } else if ((Integer.parseInt(charges)) > 500 || (Integer.parseInt(charges)) < 50) {
            etCharges.requestFocus();
            etCharges.setError("Invalid Charges");
            is2ndCardValid = false;
        } else if (working_days.isEmpty()) {
            etWorkingDays.requestFocus();
            etWorkingDays.setError("Field Cannot Be Empty");
            is2ndCardValid = false;
        } else {
            Log.d("Service Man Data : ", serviceCat + " " + sub_locality + " " + experience + " " + charges);
            is2ndCardValid = true;
        }
        return is2ndCardValid;
    }

    //    VALIDATION FOR 1ST CARD-VIEW
    private boolean isCardView_1_validate(String name, String email, String password, String age, String mobile) {
        RadioButton selectedBtn = radioGroupGender.findViewById(radioGroupGender.getCheckedRadioButtonId());
        // Validation for 1st Card-View
        if (name.isEmpty()) {
            etName.requestFocus();
            etName.setError("Field Cannot Be Empty.");
            is1stCardValid = false;
        } else if (email.isEmpty()) {
            etEmail.requestFocus();
            etEmail.setError("Field Cannot Be Empty.");
            is1stCardValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.requestFocus();
            etEmail.setError("Invalid Email.");
            is1stCardValid = false;
        } else if (password.isEmpty()) {
            etPassword.requestFocus();
            etPassword.setError("Please set a Password.");
            is1stCardValid = false;
        } else if (!(password.matches(PASSWORD_REGEX))) {
            etPassword.requestFocus();
            etPassword.setError("Invalid Password!\nMust have at least 1 uppercase, 1 digit, 1 special character, and be at least 7 characters.");
            is1stCardValid = false;
        } else if (password.length() < 6) {
            etPassword.requestFocus();
            etPassword.setError("Password Length should be more than 6 Digits.");
            is1stCardValid = false;
        } else if (age.isEmpty()) {
            etAge.requestFocus();
            etAge.setError("Field Cannot Be Empty.");
            is1stCardValid = false;
        } else if (age.length() < 2 || age.length() > 3 || (Integer.parseInt(age)) < 18 || (Integer.parseInt(age)) > 60) {
            etAge.requestFocus();
            etAge.setError("Invalid Age.");
            is1stCardValid = false;
        } else if (mobile.isEmpty()) {
            etMobile.requestFocus();
            etMobile.setError("Invalid Number.");
            is1stCardValid = false;
        } else if (mobile.length() <= 9 || !mobile.matches("[0-9]+") || mobile.length() > 10) {
            etMobile.requestFocus();
            etMobile.setError("Invalid Number.");
            is1stCardValid = false;
        } else if (selectedBtn == null) {
            // No button is selected
            gen_relative_layout.requestFocus();
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            is1stCardValid = false;
        } else {
            gender = selectedBtn.getText().toString();
            Log.d("Service Man Data : ", email + " " + password + " " + gender + " " + mobile + " " + age);
            is1stCardValid = true;
        }
        return is1stCardValid;
    }

    //    REGISTER METHOD FOR USER
    private void registerServiceMan(String name, String email, String password, String gender, String age, String mobile, String serviceCat, String experience, String charges, String workingDays, String sub_locality) {

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                final ProgressHandler progressHandler = new ProgressHandler(SignUpForServiceMan.this);
                if (task.isSuccessful()) {
                    progressHandler.show();
//                    getting current user
                    FirebaseUser serviceMan = auth.getCurrentUser();
//                    encrypting the password using becrypt hashing technique.
                    hashedPassword = PasswordHashingSecurity.hashPassword(password);
                    Log.d("HASHED PASS : ", " " + hashedPassword);
//                    Creating the short info of service to find out the role
                    NewServiceManModel shortServiceManModel = new NewServiceManModel(email, hashedPassword, serviceCat);
//                      creating the reference of realtime database for storing the only short info of service man to identify who is he.
                    DatabaseReference shortReference = FirebaseDatabase.getInstance().getReference("Registered ServiceMan User");
                    FirebaseUser shortUser = auth.getCurrentUser();
                    if (serviceMan != null) {
//                        this reference will store the data of serviceman like email pass and role to identify his role for further operations.
                        shortReference.child(shortUser.getUid()).setValue(shortServiceManModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("SHORT USER : ", "Data is Stored.");
                                }
                            }
                        });

//                        creating a object of service man with his details
                        NewServiceManModel newServiceManModel = new NewServiceManModel(name, email, hashedPassword, gender, age, mobile, serviceCat, experience, charges, workingDays, sub_locality);
//                        creating the database reference of realtime db. of "Registered Service Man".
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Service Man");
//                        this reference is storing the main / all data of service man into category of their choice in registered service man.
                        reference.child(serviceCat).child(serviceMan.getUid()).setValue(newServiceManModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignUpForServiceMan.this, "Service Man registered successful!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(SignUpForServiceMan.this, DashboardActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                    progressHandler.show();
                                    finish();
                                } else {
                                    Toast.makeText(SignUpForServiceMan.this, "Customer Registration Failed!, Please try again.", Toast.LENGTH_SHORT).show();
                                }
                                // Hide the Progress Bar
                                progressHandler.dismiss();
                                //progress_bar.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            etPassword.setError("Password too weak");
                            viewFlipper.showPrevious();
                            etPassword.requestFocus();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            etEmail.setError("Invalid Email");
                            viewFlipper.showPrevious();
                            etEmail.requestFocus();
                        } catch (FirebaseAuthUserCollisionException e) {
                            etEmail.setError("User Already registered using this email");
                            viewFlipper.showPrevious();
                            etEmail.requestFocus();
                        } catch (Exception e) {
                            Log.e("EXCEPTION : ", e.getMessage());
                            Toast.makeText(SignUpForServiceMan.this, "Something went wrong in app! Try again later.", Toast.LENGTH_SHORT).show();
                        }
                        progressHandler.dismiss();
                    }
                }
            }
        });

    }

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


