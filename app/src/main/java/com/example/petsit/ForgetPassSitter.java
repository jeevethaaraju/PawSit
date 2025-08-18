package com.example.petsit;

import android.content.Intent;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgetPassSitter extends AppCompatActivity {
    private TextView etEmail, etNewPassword, etConfirmPassword;
    private EditText etResetCode;
    private Button btnSendCode, btnVerifyCode, btnResetPassword;
    private String currentEmail, resetToken;
    private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password_sitter);

        ImageView back = findViewById(R.id.img_vector1);
        back.setOnClickListener(view -> {
            finish();
        });

        requestQueue = Volley.newRequestQueue(this);
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSendCode = findViewById(R.id.btnSendCode);
    }

    private void setupClickListeners() {
        btnSendCode.setOnClickListener(v -> sendResetCode());
    }

    private void sendResetCode() {
        currentEmail = etEmail.getText().toString().trim();

        if (currentEmail.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constants.BASE_URL + "send_reset_code_sitter.php";
        Map<String, String> params = new HashMap<>();
        params.put("email", currentEmail);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Toast.makeText(ForgetPassSitter.this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            // Switch to pin verification layout
                            setContentView(R.layout.forget_password_pin_sitter);
                            ImageView imageVector = findViewById(R.id.img_vector1);

                            imageVector.setOnClickListener(view -> {
                                finish();
                            });
                            initializePinViews();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(ForgetPassSitter.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ForgetPassOwner", "Error sending reset code: " + error.getMessage());
                });

        requestQueue.add(request);
    }

    private void initializePinViews() {
        etResetCode = findViewById(R.id.etResetCode);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);

        btnVerifyCode.setOnClickListener(v -> verifyResetCode());
    }

    private void verifyResetCode() {
        String code = etResetCode.getText().toString().trim();

        if (code.isEmpty() || code.length() != 6) {
            Toast.makeText(this, "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constants.BASE_URL + "verify_reset_code_sitter.php";
        Map<String, String> params = new HashMap<>();
        params.put("email", currentEmail);
        params.put("code", code);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");

                        if (success) {
                            resetToken = response.getString("token");
                            // Switch to password reset layout
                            setContentView(R.layout.forget_password_reset_sitter);
                            ImageView imageVector = findViewById(R.id.img_vector1);

                            imageVector.setOnClickListener(view -> {
                                finish();
                            });
                            initializeResetViews();
                        } else {
                            String message = response.getString("message");
                            Toast.makeText(ForgetPassSitter.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(ForgetPassSitter.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ForgetPassOwner", "Error verifying code: " + error.getMessage());
                });

        requestQueue.add(request);
    }

    private void initializeResetViews() {
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter and confirm your new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constants.BASE_URL + "reset_password_sitter.php";
        Map<String, String> params = new HashMap<>();
        params.put("token", resetToken);
        params.put("newPassword", newPassword);
        params.put("confirmPassword", confirmPassword);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Toast.makeText(ForgetPassSitter.this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            // Password reset successful, return to login
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(ForgetPassSitter.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ForgetPassOwner", "Error resetting password: " + error.getMessage());
                });

        requestQueue.add(request);
    }
}