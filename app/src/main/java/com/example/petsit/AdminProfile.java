package com.example.petsit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class AdminProfile extends AppCompatActivity {

    private EditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private Button updatePasswordButton;
    private TextView adminEmailTextView;
    private ImageView backArrow;
    private String adminEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminprofile);

        // Initialize views
        currentPasswordEditText = findViewById(R.id.text_current_password);
        newPasswordEditText = findViewById(R.id.text_new_password);
        confirmPasswordEditText = findViewById(R.id.text_confirm_password);
        updatePasswordButton = findViewById(R.id.container_button);
        adminEmailTextView = findViewById(R.id.text_admin_email);
        backArrow = findViewById(R.id.image_arrow_left);

        // Get admin email from intent or shared preferences
        // Here you should replace this with how you actually get the logged-in admin's email
        adminEmail = "admin@gmail.com"; // Default, replace with actual email
        adminEmailTextView.setText(adminEmail);

        // Set click listener for back arrow
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close current activity and go back
            }
        });

        // Set click listener for update password button
        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });
    }

    private void updatePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate inputs
        if (currentPassword.isEmpty()) {
            currentPasswordEditText.setError("Current password is required");
            currentPasswordEditText.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            newPasswordEditText.setError("New password is required");
            newPasswordEditText.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("Please confirm your new password");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords don't match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        // If all validations pass, send request to server
        updatePasswordOnServer(currentPassword, newPassword);
    }

    private void updatePasswordOnServer(final String currentPassword, final String newPassword) {
        String url = Constants.BASE_URL + "update_admin.php"; // Replace with your actual URL

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response.equals("success")) {
                                Toast.makeText(AdminProfile.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                // Clear fields
                                currentPasswordEditText.setText("");
                                newPasswordEditText.setText("");
                                confirmPasswordEditText.setText("");
                            } else {
                                Toast.makeText(AdminProfile.this, response, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(AdminProfile.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AdminProfile.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", adminEmail);
                params.put("current_password", currentPassword);
                params.put("new_password", newPassword);
                return params;
            }
        };

        queue.add(stringRequest);
    }
}