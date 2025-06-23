package com.example.petsit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import android.content.SharedPreferences;

public class AddPetActivity extends AppCompatActivity {

    // UI Components
    private EditText petNameEditText, birthDateEditText;
    private Spinner petTypeSpinner, breedSpinner, sizeSpinner, genderSpinner;
    private TextView typeDisplayText, breedDisplayText, sizeDisplayText, genderDisplayText;
    private ImageView calendarImage, petPhotoImageView, pencilImage;
    private Button addButton;

    // Selection Variables
    private String selectedPetType = "";
    private String selectedBreed = "";
    private String selectedSize = "";
    private String selectedGender = "";
    private String selectedBirthDate = "";
    private String selectedImagePath = "";
    private Bitmap selectedImageBitmap;

    // Constants
    private static final String

            PHP_API_URL = Constants.BASE_URL + "addpet.php";;
    private static final String DOG_BREEDS_API = "https://dog.ceo/api/breeds/list/all";
    private static final String CAT_BREEDS_API = "https://api.thecatapi.com/v1/breeds";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "AddPetActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addpet);



        ImageView imageVector = findViewById(R.id.image_arrow_left);

        imageVector.setOnClickListener(view -> {
            finish();
        });
        // Check for passed owner ID
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("petOwner_ID")) {
            int ownerId = intent.getIntExtra("petOwner_ID", -1);
            if (ownerId == -1) {
                Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "User session required", Toast.LENGTH_SHORT).show();
            finish();
        }
        initializeViews();
        setupSpinners();
        setupClickListeners();
    }

    private void initializeViews() {
        petNameEditText = findViewById(R.id.text_pet_name);
        birthDateEditText = findViewById(R.id.text_pet_born);
        calendarImage = findViewById(R.id.image_calendar);
        petPhotoImageView = findViewById(R.id.pet_image);
        pencilImage = findViewById(R.id.image_pencil_simple);
        petTypeSpinner = findViewById(R.id.type_spinner);
        breedSpinner = findViewById(R.id.breed_spinner);
        sizeSpinner = findViewById(R.id.size_spinner);
        genderSpinner = findViewById(R.id.gender_spinner);
        typeDisplayText = findViewById(R.id.text_pet_type);
        breedDisplayText = findViewById(R.id.text_pet_breed);
        sizeDisplayText = findViewById(R.id.text_pet_size);
        genderDisplayText = findViewById(R.id.text_pet_gender);
        addButton = findViewById(R.id.add_pet_button);
    }

    private void setupSpinners() {
        setupPetTypeSpinner();
        setupBreedSpinner();
        setupSizeSpinner();
        setupGenderSpinner();
    }

    private void setupClickListeners() {
        calendarImage.setOnClickListener(v -> showDatePickerDialog());
        pencilImage.setOnClickListener(v -> openGallery());

        addButton.setOnClickListener(v -> {
            if (validateForm()) {
                new AddPetTask().execute(
                        petNameEditText.getText().toString().trim(),
                        selectedPetType,
                        selectedBreed,
                        selectedSize,
                        selectedGender
                );
            }
        });
    }

    private boolean validateForm() {
        String petName = petNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(petName)) {
            petNameEditText.setError("Pet name is required");
            return false;
        }
        if (selectedPetType.isEmpty()) {
            Toast.makeText(this, "Please select pet type", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedBreed.isEmpty()) {
            Toast.makeText(this, "Please select pet breed", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedSize.isEmpty()) {
            Toast.makeText(this, "Please select pet size", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedGender.isEmpty()) {
            Toast.makeText(this, "Please select pet gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedBirthDate.isEmpty()) {
            Toast.makeText(this, "Please select birth date", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                selectedImageBitmap = BitmapFactory.decodeStream(inputStream);
                petPhotoImageView.setImageBitmap(selectedImageBitmap);
                selectedImagePath = selectedImageUri.toString();
            } catch (Exception e) {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading image", e);
            }
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedBirthDate = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    birthDateEditText.setText(selectedBirthDate);
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void setupGenderSpinner() {
        try {
            genderDisplayText.setText("Select gender");
            genderDisplayText.setTextColor(getResources().getColor(android.R.color.darker_gray));

            String[] genders = {"Male", "Female"};

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    genders
            ) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (position == genderSpinner.getSelectedItemPosition()) {
                        ((TextView) view).setText("Select gender");
                    }
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            genderSpinner.setAdapter(adapter);
            genderSpinner.setVisibility(View.INVISIBLE);

            genderDisplayText.setOnClickListener(v -> genderSpinner.performClick());

            genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedGender = parent.getItemAtPosition(position).toString();
                    genderDisplayText.setText(selectedGender);
                    genderDisplayText.setTextColor(getResources().getColor(android.R.color.black));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedGender = "";
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Gender spinner setup failed", e);
            Toast.makeText(this, "Error setting up gender selection", Toast.LENGTH_LONG).show();
        }
    }

    private void setupSizeSpinner() {
        try {
            sizeDisplayText.setText("Select size");
            sizeDisplayText.setTextColor(getResources().getColor(android.R.color.darker_gray));

            String[] sizes = {"Small (1-5kg)", "Medium (5-10kg)", "Large (10-20kg)", "Extra Large (20kg+)"};

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    sizes
            ) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (position == sizeSpinner.getSelectedItemPosition()) {
                        ((TextView) view).setText("Select size");
                    }
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sizeSpinner.setAdapter(adapter);
            sizeSpinner.setVisibility(View.INVISIBLE);

            sizeDisplayText.setOnClickListener(v -> sizeSpinner.performClick());

            sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedSize = parent.getItemAtPosition(position).toString();
                    sizeDisplayText.setText(selectedSize);
                    sizeDisplayText.setTextColor(getResources().getColor(android.R.color.black));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedSize = "";
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Size spinner setup failed", e);
            Toast.makeText(this, "Error setting up size selection", Toast.LENGTH_LONG).show();
        }
    }

    private void setupPetTypeSpinner() {
        try {
            typeDisplayText.setText("Select pet type");
            typeDisplayText.setTextColor(getResources().getColor(android.R.color.darker_gray));

            String[] petTypes = getResources().getStringArray(R.array.pet_types);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    petTypes
            ) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (position == petTypeSpinner.getSelectedItemPosition()) {
                        ((TextView) view).setText("Select pet type");
                    }
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            petTypeSpinner.setAdapter(adapter);
            petTypeSpinner.setVisibility(View.INVISIBLE);

            typeDisplayText.setOnClickListener(v -> petTypeSpinner.performClick());

            petTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedPetType = parent.getItemAtPosition(position).toString();
                    typeDisplayText.setText(selectedPetType);
                    typeDisplayText.setTextColor(getResources().getColor(android.R.color.black));

                    if (selectedPetType.equals("Dog")) {
                        new FetchBreedsTask().execute(DOG_BREEDS_API);
                    } else if (selectedPetType.equals("Cat")) {
                        new FetchBreedsTask().execute(CAT_BREEDS_API);
                    } else {
                        List<String> breeds = new ArrayList<>();
                        breeds.add("Other");
                        updateBreedsAdapter(breeds);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedPetType = "";
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Pet type spinner setup failed", e);
            Toast.makeText(this, "Error setting up pet types", Toast.LENGTH_LONG).show();
        }
    }

    private void setupBreedSpinner() {
        try {
            breedDisplayText.setText("Select breed");
            breedDisplayText.setTextColor(getResources().getColor(android.R.color.darker_gray));

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    new ArrayList<>()
            ) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (position == breedSpinner.getSelectedItemPosition()) {
                        ((TextView) view).setText("Select breed");
                    }
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            breedSpinner.setAdapter(adapter);
            breedSpinner.setVisibility(View.INVISIBLE);

            breedDisplayText.setOnClickListener(v -> {
                if (selectedPetType.isEmpty()) {
                    Toast.makeText(AddPetActivity.this, "Please select pet type first", Toast.LENGTH_SHORT).show();
                    return;
                }
                breedSpinner.performClick();
            });

            breedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedBreed = parent.getItemAtPosition(position).toString();
                    breedDisplayText.setText(selectedBreed);
                    breedDisplayText.setTextColor(getResources().getColor(android.R.color.black));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedBreed = "";
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Breed spinner setup failed", e);
            Toast.makeText(this, "Error setting up breed selection", Toast.LENGTH_LONG).show();
        }
    }

    private class FetchBreedsTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... params) {
            String apiUrl = params[0];
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            List<String> breeds = new ArrayList<>();

            try {
                URL url = new URL(apiUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                StringBuilder response = new StringBuilder();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    if (apiUrl.equals(DOG_BREEDS_API)) {
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        JSONObject breedsObject = jsonResponse.getJSONObject("message");
                        Iterator<String> keys = breedsObject.keys();
                        while (keys.hasNext()) {
                            String breed = keys.next();
                            breeds.add(breed.substring(0, 1).toUpperCase() + breed.substring(1));
                        }
                    } else if (apiUrl.equals(CAT_BREEDS_API)) {
                        JSONArray breedsArray = new JSONArray(response.toString());
                        for (int i = 0; i < breedsArray.length(); i++) {
                            JSONObject breed = breedsArray.getJSONObject(i);
                            breeds.add(breed.getString("name"));
                        }
                    }

                    breeds.sort(String::compareToIgnoreCase);
                    breeds.add("Other");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching breeds from " + apiUrl, e);
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (conn != null) conn.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing resources", e);
                }
            }
            return breeds;
        }

        @Override
        protected void onPostExecute(List<String> breeds) {
            if (breeds.isEmpty()) {
                breeds.add("Other");
                Toast.makeText(AddPetActivity.this, "Couldn't load breeds, using default", Toast.LENGTH_SHORT).show();
            }
            updateBreedsAdapter(breeds);
        }
    }

    private void updateBreedsAdapter(List<String> breeds) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                breeds
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        breedSpinner.setAdapter(adapter);

        selectedBreed = "";
        breedDisplayText.setText("Select breed");
        breedDisplayText.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    // Modify the AddPetTask class to include proper session handling:
    private class AddPetTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                // 1. Get session data
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String sessionToken = prefs.getString("session_token", "");

                // 2. Create JSON data
                JSONObject json = new JSONObject();
                json.put("petName", params[0]);
                json.put("petType", params[1]);
                json.put("petBreed", params[2]);
                json.put("petSize", params[3]);
                json.put("petGender", params[4]);
                json.put("petBirthDate", selectedBirthDate);

                // 3. Add image if available
                if (selectedImageBitmap != null) {
                    String imageBase64 = convertBitmapToBase64(selectedImageBitmap);
                    json.put("petImage", imageBase64);
                }

                // 4. Create connection
                URL url = new URL(PHP_API_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Cookie", "PHPSESSID=" + sessionToken);
                conn.setDoOutput(true);

                // 5. Send data
                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                // 6. Read response
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    return response.toString();
                } else {
                    return "{\"status\":\"error\",\"message\":\"Server returned HTTP "+responseCode+"\"}";
                }
            } catch (Exception e) {
                return "{\"status\":\"error\",\"message\":\""+e.getMessage()+"\"}";
            } finally {
                if (conn != null) conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json = new JSONObject(result);
                if (json.getString("status").equals("success")) {
                    Toast.makeText(AddPetActivity.this, "Pet added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddPetActivity.this,
                            "Error: " + json.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(AddPetActivity.this,
                        "Error parsing response: " + result, Toast.LENGTH_LONG).show();
            }
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    private void resetForm() {
        petNameEditText.setText("");
        birthDateEditText.setText("");
        petPhotoImageView.setImageResource(R.drawable.default_profile);
        petTypeSpinner.setSelection(0);
        breedSpinner.setSelection(0);
        sizeSpinner.setSelection(0);
        genderSpinner.setSelection(0);

        typeDisplayText.setText("Select pet type");
        breedDisplayText.setText("Select breed");
        sizeDisplayText.setText("Select size");
        genderDisplayText.setText("Select gender");

        typeDisplayText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        breedDisplayText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        sizeDisplayText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        genderDisplayText.setTextColor(getResources().getColor(android.R.color.darker_gray));

        selectedPetType = "";
        selectedBreed = "";
        selectedSize = "";
        selectedGender = "";
        selectedBirthDate = "";
        selectedImagePath = "";
        selectedImageBitmap = null;
    }
}