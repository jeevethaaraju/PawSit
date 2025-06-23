package com.example.petsit;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.graphics.Color;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.ViewGroup;
import java.util.*;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.*;
import android.app.AlertDialog;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
public class ComplaintFormActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION = 2;
    private Spinner spinnerOrders;
    private EditText complaintDescription;
    private Button submitButton,uploadButton;
    private ImageView imageProof;
    private TextView uploadStatus;;
    private RequestQueue requestQueue;

    private String selectedBookingId = "";
    private int petOwnerId;
    private Map<String, String> bookingIdMap = new HashMap<>();
    private Map<String, Boolean> bookingComplaintMap = new HashMap<>();
    private String encodedImage = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_form); // Make sure XML has spinner_orders, edit_complaint_description, btn_submit_complaint

        ImageView back = findViewById(R.id.image_back);
        back.setOnClickListener(view -> {
            finish();
        });
        // Initialize views
        spinnerOrders = findViewById(R.id.spinner_orders);
        complaintDescription = findViewById(R.id.edit_complaint_description);
        submitButton = findViewById(R.id.btn_submit_complaint);
        uploadButton = findViewById(R.id.btn_upload_photo);
        imageProof = findViewById(R.id.image_proof);
        uploadStatus = findViewById(R.id.text_upload_status);
        // Get petOwnerId from Intent
        petOwnerId = getIntent().getIntExtra("petOwner_ID", -1);
        if (petOwnerId == -1) {
            Toast.makeText(this, "Error: Owner ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        requestQueue = Volley.newRequestQueue(this);
        loadApprovedBookings();

        uploadButton.setOnClickListener(v -> checkPermissionAndOpenImagePicker());

        submitButton.setOnClickListener(v -> submitComplaint());
    }
    private void checkPermissionAndOpenImagePicker() {
        // For Android 10 (API 29) and above, we don't need storage permission for gallery access
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openImagePicker();
        } else {
            // For older versions, request storage permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Explain why we need the permission if user previously denied it
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Needed")
                            .setMessage("This permission is needed to access your photos for complaint proof")
                            .setPositiveButton("OK", (dialog, which) -> {
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        REQUEST_PERMISSION);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .create().show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION);
                }
            } else {
                openImagePicker();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                imageProof.setImageBitmap(bitmap);
                uploadStatus.setText("Photo selected");

                // Convert bitmap to Base64
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void loadApprovedBookings() {
        String url = Constants.BASE_URL + "get_approved_bookings.php?owner_id=" + petOwnerId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    List<String> bookingIds = new ArrayList<>();
                    List<Boolean> hasComplaintList = new ArrayList<>(); // Track which bookings have complaints
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        if (jsonObject.has("orders")) {
                            JSONArray bookingsArray = jsonObject.getJSONArray("orders");

                            for (int i = 0; i < bookingsArray.length(); i++) {
                                JSONObject booking = bookingsArray.getJSONObject(i);

                                if (booking.has("booking_id") && booking.has("FromDate")) {
                                    String bookingId = booking.getString("booking_id");
                                    String fromDateRaw = booking.getString("FromDate");
                                    boolean hasComplaint = booking.optBoolean("has_complaint", false);

                                    String displayText = "Order #" + bookingId + " (From: " + fromDateRaw + ")";
                                    if (hasComplaint) {
                                        displayText += " - Complaint Submitted";
                                    }

                                    bookingIds.add(displayText);
                                    bookingIdMap.put(displayText, bookingId);
                                    hasComplaintList.add(hasComplaint);
                                }
                            }
                        }

                        if (bookingIds.isEmpty()) {
                            bookingIds.add("No approved bookings");
                            spinnerOrders.setEnabled(false);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, bookingIds) {
                            @Override
                            public boolean isEnabled(int position) {
                                // Disable items that already have complaints
                                return position == 0 || !hasComplaintList.get(position - 1);
                            }

                            @Override
                            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                View view = super.getDropDownView(position, convertView, parent);
                                TextView textView = (TextView) view;
                                // Gray out disabled items
                                if (position > 0 && hasComplaintList.get(position - 1)) {
                                    textView.setTextColor(Color.GRAY);
                                } else {
                                    textView.setTextColor(Color.BLACK);
                                }
                                return view;
                            }
                        };

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerOrders.setAdapter(adapter);

                        spinnerOrders.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String selectedItem = parent.getItemAtPosition(position).toString();
                                selectedBookingId = bookingIdMap.getOrDefault(selectedItem, "");

                                // Show toast if selected booking already has complaint
                                if (position > 0 && hasComplaintList.get(position - 1)) {
                                    Toast.makeText(ComplaintFormActivity.this,
                                            "You already submitted a complaint for this booking",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                selectedBookingId = "";
                            }
                        });

                    } catch (JSONException e) {
                        Toast.makeText(this, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                });

        requestQueue.add(request);
    }


    private void submitComplaint() {
        String description = complaintDescription.getText().toString().trim();

        if (selectedBookingId.isEmpty()) {
            Toast.makeText(this, "Please select an approved booking", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookingComplaintMap.getOrDefault(selectedBookingId, false)) {
            Toast.makeText(this, "Complaint already exists for this booking", Toast.LENGTH_LONG).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a complaint description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (encodedImage.isEmpty()) {
            Toast.makeText(this, "Please upload proof photo", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constants.BASE_URL + "submit_complaint.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String errorMessage = jsonResponse.getString("message");
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Submission failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("booking_id", selectedBookingId);
                params.put("petOwner_ID", String.valueOf(petOwnerId));
                params.put("description", description);
                params.put("proof_image", encodedImage);
                return params;
            }
        };

        requestQueue.add(request);
    }
}
