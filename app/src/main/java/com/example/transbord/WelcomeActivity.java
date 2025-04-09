package com.example.transbord;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.transbord.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize preferences manager
        preferencesManager = new PreferencesManager(this);

        // Check if this is the first time launch
        if (preferencesManager.isFirstTimeLaunch()) {
            // Show onboarding for first-time users
            Intent intent = new Intent(WelcomeActivity.this, OnboardingActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up Get Started button
        MaterialButton btnGetStarted = findViewById(R.id.btn_get_started);
        btnGetStarted.setOnClickListener(v -> {
            // Navigate to MainActivity
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the welcome screen
        });
    }
}
