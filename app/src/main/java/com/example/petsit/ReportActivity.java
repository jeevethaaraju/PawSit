package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import java.util.Arrays;
import java.util.function.Consumer;
import com.bumptech.glide.Glide;



public class ReportActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ComplaintAdapter adapter;
    private List<Complaint> complaintList = new ArrayList<>();
    private RequestQueue requestQueue;
    private TextView textNoData;
    private String sitterId;

    private static final String KEY_OWNER_ID = "owner_id";
    private static final String PREFS_NAME = "UserPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_report);
        NavigationUtil.setupBottomNavigation(this);
        ImageView back = findViewById(R.id.image_arrow_left);
        back.setOnClickListener(view -> {
            finish();
        });

        ImageView ordericon = findViewById(R.id.ordericon);
        ordericon.setOnClickListener(view -> {
            NavigationUtil.navigateToOrders(ReportActivity.this);
        });

        ImageView imageVector = findViewById(R.id.icon_complaint_form);

        imageVector.setOnClickListener(view -> {
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int ownerId = preferences.getInt(KEY_OWNER_ID, -1);
            Intent intent = new Intent(this, ComplaintFormActivity.class);
            intent.putExtra("petOwner_ID", ownerId);
           // Toast.makeText(this, "Retrieved owner ID: " + ownerId, Toast.LENGTH_SHORT).show();
            startActivity(intent);
        });
        recyclerView = findViewById(R.id.recycler_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(complaintList);
        recyclerView.setAdapter(adapter);

        textNoData = findViewById(R.id.text_no_bookings);

        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int ownerId = preferences.getInt(KEY_OWNER_ID, -1);

        if (ownerId == -1) {
            Toast.makeText(this, "Error: Owner ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchComplaints(ownerId);
    }

    private void fetchComplaints(int ownerId) {
        String url = Constants.BASE_URL + "get_complaint.php?owner_id=" + ownerId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            JSONArray complaintArray = response.getJSONArray("complaints");
                            if (complaintArray.length() == 0) {
                                showNoData(true, "No complaints found");
                            } else {
                                showNoData(false, "");
                                parseComplaints(complaintArray);
                            }
                        } else {
                            String message = response.optString("message", "Unknown error");
                            showNoData(true, "Error: " + message);
                        }
                    } catch (JSONException e) {
                        showNoData(true, "Error parsing response: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> {
                    String errorMsg = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    if (error.networkResponse != null) {
                        errorMsg += " (Status: " + error.networkResponse.statusCode + ")";
                    }
                    showNoData(true, "Network error: " + errorMsg);
                }
        );

        requestQueue.add(request);
    }

    private void showNoData(boolean show, String message) {
        runOnUiThread(() -> {
            if (show) {
                recyclerView.setVisibility(View.GONE);
                textNoData.setVisibility(View.VISIBLE);
                textNoData.setText(message.isEmpty() ? "No complaints found" : message);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                textNoData.setVisibility(View.GONE);
            }
        });
    }
    private void parseComplaints(JSONArray array) throws JSONException {
        complaintList.clear();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Complaint complaint = new Complaint(
                    obj.getString("complaint_id"),
                    obj.getString("booking_id"),
                    obj.getString("created_at"),
                    obj.getString("status"),
                    obj.getString("description"),
                    obj.getString("sitter_id"),
                    obj.optString("proof_image", "")
            );
            complaintList.add(complaint);
        }
        adapter.notifyDataSetChanged();
    }

    public static class Complaint {
        private String complaintId, bookingId, createdAt, status, description,sitterId,proofImage;

        public Complaint(String complaintId, String bookingId, String createdAt, String status, String description,String sitterId,String proofImage) {
            this.complaintId = complaintId;
            this.bookingId = bookingId;
            this.createdAt = createdAt;
            this.status = status;
            this.description = description;
            this.sitterId = sitterId;
            this.proofImage = proofImage;
        }

        public String getComplaintId() { return complaintId; }
        public String getBookingId() { return bookingId; }
        public String getCreatedAt() { return createdAt; }
        public String getStatus() { return status; }
        public String getDescription() { return description; }
        public String getSitterId() { return sitterId; }
        public String getProofImage() { return proofImage; }
    }
    private void updateComplaintStatus(String complaintId, String newStatus) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int adminId = preferences.getInt(KEY_OWNER_ID, -1);

        if (adminId == -1) {
            Toast.makeText(this, "Admin ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject statusData = new JSONObject();
            statusData.put("complaint_id", complaintId);
            statusData.put("status", newStatus);
            statusData.put("admin_id", adminId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    Constants.BASE_URL + "update_complaint_status.php",
                    statusData,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(ReportActivity.this,
                                        "Status updated", Toast.LENGTH_SHORT).show();
                                fetchComplaints(adminId); // Refresh list
                            } else {
                                Toast.makeText(ReportActivity.this,
                                        "Error: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(ReportActivity.this,
                                    "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Toast.makeText(ReportActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatusDialog(String complaintId, String currentStatus) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Status");

        String[] statuses = {"Pending", "Investigating", "Resolved", "Rejected"};
        int currentIndex = Arrays.asList(statuses).indexOf(currentStatus);

        builder.setSingleChoiceItems(statuses, currentIndex, null)
                .setPositiveButton("Update", (dialog, which) -> {
                    int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                    String newStatus = statuses[selectedPosition];
                    updateComplaintStatus(complaintId, newStatus);
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void checkComplaintCount(int userId, Consumer<Integer> callback) {
        String url = Constants.BASE_URL + "count_complaints.php?userId=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    if (response.optBoolean("success")) {
                        int count = response.optInt("count", 0);
                        callback.accept(count);
                    }
                },
                error -> {
                    Toast.makeText(ReportActivity.this, "Error fetching count", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }
    private void showRemoveConfirmation(int sitterId) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Pet Sitter")
                .setMessage("Are you sure you want to remove this pet sitter?")
                .setPositiveButton("Remove", (dialog, which) -> removePetSitter(sitterId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removePetSitter(int sitterId) {
        try {
            JSONObject data = new JSONObject();
            data.put("sitter_id", sitterId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    Constants.BASE_URL + "remove_petsitter.php",
                    data,
                    response -> {
                        if (response.optBoolean("success")) {
                            Toast.makeText(this, "Pet sitter removed.", Toast.LENGTH_SHORT).show();
                            fetchComplaints(getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(KEY_OWNER_ID, -1));
                        } else {
                            Toast.makeText(this, "Failed to remove sitter.", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Error removing sitter", Toast.LENGTH_SHORT).show()
            );
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintViewHolder> {
        private List<Complaint> complaints;

        public ComplaintAdapter(List<Complaint> complaints) {
            this.complaints = complaints;
        }

        @Override
        public ComplaintViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_report_order, parent, false);
            return new ComplaintViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ComplaintViewHolder holder, int position) {
            Complaint c = complaints.get(position);
            holder.complaintId.setText("Complaint ID: " + c.getComplaintId());
            holder.bookingId.setText("Booking ID: " + c.getBookingId());
            holder.createdAt.setText("Created At: " + c.getCreatedAt());
            holder.status.setText("Status: " + c.getStatus());
            holder.description.setText("Description: " + c.getDescription());

            holder.removeButton.setVisibility(View.GONE); // Hide button by default

            // Load proof image
            if (!c.getProofImage().isEmpty()) {
                String imageUrl = Constants.BASE_URL + c.getProofImage();
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.load)
                        .error(R.drawable.error)
                        .into(holder.proofImage);
            } else {
                holder.proofImage.setImageResource(R.drawable.epty);
            }

            holder.itemView.setOnClickListener(v -> {
                if (isAdmin()) {
                    showStatusDialog(c.getComplaintId(), c.getStatus());
                }
            });

            // Show "Remove Pet Sitter" button for admin if complaint count >= 3
            if (isAdmin()) {
                int sitterId = Integer.parseInt(c.getSitterId()); // get from model

                checkComplaintCount(sitterId, count -> {
                    if (count >= 3) {
                        holder.removeButton.setVisibility(View.VISIBLE);
                        holder.removeButton.setOnClickListener(view -> {
                            showRemoveConfirmation(sitterId);
                        });
                    }
                });
            }
        }


        private boolean isAdmin() {
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            return preferences.getBoolean("is_admin", false); // Set this when user logs in
        }
        @Override
        public int getItemCount() {
            return complaints.size();
        }
    }

    public class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView complaintId, bookingId, createdAt, status, description;
        Button removeButton;
        ImageView proofImage;
        public ComplaintViewHolder(View itemView) {
            super(itemView);
            complaintId = itemView.findViewById(R.id.text_complaint_id);
            bookingId = itemView.findViewById(R.id.text_booking_id);
            createdAt = itemView.findViewById(R.id.text_created_at);
            status = itemView.findViewById(R.id.text_status);
            description = itemView.findViewById(R.id.text_description);
            removeButton = itemView.findViewById(R.id.button_remove_sitter);
            proofImage = itemView.findViewById(R.id.image_proof);

        }
    }
}
