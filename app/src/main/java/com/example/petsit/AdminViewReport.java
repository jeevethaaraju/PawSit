package com.example.petsit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import android.content.SharedPreferences;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

public class AdminViewReport extends AppCompatActivity {

    private static final String TAG = "AdminViewReport";
    private static final String PREFS_NAME = "RemovedSittersPrefs";
    private static final String REMOVED_KEY = "removed_sitters";
    private static final String FETCH_COMPLAINTS_URL = Constants.BASE_URL + "fetch_complaints.php";
    private static final String UPDATE_STATUS_URL = Constants.BASE_URL + "update_complaint_status.php";
    private static final String SEND_NOTIFICATION_URL = Constants.BASE_URL + "send_notification.php";
    private static final String REMOVE_PETSITTER_URL = Constants.BASE_URL + "remove_petsitter.php";
    private static final Set<Integer> removedPetSitters = new HashSet<>();
    private RecyclerView recyclerView;
    private TextView textNoBookings;
    private ImageView backArrow;
    private ComplaintsAdapter adapter;
    private ArrayList<Complaint> complaintList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_view_report);
        NavigationAdminUtil.setupBottomNavigation(this);
        loadRemovedSitters();
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        fetchComplaints();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Convert set to array for saving
        Integer[] removedArray = removedPetSitters.toArray(new Integer[0]);
        outState.putIntArray("removed_sitters", Arrays.stream(removedArray).mapToInt(i->i).toArray());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore removed sitters
        int[] removedArray = savedInstanceState.getIntArray("removed_sitters");
        if (removedArray != null) {
            for (int id : removedArray) {
                removedPetSitters.add(id);
            }
        }
    }

    private void saveRemovedSitters() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> stringSet = new HashSet<>();
        for (Integer id : removedPetSitters) {
            stringSet.add(String.valueOf(id));
        }
        prefs.edit().putStringSet(REMOVED_KEY, stringSet).apply();
    }

    private void loadRemovedSitters() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> stringSet = prefs.getStringSet(REMOVED_KEY, new HashSet<>());
        removedPetSitters.clear();
        for (String id : stringSet) {
            try {
                removedPetSitters.add(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing pet sitter ID: " + id);
            }
        }
    }
    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_orders);
        textNoBookings = findViewById(R.id.text_no_bookings);
        backArrow = findViewById(R.id.image_arrow_left);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintsAdapter(complaintList);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        backArrow.setOnClickListener(v -> finish());
    }

    private void fetchComplaints() {
        new FetchComplaintsTask().execute();
    }

    private void sendNotificationToPetSitter(int petSitterId, int complaintId) {
        new SendNotificationTask().execute(String.valueOf(petSitterId), String.valueOf(complaintId));
    }

    private void removePetSitterAccount(int petSitterId) {
        new RemovePetSitterTask().execute(String.valueOf(petSitterId));
    }

    // AsyncTask to fetch complaints from server
    private class FetchComplaintsTask extends AsyncTask<Void, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(FETCH_COMPLAINTS_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonObject = new JSONObject(response.toString());

                    if (jsonObject.getBoolean("success")) {
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        complaintList.clear();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject c = dataArray.getJSONObject(i);
                            Complaint complaint = new Complaint(
                                    c.getInt("complaint_id"),
                                    c.getInt("booking_id"),
                                    c.getString("created_at"),
                                    c.getString("status"),
                                    c.getString("description"),
                                    c.getInt("petOwner_ID"),
                                    c.getInt("petSitter_ID"),
                                    c.getString("FromDate"),
                                    c.optString("proof_image", "")
                            );

                            if (c.has("is_refunded")) {
                                complaint.isRefunded = c.getBoolean("is_refunded");
                            }
                            complaintList.add(complaint);
                        }
                        return true;
                    } else {
                        errorMessage = jsonObject.getString("message");
                    }
                } else {
                    errorMessage = "HTTP Error: " + responseCode;
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Error fetching complaints: ", e);
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing resources: ", e);
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                if (complaintList.isEmpty()) {
                    showEmptyState("No complaints found");
                } else {
                    showComplaintsList();
                }
            } else {
                showEmptyState("Failed to load complaints: " + errorMessage);
                Toast.makeText(AdminViewReport.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // AsyncTask to send notification
    private class SendNotificationTask extends AsyncTask<String, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(String... params) {
            String petSitterId = params[0];
            String complaintId = params[1];

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(SEND_NOTIFICATION_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user_id", petSitterId);
                jsonParam.put("complaint_id", complaintId);
                jsonParam.put("message", "Your complaint #" + complaintId + " has been reviewed by admin");

                OutputStream os = connection.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.getBoolean("success");
                } else {
                    errorMessage = "HTTP Error: " + responseCode;
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Error sending notification: ", e);
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing resources: ", e);
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Log.e(TAG, "Failed to send notification: " + errorMessage);
            }
        }
    }

    // AsyncTask to update complaint status
    private class UpdateStatusTask extends AsyncTask<String, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(String... params) {
            String complaintId = params[0];
            String newStatus = params[1];

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(UPDATE_STATUS_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String postData = "complaint_id=" + complaintId + "&status=" + newStatus;

                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonObject = new JSONObject(response.toString());
                    return jsonObject.getBoolean("success");
                } else {
                    errorMessage = "HTTP Error: " + responseCode;
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Error updating status: ", e);
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing resources: ", e);
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AdminViewReport.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                fetchComplaints(); // Refresh the list
            } else {
                Toast.makeText(AdminViewReport.this, "Failed to update status: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class RemovalResponse {
        private boolean success;
        private String message;
        private Map<String, Double> refundsAttempted;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Map<String, Double> getRefundsAttempted() { return refundsAttempted; }
        public void setRefundsAttempted(Map<String, Double> refundsAttempted) {
            this.refundsAttempted = refundsAttempted;
        }
    }
    // AsyncTask to remove pet sitter account
    private class RemovePetSitterTask extends AsyncTask<String, Void, RemovalResponse> {
        private String errorMessage = "";
        private int petSitterId;

        @Override
        protected RemovalResponse doInBackground(String... params) {
            petSitterId = Integer.parseInt(params[0]);
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            RemovalResponse response = new RemovalResponse();

            try {
                URL url = new URL(REMOVE_PETSITTER_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "petSitterId=" + petSitterId;
                OutputStream os = connection.getOutputStream();
                os.write(postData.getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder jsonResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonResponse.append(line);
                    }

                    JSONObject jsonObject = new JSONObject(jsonResponse.toString());
                    response.setSuccess(jsonObject.getBoolean("success"));
                    response.setMessage(jsonObject.getString("message"));

                    // Properly parse refunds_attempted
                    if (jsonObject.has("refunds_attempted")) {
                        JSONObject refundsJson = jsonObject.getJSONObject("refunds_attempted");
                        Map<String, Double> refunds = new HashMap<>();
                        Iterator<String> keys = refundsJson.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            try {
                                refunds.put(key, refundsJson.getDouble(key));
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing refund amount for owner " + key, e);
                            }
                        }
                        response.setRefundsAttempted(refunds);
                    }
                } else {
                    errorMessage = "HTTP Error: " + responseCode;
                    response.setSuccess(false);
                    response.setMessage(errorMessage);
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
                response.setSuccess(false);
                response.setMessage(errorMessage);
                Log.e(TAG, "Error removing pet sitter: ", e);
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing resources: ", e);
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(RemovalResponse response) {
            if (response.isSuccess()) {
                removedPetSitters.add(petSitterId);
                saveRemovedSitters();

                // Build refund message
                StringBuilder refundMessage = new StringBuilder();
                refundMessage.append("Pet sitter removed successfully\n");

                if (response.getRefundsAttempted() != null && !response.getRefundsAttempted().isEmpty()) {
                    refundMessage.append("\nRefunds processed:\n");
                    for (Map.Entry<String, Double> entry : response.getRefundsAttempted().entrySet()) {
                        refundMessage.append("Owner ID ")
                                .append(entry.getKey())
                                .append(": $")
                                .append(String.format("%.2f", entry.getValue()))
                                .append("\n");
                    }
                } else {
                    refundMessage.append("\nNo refunds were processed (no eligible bookings)");
                }

                new AlertDialog.Builder(AdminViewReport.this)
                        .setTitle("Success")
                        .setMessage(refundMessage.toString())
                        .setPositiveButton("OK", null)
                        .show();

                fetchComplaints();
            } else {
                new AlertDialog.Builder(AdminViewReport.this)
                        .setTitle("Error")
                        .setMessage("Failed to remove pet sitter: " + response.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
    }

    private void showComplaintsList() {
        textNoBookings.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void showEmptyState(String message) {
        recyclerView.setVisibility(View.GONE);
        textNoBookings.setVisibility(View.VISIBLE);
        textNoBookings.setText(message);
    }

    private void updateComplaintStatus(int complaintId, String newStatus) {
        new UpdateStatusTask().execute(String.valueOf(complaintId), newStatus);
    }

    // Complaint Model Class
    private static class Complaint {
        boolean isRemoved = false;
        int complaintId;
        int bookingId;
        String createdAt;
        String status;
        String description;
        int petOwnerId;
        int petSitterId;
        String fromDate;
        boolean isRefunded;
        String proofImage;

        Complaint(int complaintId, int bookingId, String createdAt, String status,
                  String description, int petOwnerId, int petSitterId, String fromDate,String proofImage) {
            this.complaintId = complaintId;
            this.bookingId = bookingId;
            this.createdAt = createdAt;
            this.status = status;
            this.description = description;
            this.petOwnerId = petOwnerId;
            this.petSitterId = petSitterId;
            this.fromDate = fromDate;
            this.proofImage = proofImage;

            this.isRefunded = false;
        }
    }






    // RecyclerView Adapter
    private class ComplaintsAdapter extends RecyclerView.Adapter<ComplaintsAdapter.ViewHolder> {
        private final ArrayList<Complaint> complaints;

        ComplaintsAdapter(ArrayList<Complaint> complaints) {
            this.complaints = complaints;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.admin_item_report, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Complaint complaint = complaints.get(position);


            holder.buttonRefund.setVisibility(View.GONE);
            holder.buttonRefund.setOnClickListener(null);


            holder.textComplaintId.setText("Complaint #" + complaint.complaintId);
            holder.textBookingId.setText("Booking #" + complaint.bookingId);
            holder.textCreatedAt.setText("Date: " + complaint.createdAt);
            holder.textStatus.setText("Status: " + complaint.status);
            holder.textDescription.setText(complaint.description);
            holder.textSitterId.setText("Sitter #" + complaint.petSitterId);


            // Load proof image if available
            if (complaint.proofImage != null && !complaint.proofImage.isEmpty()) {
                String imageUrl = Constants.BASE_URL + complaint.proofImage;
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.load)
                        .error(R.drawable.error)
                        .into(holder.proofImageView);
            } else {
                holder.proofImageView.setVisibility(View.GONE);
            }
            // Set up the status spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    holder.itemView.getContext(),
                    R.array.complaint_status_options,
                    android.R.layout.simple_spinner_item
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spinnerStatus.setAdapter(adapter);

            // Pre-select spinner value
            int spinnerPosition = adapter.getPosition(complaint.status);
            holder.spinnerStatus.setSelection(spinnerPosition);

            holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                boolean firstCall = true; // To avoid triggering on initial setSelection

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    if (firstCall) {
                        firstCall = false;
                        return;
                    }
                    String selectedStatus = parent.getItemAtPosition(pos).toString();
                    if (!selectedStatus.equals(complaint.status)) {
                        showConfirmStatusChangeDialog(complaint, selectedStatus);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            if (removedPetSitters.contains(complaint.petSitterId)) {
                holder.buttonRemove.setVisibility(View.VISIBLE);
                holder.buttonRemove.setEnabled(false);
                holder.buttonRemove.setText("Removed");
                return;
            }


            // Show Remove Pet Sitter button only if complaint count >= 3 for that pet sitter
            checkComplaintCountAndSetupRemoveButton(complaint, holder);

            if (removedPetSitters.contains(complaint.petSitterId)) {
                holder.buttonRemove.setVisibility(View.VISIBLE);
                holder.buttonRemove.setEnabled(false);
                holder.buttonRemove.setText("Removed");
            } else {
                checkComplaintCountAndSetupRemoveButton(complaint, holder);
            }


            if ("Reviewed".equalsIgnoreCase(complaint.status.trim())) {
                holder.buttonRefund.setVisibility(View.VISIBLE);

                if (complaint.isRefunded) {
                    holder.buttonRefund.setText("Refunded");
                    holder.buttonRefund.setEnabled(false);
                } else {
                    holder.buttonRefund.setText("Refund");
                    holder.buttonRefund.setEnabled(true);
                    holder.buttonRefund.setOnClickListener(v -> {
                        showRefundConfirmationDialog(complaint.bookingId, holder);
                    });
                }
            }
        }

        private void showRefundConfirmationDialog(int bookingId, ViewHolder holder) {
            new AlertDialog.Builder(AdminViewReport.this)
                    .setTitle("Confirm Refund")
                    .setMessage("Are you sure you want to refund this booking?")
                    .setPositiveButton("Refund", (dialog, which) -> {
                        holder.buttonRefund.setEnabled(false); // Disable button during processing
                        processRefund(bookingId, holder);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        private void processRefund(int bookingId, ViewHolder holder) {
            new AsyncTask<Integer, Void, JSONObject>() {

                @Override
                protected void onPreExecute() {
                    holder.buttonRefund.setText("Processing...");
                }
                @Override
                protected JSONObject doInBackground(Integer... params) {
                    int bookingId = params[0];
                    HttpURLConnection connection = null;
                    BufferedReader reader = null;

                    try {
                        URL url = new URL(Constants.BASE_URL + "process_refund.php");
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);

                        String postData = "booking_id=" + bookingId;
                        OutputStream os = connection.getOutputStream();
                        os.write(postData.getBytes("UTF-8"));
                        os.close();

                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            return new JSONObject(response.toString());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing refund", e);
                    } finally {
                        try {
                            if (reader != null) reader.close();
                            if (connection != null) connection.disconnect();
                        } catch (Exception e) {
                            Log.e(TAG, "Error closing resources", e);
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(JSONObject result) {
                    try {
                        if (result != null && result.getBoolean("success")) {
                            // Update the complaint model
                            int position = holder.getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                complaints.get(position).isRefunded = true;
                            }

                            holder.buttonRefund.setText("Refunded");
                            holder.buttonRefund.setEnabled(false);

                            Toast.makeText(AdminViewReport.this,
                                    result.getString("message"),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            String errorMsg = result != null ?
                                    result.getString("message") : "Refund failed";
                            Toast.makeText(AdminViewReport.this,
                                    errorMsg, Toast.LENGTH_LONG).show();
                            holder.buttonRefund.setEnabled(true);
                            holder.buttonRefund.setText("Refund");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing refund response", e);
                        holder.buttonRefund.setEnabled(true);
                        holder.buttonRefund.setText("Refund");
                    }
                }
            }.execute(bookingId);
        }

        @Override
        public int getItemCount() {
            return complaints.size();
        }

        private void showConfirmStatusChangeDialog(Complaint complaint, String newStatus) {
            new AlertDialog.Builder(AdminViewReport.this)
                    .setTitle("Confirm Status Change")
                    .setMessage("Change status to '" + newStatus + "'?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        updateComplaintStatus(complaint.complaintId, newStatus);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Refresh to revert spinner selection
                        notifyDataSetChanged();
                    })
                    .show();
        }

        // Check if this pet sitter has 3+ complaints to show Remove button
        private void checkComplaintCountAndSetupRemoveButton(Complaint complaint, ViewHolder holder) {
            // First check if this pet sitter is already removed
            if (removedPetSitters.contains(complaint.petSitterId)) {
                holder.buttonRemove.setVisibility(View.VISIBLE);
                holder.buttonRemove.setEnabled(false);
                holder.buttonRemove.setText("Removed");
                return;
            }


            // Check if this is the first complaint for this pet sitter in the list
            boolean isFirstComplaintForSitter = true;
            for (Complaint c : complaints) {
                if (c.petSitterId == complaint.petSitterId && c.complaintId < complaint.complaintId) {
                    isFirstComplaintForSitter = false;
                    break;
                }
            }

            if (!isFirstComplaintForSitter) {
                holder.buttonRemove.setVisibility(View.GONE);
                return;
            }

            new AsyncTask<Integer, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Integer... params) {
                    int petSitterId = params[0];
                    // Skip if already removed
                    if (removedPetSitters.contains(petSitterId)) {
                        return false;
                    }

                    HttpURLConnection connection = null;
                    BufferedReader reader = null;
                    try {
                        URL url = new URL(Constants.BASE_URL + "count_complaints.php?userId=" + petSitterId);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(10000);
                        connection.setReadTimeout(10000);

                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            JSONObject json = new JSONObject(response.toString());
                            return json.getInt("count") >= 3;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error counting complaints", e);
                    } finally {
                        try {
                            if (reader != null) reader.close();
                            if (connection != null) connection.disconnect();
                        } catch (Exception ignored) {
                        }
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean canRemove) {
                    if (canRemove && !removedPetSitters.contains(complaint.petSitterId)) {
                        holder.buttonRemove.setVisibility(View.VISIBLE);
                        holder.buttonRemove.setEnabled(true);
                        holder.buttonRemove.setOnClickListener(v -> {
                            new AlertDialog.Builder(AdminViewReport.this)
                                    .setTitle("Remove Pet Sitter")
                                    .setMessage("Are you sure you want to remove this pet sitter?Future bookings for this petsitter will be cancelled")
                                    .setPositiveButton("Remove", (dialog, which) -> {
                                        // Disable button immediately when clicked
                                        holder.buttonRemove.setEnabled(false);
                                        removePetSitterAccount(complaint.petSitterId);
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        });
                    } else {
                        holder.buttonRemove.setVisibility(View.GONE);
                        holder.buttonRemove.setEnabled(false);
                    }
                }
            }.execute(complaint.petSitterId);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textComplaintId, textBookingId, textSitterId, textCreatedAt, textStatus, textDescription;
            Spinner spinnerStatus;
            Button buttonRemove;
            Button buttonRefund;
            ImageView proofImageView;
            ViewHolder(View itemView) {
                super(itemView);
                textComplaintId = itemView.findViewById(R.id.text_complaint_id);
                textBookingId = itemView.findViewById(R.id.text_booking_id);
                textCreatedAt = itemView.findViewById(R.id.text_created_at);
                textStatus = itemView.findViewById(R.id.text_status);
                textDescription = itemView.findViewById(R.id.text_description);
                spinnerStatus = itemView.findViewById(R.id.spinner_status);
                buttonRemove = itemView.findViewById(R.id.button_remove_pet_sitter);
                textSitterId = itemView.findViewById(R.id.text_sitter_id);
                buttonRefund = itemView.findViewById(R.id.button_refund);
                proofImageView = itemView.findViewById(R.id.image_proof);
            }
        }
    }
}
