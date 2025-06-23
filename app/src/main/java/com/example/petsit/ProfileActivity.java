package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import android.util.Log;
import android.database.Cursor;
import android.provider.OpenableColumns;
import java.util.HashMap;
import java.util.Map;

import android.app.DatePickerDialog;
import android.widget.DatePicker;

public class ProfileActivity extends AppCompatActivity {

    // SharedPreferences keys
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_BIRTH = "birth_date";
    private static final String KEY_PHONE = "phone_number";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";

    // UI elements
    private TextView fullNameTextView;
    private TextView emailTextView;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private Spinner genderSpinner;
    private TextView birthDateTextView;
    private TextView phoneNumberTextView;
    private TextView locationTextView;
    private ImageView profileImageView;
    private Button updateButton;
    private ImageView editProfileImageView;
    private ImageView calendarImageView;
    private RequestQueue requestQueue;

    // Constants
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 100;
    private static final String TAG = "ProfileActivity";

    // Variables
    private Uri imageUri;
    private double selectedLatitude;
    private double selectedLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAU_0owJ3SjdwRzBiHvQv4FizP42k9S2gA");
        }

        // Initialize all components
        initializeViews();
        initializeVolley();
        setupGenderSpinner();
        loadInitialData();
        setupBackButton();
        setupUpdateButton();
        setupEditProfileImage();
        setupCalendarClick();
        setupLocationSelection();
    }

    private void initializeViews() {
        fullNameTextView = findViewById(R.id.text_full_name);
        emailTextView = findViewById(R.id.text_email);
        firstNameTextView = findViewById(R.id.text_first_name);
        lastNameTextView = findViewById(R.id.text_last_name);
        genderSpinner = findViewById(R.id.gender_spinner);
        birthDateTextView = findViewById(R.id.text_date_of_birth);
        phoneNumberTextView = findViewById(R.id.text_phone);
        locationTextView = findViewById(R.id.text_location);
        profileImageView = findViewById(R.id.container_group3);
        updateButton = findViewById(R.id.container_button);
        editProfileImageView = findViewById(R.id.image_pencil_simple);
        calendarImageView = findViewById(R.id.image_calendar);
    }

    private void initializeVolley() {
        requestQueue = Volley.newRequestQueue(this);
    }

    private void setupGenderSpinner() {
        String[] genderOptions = new String[] {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
    }

    private void loadInitialData() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = preferences.getString(KEY_EMAIL, "");
        String profileImage = preferences.getString(KEY_PROFILE_IMAGE, "");

        if (!profileImage.isEmpty()) {
            loadProfileImage(profileImage);
        }

        if (!email.isEmpty()) {
            fetchUserDetails(email);
        }
    }

    private void setupBackButton() {
        ImageView back = findViewById(R.id.image_arrow_left);
        back.setOnClickListener(view -> {
            finish();
        });
    }

    private void fetchUserDetails(String email) {
        String url = Constants.BASE_URL + "get_user_details.php";

        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONObject userData = response.getJSONObject("data");
                            updateUIWithUserData(userData);
                            saveUserData(userData);
                        }
                    } catch (JSONException e) {
                        Log.e("ProfileError", "JSON parsing error", e);
                    }
                },
                error -> Log.e("ProfileError", "Network error", error));

        requestQueue.add(request);
    }

    private void updateUIWithUserData(JSONObject userData) throws JSONException {
        String firstName = userData.getString("FirstName");
        String lastName = userData.getString("LastName");
        String gender = userData.getString("Gender");
        String email = userData.getString("Email");
        String birthDate = userData.getString("Birth_Date");
        String phoneNumber = userData.getString("Phone_Number");
        String location = userData.optString("Location", "");
        double lat = userData.optDouble("lat", 0);
        double lng = userData.optDouble("lng", 0);

        String profileImage = userData.has("ProfileImageUrl")
                ? userData.getString("ProfileImageUrl")
                : userData.optString("ProfileImage", "");

        fullNameTextView.setText(firstName + " " + lastName);
        firstNameTextView.setText(firstName);
        lastNameTextView.setText(lastName);
        emailTextView.setText(email);
        birthDateTextView.setText(birthDate);
        phoneNumberTextView.setText(phoneNumber);
        locationTextView.setText(location);

        selectedLatitude = lat;
        selectedLongitude = lng;

        setGenderInSpinner(gender);
        loadProfileImage(profileImage);
    }

    private void saveUserData(JSONObject userData) throws JSONException {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(KEY_FIRST_NAME, userData.getString("FirstName"));
        editor.putString(KEY_LAST_NAME, userData.getString("LastName"));
        editor.putString(KEY_GENDER, userData.getString("Gender"));
        editor.putString(KEY_EMAIL, userData.getString("Email"));
        editor.putString(KEY_BIRTH, userData.getString("Birth_Date"));
        editor.putString(KEY_PHONE, userData.getString("Phone_Number"));
        editor.putString(KEY_LOCATION, userData.optString("Location", ""));
        editor.putFloat(KEY_LAT, (float) userData.optDouble("lat", 0));
        editor.putFloat(KEY_LNG, (float) userData.optDouble("lng", 0));
        editor.putString(KEY_PROFILE_IMAGE, userData.optString("ProfileImage", ""));

        editor.apply();
    }

    private void setGenderInSpinner(String gender) {
        if (gender == null || gender.isEmpty()) return;

        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) genderSpinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(gender)) {
                genderSpinner.setSelection(i);
                break;
            }
        }
    }

    private void loadProfileImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            profileImageView.setImageResource(R.drawable.default_profile);
            return;
        }

        Glide.with(this)
                .load(imagePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(profileImageView);
    }

    private void setupEditProfileImage() {
        editProfileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    private void setupCalendarClick() {
        calendarImageView.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(ProfileActivity.this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        String selectedDate = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDayOfMonth;
                        birthDateTextView.setText(selectedDate);
                    }, year, month, dayOfMonth);

            // ✅ Set max date to today to disable future dates
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

            datePickerDialog.show();
        });
    }


    private void setupLocationSelection() {
        locationTextView.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
            );

            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(ProfileActivity.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                locationTextView.setText(place.getAddress());
                if (place.getLatLng() != null) {
                    selectedLatitude = place.getLatLng().latitude;
                    selectedLongitude = place.getLatLng().longitude;
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e(TAG, "Error getting place: " + status.getStatusMessage());
            }
        }
    }

    private void setupUpdateButton() {
        updateButton.setOnClickListener(v -> {
            String updatedFirstName = firstNameTextView.getText().toString();
            String updatedLastName = lastNameTextView.getText().toString();
            String updatedEmail = emailTextView.getText().toString();
            String updatedGender = genderSpinner.getSelectedItem().toString();
            String updatedBirthDate = birthDateTextView.getText().toString();
            String updatedPhoneNumber = phoneNumberTextView.getText().toString();
            String updatedLocation = locationTextView.getText().toString();

            if (updatedFirstName.isEmpty() || updatedLastName.isEmpty() || updatedPhoneNumber.isEmpty() ||
                    updatedEmail.isEmpty() || updatedBirthDate.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject updatedProfileData = new JSONObject();
                updatedProfileData.put("FirstName", updatedFirstName);
                updatedProfileData.put("LastName", updatedLastName);
                updatedProfileData.put("Email", updatedEmail);
                updatedProfileData.put("Gender", updatedGender);
                updatedProfileData.put("Birth_Date", updatedBirthDate);
                updatedProfileData.put("Phone_Number", updatedPhoneNumber);

                if (!updatedLocation.isEmpty()) {
                    updatedProfileData.put("Location", updatedLocation);
                    updatedProfileData.put("lat", selectedLatitude);
                    updatedProfileData.put("lng", selectedLongitude);
                }

                if (imageUri != null) {
                    String base64Image = getImageBase64(imageUri);
                    String fileName = getFileName(imageUri);

                    Log.d("ImageUpload", "Filename: " + fileName);
                    Log.d("ImageUpload", "Image data length: " + base64Image.length());

                    updatedProfileData.put("ProfileImage", base64Image);
                    updatedProfileData.put("OriginalFilename", fileName);
                } else {
                    updatedProfileData.put("ProfileImage", "");
                    updatedProfileData.put("OriginalFilename", "");
                }

                Log.d("APIRequest", "Sending: " + updatedProfileData.toString());
                updateProfile(updatedProfileData);
            } catch (JSONException e) {
                Log.e("APIError", "JSON creation failed", e);
                Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getImageBase64(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void updateProfile(JSONObject updatedProfileData) {
        String url = Constants.BASE_URL + "updateprofile.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                updatedProfileData,
                response -> {
                    try {
                        Log.d("APIResponse", "Received: " + response.toString());

                        if (response.getString("status").equals("success")) {
                            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();

                            editor.putString(KEY_FIRST_NAME, updatedProfileData.getString("FirstName"));
                            editor.putString(KEY_LAST_NAME, updatedProfileData.getString("LastName"));
                            editor.putString(KEY_EMAIL, updatedProfileData.getString("Email"));
                            editor.putString(KEY_GENDER, updatedProfileData.getString("Gender"));
                            editor.putString(KEY_BIRTH, updatedProfileData.getString("Birth_Date"));
                            editor.putString(KEY_PHONE, updatedProfileData.getString("Phone_Number"));

                            if (updatedProfileData.has("Location")) {
                                editor.putString(KEY_LOCATION, updatedProfileData.getString("Location"));
                                editor.putFloat(KEY_LAT, (float) updatedProfileData.getDouble("lat"));
                                editor.putFloat(KEY_LNG, (float) updatedProfileData.getDouble("lng"));
                            }

                            if (response.has("imageUrl")) {
                                editor.putString(KEY_PROFILE_IMAGE, response.getString("imageUrl"));
                            }

                            editor.apply();
                            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = response.optString("message", "Update failed");
                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("APIError", "Response parsing failed", e);
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("APIError", "Request failed", error);
                    if (error.networkResponse != null) {
                        Log.e("APIError", "Status code: " + error.networkResponse.statusCode);
                        Log.e("APIError", "Response data: " + new String(error.networkResponse.data));
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
}