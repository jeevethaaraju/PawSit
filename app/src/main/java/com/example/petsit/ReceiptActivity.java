package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {
    private String serviceName;
    private String servicePrice;
    private ArrayList<String> selectedPetNames;
    private ArrayList<String> selectedPetTypes;
    private String formattedTotalPrice;
    private String fromDateISO = null;
    private String toDateISO = null;
    private String textFromTime = null;
    private String textToTime = null;
    private String totalPrice;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_OWNER_ID = "owner_id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipt);  // make sure your layout file is named first.xml
        NavigationUtil.setupBottomNavigation(this);
        ImageView back = findViewById(R.id.img_arrow_left);

        back.setOnClickListener(view -> {
            finish();
        });
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

        String paymentMethod = getIntent().getStringExtra("selectedOption");
        TextView paymentMethodView = findViewById(R.id.method_pay); // Add this TextView in your layout
        paymentMethodView.setText("Payment Method: " + paymentMethod);

        serviceName = getIntent().getStringExtra("service_name");
        String displayServiceName = serviceName != null ? serviceName : "No service selected";
        ((TextView) findViewById(R.id.text_lina_amane)).setText(displayServiceName);

        selectedPetNames = getIntent().getStringArrayListExtra("selected_pet_names");
        String petNames = selectedPetNames.isEmpty() ? "No pet selected" : String.join(", ", selectedPetNames);
        ((TextView) findViewById(R.id.text_ricky_zon)).setText(petNames);



        selectedPetTypes = getIntent().getStringArrayListExtra("selected_pet_types");
        String petTypes = selectedPetTypes.isEmpty() ? "No pet selected" : String.join(", ", selectedPetTypes);
        ((TextView) findViewById(R.id.text_ricky_type)).setText(petTypes);

        servicePrice = getIntent().getStringExtra("price");
        String servicePriceStr = servicePrice != null ? servicePrice : "Null";
        ((TextView) findViewById(R.id.text_price_per_day)).setText("RM" + servicePriceStr + "/day");


        fromDateISO = getIntent().getStringExtra("from_date");
        String fromDate = getIntent().getStringExtra("from_date"); // yyyy-MM-dd
        ((TextView) findViewById(R.id.text_from_date_confirm)).setText(fromDate != null ? fromDate : "N/A");

        toDateISO = getIntent().getStringExtra("to_date");
        String toDate = getIntent().getStringExtra("to_date");
        ((TextView) findViewById(R.id.text_to_date_confirm)).setText(toDate != null ? toDate : "N/A");

        textFromTime =getIntent().getStringExtra("from_time");
        String fromTime = getIntent().getStringExtra("from_time"); // HH:mm
        ((TextView) findViewById(R.id.text_from_time_confirm)).setText(fromTime != null ? fromTime : "N/A");


        textToTime =getIntent().getStringExtra("to_time");
        String toTime = getIntent().getStringExtra("to_time");
        ((TextView) findViewById(R.id.text_to_time_confirm)).setText(toTime != null ? toTime : "N/A");

        formattedTotalPrice =getIntent().getStringExtra("total_price");
        String formattedTotalPrice = String.format(Locale.getDefault(), "RM%.2f", totalPrice);
        ((TextView) findViewById(R.id.text_total_price_per_day)).setText(formattedTotalPrice);

        String totalPrice = getIntent().getStringExtra("total_price"); // Get the total price

        // Display the values in your UI
        TextView totalPriceView = findViewById(R.id.text_total_price_per_day);
        totalPriceView.setText(totalPrice);
    }
}
