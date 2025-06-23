/*
package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class test extends AppCompatActivity {

    // SharedPreferences keys
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_IMAGE = "profile_image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // Initialize views
        TextView fullNameTextView = findViewById(R.id.text_full_name);
        TextView emailTextView = findViewById(R.id.text_email);
        ImageView profileImageView = findViewById(R.id.container_group3);
        ImageView backBtn = findViewById(R.id.image_arrow_left);

        // Load user data from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String firstName = preferences.getString(KEY_FIRST_NAME, "");
        String lastName = preferences.getString(KEY_LAST_NAME, "");
        String email = preferences.getString(KEY_EMAIL, "");
        String profileImage = preferences.getString(KEY_PROFILE_IMAGE, "");

        // Debug logging
        Log.d("ProfileActivity", "Profile Image Path: " + profileImage);

        // Set user data to views
        if (!firstName.isEmpty()) {
            String fullName = firstName + (lastName.isEmpty() ? "" : " " + lastName);
            fullNameTextView.setText(fullName);
        } else {
            fullNameTextView.setText("User");
        }

        emailTextView.setText(email.isEmpty() ? "email@example.com" : email);

        // Load profile image using Glide
        if (!profileImage.isEmpty()) {
            String fullImageUrl = "http://192.168.0.10/" + profileImage;
            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.default_profile);
        }

        // Back button click handler
        backBtn.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserData();
    }

    private void refreshUserData() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        ImageView profileImageView = findViewById(R.id.container_group3);
        String profileImage = preferences.getString(KEY_PROFILE_IMAGE, "");

        if (!profileImage.isEmpty()) {
            String fullImageUrl = "http://192.168.0.10/" + profileImage;
            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(profileImageView);
        }
    }
}*/
