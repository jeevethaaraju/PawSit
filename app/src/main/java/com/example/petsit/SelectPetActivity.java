package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class SelectPetActivity extends AppCompatActivity {

    private LinearLayout petsContainer;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_OWNER_ID = "owner_id";
    private String phpApiUrl = Constants.BASE_URL + "get_pets.php";
    private static final String TAG = "SelectPetActivity";

    private ArrayList<Integer> selectedPetIds = new ArrayList<>();
    private ArrayList<String> selectedPetNames = new ArrayList<>();

    private HashMap<Integer, View> petCardViews = new HashMap<>();
    private HashMap<Integer, String> selectedPetTypes = new HashMap<>();


    private int petsitterId;
    private int serviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_pet);



        // Receive service details from intent
        serviceId = getIntent().getIntExtra("id", -1);
        String serviceName = getIntent().getStringExtra("service_name");
        String serviceImageUrl = getIntent().getStringExtra("service_image");
        String servicePrice = getIntent().getStringExtra("price");

        /*Log.d("DEBUG_PRICE", "Price received in SelectPetActivity: " + servicePrice);*/
        //Toast.makeText(this, "Selected price(s): " + servicePrice, Toast.LENGTH_SHORT).show();

        petsitterId = getIntent().getIntExtra("petsitter_id", -1);
        //Toast.makeText(this, "Retrieved sitter ID: " + petsitterId, Toast.LENGTH_SHORT).show();

        if (petsitterId == -1) {
            showError("Invalid pet sitter ID");
            return;
        }

        if (serviceName != null) {
            TextView serviceTextView = findViewById(R.id.text_lina_amane);
            serviceTextView.setText(serviceName);
            Log.d("SERVICE_RECEIVED", "Received service name: " + serviceName);
        } else {
            Log.e("SERVICE_ERROR", "No service name received in intent");
        }

        ImageView serviceImageView = findViewById(R.id.img_ellipse2);
        if (serviceImageUrl != null && !serviceImageUrl.isEmpty()) {
            if (!serviceImageUrl.startsWith("http")) {
                serviceImageUrl = "http://" + serviceImageUrl;
            }
            Picasso.get()
                    .load(serviceImageUrl)
                    .placeholder(R.drawable.hugdog)
                    .error(R.drawable.hugdog)
                    .into(serviceImageView);
        } else {
            serviceImageView.setImageResource(R.drawable.hugdog);
        }

        CardView addPetCard = findViewById(R.id.addPetCard);
        petsContainer = findViewById(R.id.petsContainer);

        // Check session and ownerId
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String sessionToken = preferences.getString(KEY_SESSION_TOKEN, null);
        int ownerId = preferences.getInt(KEY_OWNER_ID, -1);

        if (sessionToken == null || ownerId == -1) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        petsitterId = getIntent().getIntExtra("petsitter_id", -1);
        //Toast.makeText(this, "Retrieved sitter ID: " + petsitterId, Toast.LENGTH_SHORT).show();

        if (petsitterId == -1) {
            showError("Invalid pet sitter ID");
            return;
        }
        // Fetch pets for this owner
        new FetchPetsTask().execute(String.valueOf(ownerId), sessionToken);

        addPetCard.setOnClickListener(view -> {
            Intent petIntent = new Intent(SelectPetActivity.this, AddPetActivity.class);
            petIntent.putExtra("petOwner_ID", ownerId);
            startActivity(petIntent);
        });

        ImageView imageVector = findViewById(R.id.img_arrow_left);
        imageVector.setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int ownerId = preferences.getInt(KEY_OWNER_ID, -1);
        String sessionToken = preferences.getString(KEY_SESSION_TOKEN, null);

        if (ownerId != -1 && sessionToken != null) {
            new FetchPetsTask().execute(String.valueOf(ownerId), sessionToken);
        }
    }

    private class FetchPetsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String ownerId = params[0];
            String sessionToken = params[1];
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(phpApiUrl + "?owner_id=" + ownerId);
                conn = (HttpURLConnection) url.openConnection();
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
                Log.e(TAG, "Error fetching pets", e);
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
                        JSONArray petsArray = jsonResponse.getJSONArray("pets");
                        displayPets(petsArray);
                    } else {
                        Toast.makeText(SelectPetActivity.this,
                                "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON", e);
                    Toast.makeText(SelectPetActivity.this, "Error parsing pet data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SelectPetActivity.this, "Failed to fetch pets", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayPets(JSONArray petsArray) throws JSONException {
        petsContainer.removeAllViews();
        selectedPetIds.clear();
        selectedPetNames.clear();
        petCardViews.clear();

        for (int i = 0; i < petsArray.length(); i++) {
            JSONObject pet = petsArray.getJSONObject(i);
            int petId = pet.getInt("id");
            String petName = pet.getString("petName");
            String petImage = pet.optString("petImage", "");
            String petType = pet.getString("petType"); // Fetch petType

            View petCard = LayoutInflater.from(this).inflate(R.layout.item_pet_card, petsContainer, false);
            ImageView petImageView = petCard.findViewById(R.id.petImage);
            ImageView selectionIcon = petCard.findViewById(R.id.img_selection);

            petCardViews.put(petId, petCard);

            if (!petImage.isEmpty()) {
                String fullImageUrl = Constants.BASE_URL + Constants.UPLOADS_DIR + petImage;
                Glide.with(this)
                        .load(fullImageUrl)
                        .placeholder(R.drawable.default_paw)
                        .error(R.drawable.default_paw)
                        .into(petImageView);
            } else {
                petImageView.setImageResource(R.drawable.default_paw);
            }

            petCard.setOnClickListener(v -> {
                if (selectedPetIds.contains(petId)) {
                    selectedPetIds.remove((Integer) petId);
                    selectedPetNames.remove(petName);
                    selectedPetTypes.remove(petId);
                    selectionIcon.setVisibility(View.GONE);
                    petCard.setBackgroundResource(R.drawable.pet_card_unselected);
                } else {
                    selectedPetIds.add(petId);
                    selectedPetNames.add(petName);
                    selectedPetTypes.put(petId, petType);
                    selectionIcon.setVisibility(View.VISIBLE);
                    petCard.setBackgroundResource(R.drawable.pet_card_selected);
                }

                ArrayList<String> selectedTypes = new ArrayList<>(selectedPetTypes.values());
                Toast.makeText(this, android.text.TextUtils.join(",", selectedTypes), Toast.LENGTH_SHORT).show();
            });

            petsContainer.addView(petCard);
        }

        // Add "Add Pet" CardView at the end
        CardView addPetCard = new CardView(this);
        CardView.LayoutParams params = new CardView.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics())
        );
        params.setMargins(16, 16, 16, 16);
        addPetCard.setLayoutParams(params);
        addPetCard.setCardElevation(4);
        addPetCard.setRadius(100); // Circular
        addPetCard.setCardBackgroundColor(getResources().getColor(R.color.white));

        ImageView addIcon = new ImageView(this);
        addIcon.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        addIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        addIcon.setImageResource(R.drawable.plus);

        addPetCard.addView(addIcon);

        addPetCard.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int ownerId = preferences.getInt(KEY_OWNER_ID, -1);
            Intent petIntent = new Intent(SelectPetActivity.this, AddPetActivity.class);
            petIntent.putExtra("petOwner_ID", ownerId);
            startActivity(petIntent);
        });

        petsContainer.addView(addPetCard);

        Button nextButton = findViewById(R.id.text_next);
        nextButton.setVisibility(View.VISIBLE);
        nextButton.setOnClickListener(v -> {
            if (selectedPetIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one pet", Toast.LENGTH_SHORT).show();
            } else {
                proceedToBooking();
            }
        });
    }

    private void proceedToBooking() {
        Intent bookingIntent = new Intent(this, BookingActivity.class);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int ownerId = preferences.getInt(KEY_OWNER_ID, -1);

        ArrayList<String> selectedTypes = new ArrayList<>(selectedPetTypes.values());
       // Toast.makeText(this, "Retrieved sitter ID: " + selectedPetIds, Toast.LENGTH_SHORT).show();

        bookingIntent.putExtra("service_name", getIntent().getStringExtra("service_name"));
        bookingIntent.putExtra("service_image", getIntent().getStringExtra("service_image"));
        bookingIntent.putIntegerArrayListExtra("selected_pet_ids", selectedPetIds);
        bookingIntent.putStringArrayListExtra("selected_pet_names", selectedPetNames);
        bookingIntent.putStringArrayListExtra("selected_pet_types", selectedTypes);

        bookingIntent.putExtra("price", getIntent().getStringExtra("price"));

        bookingIntent.putExtra("total_pets_selected", selectedPetIds.size());

        bookingIntent.putExtra("petOwner_ID", ownerId);
        bookingIntent.putExtra("petsitter_id", petsitterId);
        bookingIntent.putExtra("id", serviceId);
       // Toast.makeText(this, "Retrieved service ID: " + serviceId, Toast.LENGTH_SHORT).show();
        startActivity(bookingIntent);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
        finish();
    }
}
