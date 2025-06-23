package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;  // Add this import
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AlertDialog;

public class ViewPaymentActivity extends AppCompatActivity {

    private ConstraintLayout fpxContainer, visaContainer;
    private Button payButton;
    private String selectedOption = null;
    private String servicePrice;
    private double totalPrice = 0.0;
    private String serviceName;
    private String fromDateISO = null;
    private String toDateISO = null;
    private String textFromTime = null;
    private String textToTime = null;
    private String formattedTotalPrice;
    private ArrayList<String> selectedPetNames;
    private ArrayList<Integer> selectedPetIds;
    private ArrayList<String> selectedPetTypes;
    private int serviceId;
    private RequestQueue requestQueue;
    private static final String SAVE_BOOKING_URL = Constants.BASE_URL + "save_booking.php";
    private static final String TAG = "ViewPaymentActivity";
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_OWNER_ID = "owner_id";
    private int petsitterId;

    public static final String FETCH_WALLET_URL = Constants.BASE_URL + "fetch_wallet_balance.php";
    private static final String CHECK_WALLET_URL = Constants.BASE_URL + "check_wallet_balance.php";



    // Add this to your HomeActivity fields
    private TextView walletBalanceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_payment);
        selectedPetTypes = getIntent().getStringArrayListExtra("selected_pet_types");
        ArrayList<String> selectedPetTypes = getIntent().getStringArrayListExtra("selected_pet_types");
        serviceId = getIntent().getIntExtra("id", -1);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check for valid session token
        String sessionToken = preferences.getString(KEY_SESSION_TOKEN, null);
        int ownerId = preferences.getInt(KEY_OWNER_ID, -1);
       // Toast.makeText(this, "Retrieved owner ID: " + ownerId, Toast.LENGTH_SHORT).show();


        if (sessionToken == null || ownerId == -1) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (sessionToken != null && ownerId != -1) {
            fetchWalletBalance(ownerId, sessionToken);  // <-- ADD THIS LINE
        }
        petsitterId = getIntent().getIntExtra("petsitter_id", -1);
       // Toast.makeText(this, "Retrieved sitter ID: " + petsitterId, Toast.LENGTH_SHORT).show();

        if (petsitterId == -1) {
            showError("Invalid pet sitter ID");
            return;
        }

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);
        selectedPetIds = getIntent().getIntegerArrayListExtra("selected_pet_ids");
        totalPrice = 0.0;
        serviceName = getIntent().getStringExtra("service_name");
        selectedPetNames = getIntent().getStringArrayListExtra("selected_pet_names");
        fromDateISO = getIntent().getStringExtra("from_date");
        toDateISO = getIntent().getStringExtra("to_date");
        textFromTime = getIntent().getStringExtra("from_time");
        textToTime = getIntent().getStringExtra("to_time");
        servicePrice = getIntent().getStringExtra("price");
        formattedTotalPrice = getIntent().getStringExtra("total_price");
        selectedPetTypes = getIntent().getStringArrayListExtra("selected_pet_types");
        ConstraintLayout walletContainer = findViewById(R.id.wallet_container);

        walletContainer.setOnClickListener(view -> {
            selectedOption = "Wallet";
            updateSelectionUI();
            payButton.setEnabled(true);
        });

        ImageView backButton = findViewById(R.id.image_icon);
        fpxContainer = findViewById(R.id.fpx_container);
        visaContainer = findViewById(R.id.visa_container);
        payButton = findViewById(R.id.button_pay);
        walletBalanceText = findViewById(R.id.text_wallet_balance);



        backButton.setOnClickListener(view -> finish());

        fpxContainer.setOnClickListener(view -> {
            selectedOption = "FPX";
            updateSelectionUI();
            payButton.setEnabled(true);
        });

        visaContainer.setOnClickListener(view -> {
            selectedOption = "Visa";
            updateSelectionUI();
            payButton.setEnabled(true);
        });

        payButton.setOnClickListener(view -> {
            if (selectedOption == null) {
                Toast.makeText(this, "Please select a payment option", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("Wallet".equals(selectedOption)) {  // Add this case to your payment options
                checkWalletBalanceBeforePayment();
            } else {
                // For other payment methods (FPX/Visa)
                saveBookingToServer();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int ownerId = preferences.getInt(KEY_OWNER_ID, -1);
        String sessionToken = preferences.getString(KEY_SESSION_TOKEN, null);


        if (ownerId != -1 && sessionToken != null) {
            fetchWalletBalance(ownerId, sessionToken);  // <-- ADD THIS LINE
        }

    }

    private void saveBookingToServer() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int ownerId = preferences.getInt(KEY_OWNER_ID, -1);

        // Create JSON object with booking data
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("id", serviceId);
        bookingData.put("service_name", serviceName);
        bookingData.put("from_date", fromDateISO);
        bookingData.put("to_date", toDateISO);
        bookingData.put("from_time", textFromTime);
        bookingData.put("to_time", textToTime);
        bookingData.put("pets", selectedPetNames);
        bookingData.put("pet_types", selectedPetTypes);
        bookingData.put("price", servicePrice);
        bookingData.put("total_price", formattedTotalPrice);
        bookingData.put("pay_method", selectedOption);
        bookingData.put("petOwner_ID", ownerId);
        bookingData.put("petSitter_ID", petsitterId);

        // Send all pet IDs - convert ArrayList to JSON array
        try {
            JSONArray petIdsArray = new JSONArray();
            for (Integer id : selectedPetIds) {
                petIdsArray.put(id);
            }
            bookingData.put("pet_ids", petIdsArray);
        } catch (Exception e) {
            Log.e(TAG, "Error creating pet IDs array", e);
        }

        // Add headers for the request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                SAVE_BOOKING_URL,
                new JSONObject(bookingData),
                response -> {
                    try {
                        Log.d(TAG, "Booking response: " + response.toString());
                        if (response.getString("status").equals("success")) {
                            proceedToReceipt();
                        } else {
                            String errorMsg = response.optString("message", "Failed to save booking");
                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Booking error: " + error.getMessage());
                    if (error.networkResponse != null) {
                        String errorData = new String(error.networkResponse.data);
                        Log.e(TAG, "Error data: " + errorData);
                    }
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }
    private void proceedToReceipt() {
        Intent intent = new Intent(ViewPaymentActivity.this, ReceiptActivity.class);

        intent.putExtra("id", serviceId);
        intent.putExtra("selectedOption", selectedOption);
        intent.putExtra("price", servicePrice);
        intent.putExtra("service_name", serviceName);
        Toast.makeText(this, "Retrieved pet ID: " + selectedPetIds, Toast.LENGTH_SHORT).show();
        intent.putIntegerArrayListExtra("selected_pet_ids", selectedPetIds);
        intent.putStringArrayListExtra("selected_pet_names", selectedPetNames);
        intent.putStringArrayListExtra("selected_pet_types", selectedPetTypes);
        intent.putExtra("from_date", fromDateISO);
        intent.putExtra("to_date", toDateISO);
        intent.putExtra("from_time", textFromTime);
        intent.putExtra("to_time", textToTime);
        intent.putExtra("total_price", formattedTotalPrice);
        intent.putExtra("petsitter_id",petsitterId);
        startActivity(intent);
        finish();
    }

    private void updateSelectionUI() {
        ConstraintLayout walletContainer = findViewById(R.id.wallet_container); // Add this in XML

        fpxContainer.setBackgroundResource(R.drawable.drawable_shape_unselected);
        visaContainer.setBackgroundResource(R.drawable.drawable_shape_unselected);
        walletContainer.setBackgroundResource(R.drawable.drawable_shape_unselected);

        if ("FPX".equals(selectedOption)) {
            fpxContainer.setBackgroundResource(R.drawable.drawable_shape_selected);
        } else if ("Visa".equals(selectedOption)) {
            visaContainer.setBackgroundResource(R.drawable.drawable_shape_selected);
        } else if ("Wallet".equals(selectedOption)) {
            walletContainer.setBackgroundResource(R.drawable.drawable_shape_selected);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
        finish();
    }


    private void fetchWalletBalance(int ownerId, String sessionToken) {
        String url = FETCH_WALLET_URL + "?owner_id=" + ownerId;

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection conn = null;
                BufferedReader reader = null;

                try {
                    URL urlObj = new URL(url);
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Cookie", "PHPSESSID=" + sessionToken);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        return response.toString();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching wallet balance", e);
                } finally {
                    try {
                        if (reader != null) reader.close();
                        if (conn != null) conn.disconnect();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing resources", e);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(result);
                        if (jsonResponse.getString("status").equals("success")) {
                            double balance = jsonResponse.getDouble("balance");
                            String currency = jsonResponse.getString("currency");
                            walletBalanceText.setText(String.format("%s%.2f", currency, balance));
                        } else {
                            String errorMsg = jsonResponse.optString("message", "Failed to fetch balance");
                            Log.e(TAG, errorMsg);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing wallet balance", e);
                    }
                } else {
                    Log.e(TAG, "Null response when fetching wallet balance");
                }
            }
        }.execute();
    }

    private void checkWalletBalanceBeforePayment() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int ownerId = preferences.getInt(KEY_OWNER_ID, -1);

        if (ownerId == -1) {
            showError("Invalid owner ID");
            return;
        }

        try {
            double totalPriceValue = Double.parseDouble(formattedTotalPrice.replace("RM", "").trim());

            JSONObject requestData = new JSONObject();
            requestData.put("owner_id", ownerId);
            requestData.put("total_price", totalPriceValue);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    CHECK_WALLET_URL,
                    requestData,
                    response -> {
                        try {
                            if (response.getString("status").equals("success")) {
                                boolean hasSufficientFunds = response.getBoolean("has_sufficient_funds");
                                double currentBalance = response.getDouble("current_balance");

                                if (hasSufficientFunds) {
                                    showPaymentConfirmation( totalPriceValue);
                                } else {
                                    showInsufficientFundsDialog(currentBalance);
                                }
                            } else {
                                showError(response.getString("message"));
                            }
                        } catch (JSONException e) {
                            showError("Error parsing wallet check response");
                        }
                    },
                    error -> {
                        showError("Network error checking wallet balance");
                    }
            );

            requestQueue.add(request);
        } catch (NumberFormatException | JSONException e) {
            showError("Error processing payment amount");
        }
    }

    private void showPaymentConfirmation(double totalPriceValue) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Payment")
                .setMessage("Confirm payment of " + formattedTotalPrice + " from your wallet?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // Call a method to deduct wallet balance
                    deductWalletBalance(totalPriceValue);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void deductWalletBalance(double totalPriceValue) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int ownerId = preferences.getInt(KEY_OWNER_ID, -1);

        if (ownerId == -1) {
            showError("Invalid owner ID");
            return;
        }

        try {
            JSONObject requestData = new JSONObject();
            requestData.put("owner_id", ownerId);
            requestData.put("total_price", totalPriceValue);

            // Use your server's actual URL
            String DEDUCT_WALLET_URL = Constants.BASE_URL + "deduct_wallet_balance.php";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    DEDUCT_WALLET_URL,
                    requestData,
                    response -> {
                        try {
                            Log.d(TAG, "Deduction response: " + response.toString());

                            if (response.getString("status").equals("success")) {
                                double newBalance = response.getDouble("new_balance");
                                walletBalanceText.setText(String.format("RM%.2f", newBalance));

                                // Now save the booking after successful deduction
                                saveBookingToServer();
                            } else {
                                showError(response.getString("message"));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing deduction response", e);
                            showError("Error processing payment");
                        }
                    },
                    error -> {
                        Log.e(TAG, "Volley error: " + error.getMessage());
                        if (error.networkResponse != null) {
                            String errorData = new String(error.networkResponse.data);
                            Log.e(TAG, "Error data: " + errorData);
                        }
                        showError("Network error. Please try again.");
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating request", e);
            showError("Error creating payment request");
        }
    }

    private void showInsufficientFundsDialog(double currentBalance) {
        new AlertDialog.Builder(this)
                .setTitle("Insufficient Funds")
                .setMessage(String.format(
                        "Your wallet balance (RM%.2f) is insufficient for this payment (%s).\n\nPlease choose another payment method.",
                        currentBalance,
                        formattedTotalPrice
                ))
                .setPositiveButton("OK", null)
                .show();
    }

}