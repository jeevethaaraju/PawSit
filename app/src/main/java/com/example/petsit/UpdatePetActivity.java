package com.example.petsit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Locale;
import java.util.Calendar;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class UpdatePetActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText petNameEditText, petBirthEditText;
    private Spinner typeSpinner, breedSpinner, sizeSpinner, genderSpinner;
    private Button updateButton;
    private Button deleteButton;

    private ImageView petImageView, pencilIcon;
    private int petId;
    private RequestQueue requestQueue;
    private ArrayAdapter<String> breedAdapter;
    private String currentPetType = "";
    private String currentPetImageUrl = "";
    private Uri selectedImageUri;
    private boolean imageChanged = false;

    private final List<String> sizeCategories = Arrays.asList(
            "Small (1-5kg)",
            "Medium (5-10kg)",
            "Large (10-20kg)",
            "Extra Large (20kg+)"
    );

    private final List<String> genderOptions = Arrays.asList(
            "Male",
            "Female"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_pet);
        initializeViews();
        initializeSpinners();

        ImageView imageVector = findViewById(R.id.image_arrow_left);

        imageVector.setOnClickListener(view -> {
            finish();
        });

        petId = getIntent().getIntExtra("PET_ID", 0);
        if (petId == 0) {
            Toast.makeText(this, "Invalid pet ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchPetDetails();
        pencilIcon.setOnClickListener(v -> openGallery());
        updateButton.setOnClickListener(v -> updatePetDetails());
    }

    private void initializeViews() {
        petNameEditText = findViewById(R.id.text_pet_name);
        petBirthEditText = findViewById(R.id.text_pet_born);
        petImageView = findViewById(R.id.pet_image);
        pencilIcon = findViewById(R.id.image_pencil_simple);
        typeSpinner = findViewById(R.id.type_spinner);
        breedSpinner = findViewById(R.id.breed_spinner);
        sizeSpinner = findViewById(R.id.size_spinner);
        genderSpinner = findViewById(R.id.gender_spinner);
        updateButton = findViewById(R.id.update_button);
        requestQueue = Volley.newRequestQueue(this);

        ImageView calendarIcon = findViewById(R.id.image_calendar);
        calendarIcon.setOnClickListener(v -> showDatePickerDialog());

        deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imageChanged = true;
            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.peticon)
                    .error(R.drawable.peticon)
                    .into(petImageView);
        }
    }

    // Add this new method for date picker
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format date as YYYY-MM-DD
                    String formattedDate = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay);
                    petBirthEditText.setText(formattedDate);
                },
                year, month, day);

        // Set maximum date to today
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void initializeSpinners() {
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, Arrays.asList("Dog", "Cat"));
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        breedAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, new ArrayList<>());
        breedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        breedSpinner.setAdapter(breedAdapter);

        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, sizeCategories);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(sizeAdapter);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, genderOptions);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPetType = parent.getItemAtPosition(position).toString();
                loadBreedsForType(currentPetType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadBreedsForType(String petType) {
        breedAdapter.clear();
        breedAdapter.add("Loading...");

        if (petType.equals("Dog")) {
            fetchDogBreeds();
        } else if (petType.equals("Cat")) {
            fetchCatBreeds();
        }
    }

    private void fetchDogBreeds() {
        String url = "https://dog.ceo/api/breeds/list/all";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        breedAdapter.clear();
                        JSONObject breedsObject = response.getJSONObject("message");
                        Iterator<String> keys = breedsObject.keys();
                        while (keys.hasNext()) {
                            breedAdapter.add(keys.next());
                        }
                        if (getIntent().hasExtra("PET_BREED")) {
                            setSpinnerValue(breedSpinner, getIntent().getStringExtra("PET_BREED"));
                        }
                    } catch (JSONException e) {
                        Log.e("DogBreeds", "Error parsing dog breeds", e);
                    }
                },
                error -> Log.e("DogBreeds", "Error fetching dog breeds", error));
        requestQueue.add(request);
    }

    private void fetchCatBreeds() {
        String url = "https://api.thecatapi.com/v1/breeds";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        breedAdapter.clear();
                        for (int i = 0; i < response.length(); i++) {
                            breedAdapter.add(response.getJSONObject(i).getString("name"));
                        }
                        if (getIntent().hasExtra("PET_BREED")) {
                            setSpinnerValue(breedSpinner, getIntent().getStringExtra("PET_BREED"));
                        }
                    } catch (JSONException e) {
                        Log.e("CatBreeds", "Error parsing cat breeds", e);
                    }
                },
                error -> Log.e("CatBreeds", "Error fetching cat breeds", error));
        requestQueue.add(request);
    }

    private void fetchPetDetails() {
        String url = Constants.BASE_URL + "get_pet_details.php?pet_id=" + petId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.has("success")) {
                            JSONObject pet = response.getJSONObject("pet");
                            petNameEditText.setText(pet.getString("petName"));
                            if (pet.has("petBirthDate")) {
                                petBirthEditText.setText(pet.getString("petBirthDate"));
                            }
                            if (pet.has("petImage")) {
                                currentPetImageUrl = pet.getString("petImage");
                                Glide.with(this)
                                        .load(Constants.BASE_URL + Constants.UPLOADS_DIR + currentPetImageUrl)
                                        .placeholder(R.drawable.peticon)
                                        .error(R.drawable.peticon)
                                        .into(petImageView);
                            }
                            String petType = pet.getString("petType");
                            setSpinnerValue(typeSpinner, petType);
                            currentPetType = petType;
                            loadBreedsForType(petType);
                            new android.os.Handler().postDelayed(() -> {
                                setSpinnerValue(breedSpinner, pet.optString("petBreed"));
                            }, 1000);
                            if (pet.has("petSize")) {
                                setSpinnerValue(sizeSpinner, pet.getString("petSize"));
                            }
                            if (pet.has("petGender")) {
                                setSpinnerValue(genderSpinner, pet.getString("petGender"));
                            }
                        } else {
                            Toast.makeText(this, response.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching pet details", Toast.LENGTH_SHORT).show());
        requestQueue.add(request);
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }


    private void updatePetDetails() {
        String updatedName = petNameEditText.getText().toString().trim();
        String updatedBirth = petBirthEditText.getText().toString().trim();
        String updatedType = typeSpinner.getSelectedItem().toString();
        String updatedBreed = breedSpinner.getSelectedItem().toString();
        String updatedSize = sizeSpinner.getSelectedItem().toString();
        String updatedGender = genderSpinner.getSelectedItem().toString();

        if (updatedName.isEmpty()) {
            Toast.makeText(this, "Pet name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BASE_URL + "update_pet.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            if (jsonResponse.has("new_image")) {
                                currentPetImageUrl = jsonResponse.getString("new_image");
                            }
                            Toast.makeText(this, "Pet updated successfully", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(this, jsonResponse.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error updating pet", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("pet_id", String.valueOf(petId));
                params.put("pet_name", updatedName);
                params.put("pet_birth", updatedBirth);
                params.put("pet_type", updatedType);
                params.put("pet_breed", updatedBreed);
                params.put("pet_size", updatedSize);
                params.put("pet_gender", updatedGender);
                params.put("current_image", currentPetImageUrl);
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                if (!imageChanged || selectedImageUri == null) {
                    return super.getBody();
                }

                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("pet_id", petId);
                    jsonBody.put("pet_name", updatedName);
                    jsonBody.put("pet_birth", updatedBirth);
                    jsonBody.put("pet_type", updatedType);
                    jsonBody.put("pet_breed", updatedBreed);
                    jsonBody.put("pet_size", updatedSize);
                    jsonBody.put("pet_gender", updatedGender);
                    jsonBody.put("current_image", currentPetImageUrl);

                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                    jsonBody.put("pet_image", Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));

                    return jsonBody.toString().getBytes("UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                if (imageChanged && selectedImageUri != null) {
                    return "application/json; charset=utf-8";
                }
                return "application/x-www-form-urlencoded";
            }
        };

        requestQueue.add(stringRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }


    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Pet")
                .setMessage("Are you sure you want to delete this pet?")
                .setPositiveButton("Delete", (dialog, which) -> deletePet())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePet() {
        String url = Constants.BASE_URL + "delete_pet.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(this, "Pet deleted successfully", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(this, jsonResponse.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error deleting pet", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("pet_id", String.valueOf(petId));
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}