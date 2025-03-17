package com.example.home_buddy_captain.initial_connection;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationConnection {
    private final Context context;
    private final Fragment fragment;
    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;
    private final ActivityResultLauncher<String> permissionLauncher;
    private final ActivityResultLauncher<IntentSenderRequest> gpsLauncher;
    private LocationListener locationListener;

//    this "CONSTRUCTOR" WILL CALL BY A FRAGMENT
    public LocationConnection(Fragment fragment, Activity activity, ActivityResultLauncher<String> permissionLauncher, ActivityResultLauncher<IntentSenderRequest> gpsLauncher) {
        this.context = fragment.requireContext();
        this.fragment = fragment;
        this.activity = null;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.permissionLauncher = permissionLauncher;
        this.gpsLauncher = gpsLauncher;
    }
    // Constructor for Activity
    public LocationConnection(Activity activity, ActivityResultLauncher<String> permissionLauncher, ActivityResultLauncher<IntentSenderRequest> gpsLauncher) {
        this.context = activity;
        this.activity = activity;
        this.fragment = null; // Not used in this case
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.permissionLauncher = permissionLauncher;
        this.gpsLauncher = gpsLauncher;
    }
    // Call this method every time the app starts
    public void requestLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            // Check if GPS is enabled
            checkGPSStatus();
        }
    }

    // Check if GPS is enabled
    private void checkGPSStatus() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            // GPS is already enabled, get the location
            getUserLocation();
        });

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(resolvable.getResolution()).build();
                    gpsLauncher.launch(intentSenderRequest); // Ask user to enable GPS
                } catch (Exception sendEx) {
                    Toast.makeText(context, "Failed to enable GPS", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "GPS is not available on this device", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get the user's current location
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(activity, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        getAddressFromLocation(latitude, longitude);
                    } else {
                        Toast.makeText(context, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(activity, e ->
                        Toast.makeText(context, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // Convert latitude & longitude to address & get the PIN code
    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String postalCode = address.getPostalCode(); // Extract PIN code
                String areaName = address.getSubLocality();
                String cityName = address.getLocality();
                if (locationListener != null) {
                    locationListener.onLocationReceived(postalCode, areaName, cityName);
                }
            } else {
                Toast.makeText(context, "Unable to get address", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Geocoder failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void setLocationListener(LocationListener listener) {
        this.locationListener = listener;
    }

    public interface LocationListener {
        void onLocationReceived(String pinCode,String areaName, String cityName);
    }


    public String[] getSubLocalitiesForCity(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            Log.e("LocationConnection", "City name is null or empty!");
            return new String[0]; // Return empty array if city name is invalid
        }

        Set<String> subLocalitiesSet = new HashSet<>();
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            // Get latitude and longitude of the city
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (addresses == null || addresses.isEmpty()) {
                Log.e("LocationConnection", "City not found!");
                return new String[0]; // Return empty array if city is not found
            }

            Address cityAddress = addresses.get(0);
            double baseLat = cityAddress.getLatitude();
            double baseLng = cityAddress.getLongitude();

            // Scan nearby locations for sub-localities
            for (double lat = baseLat - 0.05; lat <= baseLat + 0.05; lat += 0.01) {
                for (double lng = baseLng - 0.05; lng <= baseLng + 0.05; lng += 0.01) {
                    List<Address> nearbyAddresses = geocoder.getFromLocation(lat, lng, 1);
                    if (nearbyAddresses != null && !nearbyAddresses.isEmpty()) {
                        Address address = nearbyAddresses.get(0);
                        String subLocality = address.getSubLocality();
                        if (subLocality != null && !subLocality.isEmpty()) {
                            subLocalitiesSet.add(subLocality); // Add unique sub-localities
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert Set to String Array
        return subLocalitiesSet.toArray(new String[0]);
    }
}
