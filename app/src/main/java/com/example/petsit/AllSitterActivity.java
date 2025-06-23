package com.example.petsit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import org.json.*;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class AllSitterActivity extends AppCompatActivity {

    private static final String TAG = "AllSitterActivity";
    private static final String PREFS = "UserPrefs";
    private static final String KEY_TOKEN = "session_token";
    private static final String KEY_OWNER_ID = "owner_id";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;

    private final List<JSONObject> sitterList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_sitter);




        recyclerView = findViewById(R.id.recyclerViewSitters);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new SitterAdapter());

        NavigationUtil.setupBottomNavigation(this);
        findViewById(R.id.backArrow).setOnClickListener(v -> {
            startActivity(new Intent(AllSitterActivity.this, HomeActivity.class));
            overridePendingTransition(0, 0);
        });

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.getString(KEY_TOKEN, null) == null || prefs.getInt(KEY_OWNER_ID, -1) == -1) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        fetchSitters();
    }

    private void fetchSitters() {
        int ownerId = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_OWNER_ID, -1);
        String url = Constants.BASE_URL + "allsitter.php?owner_id=" + ownerId;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                resp -> {
                    try {
                        if ("success".equals(resp.getString("status"))) {
                            JSONArray arr = resp.getJSONArray("sitters");
                            sitterList.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                sitterList.add(arr.getJSONObject(i));
                            }
                            refreshList();
                        } else {
                            showError(resp.optString("message", "Error loading"));
                        }
                    } catch (JSONException e) {
                        showError("Parse error");
                    }
                },
                err -> showError("Network error"));

        Volley.newRequestQueue(this).add(req);
    }

    private void refreshList() {
        progressBar.setVisibility(View.GONE);
        if (sitterList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void showError(String msg) {
        progressBar.setVisibility(View.GONE);
        emptyView.setText(msg);
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private class SitterAdapter extends RecyclerView.Adapter<SitterAdapter.VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup p, int v) {
            View v2 = LayoutInflater.from(p.getContext()).inflate(R.layout.all_sitter_card, p, false);
            return new VH(v2);
        }

        @Override
        public void onBindViewHolder(VH vh, int pos) {
            vh.bind(sitterList.get(pos));
        }

        @Override
        public int getItemCount() {
            return sitterList.size();
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView pic;
            TextView name, loc;
            Button bookBtn;
            JSONObject sitter;

            VH(View v) {
                super(v);
                pic = v.findViewById(R.id.picture);
                name = v.findViewById(R.id.firstName);
                loc = v.findViewById(R.id.location);
                bookBtn = v.findViewById(R.id.bookButton);
            }

            void bind(JSONObject s) {
                sitter = s;
                name.setText(s.optString("FirstName", ""));
                loc.setText(s.optString("distance_text", ""));
                String img = s.optString("ProfileImage", "");
                if (!img.startsWith("http")) img = "http://" + img;
                Picasso.get().load(img).placeholder(R.drawable.paw).error(R.drawable.paw_back).into(pic);

                bookBtn.setOnClickListener(v -> {
                    try {
                        int sitterId = sitter.getInt("id");
                        fetchServiceThenOpen(sitter, sitterId);
                    } catch (JSONException e) {
                        Toast.makeText(v.getContext(), "Error extracting sitter ID", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            private void fetchServiceThenOpen(JSONObject sitter, int sitterId) {
                String url = Constants.BASE_URL + "fetchdetail.php?petsitter_id=" + sitterId;
                Volley.newRequestQueue(itemView.getContext()).add(new JsonObjectRequest(Request.Method.GET, url, null,
                        resp -> {
                            try {
                                if ("success".equals(resp.getString("status"))) {
                                    JSONObject svcObj = resp.optJSONObject("service");
                                    if (svcObj != null) {
                                        // Preserve the original sitter data including phone number
                                        JSONObject mergedSitter = new JSONObject(sitter.toString());

                                        // Update with any new data from the response
                                        if (resp.has("sitter")) {
                                            JSONObject newSitterData = resp.getJSONObject("sitter");
                                            Iterator<String> keys = newSitterData.keys();
                                            while (keys.hasNext()) {
                                                String key = keys.next();
                                                mergedSitter.put(key, newSitterData.get(key));
                                            }
                                        }

                                        // Log the final sitter data
                                        Log.d("AllSitterActivity", "Merged sitter data: " + mergedSitter.toString());

                                        Intent i = new Intent(itemView.getContext(), ViewDetailActivity.class);
                                        i.putExtra("sitter", mergedSitter.toString());
                                        i.putExtra("service", svcObj.toString());
                                        itemView.getContext().startActivity(i);
                                    } else {
                                        Toast.makeText(itemView.getContext(), "No service found", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(itemView.getContext(), resp.optString("message", "No service"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(itemView.getContext(), "Parse error", Toast.LENGTH_SHORT).show();
                            }
                        },
                        err -> Toast.makeText(itemView.getContext(), "Network error", Toast.LENGTH_SHORT).show()
                ));
            }
        }
    }
}
