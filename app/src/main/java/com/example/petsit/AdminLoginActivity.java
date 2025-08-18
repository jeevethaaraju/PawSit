package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdminLoginActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "UserPrefs";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private static final String LOGIN_URL = Constants.BASE_URL + "loginadmin.php";;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginadmin);

        etEmail = findViewById(R.id.edit_email);
        etPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);

        ImageView imageVector = findViewById(R.id.img_vector1);

        imageVector.setOnClickListener(view -> {
            finish();
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(AdminLoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                } else {
                    new AdminLoginTask().execute(email, password);
                }
            }
        });
    }

    private class AdminLoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String password = params[1];

            try {
                URL url = new URL(LOGIN_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Create JSON request
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("Email", email);
                jsonParam.put("Password", password);

                // Send request
                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.close();

                // Get response
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    if (jsonResponse.getBoolean("success")) {
                        // Login successful
                        JSONObject adminData = jsonResponse.getJSONObject("admin");

                        // Save admin data to SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("admin_id", adminData.getString("id"));
                        editor.putString("admin_name", adminData.getString("name"));
                        editor.putString("admin_email", adminData.getString("email"));
                        editor.putBoolean("is_logged_in", true);
                        editor.apply();

                        // Navigate to AdminHomeActivity
                        Intent intent = new Intent(AdminLoginActivity.this, AdminHomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed
                        Toast.makeText(AdminLoginActivity.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(AdminLoginActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(AdminLoginActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
            }
        }
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