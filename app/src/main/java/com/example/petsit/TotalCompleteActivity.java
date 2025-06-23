package com.example.petsit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TotalCompleteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PetOwnerAdapter adapter;
    private List<PetOwner> petOwnerList;
    private TextView noPetOwnersText;
    private RequestQueue requestQueue;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_booking_order);
        NavigationAdminUtil.setupBottomNavigation(this);
        recyclerView = findViewById(R.id.recycler_orders);
        noPetOwnersText = findViewById(R.id.text_no_bookings);
        backArrow = findViewById(R.id.image_arrow_left);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        petOwnerList = new ArrayList<>();
        adapter = new PetOwnerAdapter(petOwnerList);
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        backArrow.setOnClickListener(v -> onBackPressed());

        loadPetOwners();
    }

    private void loadPetOwners() {
        String url = Constants.BASE_URL + "view_completed_bookings.php";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        petOwnerList.clear();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);

                            Log.d("JSON_RESPONSE", jsonObject.toString()); // Debug

                            PetOwner petOwner = new PetOwner(
                                    jsonObject.getString("booking_id"),
                                    jsonObject.getString("petOwner_ID"),
                                    jsonObject.getString("petSitter_ID"),
                                    jsonObject.getString("service_name"),
                                    jsonObject.getString("FromDate"),
                                    jsonObject.getString("ToDate"),
                                    jsonObject.getString("booking_date"),
                                    jsonObject.getString("TotalPrice")
                            );

                            petOwnerList.add(petOwner);
                        }

                        adapter.notifyDataSetChanged();

                        if (petOwnerList.isEmpty()) {
                            noPetOwnersText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            noPetOwnersText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                    noPetOwnersText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                });

        requestQueue.add(jsonArrayRequest);
    }

    private class PetOwnerAdapter extends RecyclerView.Adapter<PetOwnerAdapter.PetOwnerViewHolder> {

        private final List<PetOwner> petOwnerList;

        public PetOwnerAdapter(List<PetOwner> petOwnerList) {
            this.petOwnerList = petOwnerList;
        }

        @NonNull
        @Override
        public PetOwnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_booking_order, parent, false);
            return new PetOwnerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PetOwnerViewHolder holder, int position) {
            PetOwner petOwner = petOwnerList.get(position);

            holder.textId.setText("Booking ID: " + petOwner.getId());
            holder.textOwnerId.setText("Pet Owner ID: " + petOwner.getOwnerId());
            holder.textSitterId.setText("Pet Sitter ID: " + petOwner.getSitterId());
            holder.textService.setText("Service Name: " + petOwner.getService());
            holder.textFrom.setText("From Date: " + petOwner.getFrom());
            holder.textTo.setText("To Date: " + petOwner.getTo());
            holder.textBooking.setText("Booking Date: " + petOwner.getBooking());
            holder.textTotal.setText("Total: " + petOwner.getTotal());
        }

        @Override
        public int getItemCount() {
            return petOwnerList.size();
        }

        class PetOwnerViewHolder extends RecyclerView.ViewHolder {
            TextView textId, textOwnerId, textSitterId, textService, textFrom, textTo, textBooking, textTotal;

            public PetOwnerViewHolder(@NonNull View itemView) {
                super(itemView);
                textId = itemView.findViewById(R.id.text_booking_id);
                textOwnerId = itemView.findViewById(R.id.text_petowner_id);
                textSitterId = itemView.findViewById(R.id.text_petsitter_id);
                textService = itemView.findViewById(R.id.text_Service_Name);
                textFrom = itemView.findViewById(R.id.text_from);
                textTo = itemView.findViewById(R.id.text_to);
                textBooking = itemView.findViewById(R.id.booking_at);
                textTotal = itemView.findViewById(R.id.total);
            }
        }
    }

    private static class PetOwner {
        private final String id, ownerId, sitterId, service, from, to, booking, total;

        public PetOwner(String id, String ownerId, String sitterId, String service,
                        String from, String to, String booking, String total) {
            this.id = id;
            this.ownerId = ownerId;
            this.sitterId = sitterId;
            this.service = service;
            this.from = from;
            this.to = to;
            this.booking = booking;
            this.total = total;
        }

        public String getId() { return id; }
        public String getOwnerId() { return ownerId; }
        public String getSitterId() { return sitterId; }
        public String getService() { return service; }
        public String getFrom() { return from; }
        public String getTo() { return to; }
        public String getBooking() { return booking; }
        public String getTotal() { return total; }
    }
}
