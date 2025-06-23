package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ConfirmationActivity extends AppCompatActivity {
    private ArrayList<String> selectedPetNames;
    private ArrayList<String> selectedPetTypes;


    private String serviceName;
    private String servicePrice;
    private String formattedTotalPrice;
    private double totalPrice = 0.0;
    private String fromDateISO = null;
    private String toDateISO = null;
    private ArrayList<Integer> selectedPetIds;
    private String textFromTime = null;
    private String textToTime = null;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String TAG = "ConfirmationActivity";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_OWNER_ID = "owner_id";

    private int petsitterId,serviceId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmation);
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

        selectedPetIds = getIntent().getIntegerArrayListExtra("selected_pet_ids");
        serviceId = getIntent().getIntExtra("id", -1);
        petsitterId = getIntent().getIntExtra("petsitter_id", -1);
        //Toast.makeText(this, "Retrieved sitter ID: " + petsitterId, Toast.LENGTH_SHORT).show();

        if (petsitterId == -1) {
            showError("Invalid pet sitter ID");
            return;
        }
        totalPrice = 0.0;
        serviceName = getIntent().getStringExtra("service_name");
        selectedPetNames = getIntent().getStringArrayListExtra("selected_pet_names");
        servicePrice = getIntent().getStringExtra("price");

        fromDateISO = getIntent().getStringExtra("from_date");
        toDateISO = getIntent().getStringExtra("to_date");

        textFromTime =getIntent().getStringExtra("from_time");
        textToTime =getIntent().getStringExtra("to_time");

        ArrayList<String> selectedPetTypes = getIntent().getStringArrayListExtra("selected_pet_types");

        TextView imageVector = findViewById(R.id.text_pay_now);

        imageVector.setOnClickListener(view -> {
            Intent intent = new Intent(ConfirmationActivity.this, ViewPaymentActivity.class);
           // Toast.makeText(this, "Retrieved sitter ID: " + serviceId, Toast.LENGTH_SHORT).show();
            intent.putExtra("id", serviceId);
            intent.putExtra("service_name", serviceName);
            intent.putStringArrayListExtra("selected_pet_names", selectedPetNames);
            intent.putStringArrayListExtra("selected_pet_types", selectedPetTypes);
            intent.putIntegerArrayListExtra("selected_pet_ids", selectedPetIds);
            intent.putExtra("price", servicePrice);
            intent.putExtra("from_date", fromDateISO);
            intent.putExtra("to_date", toDateISO);
            intent.putExtra("from_time", textFromTime);
            intent.putExtra("to_time", textToTime);
            intent.putExtra("total_price", formattedTotalPrice);
           // Toast.makeText(this, "Selected price(s): " + formattedTotalPrice, Toast.LENGTH_SHORT).show();
         //   Toast.makeText(ConfirmationActivity.this, "Total Price: " + formattedTotalPrice, Toast.LENGTH_SHORT).show();
            intent.putExtra("petsitter_id",petsitterId);
            intent.putExtra("service_id",serviceId);
           // String message = "Service: " + serviceName +
                   // "\nPet Types: " + selectedPetTypes;

            //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            startActivity(intent);
        });



        String serviceName = getIntent().getStringExtra("service_name");
        String servicePriceStr = getIntent().getStringExtra("price");
        ArrayList<String> selectedPetNames = getIntent().getStringArrayListExtra("selected_pet_names");
        ArrayList<Integer> selectedPetIds = getIntent().getIntegerArrayListExtra("selected_pet_ids");

        String fromDate = getIntent().getStringExtra("from_date"); // yyyy-MM-dd
        String toDate = getIntent().getStringExtra("to_date");     // yyyy-MM-dd
        String fromTime = getIntent().getStringExtra("from_time"); // HH:mm
        String toTime = getIntent().getStringExtra("to_time");     // HH:mm

        int totalNumOfPets = getIntent().getIntExtra("total_pets_selected", 1); // default 1 pet

        int totalPetsSelected = getIntent().getIntExtra("total_pets_selected", 0);

        //Toast.makeText(this, "Total pets selected: " + totalPetsSelected, Toast.LENGTH_SHORT).show();
        if (selectedPetNames == null) selectedPetNames = new ArrayList<>();
        if (selectedPetIds == null) selectedPetIds = new ArrayList<>();

        String petNames = selectedPetNames.isEmpty() ? "No pet selected" : String.join(", ", selectedPetNames);
        String petTypes = selectedPetTypes.isEmpty() ? "No pet selected" : String.join(", ", selectedPetTypes);
        String displayServiceName = serviceName != null ? serviceName : "No service selected";

        ((TextView) findViewById(R.id.text_lina_amane)).setText(displayServiceName);
        ((TextView) findViewById(R.id.text_ricky_zon)).setText(petNames);
        ((TextView) findViewById(R.id.text_ricky_type)).setText(petTypes);
        ((TextView) findViewById(R.id.text_from_date_confirm)).setText(fromDate != null ? fromDate : "N/A");
        ((TextView) findViewById(R.id.text_to_date_confirm)).setText(toDate != null ? toDate : "N/A");
        ((TextView) findViewById(R.id.text_from_time_confirm)).setText(fromTime != null ? fromTime : "N/A");
        ((TextView) findViewById(R.id.text_to_time_confirm)).setText(toTime != null ? toTime : "N/A");

        ((TextView) findViewById(R.id.text_price_per_day)).setText("RM" + servicePriceStr + "/day");

        double pricePerDay = 0.0;
        try {
            if (servicePriceStr != null) {
                pricePerDay = Double.parseDouble(servicePriceStr);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        double totalPrice = 0.0;

        if (fromDate != null && toDate != null && fromTime != null && toTime != null && pricePerDay > 0) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                Date start = sdf.parse(fromDate + " " + fromTime);
                Date end = sdf.parse(toDate + " " + toTime);

                if (start != null && end != null && !end.before(start)) {
                    long diffMillis = end.getTime() - start.getTime();

                    long fullDays = TimeUnit.MILLISECONDS.toDays(diffMillis);

                    long leftoverMillis = diffMillis - TimeUnit.DAYS.toMillis(fullDays);

                    // Calculate leftover hours with minutes as fractional hour
                    double leftoverHours = leftoverMillis / (1000.0 * 60 * 60);

                    double pricePerHour = pricePerDay / 24.0;

                    totalPrice = (fullDays * pricePerDay) + (leftoverHours * pricePerHour);

                    // Multiply by total number of pets selected
                    totalPrice = totalPrice * totalNumOfPets;
                    formattedTotalPrice = String.format(Locale.getDefault(), "RM%.2f", totalPrice);
                    ((TextView) findViewById(R.id.text_total_price_per_day)).setText(formattedTotalPrice);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        String formattedTotalPrice = String.format(Locale.getDefault(), "RM%.2f", totalPrice);
        ((TextView) findViewById(R.id.text_total_price_per_day)).setText(formattedTotalPrice);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
        finish();
    }
}
