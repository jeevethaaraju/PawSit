package com.example.petsit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import android.widget.LinearLayout;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import android.widget.TextView;
import android.widget.ImageView;
import java.util.List;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import java.util.Map;
import java.util.HashMap;
import androidx.appcompat.app.AlertDialog;

public class ViewSitterOrderActivity extends AppCompatActivity {

    private static final String TAG = "ViewSitterOrderActivity";
    private int petSitterId;
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private RequestQueue requestQueue;

    private TextView textNoBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_sitter_order);
        NavigationSitterUtil.setupBottomNavigation(this);

        ImageView back = findViewById(R.id.image_arrow_left);

        back.setOnClickListener(view -> {
            finish();
        });



        ImageView order = findViewById(R.id.ordericon);
        order.setOnClickListener(view -> {

            NavigationSitterUtil.navigateToOrders(ViewSitterOrderActivity.this);

        });
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(orderList, this);
        recyclerView.setAdapter(adapter);

        textNoBookings = findViewById(R.id.text_no_bookings);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Retrieve the petOwner_ID from the intent
        petSitterId = getIntent().getIntExtra("petSitter_ID", -1);

        if (petSitterId == -1) {
            Toast.makeText(this, "Error: Sitter ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch orders from API
        fetchOrders(petSitterId);
    }

    private void fetchOrders(int sitterId) {
        String url = Constants.BASE_URL + "get_orders_sitters.php?sitter_id=" + sitterId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray ordersArray = response.getJSONArray("orders");
                            if (ordersArray.length() == 0) {
                                recyclerView.setVisibility(View.GONE);
                                textNoBookings.setVisibility(View.VISIBLE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                textNoBookings.setVisibility(View.GONE);
                                parseOrders(ordersArray);
                            }
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            textNoBookings.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        recyclerView.setVisibility(View.GONE);
                        textNoBookings.setVisibility(View.VISIBLE);
                        e.printStackTrace();
                    }
                },
                error -> {
                    recyclerView.setVisibility(View.GONE);
                    textNoBookings.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Error fetching orders", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }

    private void parseOrders(JSONArray ordersArray) throws JSONException {
        orderList.clear();

        for (int i = 0; i < ordersArray.length(); i++) {
            JSONObject orderJson = ordersArray.getJSONObject(i);
            Order order = new Order(
                    orderJson.getString("booking_id"),
                    orderJson.getString("service_name"),
                    orderJson.getString("FromDate"),
                    orderJson.getString("ToDate"),
                    orderJson.getString("FromTime"),
                    orderJson.getString("ToTime"),
                    orderJson.getString("Pets"),
                    orderJson.getString("PetTypes"),
                    orderJson.getString("Price"),
                    orderJson.getString("TotalPrice"),
                    orderJson.getString("booking_date"),
                    orderJson.optString("approval", "pending"),
                    orderJson.getString("petOwner_ID") // Add this
            );
            orderList.add(order);
        }

        adapter.notifyDataSetChanged();
    }

    public void updateApprovalStatus(String bookingId, String status) {
        String url = Constants.BASE_URL + "get_orders_sitters.php";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("booking_id", bookingId);
            jsonBody.put("approval_status", status);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, url, jsonBody,
                    response -> {
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                                // Refresh the orders
                                fetchOrders(petSitterId);
                            } else {
                                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show();
                    }
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }

    // Order model class
    public static class Order {
        private String bookingId;
        private String serviceName;
        private String fromDate;
        private String toDate;
        private String fromTime;
        private String toTime;
        private String pets;
        private String petstype;
        private String price;
        private String totalPrice;
        private String bookingDate;
        private String approvalStatus;
        private String petOwnerId;

        public Order(String bookingId, String serviceName, String fromDate, String toDate,
                     String fromTime, String toTime, String pets,String petstype, String price,
                     String totalPrice, String bookingDate, String approvalStatus,String petOwnerId) {
            this.bookingId = bookingId;
            this.serviceName = serviceName;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.fromTime = fromTime;
            this.toTime = toTime;
            this.pets = pets;
            this.petstype = petstype;
            this.price = price;
            this.totalPrice = totalPrice;
            this.bookingDate = bookingDate;
            this.approvalStatus = approvalStatus;
            this.petOwnerId = petOwnerId;
        }

        // Getters
        public String getBookingId() { return bookingId; }
        public String getServiceName() { return serviceName; }
        public String getFromDate() { return fromDate; }
        public String getToDate() { return toDate; }
        public String getFromTime() { return fromTime; }
        public String getToTime() { return toTime; }
        public String getPets() { return pets; }
        public String getPetsType() { return petstype; }
        public String getPrice() { return price; }
        public String getTotalPrice() { return totalPrice; }
        public String getBookingDate() { return bookingDate; }
        public String getApprovalStatus() { return approvalStatus; }

        public String getPetOwnerId() {
            return petOwnerId;
        }
    }

    // OrderAdapter class
    public class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {
        private List<Order> orderList;
        private ViewSitterOrderActivity activity;

        public OrderAdapter(List<Order> orderList, ViewSitterOrderActivity activity) {
            this.orderList = orderList;
            this.activity = activity;
        }

        @Override
        public OrderViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_sitter, parent, false);
            return new OrderViewHolder(view, activity);
        }

        @Override
        public void onBindViewHolder(OrderViewHolder holder, int position) {
            Order order = orderList.get(position);
            holder.orderNumber.setText("Order #" + order.getBookingId());
            holder.placedDate.setText("Placed on " + order.getBookingDate());
            holder.pets.setText("Pets: " + order.getPets());
            holder.petstype.setText("Pets Type: " + order.getPetsType());
            holder.totalAmount.setText("Total:" + order.getTotalPrice());

            holder.textFromDateTime.setText("From: " + order.getFromDate() + ", " + order.getFromTime());
            holder.textToDateTime.setText("To: " + order.getToDate() + ", " + order.getToTime());
            // Update approval status display
            holder.updateApprovalStatusDisplay(order.getApprovalStatus());
        }

        @Override
        public int getItemCount() {
            return orderList.size();
        }
    }

    // OrderViewHolder class
    public class OrderViewHolder extends RecyclerView.ViewHolder {
        public TextView orderNumber;
        public TextView placedDate;
        public TextView pets;
        public TextView petstype;
        public TextView totalAmount;
        public ImageView arrowUp;
        public LinearLayout timelineContainer;
        public Button btnApprove;
        public Button btnReject;
        public Button btnComplete;
        public TextView approvalStatusText;
        public boolean isExpanded = false;
        private ViewSitterOrderActivity activity;
        public TextView textFromDateTime;
        public TextView textToDateTime;

        public OrderViewHolder(android.view.View itemView, ViewSitterOrderActivity activity) {
            super(itemView);
            this.activity = activity;

            orderNumber = itemView.findViewById(R.id.text_order_number);
            placedDate = itemView.findViewById(R.id.text_placed_date);
            pets = itemView.findViewById(R.id.text_pets);
            petstype = itemView.findViewById(R.id.text_pet_type);
            totalAmount = itemView.findViewById(R.id.text_total_amount);
            arrowUp = itemView.findViewById(R.id.arrow_up);
            timelineContainer = itemView.findViewById(R.id.timeline_container);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnComplete = itemView.findViewById(R.id.btn_complete);
            approvalStatusText = itemView.findViewById(R.id.text_approval_status);
            textFromDateTime = itemView.findViewById(R.id.text_from_date_time);
            textToDateTime = itemView.findViewById(R.id.text_to_date_time);

            arrowUp.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                toggleExpansion();
            });

            // Make the whole card clickable
            itemView.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                toggleExpansion();
            });

            btnApprove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Order order = activity.orderList.get(position);
                    activity.updateApprovalStatus(order.getBookingId(), "approved");
                }
            });

            btnComplete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Order order = activity.orderList.get(position);
                    activity.updateApprovalStatus(order.getBookingId(), "completed");
                }
            });
            btnReject.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Order order = activity.orderList.get(position);

                    new AlertDialog.Builder(activity)
                            .setTitle("Confirm Rejection")
                            .setMessage("Reject this order and refund " + order.getTotalPrice() + " to owner?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Pass a callback to handle approval status update AFTER wallet succeeds
                                activity.updateWalletBalance(
                                        order.getPetOwnerId(),
                                        order.getTotalPrice(),
                                        () -> { // This is the new Runnable callback
                                            activity.updateApprovalStatus(order.getBookingId(), "rejected");
                                        }
                                );
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });
        }

        private void toggleExpansion() {
            if (isExpanded) {
                timelineContainer.setVisibility(View.VISIBLE);
                arrowUp.setImageResource(R.drawable.arrow_down);
            } else {
                timelineContainer.setVisibility(View.GONE);
                arrowUp.setImageResource(R.drawable.arrow_upp);
            }
        }



        public void updateApprovalStatusDisplay(String status) {
            // Reset all buttons first
            btnApprove.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.VISIBLE);
            btnComplete.setVisibility(View.VISIBLE);

            switch (status.toLowerCase()) {
                case "approved":
                    approvalStatusText.setText("Status: Approved");
                    approvalStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    btnApprove.setEnabled(false);
                    btnReject.setEnabled(false);
                    btnComplete.setEnabled(true);
                    break;
                case "rejected":
                    approvalStatusText.setText("Status: Rejected");
                    approvalStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    btnApprove.setEnabled(false);
                    btnReject.setEnabled(false);
                    btnComplete.setEnabled(false);
                    break;
                case "completed":
                    approvalStatusText.setText("Status: Completed");
                    approvalStatusText.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
                    btnApprove.setEnabled(false);
                    btnReject.setEnabled(false);
                    btnComplete.setEnabled(false);
                    break;
                case "cancelled":
                    approvalStatusText.setText("Status: Cancelled");
                    approvalStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    // Hide all buttons for cancelled orders
                    btnApprove.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnComplete.setVisibility(View.GONE);
                    break;
                default:  // pending
                    approvalStatusText.setText("Status: Pending");
                    approvalStatusText.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    btnApprove.setEnabled(true);
                    btnReject.setEnabled(true);
                    btnComplete.setEnabled(false);
                    break;
            }
        }
        }
    private void updateWalletBalance(String ownerId, String amount, Runnable onSuccess) {
        String url = Constants.BASE_URL + "update_wallet.php";
        Log.d(TAG, "Attempting wallet update - Owner: " + ownerId + " Amount: " + amount);

        try {
            // Convert amount to proper numeric format
            double numericAmount = Double.parseDouble(amount.replace("RM", "").trim());

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("owner_id", ownerId);
            jsonBody.put("amount", numericAmount); // Send as number, not string

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, url, jsonBody,
                    response -> {
                        try {
                            Log.d(TAG, "Wallet response: " + response.toString());
                            if (response.getString("status").equals("success")) {
                                double newBalance = response.getDouble("new_balance");
                                runOnUiThread(() -> {
                                    Toast.makeText(this,
                                            "Refund successful!",
                                            Toast.LENGTH_LONG).show();
                                    onSuccess.run();
                                });
                            } else {
                                String errorMsg = response.optString("message", "Unknown error");
                                Log.e(TAG, "Server error: " + errorMsg);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parse error", e);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Volley error: " + error.getMessage());
                        if (error.networkResponse != null) {
                            Log.e(TAG, "Error data: " + new String(error.networkResponse.data));
                        }
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
        } catch (Exception e) {
            Log.e(TAG, "Request creation failed", e);
        }
    }

}