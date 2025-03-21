package com.example.home_buddy_captain.MyProfile_Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newhomieshifter.MainActivity;
import com.example.newhomieshifter.R;

public class Successful_Password_Changed extends AppCompatActivity {

    Button towards_dashboard_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_successful_password_changed);

        towards_dashboard_btn = findViewById(R.id.dashboard_btn);

        towards_dashboard_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Successful_Password_Changed.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}