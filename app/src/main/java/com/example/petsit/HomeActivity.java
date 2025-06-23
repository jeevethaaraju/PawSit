package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class HomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_OWNER_ID = "owner_id";
    private static final String TAG = "HomeActivity";

    private LinearLayout petsContainer;
    private LinearLayout sittersContainer;
    private String phpApiUrl = Constants.BASE_URL + "get_pets.php";
    private String nearestSittersUrl = Constants.BASE_URL + "get_nearest_sitters.php";
    public static final String FETCH_WALLET_URL = Constants.BASE_URL + "fetch_wallet_balance.php";


    // Add this to your HomeActivity fields
    private TextView walletBalanceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        NavigationUtil.setupBottomNavigation(this);
        // Initialize views
        ImageView profileBtn = findViewById(R.id.profilehome);
        TextView nameTextView = findViewById(R.id.name);
        ImageView profileIcon = findViewById(R.id.profileicon);
        CardView addPetCard = findViewById(R.id.addPetCard);
        petsContainer = findViewById(R.id.petsContainer);
        sittersContainer = findViewById(R.id.cardview1);
        walletBalanceText = findViewById(R.id.wallet_balance);
        ImageView map = findViewById(R.id.mapicon);
        map.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, MapActivity.class));
        });

        ImageView imageVector = findViewById(R.id.reporticon);

        imageVector.setOnClickListener(view -> {
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int ownerId = preferences.getInt(KEY_OWNER_ID, -1);
            Intent intent = new Intent(this, ReportActivity.class);
            intent.putExtra("petOwner_ID", ownerId);
            Toast.makeText(this, "Retrieved owner ID: " + ownerId, Toast.LENGTH_SHORT).show();
            startActivity(intent);
        });



        ImageView ordericon = findViewById(R.id.ordericon);
        ordericon.setOnClickListener(view -> {
            NavigationUtil.navigateToOrders(HomeActivity.this);
        });
        TextView see = findViewById(R.id.seeMoreText);
        see.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, AllSitterActivity.class));
        });

        // Set up profile icon click with logout option
        profileIcon.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(HomeActivity.this, profileIcon);
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_logout) {
                    logoutUser();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        // Get user data from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check for valid session token
        String sessionToken = preferences.getString(KEY_SESSION_TOKEN, null);
        int ownerId = preferences.getInt(KEY_OWNER_ID, -1);


        if (sessionToken != null && ownerId != -1) {
            fetchWalletBalance(ownerId, sessionToken);  // <-- ADD THIS LINE
        }

        if (sessionToken == null || ownerId == -1) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Set user data
        String firstName = preferences.getString(KEY_FIRST_NAME, "");
        String profileImage = preferences.getString(KEY_PROFILE_IMAGE, "");

        if (!firstName.isEmpty()) {
            nameTextView.setText(firstName);
        } else {
            nameTextView.setText("User");
        }

        // Load profile image
        if (profileImage != null && !profileImage.isEmpty()) {
            String fullImageUrl = Constants.BASE_URL + profileImage;
            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(profileIcon);
        } else {
            profileIcon.setImageResource(R.drawable.default_profile);
        }

        // Set click listeners
        profileBtn.setOnClickListener(view -> {
            Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
            profileIntent.putExtra("first_name", firstName);
            startActivity(profileIntent);
        });

        addPetCard.setOnClickListener(view -> {
            Intent petIntent = new Intent(HomeActivity.this, AddPetActivity.class);
            petIntent.putExtra("petOwner_ID", ownerId);
            startActivity(petIntent);
        });

        // Fetch pets for this owner
        new FetchPetsTask().execute(String.valueOf(ownerId), sessionToken);
        // Fetch nearest petsitters
        new FetchNearestSittersTask().execute(String.valueOf(ownerId), sessionToken);
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

        if (ownerId != -1 && sessionToken != null) {
            fetchWalletBalance(ownerId, sessionToken);
            new FetchPetsTask().execute(String.valueOf(ownerId), sessionToken);
            new FetchNearestSittersTask().execute(String.valueOf(ownerId), sessionToken);
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
                        Toast.makeText(HomeActivity.this,
                                "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON", e);
                    Toast.makeText(HomeActivity.this, "Error parsing pet data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(HomeActivity.this, "Failed to fetch pets", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FetchNearestSittersTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String ownerId = params[0];
            String sessionToken = params[1];
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(nearestSittersUrl + "?owner_id=" + ownerId);
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
                Log.e(TAG, "Error fetching nearest sitters", e);
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
                        JSONArray sittersArray = jsonResponse.getJSONArray("sitters");
                        displayNearestSitters(sittersArray);
                    } else {
                        Toast.makeText(HomeActivity.this,
                                "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON", e);
                    Toast.makeText(HomeActivity.this, "Error parsing sitters data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(HomeActivity.this, "Failed to fetch nearest sitters", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayPets(JSONArray petsArray) throws JSONException {
        petsContainer.removeViews(0, petsContainer.getChildCount() - 1);

        for (int i = 0; i < petsArray.length(); i++) {
            JSONObject pet = petsArray.getJSONObject(i);
            int petId = pet.getInt("id");
            String petName = pet.getString("petName");
            String petImage = pet.optString("petImage", "");
            String petType = pet.optString("petType", "");
            String petBreed = pet.optString("petBreed", "");
            String petSize = pet.optString("petSize", "");
            String petGender = pet.optString("petGender", "");
            String petBirthDate = pet.optString("petBirthDate", "");

            View petCard = LayoutInflater.from(this).inflate(R.layout.item_pet_card, petsContainer, false);
            ImageView petImageView = petCard.findViewById(R.id.petImage);

            if (!petImage.isEmpty()) {
                String fullImageUrl = Constants.BASE_URL + Constants.UPLOADS_DIR + petImage;
                Glide.with(this)
                        .load(fullImageUrl)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(petImageView);
            } else {
                petImageView.setImageResource(R.drawable.default_paw);
            }

            petCard.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, UpdatePetActivity.class);
                intent.putExtra("PET_ID", petId);
                intent.putExtra("PET_NAME", petName);
                intent.putExtra("PET_TYPE", petType);
                intent.putExtra("PET_BREED", petBreed);
                intent.putExtra("PET_SIZE", petSize);
                intent.putExtra("PET_GENDER", petGender);
                intent.putExtra("PET_BIRTH", petBirthDate);
                intent.putExtra("PET_IMAGE", petImage);
                startActivity(intent);
            });

            petsContainer.addView(petCard, petsContainer.getChildCount() - 1);
        }
    }

    private void displayNearestSitters(JSONArray sittersArray) throws JSONException {
        sittersContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < sittersArray.length(); i++) {
            JSONObject sitter = sittersArray.getJSONObject(i);
            int sitterId = sitter.getInt("id"); // Changed to int
            String firstName = sitter.getString("FirstName");
            String location = sitter.optString("Location", ""); // Make sure this matches your API response
            String profileImage = sitter.optString("ProfileImage", "");
            String distance = sitter.optString("distance", "N/A km away");

            View sitterCard = inflater.inflate(R.layout.sitter_card_template, sittersContainer, false);

            ImageView picture = sitterCard.findViewById(R.id.picture);
            TextView firstNameView = sitterCard.findViewById(R.id.firstName);
            TextView locationView = sitterCard.findViewById(R.id.location);
            Button bookButton = sitterCard.findViewById(R.id.bookButton);

            firstNameView.setText(firstName);
            locationView.setText(distance);

            if (!profileImage.isEmpty()) {
                String fullImageUrl = Constants.BASE_URL + profileImage;
                Glide.with(HomeActivity.this)
                        .load(fullImageUrl)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(picture);
            } else {
                picture.setImageResource(R.drawable.default_profile);
            }

            bookButton.setOnClickListener(v -> {
                fetchSitterDetailsBeforeOpening(sitterId);
            });

            sittersContainer.addView(sitterCard);
        }
    }

    private void fetchSitterDetailsBeforeOpening(int petsitterId) {
        String url = Constants.BASE_URL + "fetchdetail.php?petsitter_id=" + petsitterId;
        Log.d("HomeActivity", "Fetching from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    Log.d("HomeActivity", "API Response: " + response.toString());
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONObject sitter = response.getJSONObject("sitter");
                            JSONObject service = response.optJSONObject("service");

                            // Ensure phone number is included
                            if (!sitter.has("Phone_Number") && response.has("phone_number")) {
                                sitter.put("Phone_Number", response.getString("phone_number"));
                            }

                            if (service != null) {
                                // Log the sitter data to verify phone number exists
                                Log.d("HomeActivity", "Sitter data with phone: " + sitter.toString());

                                Intent intent = new Intent(HomeActivity.this, ViewDetailActivity.class);
                                intent.putExtra("sitter", sitter.toString());
                                intent.putExtra("service", service.toString());

                                startActivity(intent);
                            } else {
                                Toast.makeText(HomeActivity.this, "No service found for this pet sitter", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(HomeActivity.this, "Invalid petsitter ID or failed to load", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("HomeActivity", "JSON error", e);
                        Toast.makeText(HomeActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("HomeActivity", "Volley error", error);
                    Toast.makeText(HomeActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = Volley.newRequestQueue(HomeActivity.this);
        queue.add(request);
    }


    private void logoutUser() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, RoleActivity.class));
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
}