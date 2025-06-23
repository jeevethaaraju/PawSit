package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LoginSitActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private RequestQueue requestQueue;

    // SharedPreferences keys
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SESSION_TOKEN = "session_token";  // Store the session token here
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_SITTER_ID = "sitter_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login1);

        emailEditText = findViewById(R.id.edit_email);
        passwordEditText = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.btn_login);
        requestQueue = Volley.newRequestQueue(this);

        loginButton.setOnClickListener(view -> attemptLogin());

        ImageView imageVector = findViewById(R.id.img_vector1);

        TextView forgetpass = findViewById(R.id.text_forget_password);
        forgetpass.setOnClickListener(view -> {
            startActivity(new Intent(LoginSitActivity.this, ForgetPassSitter.class));
        });
        imageVector.setOnClickListener(view -> {
            Intent intent = new Intent(LoginSitActivity.this, RoleActivity.class);
            startActivity(intent);
        });
        TextView create = findViewById(R.id.text_create_account);
        create.setOnClickListener(view -> {
            Intent intent = new Intent(LoginSitActivity.this, SignUp.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constants.BASE_URL + "loginsit.php";  // Replace with your actual PHP server URL

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("Email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    try {
                        // Log the raw response for debugging
                        Log.d("LoginResponse", "Response: " + response.toString());

                        boolean success = response.getBoolean("success");
                        if (success) {
                            // Get the session token and petOwnerId from the response
                            String sessionToken = response.getString("session_token");  // Session token from PHP server
                            int petSitterId = response.getJSONObject("petSitter").getInt("petSitterId");  // Get petOwnerId from the response

                            // Save session token and petOwnerId (user ID) to SharedPreferences
                            SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt(KEY_SITTER_ID, petSitterId);  // Save the petOwnerId (user ID from petowner table)
                            editor.putString(KEY_SESSION_TOKEN, sessionToken);  // Save the session token
                            editor.apply();  // Don't forget to apply changes

                            // You can also save additional pet owner data (optional)
                            JSONObject petSitter = response.getJSONObject("petSitter");
                            editor.putString(KEY_FIRST_NAME, petSitter.getString("firstName"));
                            editor.putString(KEY_LAST_NAME, petSitter.getString("lastName"));
                            editor.putString(KEY_EMAIL, email);
                            editor.putString(KEY_PROFILE_IMAGE, petSitter.getString("profileImage"));
                            editor.apply();

                            // Pass data to HomeActivity
                            Intent intent = new Intent(LoginSitActivity.this, SitterHomeActivity.class);
                            intent.putExtra("profile_image", petSitter.getString("profileImage"));
                            intent.putExtra("first_name", petSitter.getString("firstName"));
                            intent.putExtra("last_name", petSitter.getString("lastName"));
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginSitActivity.this,
                                    response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginSitActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(LoginSitActivity.this,
                            "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }
}
