package com.example.petsit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.AdapterView;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.Place.Field;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SignUp extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 2;
    private EditText firstName, lastName, email, password, birthDate, phoneNumber, location,accountNumber;
    private Spinner genderSpinner,bankSpinner;
    private RadioGroup roleRadioGroup;
    private Button btnSignUp, btnUploadPhoto;
    private ImageView calendarIcon, profileImagePreview;
    private String selectedRole;
    private LinearLayout bankDetailsLayout,accbox;
    private Uri selectedImageUri;
    private String imagePath = "";
    private Double selectedLat = null;
    private Double selectedLng = null;
    private final String[] malaysianBanks = {
            "Select bank",
            "Maybank",
            "CIMB Bank",
            "Public Bank",
            "RHB Bank",
            "Hong Leong Bank",
            "AmBank",
            "Bank Rakyat",
            "Bank Islam",
            "Affin Bank",
            "Alliance Bank",
            "BSN (Bank Simpanan Nasional)",
            "HSBC Bank Malaysia",
            "OCBC Bank Malaysia",
            "Standard Chartered Bank Malaysia",
            "UOB Malaysia",
            "Bank Muamalat",
            "Kuwait Finance House",
            "Al-Rajhi Bank",
            "MBSB Bank",
            "Agro Bank"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        TextView login = findViewById(R.id.text_login);
        ImageView back = findViewById(R.id.img_arrow_left);

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAU_0owJ3SjdwRzBiHvQv4FizP42k9S2gA");
        }

        login.setOnClickListener(view -> {
            Intent intent = new Intent(SignUp.this, RoleActivity.class);
            startActivity(intent);
        });



        back.setOnClickListener(view -> {
            finish(); // Go back to the previous activity
        });


        initializeViews();
        setupGenderSpinner();
        setupBankSpinner();
        setupListeners();
    }

    private void initializeViews() {
        firstName = findViewById(R.id.FirstName);
        lastName = findViewById(R.id.LastName);
        email = findViewById(R.id.Email);
        password = findViewById(R.id.Pass);
        birthDate = findViewById(R.id.birth);
        phoneNumber = findViewById(R.id.phone);
        location = findViewById(R.id.location);
        genderSpinner = findViewById(R.id.gender);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        btnSignUp = findViewById(R.id.btnSignUp);
        calendarIcon = findViewById(R.id.calendarIcon);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        profileImagePreview = findViewById(R.id.profileImagePreview);
        accountNumber = findViewById(R.id.accountNumber);
        bankSpinner = findViewById(R.id.bankSpinner);
        bankDetailsLayout = findViewById(R.id.bankDetailsLayout);
        accbox =findViewById(R.id.accbox);
    }

    private void setupBankSpinner() {
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                malaysianBanks
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position > 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bankSpinner.setAdapter(bankAdapter);
        bankSpinner.setSelection(0, false);
    }
    private void setupGenderSpinner() {
        List<String> genders = new ArrayList<>();
        genders.add("Select gender");
        genders.add("Male");
        genders.add("Female");

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                genders
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position > 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);
        genderSpinner.setSelection(0, false);
    }

    private void setupListeners() {
        calendarIcon.setOnClickListener(v -> showDatePickerDialog());

        roleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioOwner) {
                selectedRole = "petowner";  // Make sure this matches your backend expectation
                // Disable bank details for pet owners
                accbox.setVisibility(View.GONE);
                bankDetailsLayout.setVisibility(View.GONE);
                accountNumber.setVisibility(View.GONE);
                bankSpinner.setSelection(0); // Reset selection
            } else if (checkedId == R.id.radioSitter) {
                selectedRole = "petsitter";  // Make sure this matches your backend expectation
                // Enable bank details for pet sitters
                accbox.setVisibility(View.VISIBLE);
                bankDetailsLayout.setVisibility(View.VISIBLE);
                accountNumber.setVisibility(View.VISIBLE);
            }
        });

        // Initialize with default selection
        roleRadioGroup.check(R.id.radioOwner); // Auto-select pet owner by default
        selectedRole = "petowner"; // Initialize the variable

        btnUploadPhoto.setOnClickListener(v -> openImageChooser());
        btnSignUp.setOnClickListener(v -> attemptRegistration());

        location.setOnClickListener(v -> openAutocompleteActivity());
    }

    private void openAutocompleteActivity() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG);

        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                location.setText(place.getAddress());

                // Get latitude and longitude from the selected place
                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    selectedLat = latLng.latitude;
                    selectedLng = latLng.longitude;
                    Log.d("Location", "Lat: " + selectedLat + ", Lng: " + selectedLng);
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                com.google.android.gms.common.api.Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profileImagePreview.setImageURI(selectedImageUri);
            imagePath = getPathFromUri(selectedImageUri);
        }
    }

    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }

    private void attemptRegistration() {
        String firstNameText = firstName.getText().toString().trim();
        String lastNameText = lastName.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String birthDateText = birthDate.getText().toString().trim();
        String phoneNumberText = phoneNumber.getText().toString().trim();
        String locationText = location.getText().toString().trim();
        String accountNumberText = accountNumber.getText().toString().trim();

        if (genderSpinner.getSelectedItemPosition() <= 0) {
            showToast("Please select your gender");
            return;
        }

        String genderText = genderSpinner.getSelectedItem().toString();

        if (selectedLat == null || selectedLng == null) {
            showToast("Please select a valid location from the suggestions");
            return;
        }

        // Only validate bank details for pet sitters
        String bankText = "";
        if (selectedRole.equals("petsitter")) {
            if (bankSpinner.getSelectedItemPosition() <= 0) {
                showToast("Please select your bank");
                return;
            }
            if (accountNumberText.isEmpty()) {
                showToast("Please enter your account number");
                return;
            }
            bankText = bankSpinner.getSelectedItem().toString();
        }

        if (validateInputs(firstNameText, lastNameText, emailText, passwordText,
                birthDateText, phoneNumberText, locationText)) {
            if (selectedImageUri != null) {
                uploadImageAndRegister(firstNameText, lastNameText, emailText, passwordText,
                        birthDateText, phoneNumberText, locationText, genderText, bankText,
                        accountNumberText, selectedRole, selectedLat, selectedLng);
            } else {
                sendDataToServer(firstNameText, lastNameText, emailText, passwordText,
                        birthDateText, phoneNumberText, locationText, genderText, bankText,
                        accountNumberText, selectedRole, "", selectedLat, selectedLng);
            }
        }
    }

    private void uploadImageAndRegister(String firstName, String lastName, String email,
                                        String password, String birthDate, String phoneNumber,
                                        String location, String gender, String bank,
                                        String accountNumber, String role, Double lat, Double lng) {
        showToast("Uploading image...");


        new Thread(() -> {
            try {
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";

                URL url = new URL(Constants.BASE_URL + "upload.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", imagePath);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                // Add form data with coordinates
                addFormField(dos, boundary, "FirstName", firstName);
                addFormField(dos, boundary, "LastName", lastName);
                addFormField(dos, boundary, "Email", email);
                addFormField(dos, boundary, "password", password);
                addFormField(dos, boundary, "birth_date", birthDate);
                addFormField(dos, boundary, "Phone_Number", phoneNumber);
                addFormField(dos, boundary, "Location", location);
                addFormField(dos, boundary, "Gender", gender);
                addFormField(dos, boundary, "Role", role);
                addFormField(dos, boundary, "lat", lat.toString());
                addFormField(dos, boundary, "lng", lng.toString());

                // Add image file
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"profile_image\";filename=\"" + imagePath + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                FileInputStream fileInputStream = new FileInputStream(new File(imagePath));
                int bytesAvailable = fileInputStream.available();
                int maxBufferSize = 1024 * 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[] buffer = new byte[bufferSize];

                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                fileInputStream.close();
                dos.flush();
                dos.close();

                // Get response
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");
                    String imageUrl = jsonResponse.optString("image_path", "");

                    runOnUiThread(() -> {
                        if (success) {
                            sendDataToServer(firstName, lastName, email, password,
                                    birthDate, phoneNumber, location, gender, bank,
                                    accountNumber, role, imageUrl, lat, lng);
                        } else {
                            showToast("Image upload failed: " + message);
                        }
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> showToast("Image upload error: " + e.getMessage()));
            }
        }).start();
    }

    private void addFormField(DataOutputStream dos, String boundary, String fieldName, String fieldValue) throws Exception {
        dos.writeBytes("--" + boundary + "\r\n");
        dos.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n");
        dos.writeBytes(fieldValue + "\r\n");
    }

    private void sendDataToServer(String firstName, String lastName, String email,
                                  String password, String birthDate, String phoneNumber,
                                  String location, String gender, String bank,
                                  String accountNumber, String role, String imagePath,
                                  Double lat, Double lng) {
        showToast("Registering...");

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Constants.BASE_URL + "register.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                String postData = "FirstName=" + URLEncoder.encode(firstName, "UTF-8") +
                        "&LastName=" + URLEncoder.encode(lastName, "UTF-8") +
                        "&Email=" + URLEncoder.encode(email, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8") +
                        "&birth_date=" + URLEncoder.encode(birthDate, "UTF-8") +
                        "&Phone_Number=" + URLEncoder.encode(phoneNumber, "UTF-8") +
                        "&Bank=" + URLEncoder.encode(bank, "UTF-8") +
                        "&AccountNumber=" + URLEncoder.encode(accountNumber, "UTF-8") +
                        "&Location=" + URLEncoder.encode(location, "UTF-8") +
                        "&Gender=" + URLEncoder.encode(gender, "UTF-8") +
                        "&Role=" + URLEncoder.encode(role, "UTF-8") +
                        "&ProfileImage=" + URLEncoder.encode(imagePath, "UTF-8") +
                        "&lat=" + lat +
                        "&lng=" + lng;

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = postData.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                boolean success = jsonResponse.getBoolean("success");
                String message = jsonResponse.getString("message");

                runOnUiThread(() -> {
                    if (success) {
                        showToast("Registration successful!");
                        startActivity(new Intent(SignUp.this, RoleActivity.class));
                        finish();
                    } else {
                        showToast("Registration failed: " + message);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> showToast("Error: " + e.getMessage()));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                SignUp.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%04d-%02d-%02d",
                            selectedYear, (selectedMonth + 1), selectedDay);
                    birthDate.setText(formattedDate);
                },
                year, month, day
        );

        // ✅ Disable future dates
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }

    private boolean validateInputs(String firstName, String lastName, String email,
                                   String password, String birthDate, String phoneNumber,
                                   String location) {
        if (firstName.isEmpty()) {
            showToast("First name is required");
            return false;
        }
        if (lastName.isEmpty()) {
            showToast("Last name is required");
            return false;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Valid email is required");
            return false;
        }
        if (!isPasswordValid(password)) {
            showToast("Password must be 8+ characters with uppercase, lowercase, and a number");
            return false;
        }
        if (birthDate.isEmpty()) {
            showToast("Birth date is required");
            return false;
        }
        if (phoneNumber.isEmpty()) {
            showToast("Phone number is required");
            return false;
        }
        if (!phoneNumber.matches("^\\d{10,12}$")) {
            showToast("Phone number must be 10 to 12 digits");
            return false;
        }
        if (!phoneNumber.matches("^[0-9]+$")) {
            showToast("Phone number must contain only numbers");
            return false;
        }

        if (location.isEmpty()) {
            showToast("Location is required");
            return false;
        }
        if (location.isEmpty()) {
            showToast("Location is required");
            return false;
        }
        if (selectedRole == null || selectedRole.isEmpty()) {
            showToast("Please select your role");
            return false;
        }
        return true;
    }
    private boolean isPasswordValid(String password) {
        // At least 8 characters
        if (password.length() < 8) {
            return false;
        }

        // At least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // At least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // At least one number
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        return true;
    }
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(SignUp.this, message, Toast.LENGTH_LONG).show());
    }
}