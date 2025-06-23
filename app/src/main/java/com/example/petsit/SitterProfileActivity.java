package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import android.widget.EditText;
import android.util.Log;
import android.app.DatePickerDialog;
import android.widget.DatePicker;

public class SitterProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_PHONE = "phone_number";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_BIRTH = "birth_date";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_LOCATION = "location";
    private static final String TAG = "SitterProfileActivity";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 2;

    private TextView fullNameTextView, emailTextView, firstNameTextView, lastNameTextView;
    private Spinner genderSpinner;
    private TextView birthDateTextView, editPhoneTextView, locationTextView;
    private ImageView profileImageView, editProfileImageView, calendarImageView;
    private Button updateButton;

    private RequestQueue requestQueue;
    private Uri imageUri;
    private double selectedLatitude;
    private double selectedLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sitter_profile);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAU_0owJ3SjdwRzBiHvQv4FizP42k9S2gA");
        }

        initializeViews();
        initializeVolley();
        setupGenderSpinner();
        setupLocationSelection();
        loadInitialData();
        setupBackButton();
        setupUpdateButton();
        setupEditProfileImage();
        setupCalendarClick();
    }

    private void initializeViews() {
        fullNameTextView = findViewById(R.id.text_full_name);
        emailTextView = findViewById(R.id.text_email);
        firstNameTextView = findViewById(R.id.text_first_name);
        lastNameTextView = findViewById(R.id.text_last_name);
        genderSpinner = findViewById(R.id.gender_spinner);
        birthDateTextView = findViewById(R.id.text_date_of_birth);
        profileImageView = findViewById(R.id.container_group3);
        updateButton = findViewById(R.id.container_button);
        editProfileImageView = findViewById(R.id.image_pencil_simple);
        calendarImageView = findViewById(R.id.image_calendar);
        editPhoneTextView = findViewById(R.id.edit_phone);
        locationTextView = findViewById(R.id.text_location);
    }

    private void initializeVolley() {
        requestQueue = Volley.newRequestQueue(this);
    }

    private void setupGenderSpinner() {
        String[] genderOptions = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
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
                    .build(SitterProfileActivity.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });
    }

    private void loadInitialData() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = preferences.getString(KEY_EMAIL, "");
        String profileImage = preferences.getString(KEY_PROFILE_IMAGE, "");
        String location = preferences.getString(KEY_LOCATION, "");

        if (!profileImage.isEmpty()) loadProfileImage(profileImage);
        if (!email.isEmpty()) fetchUserDetails(email);
        if (!location.isEmpty()) locationTextView.setText(location);
    }

    private void setupBackButton() {
        findViewById(R.id.image_arrow_left).setOnClickListener(view -> {
            finish();
        });
    }

    private void fetchUserDetails(String email) {
        String url = Constants.BASE_URL + "get_sitter_details.php";

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
        String phone = userData.getString("Phone_Number");
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
        editPhoneTextView.setText(phone);
        locationTextView.setText(location);

        selectedLatitude = lat;
        selectedLongitude = lng;

        setGenderInSpinner(gender);
        loadProfileImage(profileImage);
    }

    private void saveUserData(JSONObject userData) throws JSONException {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_FIRST_NAME, userData.getString("FirstName"));
        editor.putString(KEY_LAST_NAME, userData.getString("LastName"));
        editor.putString(KEY_GENDER, userData.getString("Gender"));
        editor.putString(KEY_EMAIL, userData.getString("Email"));
        editor.putString(KEY_BIRTH, userData.getString("Birth_Date"));
        editor.putString(KEY_PHONE, userData.getString("Phone_Number"));
        editor.putString(KEY_PROFILE_IMAGE, userData.optString("ProfileImage", ""));
        editor.putString(KEY_LOCATION, userData.optString("Location", ""));
        editor.putFloat(KEY_LAT, (float) userData.optDouble("lat", 0));
        editor.putFloat(KEY_LNG, (float) userData.optDouble("lng", 0));
        editor.apply();
    }

    private void setGenderInSpinner(String gender) {
        if (gender == null) return;
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
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    SitterProfileActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String selectedDate = year + "/" + (month + 1) + "/" + dayOfMonth;
                        birthDateTextView.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
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
        } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
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
            String updatedPhoneNumber = editPhoneTextView.getText().toString();
            String updatedLocation = locationTextView.getText().toString();

            if (updatedFirstName.isEmpty() || updatedLastName.isEmpty() ||
                    updatedEmail.isEmpty() || updatedBirthDate.isEmpty() || updatedLocation.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                return;
            }
            // Phone number validation
            String cleanedPhone = updatedPhoneNumber.replaceAll("[^0-9]", ""); // Remove all non-digit characters
            if (cleanedPhone.length() < 10 || cleanedPhone.length() > 12) {
                Toast.makeText(this, "Phone number must be 10-12 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject updatedProfileData = new JSONObject();
            try {
                updatedProfileData.put("FirstName", updatedFirstName);
                updatedProfileData.put("LastName", updatedLastName);
                updatedProfileData.put("Email", updatedEmail);
                updatedProfileData.put("Gender", updatedGender);
                updatedProfileData.put("Birth_Date", updatedBirthDate);
                updatedProfileData.put("Phone_Number", updatedPhoneNumber);
                updatedProfileData.put("Location", updatedLocation);
                updatedProfileData.put("lat", selectedLatitude);
                updatedProfileData.put("lng", selectedLongitude);

                if (imageUri != null) {
                    updatedProfileData.put("ProfileImage", getImageBase64(imageUri));
                    updatedProfileData.put("OriginalFilename", getFileName(imageUri));
                } else {
                    updatedProfileData.put("ProfileImage", "");
                    updatedProfileData.put("OriginalFilename", "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            updateProfile(updatedProfileData);
        });
    }

    private String getImageBase64(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return "data:image/jpeg;base64," + Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void updateProfile(JSONObject data) {
        String url = Constants.BASE_URL + "updatesitterprofile.php";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

                            editor.putString(KEY_FIRST_NAME, data.getString("FirstName"));
                            editor.putString(KEY_LAST_NAME, data.getString("LastName"));
                            editor.putString(KEY_EMAIL, data.getString("Email"));
                            editor.putString(KEY_GENDER, data.getString("Gender"));
                            editor.putString(KEY_BIRTH, data.getString("Birth_Date"));
                            editor.putString(KEY_PHONE, data.getString("Phone_Number"));
                            if (data.has("Location")) {
                                editor.putString(KEY_LOCATION, data.getString("Location"));
                                editor.putFloat(KEY_LAT, (float) data.getDouble("lat"));
                                editor.putFloat(KEY_LNG, (float) data.getDouble("lng"));
                            }

                            editor.apply();
                            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "JSON error!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error!", Toast.LENGTH_SHORT).show());
        requestQueue.add(request);
    }
}