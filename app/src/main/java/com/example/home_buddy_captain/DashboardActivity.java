package com.example.home_buddy_captain;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private EditText etCity;
    private Spinner spinnerSubLocality;
    private Button btnLoadSubLocalities;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        etCity = findViewById(R.id.etCity);
        spinnerSubLocality = findViewById(R.id.spinnerSubLocality);
        btnLoadSubLocalities = findViewById(R.id.btnFetchSubLocalities); // Button to load sub-localities
        geocoder = new Geocoder(this, Locale.getDefault());

        // Set an OnClickListener for the button
        btnLoadSubLocalities.setOnClickListener(v -> {
            String cityName = etCity.getText().toString().trim();
            if (!TextUtils.isEmpty(cityName)) {
                Toast.makeText(this, "city name" + cityName, Toast.LENGTH_SHORT).show();
                // Fetch sub-localities when the button is clicked and the city name is valid
                fetchSubLocalities(cityName);
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to fetch sub-localities based on the city name
    private void fetchSubLocalities(String cityName) {
        List<String> subLocalities = new ArrayList<>();

        try {
            // Get a list of addresses based on the city name
            List<Address> addresses = geocoder.getFromLocationName(cityName, 10); // Limit to 10 results
            if (addresses != null && !addresses.isEmpty()) {
                for (Address address : addresses) {
                    // Add the sub-locality if it exists
                    if (address.getSubLocality() != null) {
                        subLocalities.add(address.getSubLocality());
                    }
                }
            }

            // If sub-localities were found, update the Spinner
            if (!subLocalities.isEmpty()) {
                ArrayAdapter<String> subLocalityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subLocalities);
                spinnerSubLocality.setAdapter(subLocalityAdapter);

                // Set an item selected listener to display the selected sub-locality
                spinnerSubLocality.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        String selectedSubLocality = (String) parentView.getItemAtPosition(position);
                        Toast.makeText(DashboardActivity.this, "Selected Sub-locality: " + selectedSubLocality, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // No action needed
                    }
                });
            } else {
                Toast.makeText(this, "No sub-localities found for this city.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error retrieving sub-localities.", Toast.LENGTH_SHORT).show();
        }
    }
}
