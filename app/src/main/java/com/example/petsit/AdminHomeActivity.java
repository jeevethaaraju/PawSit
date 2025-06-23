package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdminHomeActivity extends AppCompatActivity {

    private static final String KEY_ADMIN_NAME = "admin_name";
    private static final String PREFS_NAME = "AdminPrefs";

    private static final String COUNT_PETOWNERS_URL = Constants.BASE_URL + "count_petowner.php";
    private static final String COUNT_PETSITTERS_URL = Constants.BASE_URL + "count_petsitter.php";
    private static final String COUNT_APPROVED_BOOKINGS_URL = Constants.BASE_URL + "count_approved_bookings.php";
    private static final String COUNT_PENDING_BOOKINGS_URL = Constants.BASE_URL + "count_pending_bookings.php";

    private TextView petOwnerCountText;
    private TextView petSitterCountText;
    private TextView totalBookingsCountText;
    private TextView pendingBookingsCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminhome);

        // Setup bottom navigation
        NavigationAdminUtil.setupBottomNavigation(this);

        // Initialize views
        TextView nameTextView = findViewById(R.id.name);
        petOwnerCountText = findViewById(R.id.text_pet_owner_count);
        petSitterCountText = findViewById(R.id.text_pet_sitter_count);
        totalBookingsCountText = findViewById(R.id.text_total_bookings_count);
        pendingBookingsCountText = findViewById(R.id.text_pending_bookings_count);

        // Set admin name
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String firstName = preferences.getString(KEY_ADMIN_NAME, "");
        nameTextView.setText(!firstName.isEmpty() ? firstName : "Admin");

        // Fetch and display all counts
        new FetchPetOwnerCountTask().execute();
        new FetchPetSitterCountTask().execute();
        new FetchApprovedBookingsCountTask().execute();
        new FetchPendingBookingsCountTask().execute();

        // Setup profile icon menu
        ImageView profileIcon = findViewById(R.id.profileicon);
        profileIcon.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(AdminHomeActivity.this, profileIcon);
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

        // Setup click listeners for dashboard cards
        findViewById(R.id.petowner_container).setOnClickListener(view ->
                startActivity(new Intent(AdminHomeActivity.this, PetOwnersActivity.class)));

        findViewById(R.id.petsitter_container).setOnClickListener(view ->
                startActivity(new Intent(AdminHomeActivity.this, PetSittersActivity.class)));

        findViewById(R.id.total_container).setOnClickListener(view ->
                startActivity(new Intent(AdminHomeActivity.this, TotalCompleteActivity.class)));

        findViewById(R.id.pending_container).setOnClickListener(view ->
                startActivity(new Intent(AdminHomeActivity.this, TotalPendingActivity.class)));

        findViewById(R.id.message).setOnClickListener(view ->
                startActivity(new Intent(AdminHomeActivity.this, AdminViewReport.class)));

        findViewById(R.id.profilehome).setOnClickListener(view ->
                startActivity(new Intent(AdminHomeActivity.this, AdminProfile.class)));
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

    // Reusable method to fetch count from a URL with a JSON key
    private int fetchCountFromUrl(String urlString, String key) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                if (jsonObject.getBoolean("success")) {
                    return jsonObject.getInt(key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private class FetchPetOwnerCountTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            return fetchCountFromUrl(COUNT_PETOWNERS_URL, "total_petowners");
        }

        @Override
        protected void onPostExecute(Integer count) {
            petOwnerCountText.setText(count >= 0 ? String.valueOf(count) : "0");
        }
    }

    private class FetchPetSitterCountTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            return fetchCountFromUrl(COUNT_PETSITTERS_URL, "total_petsitters");
        }

        @Override
        protected void onPostExecute(Integer count) {
            petSitterCountText.setText(count >= 0 ? String.valueOf(count) : "0");
        }
    }

    private class FetchApprovedBookingsCountTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            return fetchCountFromUrl(COUNT_APPROVED_BOOKINGS_URL, "total_approved_bookings");
        }

        @Override
        protected void onPostExecute(Integer count) {
            totalBookingsCountText.setText(count >= 0 ? String.valueOf(count) : "0");
        }
    }

    private class FetchPendingBookingsCountTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            return fetchCountFromUrl(COUNT_PENDING_BOOKINGS_URL, "total_pending_bookings");
        }

        @Override
        protected void onPostExecute(Integer count) {
            pendingBookingsCountText.setText(count >= 0 ? String.valueOf(count) : "0");
        }
    }
}