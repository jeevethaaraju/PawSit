package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.FrameLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_OWNER_ID = "owner_id";

    private GoogleMap mMap;
    private TextView servicePriceTextView;
    private List<LatLng> petSitterLocations = new ArrayList<>();
    private TextView serviceNameTextView;
    private ImageView catIcon, dogIcon;
    private FrameLayout containerCard;
    private TextView distanceTextView;
    private int ownerId;
    private JSONObject currentPetSitterData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        ownerId = preferences.getInt(KEY_OWNER_ID, -1);

        distanceTextView = findViewById(R.id.text_distance);
        containerCard = findViewById(R.id.container_card);
        containerCard.setVisibility(View.GONE);
        containerCard.setOnClickListener(v -> {
            if (currentPetSitterData != null) {
                openViewDetailActivity();
            }
        });

        serviceNameTextView = findViewById(R.id.service_name);
        servicePriceTextView = findViewById(R.id.service_price);
        catIcon = findViewById(R.id.caticon);
        dogIcon = findViewById(R.id.dogicon);

        setupBottomNavigation();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        new FetchPetSitterLocationsTask().execute();
    }

    private void openViewDetailActivity() {
        if (currentPetSitterData == null) {
            Toast.makeText(this, "No pet sitter data available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject sitter = new JSONObject();
            JSONObject service = new JSONObject();

            // Sitter data
            sitter.put("id", currentPetSitterData.optInt("id", -1));
            sitter.put("Location", currentPetSitterData.optString("location", ""));
            sitter.put("Phone_Number", currentPetSitterData.optString("phone", ""));
            sitter.put("ProfileImage", Constants.BASE_URL + currentPetSitterData.optString("profile_image", ""));

            // Service data
            service.put("service_name", currentPetSitterData.optString("service_name", ""));
            service.put("picture", Constants.BASE_URL + currentPetSitterData.optString("picture", ""));
            service.put("price", currentPetSitterData.optString("price", "0.00"));
            service.put("description", currentPetSitterData.optString("summ", ""));
            service.put("numofpets", currentPetSitterData.optString("numofpets", ""));
            service.put("accept_pet", currentPetSitterData.optString("accept_pet", ""));
            service.put("accept_petsize", currentPetSitterData.optString("accept_petsize", ""));
            service.put("unsupervised", currentPetSitterData.optString("unsupervised", "No"));
            service.put("potty", currentPetSitterData.optString("potty", "No"));
            service.put("walks", currentPetSitterData.optString("walks", "No"));
            service.put("home", currentPetSitterData.optString("home", "No"));
            service.put("transport", currentPetSitterData.optString("transport", "No"));
            service.put("service", currentPetSitterData.optString("service", ""));

            Intent intent = new Intent(MapActivity.this, ViewDetailActivity.class);
            intent.putExtra("sitter", sitter.toString());
            intent.putExtra("service", service.toString());
            startActivity(intent);

        } catch (JSONException e) {
            Log.e("MapActivity", "Error creating JSON: " + e.getMessage());
            Toast.makeText(this, "Error preparing details", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        ImageView ordericon = findViewById(R.id.ordericon);

        ordericon.setOnClickListener(view -> {
            NavigationUtil.navigateToOrders(MapActivity.this);
        });

        ImageView complaint = findViewById(R.id.reporticon);
        complaint.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int ownerId = preferences.getInt(KEY_OWNER_ID, -1);

            if (ownerId != -1) {
                Intent intent = new Intent(MapActivity.this, ReportActivity.class);
                intent.putExtra("petOwner_ID", ownerId);
                startActivity(intent);
            } else {
                showLoginPrompt();
            }
        });


        ImageView home = findViewById(R.id.home);
   home.setOnClickListener(v -> {
       NavigationUtil.navigateToHome(MapActivity.this);
        });

        ImageView back = findViewById(R.id.image_arrow_left);
        back.setOnClickListener(view -> finish());
    }

    private void showLoginPrompt() {
        Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        containerCard.setVisibility(View.GONE);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        LatLng defaultLocation = new LatLng(1.3521, 103.8198);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        new FetchPetSitterServiceTask(marker.getPosition()).execute();
        return true;
    }

    private void updateMapMarkers() {
        if (mMap == null) return;

        mMap.clear();
        for (LatLng location : petSitterLocations) {
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Pet Sitter"));
        }

        if (!petSitterLocations.isEmpty()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    petSitterLocations.get(0), 12));
        }
    }

    private class FetchPetSitterLocationsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(Constants.BASE_URL + "map.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    petSitterLocations.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        double lat = obj.getDouble("lat");
                        double lng = obj.getDouble("lng");
                        petSitterLocations.add(new LatLng(lat, lng));
                    }

                    updateMapMarkers();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class FetchPetSitterServiceTask extends AsyncTask<Void, Void, String> {
        private LatLng location;

        public FetchPetSitterServiceTask(LatLng location) {
            this.location = location;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String urlString = Constants.BASE_URL + "get_petsitter_name.php?lat=" +
                        location.latitude + "&lng=" + location.longitude +
                        "&owner_id=" + ownerId;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.has("service_name") && obj.has("price") && obj.has("accept_pet") && obj.has("distance")) {
                        currentPetSitterData = obj;

                        containerCard.setVisibility(View.VISIBLE);

                        serviceNameTextView.setText(obj.getString("service_name"));
                        servicePriceTextView.setText(String.format("RM %.2f/day", obj.getDouble("price")));
                        distanceTextView.setText(String.format("%.1f km away", obj.getDouble("distance")));

                        String acceptPet = obj.getString("accept_pet");
                        catIcon.setVisibility(acceptPet.toLowerCase().contains("cat") ? View.VISIBLE : View.GONE);
                        dogIcon.setVisibility(acceptPet.toLowerCase().contains("dog") ? View.VISIBLE : View.GONE);

                    } else if (obj.has("error")) {
                        hidePetInfoCard();
                        Toast.makeText(MapActivity.this, obj.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hidePetInfoCard();
                    Toast.makeText(MapActivity.this, "Error parsing service data", Toast.LENGTH_SHORT).show();
                }
            } else {
                hidePetInfoCard();
                Toast.makeText(MapActivity.this, "Failed to fetch service data", Toast.LENGTH_SHORT).show();
            }
        }

        private void hidePetInfoCard() {
            containerCard.setVisibility(View.GONE);
            serviceNameTextView.setText("");
            servicePriceTextView.setText("");
            catIcon.setVisibility(View.GONE);
            dogIcon.setVisibility(View.GONE);
            currentPetSitterData = null;
        }
    }
}