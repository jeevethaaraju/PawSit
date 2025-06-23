package com.example.petsit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.content.SharedPreferences;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";
    private static final String FETCH_NOTIFICATIONS_URL = Constants.BASE_URL + "fetch_notifications.php";
    private static final String MARK_AS_READ_URL = Constants.BASE_URL + "mark_notification_read.php";

    private RecyclerView recyclerView;
    private TextView textEmpty;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NotificationsAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();


    private int getCurrentUserId() {
        return getIntent().getIntExtra("petSitter_ID", -1);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        fetchNotifications();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_notifications);
        textEmpty = findViewById(R.id.text_empty);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Complaint Notifications");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(notificationList);
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchNotifications();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void fetchNotifications() {
        new FetchNotificationsTask().execute();
    }

    private void markNotificationAsRead(int notificationId) {
        new MarkAsReadTask().execute(String.valueOf(notificationId));
    }

    private class FetchNotificationsTask extends AsyncTask<Void, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                // Get user ID from shared preferences or wherever you store it
                int userId = getCurrentUserId();
                URL url = new URL(FETCH_NOTIFICATIONS_URL + "?user_id=" + userId);
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
                        notificationList.clear();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject n = dataArray.getJSONObject(i);
                            Notification notification = new Notification(
                                    n.getInt("id"),
                                    n.getInt("user_id"),
                                    n.getInt("complaint_id"),
                                    n.getString("message"),
                                    n.getString("created_at"),
                                    n.getInt("is_read") == 1
                            );
                            notificationList.add(notification);
                        }

                        // Sort by date (newest first)
                        Collections.sort(notificationList, (n1, n2) -> n2.createdAt.compareTo(n1.createdAt));
                        return true;
                    } else {
                        errorMessage = jsonObject.getString("message");
                    }
                } else {
                    errorMessage = "HTTP Error: " + responseCode;
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Error fetching notifications: ", e);
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
                if (notificationList.isEmpty()) {
                    showEmptyState("No notifications found");
                } else {
                    showNotificationsList();
                }
            } else {
                showEmptyState("Failed to load notifications: " + errorMessage);
            }
        }
    }

    private class MarkAsReadTask extends AsyncTask<String, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(String... params) {
            String notificationId = params[0];

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(MARK_AS_READ_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String postData = "notification_id=" + notificationId;

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
                Log.e(TAG, "Error marking notification as read: ", e);
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
                Log.e(TAG, "Failed to mark notification as read: " + errorMessage);
            }
        }
    }

    private void showNotificationsList() {
        textEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void showEmptyState(String message) {
        recyclerView.setVisibility(View.GONE);
        textEmpty.setVisibility(View.VISIBLE);
        textEmpty.setText(message);
    }

    // Notification Model Class
    private static class Notification {
        int id;
        int userId;
        int complaintId;
        String message;
        String createdAt;
        boolean isRead;

        Notification(int id, int userId, int complaintId, String message, String createdAt, boolean isRead) {
            this.id = id;
            this.userId = userId;
            this.complaintId = complaintId;
            this.message = message;
            this.createdAt = createdAt;
            this.isRead = isRead;
        }
    }

    // RecyclerView Adapter
    private class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
        private final List<Notification> notifications;

        NotificationsAdapter(List<Notification> notifications) {
            this.notifications = notifications;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Notification notification = notifications.get(position);

            // Highlight unread notifications
            if (!notification.isRead) {
                holder.itemView.setAlpha(1f);
                holder.itemView.setBackgroundResource(R.drawable.notification_unread_bg);
            } else {
                holder.itemView.setAlpha(0.8f);
                holder.itemView.setBackgroundResource(R.drawable.notification_read_bg);
            }

            holder.textMessage.setText(notification.message);
            holder.textTime.setText(formatTime(notification.createdAt));
            holder.textComplaintRef.setText("Complaint ID: #" + notification.complaintId);



             /*   // Navigate to complaint details
                Intent intent = new Intent(NotificationsActivity.this, ComplaintDetailsActivity.class);
                intent.putExtra("complaint_id", notification.complaintId);
                startActivity(intent);*/

        }

        private String formatTime(String createdAt) {
            // Implement your time formatting logic here
            // Example: convert "2023-05-15 14:30:00" to "2h ago"
            return createdAt; // Return formatted time
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textMessage, textTime, textComplaintRef;


            ViewHolder(View itemView) {
                super(itemView);
                textMessage = itemView.findViewById(R.id.text_message);
                textTime = itemView.findViewById(R.id.text_time);
                textComplaintRef = itemView.findViewById(R.id.text_complaint_ref);

            }
        }
    }

    private void updateUnreadCount() {
        int unreadCount = 0;
        for (Notification notification : notificationList) {
            if (!notification.isRead) {
                unreadCount++;
            }
        }

        // Update badge in MainActivity
       // MainActivity.updateNotificationBadge(unreadCount);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchNotifications();
    }
}