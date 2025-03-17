package com.example.home_buddy_captain.initial_connection;

import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseConnection extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initializeSecondDB(getApplicationContext());
    }

    public DatabaseReference initializeSecondDB(Context context) {

        // Initialize the default Firebase app (primary project)
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context);
        }

        // Initialize the secondary Firebase app (first project)
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId("home-buddy-35fee")
                .setApplicationId("1:78669338367:android:07ce4db9b5ad6055185eca")
                .setApiKey("AIzaSyBUl42EbK0D4dzs7PDFrYnx0MwdNefvfCk")
                .setDatabaseUrl("https://home-buddy-35fee-default-rtdb.firebaseio.com")
                .build();


        FirebaseApp secondaryApp;
        try {
            secondaryApp = FirebaseApp.initializeApp(context, options, "secondary");
        } catch (IllegalStateException e) {
            secondaryApp = FirebaseApp.getInstance("secondary");
        }

        // Access the Realtime Database of the secondary project
        FirebaseDatabase secondaryDatabase = FirebaseDatabase.getInstance(secondaryApp);
        DatabaseReference secondaryRef = secondaryDatabase.getReference();

        return secondaryRef;
    }

}
