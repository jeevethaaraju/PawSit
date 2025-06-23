package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;

import com.squareup.picasso.Picasso;
import org.json.*;

import java.util.ArrayList;
import java.util.List;

public class ViewDetailActivity extends AppCompatActivity {

    private static final String PREFS = "UserPrefs";
    private String serviceName, price, phoneNumber;
    private int serviceId;
    private List<String> serviceImages = new ArrayList<>();
    private int sitterId;

    ImageView image1, image2, image3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewdetails);

        // Initialize views
        TextView locationText = findViewById(R.id.text_location_address);
        Button contactButton = findViewById(R.id.btn_get_contact);
        image1 = findViewById(R.id.img_image1);
        image2 = findViewById(R.id.img_image2);
        image3 = findViewById(R.id.img_image3);
        ImageView dogIcon = findViewById(R.id.img_dog);
        ImageView catIcon = findViewById(R.id.img_cat);
        TextView nameText = findViewById(R.id.text_name);
        TextView descriptionText = findViewById(R.id.text_distance_value);
        TextView petsText = findViewById(R.id.text_pet);
        TextView priceText = findViewById(R.id.text_price);
        TextView acceptSizeText = findViewById(R.id.text_accepted_pet_size_range);
        TextView unsupervisedText = findViewById(R.id.text_unattended_pets);
        TextView pottyText = findViewById(R.id.potty_value);
        TextView walksText = findViewById(R.id.text_walks_per_day_value);
        TextView homeText = findViewById(R.id.live_value);
        TextView transportText = findViewById(R.id.text_emergency_transport_yes);
        TextView serviceTypeText = findViewById(R.id.service_value);

        try {
            JSONObject sitter = new JSONObject(getIntent().getStringExtra("sitter"));
            JSONObject service = new JSONObject(getIntent().getStringExtra("service"));

            sitterId = sitter.optInt("id", -1);
            serviceId = service.optInt("id", -1);
            serviceName = service.optString("service_name");
            price = service.optString("price");
            phoneNumber = sitter.optString("Phone_Number", "");

            // Load basic sitter & service info
            locationText.setText(sitter.optString("Location", "Location not available"));
            nameText.setText(serviceName);
            descriptionText.setText(service.optString("description", "No description available"));
            petsText.setText(service.optString("numofpets", "1"));
            priceText.setText("RM" + price + "/day");

            // Handle pet type icons
            String acceptPet = service.optString("accept_pet", "").toLowerCase();
            dogIcon.setVisibility(acceptPet.contains("dog") ? View.VISIBLE : View.GONE);
            catIcon.setVisibility(acceptPet.contains("cat") ? View.VISIBLE : View.GONE);

            // Set additional service details
            acceptSizeText.setText(service.optString("accept_petsize", "Not specified"));
            unsupervisedText.setText(service.optString("unsupervised", "No"));
            pottyText.setText(service.optString("potty", "No"));
            walksText.setText(service.optString("walks", "No"));
            homeText.setText(service.optString("home", "No"));
            transportText.setText(service.optString("transport", "No"));

            // Format service type
            String serviceTypeStr = service.optString("service", "");
            try {
                JSONArray serviceArray = new JSONArray(serviceTypeStr);
                List<String> serviceLabels = new ArrayList<>();
                for (int i = 0; i < serviceArray.length(); i++) {
                    String type = serviceArray.optString(i, "").toLowerCase();
                    switch (type) {
                        case "walk": serviceLabels.add("Walking"); break;
                        case "bath": serviceLabels.add("Bathing"); break;
                        default:
                            if (!type.isEmpty())
                                serviceLabels.add(Character.toUpperCase(type.charAt(0)) + type.substring(1));
                            break;
                    }
                }
                serviceTypeText.setText(serviceLabels.isEmpty() ? "Not specified" : TextUtils.join(", ", serviceLabels));
            } catch (JSONException e) {
                serviceTypeText.setText("Invalid service data");
                Log.e("ViewDetail", "Error parsing service array", e);
            }

        } catch (Exception e) {
            Log.e("ViewDetail", "Error parsing data", e);
            Toast.makeText(this, "Error loading details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔽 Fetch image URLs from PHP
        fetchServiceImages(sitterId);

        // Back arrow
        findViewById(R.id.img_arrow_left).setOnClickListener(v -> finish());

        // Call sitter
        contactButton.setOnClickListener(v -> {
            if (!phoneNumber.isEmpty()) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Book now
        findViewById(R.id.btn_get_book).setOnClickListener(v -> {
            Intent sel = new Intent(ViewDetailActivity.this, SelectPetActivity.class);
            //Toast.makeText(this, "Retrieved service ID: " + serviceId, Toast.LENGTH_SHORT).show();
            sel.putExtra("id", serviceId);
            sel.putExtra("service_name", serviceName);
            sel.putExtra("service_images", new JSONArray(serviceImages).toString());
            sel.putExtra("price", price);
            sel.putExtra("petsitter_id", sitterId);
            startActivity(sel);
        });

        // Open Google Maps
        findViewById(R.id.btn_get_direction).setOnClickListener(v -> {
            String location = locationText.getText().toString();
            if (!location.equals("Location not available")) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No address available", Toast.LENGTH_SHORT).show();
            }
        });


        // Check login
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.getString("session_token", null) == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void fetchServiceImages(int petsitterId) {
        String url = Constants.BASE_URL + "viewdetails.php?petsitter_id=" + petsitterId; // 🔁 Change if needed

        new Thread(() -> {
            try {
                java.net.URL connectionUrl = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) connectionUrl.openConnection();
                conn.setRequestMethod("GET");

                java.io.InputStream in = conn.getInputStream();
                java.util.Scanner scanner = new java.util.Scanner(in).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";

                JSONObject json = new JSONObject(response);
                if (json.getBoolean("success")) {
                    JSONObject data = json.getJSONObject("data");

                    runOnUiThread(() -> {
                        if (data.has("picture1")) {
                            String img1 = data.optString("picture1");
                            serviceImages.add(img1);
                            Picasso.get().load(img1).placeholder(R.drawable.hugdog).into(image1);
                        }
                        if (data.has("picture2")) {
                            String img2 = data.optString("picture2");
                            serviceImages.add(img2);
                            image2.setVisibility(View.VISIBLE);
                            Picasso.get().load(img2).placeholder(R.drawable.hugdog).into(image2);
                        } else {
                            image2.setVisibility(View.GONE);
                        }
                        if (data.has("picture3")) {
                            String img3 = data.optString("picture3");
                            serviceImages.add(img3);
                            image3.setVisibility(View.VISIBLE);
                            Picasso.get().load(img3).placeholder(R.drawable.hugdog).into(image3);
                        } else {
                            image3.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Log.e("FetchImages", "No images found: " + json.optString("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("FetchImages", "Error fetching service images: " + e.getMessage());
            }
        }).start();
    }
}
