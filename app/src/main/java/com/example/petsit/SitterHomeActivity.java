package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.android.volley.DefaultRetryPolicy;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import org.json.JSONException;
import com.android.volley.toolbox.JsonArrayRequest;
import android.widget.LinearLayout;
import org.json.JSONObject;

import android.widget.RelativeLayout;


public class SitterHomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_SITTER_ID = "sitter_id";
    private static final String KEY_EMAIL = "email";
    private static final String TAG = "SitterHomeActivity";

    private ImageView profileIcon;
    private RequestQueue requestQueue;
    private TextView totalBookingsCount;
    private TextView completedBookingsCount;
    private TextView pendingBookingsCount;
    private TextView approvedBookingsCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sitterhome);
        NavigationSitterUtil.setupBottomNavigation(this);
        totalBookingsCount = findViewById(R.id.text_total_bookings_count);
        completedBookingsCount = findViewById(R.id.text_completed_count);
        pendingBookingsCount = findViewById(R.id.text_pending_count);
        approvedBookingsCount = findViewById(R.id.text_approved_count);
        requestQueue = Volley.newRequestQueue(this);

        ImageView listing = findViewById(R.id.image_frame18);
        ImageView order = findViewById(R.id.ordericon);

        order.setOnClickListener(view -> {

                NavigationSitterUtil.navigateToOrders(SitterHomeActivity.this);

        });

        listing.setOnClickListener(view -> {
            startActivity(new Intent(SitterHomeActivity.this, ListingActivity.class));
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int petsitterId = preferences.getInt(KEY_SITTER_ID, -1);
            String sessionToken = preferences.getString(KEY_SESSION_TOKEN, null);
            Toast.makeText(this, "Retrieved owner ID: " + petsitterId, Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.notification).setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int sitterId = preferences.getInt(KEY_SITTER_ID, -1);
            Intent intent = new Intent(this, NotificationsActivity.class);
            intent.putExtra("petSitter_ID", sitterId);
            Toast.makeText(this, "Retrieved sitter ID: " + sitterId, Toast.LENGTH_SHORT).show();
            startActivity(intent);
        });

        // Initialize views
        ImageView profileBtn = findViewById(R.id.profilehome);
        TextView nameTextView = findViewById(R.id.name);
        profileIcon = findViewById(R.id.profileicon);

        // Set up profile icon click with logout option
        profileIcon.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(SitterHomeActivity.this, profileIcon);
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

        // Get user data from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String sessionToken = preferences.getString(KEY_SESSION_TOKEN, null);
        int sitterId = preferences.getInt(KEY_SITTER_ID, -1);
        String email = preferences.getString(KEY_EMAIL, "");

        if (sessionToken == null || sitterId == -1 || email.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Set user data
        String firstName = preferences.getString(KEY_FIRST_NAME, "");
        String profileImage = preferences.getString(KEY_PROFILE_IMAGE, "");

        if (!firstName.isEmpty()) {
            nameTextView.setText(firstName);
        } else {
            nameTextView.setText("User");
        }

        // Load profile image
        if (profileImage != null && !profileImage.isEmpty()) {
            String fullImageUrl = Constants.BASE_URL + profileImage;
            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(profileIcon);
        } else {
            profileIcon.setImageResource(R.drawable.default_profile);
        }

        // Set click listeners
        profileBtn.setOnClickListener(view -> {
            Intent profileIntent = new Intent(SitterHomeActivity.this, SitterProfileActivity.class);
            profileIntent.putExtra("first_name", firstName);
            startActivity(profileIntent);
        });

        // Load upcoming booking
        loadUpcomingBooking(sitterId);

        // Set click listener for view booking button

    }

    private void loadBookingCounts(int sitterId) {
        // Load total bookings count
        String totalUrl = Constants.BASE_URL + "total_booking_sitter.php?sitter_id=" + sitterId;
        JsonObjectRequest totalRequest = new JsonObjectRequest(
                Request.Method.GET, totalUrl, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            int count = response.getInt("total_bookings");
                            totalBookingsCount.setText(String.valueOf(count));
                        } else {
                            String errorMsg = response.optString("message", "Unknown error");
                            Log.e(TAG, "Server error (total): " + errorMsg);
                            totalBookingsCount.setText("0");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing total bookings response", e);
                        totalBookingsCount.setText("0");
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error (total): " + error.getMessage());
                    totalBookingsCount.setText("0");
                }
        );

        // Load completed bookings count
        String completedUrl = Constants.BASE_URL + "totalcomp_booking_sitter.php?sitter_id=" + sitterId;
        JsonObjectRequest completedRequest = new JsonObjectRequest(
                Request.Method.GET, completedUrl, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            int count = response.getInt("completed_bookings");
                            completedBookingsCount.setText(String.valueOf(count));
                        } else {
                            String errorMsg = response.optString("message", "Unknown error");
                            Log.e(TAG, "Server error (completed): " + errorMsg);
                            completedBookingsCount.setText("0");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing completed bookings response", e);
                        completedBookingsCount.setText("0");
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error (completed): " + error.getMessage());
                    completedBookingsCount.setText("0");
                }
        );

        String pendingUrl = Constants.BASE_URL + "totalpend_booking_sitter.php?sitter_id=" + sitterId;
        JsonObjectRequest pendingRequest = new JsonObjectRequest(
                Request.Method.GET, pendingUrl, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            int count = response.getInt("pending_bookings");
                            pendingBookingsCount.setText(String.valueOf(count));
                        } else {
                            String errorMsg = response.optString("message", "Unknown error");
                            Log.e(TAG, "Server error (pending): " + errorMsg);
                            pendingBookingsCount.setText("0");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing pending bookings response", e);
                        pendingBookingsCount.setText("0");
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error (completed): " + error.getMessage());
                    pendingBookingsCount.setText("0");
                }
        );

        // Load completed bookings count
        String approvedUrl = Constants.BASE_URL + "totalapp_booking_sitter.php?sitter_id=" + sitterId;
        JsonObjectRequest approvedRequest = new JsonObjectRequest(
                Request.Method.GET, approvedUrl, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            int count = response.getInt("approved_bookings");
                            approvedBookingsCount.setText(String.valueOf(count));
                        } else {
                            String errorMsg = response.optString("message", "Unknown error");
                            Log.e(TAG, "Server error (completed): " + errorMsg);
                           approvedBookingsCount.setText("0");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing completed bookings response", e);
                        approvedBookingsCount.setText("0");
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error (completed): " + error.getMessage());
                    approvedBookingsCount.setText("0");
                }
        );


        // Add requests to queue
        requestQueue.add(totalRequest);
        requestQueue.add(completedRequest);
        requestQueue.add(pendingRequest);
        requestQueue.add(approvedRequest);
    }
    private void loadUpcomingBooking(int sitterId) {
        String url = Constants.BASE_URL + "get_upcoming_bookings.php?sitter_id=" + sitterId;

        // First check if views exist
        LinearLayout container = findViewById(R.id.upcoming_bookings_container);
        TextView noBookingText = findViewById(R.id.no_booking_text);

        if (container == null || noBookingText == null) {
            Log.e(TAG, "Critical views not found in layout");
            return;
        }

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        runOnUiThread(() -> {
                            container.removeAllViews();

                            if (response.length() == 0) {
                                noBookingText.setVisibility(View.VISIBLE);
                                container.addView(noBookingText);
                                return;
                            }

                            noBookingText.setVisibility(View.GONE);

                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject booking = response.getJSONObject(i);
                                    View bookingCard = getLayoutInflater().inflate(
                                            R.layout.item_upcoming_booking,
                                            container,
                                            false
                                    );

                                    // Set booking data
                                    TextView clientName = bookingCard.findViewById(R.id.upcoming_client_name);

                                    TextView date = bookingCard.findViewById(R.id.upcoming_date);
                                    ImageView clientImage = bookingCard.findViewById(R.id.upcoming_client_image);



                                    clientName.setText(booking.getString("client_name"));

                                    date.setText(booking.getString("date_display"));

                                    // Load profile image
                                    String profileImage = booking.optString("profile_image", "");
                                    if (!profileImage.isEmpty()) {
                                        String imageUrl = profileImage.startsWith("http") ?
                                                profileImage :
                                                Constants.BASE_URL + profileImage;

                                        Glide.with(this)
                                                .load(imageUrl)
                                                .placeholder(R.drawable.default_profile)
                                                .error(R.drawable.default_profile)
                                                .circleCrop()
                                                .into(clientImage);
                                    }

                                    // Set click listener for view button
                                    final String bookingId = booking.getString("booking_id");


                                    // Add margin between cards (except first one)
                                    if (i > 0) {
                                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bookingCard.getLayoutParams();
                                        params.setMargins(16, 0, 0, 0);
                                        bookingCard.setLayoutParams(params);
                                    }

                                    container.addView(bookingCard);
                                } catch (JSONException e) {
                                    Log.e(TAG, "Error parsing booking at index " + i + ": " + e.getMessage());
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing response: " + e.getMessage());
                        runOnUiThread(() -> showNoBookingsMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.getMessage());
                    runOnUiThread(() -> showNoBookingsMessage());
                }
        );

        // Set retry policy
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonArrayRequest);
    }

    private void showNoBookingsMessage() {
        LinearLayout container = findViewById(R.id.upcoming_bookings_container);
        TextView noBookingText = findViewById(R.id.no_booking_text);

        if (container != null && noBookingText != null) {
            container.removeAllViews();
            noBookingText.setVisibility(View.VISIBLE);
            container.addView(noBookingText);
        }
    }
    @Override

    protected void onResume() {
        super.onResume();
        // Refresh upcoming bookings and counts when activity resumes
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int sitterId = preferences.getInt(KEY_SITTER_ID, -1);
        if (sitterId != -1) {
            loadUpcomingBooking(sitterId);
            loadBookingCounts(sitterId); // Add this line
        }

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
}