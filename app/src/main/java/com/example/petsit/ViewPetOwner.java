package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ViewPetOwner extends AppCompatActivity {
    private static final String PREFS_NAME = "UserPrefs";

    private LinearLayout tableBody;
    private GestureDetector gestureDetector;
    private static final String TAG = "ViewPetOwner";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_petowner);

        // Initialize views
        tableBody = findViewById(R.id.table_body);
        ImageView profileIcon = findViewById(R.id.profileicon);

        // Set up profile icon click with logout option
        profileIcon.setOnClickListener(view -> {
            // Create and show a popup menu
            PopupMenu popupMenu = new PopupMenu(ViewPetOwner.this, profileIcon);
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_logout) {
                    // Handle logout
                    logoutUser();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
        ImageView containerBtn = findViewById(R.id.container_frame14);
        containerBtn.setOnClickListener(view -> {
            startActivity(new Intent(ViewPetOwner.this, AdminHomeActivity.class));
        });

        // Initialize gesture detector
        gestureDetector = new GestureDetector(this, new SwipeGestureListener());

        // Set touch listener on the root view
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        // Also set touch listener on scrollable content
        ScrollView scrollView = (ScrollView) tableBody.getChildAt(1);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false; // Let scroll view handle scrolling
            }
        });

        fetchPetOwners();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return gestureDetector.onTouchEvent(ev);
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 150;
        private static final int SWIPE_VELOCITY_THRESHOLD = 150;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            try {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD &&
                        Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) {
                        // Left swipe detected
                        Log.d(TAG, "Left swipe detected");
                        navigateToPetSitters();
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error on swipe", e);
            }
            return false;
        }
    }

    private void navigateToPetSitters() {
        Log.d(TAG, "Navigating to ViewPetSitter");
        try {
            Intent intent = new Intent(this, ViewPetSitter.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_rightt, R.anim.slide_out_leftt);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to ViewPetSitter", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPetOwners() {
        new Thread(() -> {
            try {
                URL url = new URL(Constants.BASE_URL + "fetchowners.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    processResponse(response.toString());
                } else {
                    runOnUiThread(() ->
                            showError("Error fetching data. Code: " + responseCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> showError("Connection error: " + e.getMessage()));
            }
        }).start();
    }

    private void processResponse(String response) {
        runOnUiThread(() -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.getBoolean("success")) {
                    JSONArray owners = jsonResponse.getJSONArray("owners");

                    ScrollView scrollView = (ScrollView) tableBody.getChildAt(1);
                    LinearLayout tableContent = (LinearLayout) scrollView.getChildAt(0);

                    if (tableContent.getChildCount() > 0) {
                        tableContent.removeViewAt(0);
                    }

                    for (int i = 0; i < owners.length(); i++) {
                        JSONObject owner = owners.getJSONObject(i);
                        addOwnerRow(
                                owner.getString("FirstName"),
                                owner.getString("Email"),
                                owner.getString("Phone_Number"),
                                tableContent
                        );
                    }
                } else {
                    showError(jsonResponse.getString("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Data parsing error: " + e.getMessage());
            }
        });
    }

    private void addOwnerRow(String name, String email, String phone, LinearLayout tableContent) {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView nameView = new TextView(this);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1));
        nameView.setGravity(android.view.Gravity.CENTER);
        nameView.setText(name);
        nameView.setTextColor(0xFF3B5571);
        nameView.setTextSize(14);

        TextView emailView = new TextView(this);
        emailView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.5f));
        emailView.setGravity(android.view.Gravity.CENTER);
        emailView.setText(email);
        emailView.setTextColor(0xFF3B5571);
        emailView.setTextSize(14);

        TextView phoneView = new TextView(this);
        phoneView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1));
        phoneView.setGravity(android.view.Gravity.CENTER);
        phoneView.setText(phone);
        phoneView.setTextColor(0xFF3B5571);
        phoneView.setTextSize(14);

        row.addView(nameView);
        row.addView(emailView);
        row.addView(phoneView);
        row.setMinimumHeight(40);

        tableContent.addView(row);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void logoutUser() {
        // Clear SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        // Redirect to login screen
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, RoleActivity.class));
        finish();
    }
}