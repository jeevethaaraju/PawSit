package com.example.petsit;

import android.os.Bundle;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PetSittersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PetOwnerAdapter adapter;
    private List<PetOwner> petOwnerList;
    private TextView noPetOwnersText;
    private RequestQueue requestQueue;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_petsitter);
        NavigationAdminUtil.setupBottomNavigation(this);
        // Initialize views
        recyclerView = findViewById(R.id.recycler_orders);
        noPetOwnersText = findViewById(R.id.text_no_petsitter);
        backArrow = findViewById(R.id.image_arrow_left);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        petOwnerList = new ArrayList<>();
        adapter = new PetOwnerAdapter(petOwnerList);
        recyclerView.setAdapter(adapter);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Set click listener for back arrow
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Load pet owners data
        loadPetOwners();
    }

    private void loadPetOwners() {
        String url = Constants.BASE_URL + "view_petsitters.php"; // Replace with your actual URL

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            petOwnerList.clear();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject petOwnerJson = response.getJSONObject(i);

                                PetOwner petOwner = new PetOwner(
                                        petOwnerJson.getString("id"),
                                        petOwnerJson.getString("FirstName"),
                                        petOwnerJson.getString("LastName"),
                                        petOwnerJson.getString("Email"),
                                        petOwnerJson.getString("Phone_Number"),
                                        petOwnerJson.getString("Location"),
                                        petOwnerJson.getString("Gender"),
                                        petOwnerJson.optString("ProfileImage", Constants.BASE_URL + Constants.UPLOADS_DIR + "default_profile.jpg")

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
                            Toast.makeText(PetSittersActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(PetSittersActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                        noPetOwnersText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                }
        );

        requestQueue.add(jsonArrayRequest);
    }

    // Inner Adapter Class
    private class PetOwnerAdapter extends RecyclerView.Adapter<PetOwnerAdapter.PetOwnerViewHolder> {

        private List<PetOwner> petOwnerList;

        public PetOwnerAdapter(List<PetOwner> petOwnerList) {
            this.petOwnerList = petOwnerList;
        }

        @NonNull
        @Override
        public PetOwnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_petsitter_order, parent, false);
            return new PetOwnerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PetOwnerViewHolder holder, int position) {
            PetOwner petOwner = petOwnerList.get(position);

            holder.textId.setText("Pet Owner ID: " + petOwner.getId());
            holder.textFirstName.setText("First Name: " + petOwner.getFirstName());
            holder.textLastName.setText("Last Name: " + petOwner.getLastName());
            holder.textGender.setText("Gender: " + petOwner.getGender());
            holder.textPhone.setText("Phone Number: " + petOwner.getPhoneNumber());
            holder.textEmail.setText("Email: " + petOwner.getEmail());
            holder.textLocation.setText("Location: " + petOwner.getLocation());


            // Load profile image (example using Picasso)
            if (petOwner.getProfileImageUrl() != null && !petOwner.getProfileImageUrl().isEmpty()) {
                Picasso.get()
                        .load(petOwner.getProfileImageUrl())
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(holder.profileImage);
            } else {
                holder.profileImage.setImageResource(R.drawable.default_profile);
            }
        }

        @Override
        public int getItemCount() {
            return petOwnerList.size();
        }

        class PetOwnerViewHolder extends RecyclerView.ViewHolder {
            TextView textId, textFirstName, textLastName, textGender, textPhone, textEmail, textLocation;
            ImageView profileImage;
            public PetOwnerViewHolder(@NonNull View itemView) {
                super(itemView);

                textId = itemView.findViewById(R.id.text_petsitter_id);
                textFirstName = itemView.findViewById(R.id.text_First_Name);
                textLastName = itemView.findViewById(R.id.text_Last_Name);
                textGender = itemView.findViewById(R.id.text_gender);
                textPhone = itemView.findViewById(R.id.text_phone);
                textEmail = itemView.findViewById(R.id.Email);
                textLocation = itemView.findViewById(R.id.Location);
                profileImage = itemView.findViewById(R.id.profile_image);
            }
        }
    }



    // Inner Model Class
    private static class PetOwner {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String location;
        private String gender;
        private String profileImageUrl;

        public PetOwner(String id, String firstName, String lastName, String email,
                        String phoneNumber, String location, String gender,String profileImageUrl) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.location = location;
            this.gender = gender;
            this.profileImageUrl = profileImageUrl;
        }

        // Getters
        public String getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getLocation() { return location; }
        public String getGender() { return gender; }
        public String getProfileImageUrl() {
            return profileImageUrl;
        }
    }
}