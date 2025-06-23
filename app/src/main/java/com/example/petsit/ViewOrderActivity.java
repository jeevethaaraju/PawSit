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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.ImageView;
import java.util.List;
import android.view.View;
import android.graphics.Color;


public class ViewOrderActivity extends AppCompatActivity {

    private static final String TAG = "ViewOrderActivity";
    private int petOwnerId;
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private RequestQueue requestQueue;
    private TextView textNoBookings;
    public TextView serviceName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_order);

        ImageView imageVector = findViewById(R.id.image_arrow_left);

        imageVector.setOnClickListener(view -> {
            finish();
        });

        NavigationUtil.setupBottomNavigation(this);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(orderList);
        recyclerView.setAdapter(adapter);

        textNoBookings = findViewById(R.id.text_no_bookings);


        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Retrieve the petOwner_ID from the intent
        petOwnerId = getIntent().getIntExtra("owner_id", -1);

        if (petOwnerId == -1) {
            Toast.makeText(this, "Error: Owner ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch orders from API
        fetchOrders(petOwnerId);
    }

    private String formatDateTime(String date, String time) {
        try {
            // Assuming date is in format "YYYY-MM-DD" and time is in "HH:MM:SS"
            String[] dateParts = date.split("-");
            String year = dateParts[0];
            String month = getMonthName(Integer.parseInt(dateParts[1]));
            String day = dateParts[2];

            // Format time (remove seconds if present)
            String formattedTime = time;
            if (time.length() > 5) { // If includes seconds
                formattedTime = time.substring(0, 5); // Take only HH:MM
            }

            return day + " " + month + " " + year + ", " + formattedTime;
        } catch (Exception e) {
            return date + ", " + time; // Fallback if parsing fails
        }
    }

    private String getMonthName(int month) {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthNames[month - 1];
    }
    private void fetchOrders(int ownerId) {
        String url = Constants.BASE_URL + "get_orders.php?owner_id=" + ownerId;

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
                    orderJson.getString("Price"),
                    orderJson.getString("TotalPrice"),
                    orderJson.getString("booking_date"),
                    orderJson.getString("approval")


                    );
            orderList.add(order);
        }

        adapter.notifyDataSetChanged();
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
        private String price;
        private String totalPrice;
        private String bookingDate;
        private String approvalStatus;

        public Order(String bookingId, String serviceName, String fromDate, String toDate,
                     String fromTime, String toTime, String pets, String price,
                     String totalPrice, String bookingDate, String approvalStatus) {
            this.bookingId = bookingId;
            this.serviceName = serviceName;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.fromTime = fromTime;
            this.toTime = toTime;
            this.pets = pets;
            this.price = price;
            this.totalPrice = totalPrice;
            this.bookingDate = bookingDate;
            this.approvalStatus = approvalStatus;
        }

        // Getters
        public String getBookingId() { return bookingId; }
        public String getApproval() { return approvalStatus; }
        public String getServiceName() { return serviceName; }
        public String getFromDate() { return fromDate; }
        public String getToDate() { return toDate; }
        public String getFromTime() { return fromTime; }
        public String getToTime() { return toTime; }
        public String getPets() { return pets; }
        public String getPrice() { return price; }
        public String getTotalPrice() { return totalPrice; }
        public String getBookingDate() { return bookingDate; }
    }

    // OrderAdapter class
    public class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {
        private List<Order> orderList;

        public OrderAdapter(List<Order> orderList) {
            this.orderList = orderList;
        }

        @Override
        public OrderViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(OrderViewHolder holder, int position) {
            Order order = orderList.get(position);
            holder.orderNumber.setText("Order #" + order.getBookingId());
            holder.placedDate.setText("Placed on " + order.getBookingDate());
            holder.pets.setText("Pets: " + order.getPets());
            holder.totalAmount.setText("Total: " + order.getTotalPrice());
            // Set the approval status text
            String approvalStatus = order.getApproval();
            holder.approvestatus.setText("Status: " + approvalStatus);
            holder.serviceName.setText("Service: " + order.getServiceName());

            holder.fromDateTime.setText("From: " + formatDateTime(order.getFromDate(), order.getFromTime()));
            holder.toDateTime.setText("To: " + formatDateTime(order.getToDate(), order.getToTime()));
            // Change the color based on approval status
            if ("approved".equalsIgnoreCase(approvalStatus)) {
                holder.approvestatus.setTextColor(Color.parseColor("#008000")); // Green color for approved
            } else if ("rejected".equalsIgnoreCase(approvalStatus)) {
                holder.approvestatus.setTextColor(Color.parseColor("#FF0000")); // Red color for rejected
            } else if ("completed".equalsIgnoreCase(approvalStatus)) {
                holder.approvestatus.setTextColor(Color.parseColor("#ADD8E6")); // light blue color for rejected
            } else {
                holder.approvestatus.setTextColor(Color.parseColor("#808080")); // Default color for pending or unknown status
            }


        }

        @Override
        public int getItemCount() {
            return orderList.size();
        }
    }

    // OrderViewHolder class
    public class OrderViewHolder extends RecyclerView.ViewHolder {
        public TextView orderNumber;
        public TextView approvestatus;
        public TextView placedDate;
        public TextView pets;
        public TextView totalAmount;
        public ImageView arrowUp;
        public LinearLayout timelineContainer;
        public boolean isExpanded = false;
        public TextView serviceName;
        public TextView fromDateTime;  // Add this
        public TextView toDateTime;

        public OrderViewHolder(android.view.View itemView) {
            super(itemView);
            orderNumber = itemView.findViewById(R.id.text_order_number);
            placedDate = itemView.findViewById(R.id.text_placed_date);
            pets = itemView.findViewById(R.id.text_pets);
            totalAmount = itemView.findViewById(R.id.text_total_amount);
            arrowUp = itemView.findViewById(R.id.arrow_up);
            timelineContainer = itemView.findViewById(R.id.timeline_container);
            approvestatus = itemView.findViewById(R.id.order_status);
            serviceName = itemView.findViewById(R.id.text_service_name);
            fromDateTime = itemView.findViewById(R.id.text_from_date_time);  // Add this
            toDateTime = itemView.findViewById(R.id.text_to_date_time);


            arrowUp.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                toggleExpansion();
            });

            // Optional: Make the whole card clickable
            itemView.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                toggleExpansion();
            });
        }

        private void toggleExpansion() {
            if (isExpanded) {
                timelineContainer.setVisibility(View.VISIBLE);
                arrowUp.setImageResource(R.drawable.arrow_down); // Change to your down arrow drawable
            } else {
                timelineContainer.setVisibility(View.GONE);
                arrowUp.setImageResource(R.drawable.arrow_upp);
            }
        }
    }
}