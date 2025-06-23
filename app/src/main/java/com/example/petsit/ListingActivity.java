package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.FileWriter;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class ListingActivity extends AppCompatActivity {
    // UI Components
    private EditText textServiceName, textSummary, pets, editPrice;
    private CheckBox checkboxCat, checkboxDog;
    private CheckBox checkboxSmall, checkboxMedium, checkboxLarge, checkboxGiant;
    private CheckBox checkboxBath, checkboxWalking, checkboxFeeding, checkboxPlaying;
    private Spinner whereSpinner, pottySpinner, walksSpinner, homeSpinner, transportSpinner;
    private Button saveButton;

    // Image handling
    private List<ImageContainer> imageContainers = new ArrayList<>();
    private int currentImageContainerIndex = -1;

    // State variables
    private int currentServiceId = -1;

    // SharedPreferences keys
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_SITTER_ID = "sitter_id";
    private static final String KEY_EMAIL = "email";

    // Constants
    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final String TAG = "ListingActivity";

    class ImageContainer {
        RelativeLayout container;
        ImageView imageView;
        Button uploadButton;
        ImageButton deleteButton;
        Uri imageUri;
        String imagePath;
        boolean shouldDelete = false;

        ImageContainer(RelativeLayout container, ImageView imageView, Button uploadButton, ImageButton deleteButton) {
            this.container = container;
            this.imageView = imageView;
            this.uploadButton = uploadButton;
            this.deleteButton = deleteButton;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.services);
        ImageView back = findViewById(R.id.image_arrow_left);

        back.setOnClickListener(view -> finish());

        initializeViews();
        setupSpinners();
        setupClickListeners();
        checkSessionAndFetchData();
    }

    private void initializeViews() {
        // Text inputs
        textServiceName = findViewById(R.id.text_service_name);
        textSummary = findViewById(R.id.text_summary);
        pets = findViewById(R.id.pets);
        editPrice = findViewById(R.id.edit_price);

        // Checkboxes
        checkboxCat = findViewById(R.id.checkbox_cat);
        checkboxDog = findViewById(R.id.checkbox_dog);
        checkboxSmall = findViewById(R.id.checkbox_size1);
        checkboxMedium = findViewById(R.id.checkbox_size2);
        checkboxLarge = findViewById(R.id.checkbox_size3);
        checkboxGiant = findViewById(R.id.checkbox_size4);
        checkboxBath = findViewById(R.id.checkbox_bath);
        checkboxWalking = findViewById(R.id.checkbox_walking);
        checkboxFeeding = findViewById(R.id.checkbox_feeding);
        checkboxPlaying = findViewById(R.id.checkbox_playing);

        // Spinners
        whereSpinner = findViewById(R.id.where_spinner);
        pottySpinner = findViewById(R.id.potty_spinner);
        walksSpinner = findViewById(R.id.walks_spinner);
        homeSpinner = findViewById(R.id.home_spinner);
        transportSpinner = findViewById(R.id.transport_spinner);

        // Buttons
        saveButton = findViewById(R.id.save_button);

        // Initialize image containers
        setupImageContainers();
    }

    private void setupImageContainers() {
        imageContainers.add(new ImageContainer(
                findViewById(R.id.image_container1),
                findViewById(R.id.selected_image_view1),
                findViewById(R.id.btn_upload_image1),
                findViewById(R.id.btn_delete_image1)
        ));

        imageContainers.add(new ImageContainer(
                findViewById(R.id.image_container2),
                findViewById(R.id.selected_image_view2),
                findViewById(R.id.btn_upload_image2),
                findViewById(R.id.btn_delete_image2)
        ));

        imageContainers.add(new ImageContainer(
                findViewById(R.id.image_container3),
                findViewById(R.id.selected_image_view3),
                findViewById(R.id.btn_upload_image3),
                findViewById(R.id.btn_delete_image3)
        ));

        // Set click listeners for each container
        for (int i = 0; i < imageContainers.size(); i++) {
            final int index = i;
            ImageContainer container = imageContainers.get(i);

            // Add hint for first image (required)
            if (i == 0) {
                container.uploadButton.setHint("Required Image");
                container.uploadButton.setHintTextColor(getResources().getColor(R.color.colorAccent));
            }

            container.uploadButton.setOnClickListener(v -> {
                currentImageContainerIndex = index;
                openImagePicker();
            });

            container.deleteButton.setOnClickListener(v -> {
                clearImagePreview(container);
                // Show toast if no images left
                if (!hasAtLeastOneImage()) {
                    Toast.makeText(ListingActivity.this,
                            "Please upload at least one image",
                            Toast.LENGTH_SHORT).show();
                }
            });

            container.imageView.setOnClickListener(v -> {
                if (container.imageView.getVisibility() == View.VISIBLE &&
                        container.imageView.getDrawable() != null) {
                    container.deleteButton.setVisibility(
                            container.deleteButton.getVisibility() == View.VISIBLE ?
                                    View.GONE : View.VISIBLE);
                }
            });
        }
    }

    // Helper method to check if at least one image exists
    private boolean hasAtLeastOneImage() {
        for (ImageContainer container : imageContainers) {
            if (container.imageUri != null && !container.shouldDelete) {
                return true;
            }
        }
        return false;
    }
    private void setupSpinners() {
        setupSpinner(whereSpinner, R.array.where_options);
        setupSpinner(pottySpinner, R.array.potty_options);
        setupSpinner(walksSpinner, R.array.walks_options);
        setupSpinner(homeSpinner, R.array.home_options);
        setupSpinner(transportSpinner, R.array.transport_options);
    }

    private void setupSpinner(Spinner spinner, int arrayResource) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                arrayResource,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> {
            if (currentServiceId == -1) {
                saveServiceData();
            } else {
                updateServiceData();
            }
        });
    }

    private void checkSessionAndFetchData() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String sessionToken = preferences.getString(KEY_SESSION_TOKEN, null);
        int sitterId = preferences.getInt(KEY_SITTER_ID, -1);
        String email = preferences.getString(KEY_EMAIL, "");

        if (sessionToken == null || sitterId == -1 || email.isEmpty()) {
            showSessionExpired();
            return;
        }

        fetchServiceDetails(sitterId);
    }

    private void showSessionExpired() {
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && currentImageContainerIndex != -1) {
            ImageContainer container = imageContainers.get(currentImageContainerIndex);
            handleSelectedImage(container, data.getData());
            currentImageContainerIndex = -1;
        }
    }

    private void handleSelectedImage(ImageContainer container, Uri imageUri) {
        container.imageUri = imageUri;
        container.imagePath = getPathFromUri(container.imageUri);
        container.uploadButton.setText("Change");

        container.imageView.setImageURI(container.imageUri);
        container.imageView.setVisibility(View.VISIBLE);
        container.deleteButton.setVisibility(View.VISIBLE);
        container.shouldDelete = false;
    }

    private void clearImagePreview(ImageContainer container) {
        container.imageView.setImageURI(null);
        container.imageView.setVisibility(View.GONE);
        container.deleteButton.setVisibility(View.GONE);
        container.imageUri = null;
        container.imagePath = null;
        container.uploadButton.setText("+ Add");
        container.shouldDelete = true;
    }

    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            try {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(columnIndex);
            } finally {
                cursor.close();
            }
        }
        return uri.getPath();
    }

    private void saveServiceData() {
        if (!validateInputFields()) {
            return;
        }

        try {
            JSONObject postData = buildPostData(false);
            if (postData == null) return;

            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            saveButton.setEnabled(false);
            saveButton.setText("Saving...");

            new SaveServiceTask(getImageFiles(), postData, preferences).execute();
        } catch (Exception e) {
            handleError("Save failed: " + e.getMessage(), e);
        }
    }

    private List<File> getImageFiles() {
        List<File> files = new ArrayList<>();
        for (ImageContainer container : imageContainers) {
            if (container.imagePath != null) {
                files.add(new File(container.imagePath));
            }
        }
        return files;
    }

    private void updateServiceData() {
        if (!validateInputFields()) {
            return;
        }

        if (currentServiceId == -1) {
            Toast.makeText(this, "Service ID missing. Please fetch service details first.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            JSONObject postData = buildPostData(true);
            if (postData == null) return;

            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            postData.put("petsitter_id", preferences.getInt(KEY_SITTER_ID, -1));

            saveButton.setEnabled(false);
            saveButton.setText("Updating...");

            new UpdateServiceTask(getImageFiles(), postData, preferences).execute();
        } catch (Exception e) {
            Log.e(TAG, "Update failed", e);
            runOnUiThread(() -> {
                Toast.makeText(this, "Update error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                saveButton.setEnabled(true);
                saveButton.setText("Update");
            });
        }
    }

    private JSONObject buildPostData(boolean includeServiceId) {
        if (!validateInputFields()) {
            return null;
        }

        try {
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            JSONObject postData = new JSONObject();

            if (includeServiceId && currentServiceId != -1) {
                postData.put("id", currentServiceId);
            }

            addBasicServiceInfo(postData, preferences);
            addPetPreferences(postData);
            addServiceOptions(postData);
            postData.put("price", editPrice.getText().toString().trim());

            return postData;
        } catch (JSONException e) {
            handleError("Error preparing data", e);
            return null;
        }
    }

    private boolean validateInputFields() {
        String serviceName = textServiceName.getText().toString().trim();
        String summary = textSummary.getText().toString().trim();
        String numOfPets = pets.getText().toString().trim();
        String price = editPrice.getText().toString().trim();

        if (serviceName.isEmpty() || summary.isEmpty() || numOfPets.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!checkboxCat.isChecked() && !checkboxDog.isChecked()) {
            Toast.makeText(this, "Please select at least one pet type", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!checkboxSmall.isChecked() && !checkboxMedium.isChecked() &&
                !checkboxLarge.isChecked() && !checkboxGiant.isChecked()) {
            Toast.makeText(this, "Please select at least one pet size", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (price.isEmpty()) {
            Toast.makeText(this, "Please enter a price", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!checkboxBath.isChecked() && !checkboxWalking.isChecked() &&
                !checkboxFeeding.isChecked() && !checkboxPlaying.isChecked()) {
            Toast.makeText(this, "Please select at least one service", Toast.LENGTH_SHORT).show();
            return false;
        }
        // New image validation
        boolean hasAtLeastOneImage = false;
        for (ImageContainer container : imageContainers) {
            if (container.imageUri != null && !container.shouldDelete) {
                hasAtLeastOneImage = true;
                break;
            }
        }

        if (!hasAtLeastOneImage) {
            Toast.makeText(this, "Please upload at least one image", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }



    private void addBasicServiceInfo(JSONObject postData, SharedPreferences preferences) throws JSONException {
        postData.put("service_name", textServiceName.getText().toString().trim());
        postData.put("summary", textSummary.getText().toString().trim());
        postData.put("num_pets", pets.getText().toString().trim());
        postData.put("petsitter_id", preferences.getInt(KEY_SITTER_ID, -1));

        postData.put("where_spinner", whereSpinner.getSelectedItem().toString());
        postData.put("potty_spinner", pottySpinner.getSelectedItem().toString());
        postData.put("walks_spinner", walksSpinner.getSelectedItem().toString());
        postData.put("home_spinner", homeSpinner.getSelectedItem().toString());
        postData.put("transport_spinner", transportSpinner.getSelectedItem().toString());
    }

    private void addPetPreferences(JSONObject postData) throws JSONException {
        boolean catChecked = checkboxCat.isChecked();
        boolean dogChecked = checkboxDog.isChecked();
        boolean sizeSmallChecked = checkboxSmall.isChecked();
        boolean sizeMediumChecked = checkboxMedium.isChecked();
        boolean sizeLargeChecked = checkboxLarge.isChecked();
        boolean sizeGiantChecked = checkboxGiant.isChecked();

        postData.put("cat_checked", catChecked);
        postData.put("dog_checked", dogChecked);
        postData.put("size_small_checked", sizeSmallChecked);
        postData.put("size_medium_checked", sizeMediumChecked);
        postData.put("size_large_checked", sizeLargeChecked);
        postData.put("size_giant_checked", sizeGiantChecked);

        StringBuilder acceptPet = new StringBuilder();
        if (catChecked) acceptPet.append("cat,");
        if (dogChecked) acceptPet.append("dog,");
        if (acceptPet.length() > 0) acceptPet.setLength(acceptPet.length() - 1);
        postData.put("accept_pet", acceptPet.toString());

        StringBuilder acceptPetSize = new StringBuilder();
        if (sizeSmallChecked) acceptPetSize.append("small,");
        if (sizeMediumChecked) acceptPetSize.append("medium,");
        if (sizeLargeChecked) acceptPetSize.append("large,");
        if (sizeGiantChecked) acceptPetSize.append("giant,");
        if (acceptPetSize.length() > 0) acceptPetSize.setLength(acceptPetSize.length() - 1);
        postData.put("accept_petSize", acceptPetSize.toString());
    }

    private void addServiceOptions(JSONObject postData) throws JSONException {
        boolean bathChecked = checkboxBath.isChecked();
        boolean walkingChecked = checkboxWalking.isChecked();
        boolean feedingChecked = checkboxFeeding.isChecked();
        boolean playingChecked = checkboxPlaying.isChecked();

        postData.put("bath_checked", bathChecked);
        postData.put("walking_checked", walkingChecked);
        postData.put("feeding_checked", feedingChecked);
        postData.put("playing_checked", playingChecked);
    }

    private void handleError(String message, Exception e) {
        Log.e(TAG, message, e);
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            saveButton.setEnabled(true);
            saveButton.setText(currentServiceId == -1 ? "Save" : "Update");
        });
    }

    private class SaveServiceTask extends AsyncTask<Void, Void, String> {
        private final List<File> imageFiles;
        private final JSONObject serviceData;
        private final SharedPreferences preferences;

        public SaveServiceTask(List<File> imageFiles, JSONObject serviceData, SharedPreferences preferences) {
            this.imageFiles = imageFiles;
            this.serviceData = serviceData;
            this.preferences = preferences;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String boundary = "Boundary-" + System.currentTimeMillis();
                HttpURLConnection connection = createMultipartConnection(Constants.BASE_URL + "services.php", boundary);

                try (OutputStream outputStream = connection.getOutputStream();
                     PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {

                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"data\"").append("\r\n");
                    writer.append("Content-Type: application/json; charset=UTF-8").append("\r\n\r\n");

                    // Add position information to the JSON data
                    JSONArray positions = new JSONArray();
                    for (int i = 0; i < imageFiles.size(); i++) {
                        positions.put(i);
                    }
                    serviceData.put("image_positions", positions);

                    writer.append(serviceData.toString()).append("\r\n");

                    for (int i = 0; i < imageFiles.size(); i++) {
                        File imageFile = imageFiles.get(i);
                        if (imageFile.exists()) {
                            writer.append("--").append(boundary).append("\r\n");
                            writer.append("Content-Disposition: form-data; name=\"picture[]\"; filename=\"")
                                    .append(imageFile.getName()).append("\"\r\n");
                            writer.append("Content-Type: image/jpeg").append("\r\n\r\n");
                            writer.flush();

                            try (FileInputStream inputStream = new FileInputStream(imageFile)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }
                                outputStream.flush();
                            }
                            writer.append("\r\n");
                        }
                    }

                    writer.append("--").append(boundary).append("--").append("\r\n");
                    writer.flush();
                }

                return readResponse(connection);
            } catch (Exception e) {
                Log.e(TAG, "Error uploading service", e);
                return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            handleSaveResponse(result, preferences);
        }
    }

    private class UpdateServiceTask extends AsyncTask<Void, Void, String> {
        private final List<File> imageFiles;
        private final JSONObject serviceData;
        private final SharedPreferences preferences;

        public UpdateServiceTask(List<File> imageFiles, JSONObject serviceData, SharedPreferences preferences) {
            this.imageFiles = imageFiles;
            this.serviceData = serviceData;
            this.preferences = preferences;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                JSONArray imagesArray = new JSONArray();

                // Track which containers have images
                for (int i = 0; i < imageContainers.size(); i++) {
                    ImageContainer container = imageContainers.get(i);
                    if (container.imagePath != null) {
                        File imageFile = new File(container.imagePath);
                        if (imageFile.exists()) {
                            JSONObject imageObj = new JSONObject();
                            imageObj.put("image_base64", convertImageToBase64(imageFile));
                            imageObj.put("original_filename", imageFile.getName());
                            imageObj.put("position", i); // Add position information
                            imagesArray.put(imageObj);
                        }
                    }
                }

                serviceData.put("images", imagesArray);

                JSONArray deleteArray = new JSONArray();
                for (int i = 0; i < imageContainers.size(); i++) {
                    if (imageContainers.get(i).shouldDelete) {
                        deleteArray.put(i);
                    }
                }
                serviceData.put("delete_images", deleteArray);

                HttpURLConnection conn = createJsonConnection(Constants.BASE_URL + "updateservice.php");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(serviceData.toString().getBytes("UTF-8"));
                    os.flush();
                }

                return readResponse(conn);
            } catch (Exception e) {
                Log.e(TAG, "Error in UpdateServiceTask", e);
                return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
            }
        }


        private String convertImageToBase64(File imageFile) throws IOException {
            FileInputStream fileInputStream = new FileInputStream(imageFile);
            byte[] bytes = new byte[(int) imageFile.length()];
            fileInputStream.read(bytes);
            fileInputStream.close();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }

        @Override
        protected void onPostExecute(String result) {
            handleUpdateResponse(result, preferences);
        }
    }

    private class UpdateDataToServer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                JSONObject data = new JSONObject(params[0]);
                JSONObject transformedData = transformDataForUpdate(data);

                HttpURLConnection conn = createJsonConnection(Constants.BASE_URL + "updateservice.php");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(transformedData.toString().getBytes("UTF-8"));
                    os.flush();
                }

                return readResponse(conn);
            } catch (Exception e) {
                Log.e(TAG, "Error in UpdateDataToServer", e);
                return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
            }
        }

        private JSONObject transformDataForUpdate(JSONObject data) throws JSONException {
            JSONObject transformedData = new JSONObject();

            transformedData.put("petsitter_id", data.optInt("petsitter_id", -1));
            transformedData.put("service_name", data.optString("service_name", ""));
            transformedData.put("summ", data.optString("summary", ""));
            transformedData.put("numofpets", data.optString("num_pets", ""));
            transformedData.put("accept_pet", data.optString("accept_pet", ""));
            transformedData.put("accept_petsize", data.optString("accept_petSize", ""));
            transformedData.put("unsupervised", data.optString("where_spinner", ""));
            transformedData.put("potty", data.optString("potty_spinner", ""));
            transformedData.put("walks", data.optString("walks_spinner", ""));
            transformedData.put("home", data.optString("home_spinner", ""));
            transformedData.put("transport", data.optString("transport_spinner", ""));
            transformedData.put("price", data.optString("price", ""));
            transformedData.put("should_delete_image", data.optBoolean("should_delete_image", false));

            StringBuilder services = new StringBuilder();
            if (data.optBoolean("bath_checked", false)) services.append("bath,");
            if (data.optBoolean("walking_checked", false)) services.append("walking,");
            if (data.optBoolean("feeding_checked", false)) services.append("feeding,");
            if (data.optBoolean("playing_checked", false)) services.append("playing,");
            if (services.length() > 0) services.setLength(services.length() - 1);
            transformedData.put("service", services.toString());

            return transformedData;
        }

        @Override
        protected void onPostExecute(String result) {
            handleUpdateResponse(result, getSharedPreferences(PREFS_NAME, MODE_PRIVATE));
        }
    }

    private class FetchServiceDetailsTask extends AsyncTask<Void, Void, String> {
        private final int petsitterId;

        public FetchServiceDetailsTask(int petsitterId) {
            this.petsitterId = petsitterId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(Constants.BASE_URL + "get_service_details.php?petsitter_id=" + petsitterId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return readResponse(conn);
                }
            } catch (Exception e) {
                Log.e(TAG, "FetchServiceDetailsTask error", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(ListingActivity.this, "Failed to fetch service data", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonResponse = new JSONObject(result);
                if (jsonResponse.getString("status").equals("success")) {
                    JSONObject service = jsonResponse.getJSONObject("service");
                    populateServiceDetails(service);
                    saveButton.setText("Update");
                } else {
                    saveButton.setText("Save");
                    Toast.makeText(ListingActivity.this,
                            "No existing service found. You can create a new one.",
                            Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON parse error", e);
                Toast.makeText(ListingActivity.this, "Error parsing server response", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchServiceDetails(int petsitterId) {
        new FetchServiceDetailsTask(petsitterId).execute();
    }

    private void populateServiceDetails(JSONObject service) throws JSONException {
        currentServiceId = service.optInt("id", service.optInt("petsitter_id", -1));

        textServiceName.setText(service.optString("service_name", ""));
        textSummary.setText(service.optString("description", ""));
        pets.setText(service.optString("numofpets", ""));
        editPrice.setText(service.optString("price", ""));

        setCheckboxesFromServiceData(service);
        setSpinnersFromServiceData(service);
        handleServiceImages(service.optString("pictures", ""));
    }

    private void setCheckboxesFromServiceData(JSONObject service) {
        String acceptPet = service.optString("accept_pet", "").toLowerCase();
        checkboxCat.setChecked(acceptPet.contains("cat"));
        checkboxDog.setChecked(acceptPet.contains("dog"));

        String acceptPetSize = service.optString("accept_petsize", "").toLowerCase();
        checkboxSmall.setChecked(acceptPetSize.contains("small"));
        checkboxMedium.setChecked(acceptPetSize.contains("medium"));
        checkboxLarge.setChecked(acceptPetSize.contains("large"));
        checkboxGiant.setChecked(acceptPetSize.contains("giant"));

        String serviceString = service.optString("service", "").toLowerCase();
        checkboxBath.setChecked(serviceString.contains("bath"));
        checkboxWalking.setChecked(serviceString.contains("walking"));
        checkboxFeeding.setChecked(serviceString.contains("feeding"));
        checkboxPlaying.setChecked(serviceString.contains("playing"));
    }

    private void setSpinnersFromServiceData(JSONObject service) {
        setSpinnerSelection(whereSpinner, service.optString("where_spinner", ""));
        setSpinnerSelection(pottySpinner, service.optString("potty", ""));
        setSpinnerSelection(walksSpinner, service.optString("walks", ""));
        setSpinnerSelection(homeSpinner, service.optString("home", ""));
        setSpinnerSelection(transportSpinner, service.optString("transport", ""));
    }

    private void handleServiceImages(String picturesJson) {
        try {
            if (picturesJson == null || picturesJson.isEmpty() || picturesJson.equals("null")) {
                return;
            }

            JSONArray picturesArray = new JSONArray(picturesJson);
            for (int i = 0; i < Math.min(picturesArray.length(), imageContainers.size()); i++) {
                String pictureUrl = picturesArray.getString(i);
                if (pictureUrl != null && !pictureUrl.isEmpty()) {
                    if (!pictureUrl.startsWith("http")) {
                        if (!pictureUrl.contains("uploads/")) {
                            pictureUrl = Constants.BASE_URL + "uploads/" + pictureUrl;
                        } else {
                            pictureUrl = Constants.BASE_URL + pictureUrl;
                        }
                    }

                    ImageContainer container = imageContainers.get(i);
                    container.imageUri = Uri.parse(pictureUrl);
                    container.uploadButton.setText("Change");
                    container.imageView.setVisibility(View.VISIBLE);
                    container.deleteButton.setVisibility(View.VISIBLE);

                    Glide.with(ListingActivity.this)
                            .load(pictureUrl)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .into(container.imageView);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing pictures JSON", e);
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || value.isEmpty()) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private class SendDataToServer extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            saveButton.setEnabled(false);
            saveButton.setText("Saving...");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                HttpURLConnection conn = createJsonConnection(Constants.BASE_URL + "services.php");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(params[0].getBytes());
                    os.flush();
                }

                return readResponse(conn);
            } catch (Exception e) {
                return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            saveButton.setEnabled(true);
            saveButton.setText("Save");

            try {
                JSONObject jsonResponse = new JSONObject(result);
                if (jsonResponse.getString("status").equals("success")) {
                    Toast.makeText(ListingActivity.this, "Service saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ListingActivity.this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(ListingActivity.this, "Error parsing server response", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private HttpURLConnection createMultipartConnection(String urlString, String boundary) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String sessionToken = preferences.getString(KEY_SESSION_TOKEN, null);
        if (sessionToken != null) {
            connection.setRequestProperty("Cookie", "PHPSESSID=" + sessionToken);
        }

        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        return connection;
    }

    private HttpURLConnection createJsonConnection(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String sessionToken = preferences.getString(KEY_SESSION_TOKEN, null);
        if (sessionToken != null) {
            conn.setRequestProperty("Cookie", "PHPSESSID=" + sessionToken);
        }

        return conn;
    }

    private String readResponse(HttpURLConnection connection) throws Exception {
        BufferedReader reader;
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    private void handleSaveResponse(String result, SharedPreferences preferences) {
        Log.d(TAG, "Save response: " + result);
        saveButton.setEnabled(true);
        saveButton.setText("Save");

        try {
            JSONObject jsonResponse = new JSONObject(result);
            if ("success".equalsIgnoreCase(jsonResponse.optString("status"))) {
                int serviceId = jsonResponse.optInt("service_id", -1);
                if (serviceId != -1) {
                    currentServiceId = serviceId;
                    saveButton.setText("Update");
                }
                Toast.makeText(ListingActivity.this, "Service saved successfully!", Toast.LENGTH_SHORT).show();
                fetchServiceDetails(preferences.getInt(KEY_SITTER_ID, -1));
            } else {
                Toast.makeText(ListingActivity.this,
                        "Save Error: " + jsonResponse.optString("message"),
                        Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(ListingActivity.this,
                    "Invalid server response",
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "JSON parse error in save response", e);
        }
    }

    private void handleUpdateResponse(String result, SharedPreferences preferences) {
        Log.d(TAG, "Update response: " + result);
        saveButton.setEnabled(true);
        saveButton.setText("Update");

        try {
            JSONObject jsonResponse = new JSONObject(result);
            if (jsonResponse.getString("status").equals("success")) {
                Toast.makeText(ListingActivity.this, "Service updated successfully!", Toast.LENGTH_SHORT).show();
                fetchServiceDetails(preferences.getInt(KEY_SITTER_ID, -1));
            } else {
                String message = jsonResponse.optString("message", "Update failed");
                if (jsonResponse.has("debug")) {
                    message += "\nDebug: " + jsonResponse.optString("debug");
                }
                Toast.makeText(ListingActivity.this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(ListingActivity.this, "Error parsing server response", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "JSON parse error", e);
        }
    }
}