package com.example.petsit;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.app.PendingIntent;
import android.media.AudioManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportingGraphActivity extends AppCompatActivity {

    private static final String TAG = "ReportingGraphActivity";
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    // Views
    private PieChart bookingChart;
    private Spinner spinnerYear, spinnerMonth, spinnerStatus;
    private TextView tvPendingCount, tvApprovedCount, tvCancelledCount, tvAppCount;
    private RecyclerView rvBookings;
    private Button btnDownloadReport;
    private BookingAdapter bookingAdapter;

    // Download tracking
    private BroadcastReceiver downloadCompleteReceiver;
    private long currentDownloadId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporting_graph);

        NavigationAdminUtil.setupBottomNavigation(this);
        initializeViews();
        setupRecyclerView();
        setupChart();
        setupSpinners();
        setupDownloadButton();
        loadInitialData();
        createNotificationChannel();
    }

    private void initializeViews() {
        bookingChart = findViewById(R.id.bookingChart);
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvApprovedCount = findViewById(R.id.tvApprovedCount);
        tvAppCount = findViewById(R.id.tvAppCount);
        tvCancelledCount = findViewById(R.id.tvCancelledCount);
        rvBookings = findViewById(R.id.rvBookings);
        btnDownloadReport = findViewById(R.id.btnDownloadReport);
    }

    private void setupRecyclerView() {
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingAdapter = new BookingAdapter(new ArrayList<>());
        rvBookings.setAdapter(bookingAdapter);
    }

    private void setupChart() {
        try {
            bookingChart.setUsePercentValues(true);
            bookingChart.getDescription().setEnabled(false);
            bookingChart.setExtraOffsets(5, 10, 5, 5);
            bookingChart.setDragDecelerationFrictionCoef(0.95f);
            bookingChart.setDrawHoleEnabled(true);
            bookingChart.setHoleColor(Color.WHITE);
            bookingChart.setTransparentCircleColor(Color.WHITE);
            bookingChart.setTransparentCircleAlpha(110);
            bookingChart.setHoleRadius(58f);
            bookingChart.setTransparentCircleRadius(61f);
            bookingChart.setDrawCenterText(true);
            bookingChart.setRotationAngle(0);
            bookingChart.setRotationEnabled(true);
            bookingChart.setHighlightPerTapEnabled(true);

            PieData data = bookingChart.getData();
            if (data != null) {
                data.setValueFormatter(new PercentFormatter(bookingChart));
            }

            Legend l = bookingChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(0f);
            l.setYOffset(0f);
        } catch (Exception e) {
            Log.e(TAG, "Chart setup error", e);
        }
    }

    private void setupSpinners() {
        // Year spinner
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 5; i <= currentYear; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        spinnerYear.setAdapter(yearAdapter);
        spinnerYear.setSelection(years.size() - 1);

        // Month spinner
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Status spinner
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.booking_status_array, android.R.layout.simple_spinner_item);
        spinnerStatus.setAdapter(statusAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateReportData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        spinnerYear.setOnItemSelectedListener(listener);
        spinnerMonth.setOnItemSelectedListener(listener);
        spinnerStatus.setOnItemSelectedListener(listener);
    }

    private void setupDownloadButton() {
        btnDownloadReport.setOnClickListener(v -> {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "BookingReport_" + timeStamp + ".pdf";
            String downloadUrl = buildDownloadUrl();

            if (checkStoragePermission()) {
                checkNotificationPermission(() -> startDownload(downloadUrl, fileName));
            }
        });
    }
    private void checkDownloadStatus(long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Cursor cursor = manager.query(query);

        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                // Download actually succeeded
            } else if (status == DownloadManager.STATUS_FAILED) {
                int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                Log.e(TAG, "Download failed with reason: " + reason);
            }
        }
        cursor.close();
    }
    private void checkNotificationPermission(Runnable onGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Notification Permission Needed")
                            .setMessage("This app needs notification permission to alert you when downloads complete")
                            .setPositiveButton("OK", (dialog, which) -> requestPermissions(
                                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                    NOTIFICATION_PERMISSION_CODE))
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    requestPermissions(
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            NOTIFICATION_PERMISSION_CODE);
                }
            } else {
                onGranted.run();
            }
        } else {
            onGranted.run();
        }
    }

    private boolean checkStoragePermission() {
        // No storage permission needed on Android 10+ for Downloads
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }

        // For older versions, check and request if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
        return false;
    }

    private void startDownload(String url, String fileName) {
        try {
            // Play test sound first (won't block download if it fails)
            try {
                playTestSound();
            } catch (Exception e) {
                Log.e(TAG, "Test sound failed, continuing download", e);
            }

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle("Booking Report Download");
            request.setDescription("PDF report download in progress");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Set destination
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            } else {
                request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, fileName);
            }

            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            currentDownloadId = manager.enqueue(request);

            if (currentDownloadId == -1) {
                Toast.makeText(this, "Download failed to start", Toast.LENGTH_SHORT).show();
                return;
            }

            registerDownloadCompleteReceiver();
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();

        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException", e);
            Toast.makeText(this, "Download started (notifications might not work)", Toast.LENGTH_LONG).show();
            scheduleDownloadStatusCheck();  // Fallback to manual checking
        } catch (Exception e) {
            Log.e(TAG, "Download error", e);
            if (!isDownloadActive(currentDownloadId)) {
                Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
    private boolean isDownloadActive(long downloadId) {
        if (downloadId == -1) return false;

        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        try (Cursor cursor = manager.query(query)) {
            if (cursor != null && cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                return status == DownloadManager.STATUS_PENDING ||
                        status == DownloadManager.STATUS_RUNNING ||
                        status == DownloadManager.STATUS_PAUSED;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to check download status", e);
            return false;
        }
    }
    private void playTestSound() {
        try {
            MediaPlayer testPlayer = MediaPlayer.create(this, R.raw.dogs_barking);
            testPlayer.setOnCompletionListener(mp -> {
                mp.release();
                Log.d(TAG, "Test sound played successfully");
            });
            testPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Test sound failed", e);
            Toast.makeText(this, "Sound file error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            manager.deleteNotificationChannel("download_channel");

            NotificationChannel channel = new NotificationChannel(
                    "download_channel",
                    "Downloads",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Download notifications");
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.dogs_barking);
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            channel.setSound(soundUri, attrs);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300});

            manager.createNotificationChannel(channel);
        }
    }

    private void registerDownloadCompleteReceiver() {
        // Unregister previous receiver if exists
        if (downloadCompleteReceiver != null) {
            try {
                unregisterReceiver(downloadCompleteReceiver);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Receiver not registered");
            }
        }

        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long receivedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (receivedId == currentDownloadId) {
                    checkDownloadStatus(receivedId);
                    playNotificationSound();  // This will always play when download completes
                    triggerSystemNotification();

                    try {
                        unregisterReceiver(this);
                    } catch (IllegalArgumentException e) {
                        Log.d(TAG, "Receiver already unregistered");
                    }
                }
            }
        };

        try {
            // For Android 8.0+ we need to specify the receiver isn't exported
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerReceiver(
                        downloadCompleteReceiver,
                        new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                        Context.RECEIVER_NOT_EXPORTED
                );
            } else {
                registerReceiver(
                        downloadCompleteReceiver,
                        new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Receiver registration failed, but download will continue", e);
            // Even if receiver fails, we'll check status manually later
            scheduleDownloadStatusCheck();
        }
    }
    private void scheduleDownloadStatusCheck() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (currentDownloadId != -1) {
                checkDownloadStatus(currentDownloadId);
                playNotificationSound();  // Manual sound trigger
                triggerSystemNotification();
            }
        }, 5000); // Check after 5 seconds
    }
    private void playNotificationSound() {
        try {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

            // Set volume to max for notification
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxVolume, 0);

            final MediaPlayer player = MediaPlayer.create(this, R.raw.dogs_barking);
            player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);

            // Stop playback after 3 seconds (3000 milliseconds)
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (player != null && player.isPlaying()) {
                    player.stop();
                    player.release();
                    Log.d(TAG, "Sound stopped after delay");
                }
            }, 100); // 3000ms = 3 seconds

            player.setOnCompletionListener(mp -> {
                mp.release();
                Log.d(TAG, "Sound completed naturally");
                // Restore original volume
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, currentVolume, 0);
            });

            player.start();
            Log.d(TAG, "Playing notification sound at max volume");
        } catch (Exception e) {
            Log.e(TAG, "Notification sound error", e);
            triggerSystemNotification(); // Fallback
        }
    }

    private void triggerSystemNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);

        // Create pending intent to open app when notification is tapped
        Intent intent = new Intent(this, ReportingGraphActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, "download_channel")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Download Complete")
                .setContentText("Your PDF is ready")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        manager.notify((int) System.currentTimeMillis(), notification);
    }
    private void showCustomNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);

        Notification notification = new NotificationCompat.Builder(this, "download_channel")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Download Complete")
                .setContentText("Your report is ready")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        manager.notify((int) System.currentTimeMillis(), notification);
    }

    private boolean areNotificationsEnabled() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel("download_channel");
            return channel == null || channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
        }
        return true;
    }

    private void showNotificationSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Notifications Disabled")
                .setMessage("Please enable notifications to receive download alerts")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    } else {
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                    }
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            btnDownloadReport.performClick();
        } else if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            btnDownloadReport.performClick();
        }
    }

    private String buildDownloadUrl() {
        String url = Constants.BASE_URL + "generate_pdf_report.php?year=" + spinnerYear.getSelectedItem().toString();

        if (spinnerMonth.getSelectedItemPosition() > 0) {
            url += "&month=" + spinnerMonth.getSelectedItemPosition();
        }

        if (spinnerStatus.getSelectedItemPosition() > 0) {
            url += "&status=" + spinnerStatus.getSelectedItem().toString().toLowerCase();
        }

        return url;
    }

    private void loadInitialData() {
        updateReportData();
    }

    private void updateReportData() {
        String year = spinnerYear.getSelectedItem().toString();
        String month = spinnerMonth.getSelectedItemPosition() > 0 ?
                String.valueOf(spinnerMonth.getSelectedItemPosition()) : null;
        String status = spinnerStatus.getSelectedItemPosition() > 0 ?
                spinnerStatus.getSelectedItem().toString().toLowerCase() : null;

        new FetchReportDataTask().execute(year, month, status);
    }

    private class FetchReportDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                String urlString = Constants.BASE_URL + "get_booking_report.php?year=" + params[0];
                if (params[1] != null) urlString += "&month=" + params[1];
                if (params[2] != null) urlString += "&status=" + params[2];

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                InputStream inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            } catch (Exception e) {
                Log.e(TAG, "Network error", e);
                return "Error: " + e.getMessage();
            } finally {
                if (connection != null) connection.disconnect();
                if (reader != null) try { reader.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.startsWith("Error:")) {
                showError(result != null ? result : "No data available");
                clearAllData();
                return;
            }

            try {
                JSONObject jsonResponse = new JSONObject(result);

                if (!jsonResponse.has("summary") || !jsonResponse.has("monthlyData") || !jsonResponse.has("recentBookings")) {
                    showError("No data available");
                    clearAllData();
                    return;
                }

                updateSummaryCounts(jsonResponse.getJSONObject("summary"));
                updateChartData(jsonResponse.getJSONArray("monthlyData"));
                updateBookingsList(jsonResponse.getJSONArray("recentBookings"));

            } catch (JSONException e) {
                showError("No bookings for the selected filter");
                clearAllData();
            }
        }
    }

    private void updateSummaryCounts(JSONObject summary) throws JSONException {
        tvPendingCount.setText(String.valueOf(summary.getInt("pending")));
        tvAppCount.setText(String.valueOf(summary.getInt("approved")));
        tvApprovedCount.setText(String.valueOf(summary.getInt("completed")));
        tvCancelledCount.setText(String.valueOf(summary.getInt("cancelled")));
    }

    private void updateChartData(JSONArray monthlyData) throws JSONException {
        bookingChart.clear();

        if (monthlyData == null || monthlyData.length() == 0) {
            bookingChart.setCenterText("No data available");
            bookingChart.invalidate();
            return;
        }

        int pendingTotal = 0;
        int appTotal=0;
        int approvedTotal = 0;
        int cancelledTotal = 0;
        int selectedMonthPos = spinnerMonth.getSelectedItemPosition();
        int selectedMonthNum = selectedMonthPos;

        for (int i = 0; i < monthlyData.length(); i++) {
            JSONObject o = monthlyData.getJSONObject(i);
            if (selectedMonthPos > 0 && o.getInt("month") != selectedMonthNum) {
                continue;
            }
            pendingTotal += o.optInt("pending", 0);
            approvedTotal += o.optInt("completed", 0);
            appTotal += o.optInt("approved", 0);
            cancelledTotal += o.optInt("cancelled", 0);
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (approvedTotal > 0) {
            entries.add(new PieEntry(approvedTotal, "Completed"));
            colors.add(Color.parseColor("#2196F3"));
        }

        if (pendingTotal > 0) {
            entries.add(new PieEntry(pendingTotal, "Pending"));
            colors.add(Color.parseColor("#FFA500"));
        }

        if (cancelledTotal > 0) {
            entries.add(new PieEntry(cancelledTotal, "Cancelled"));
            colors.add(Color.parseColor("#F44336"));
        }
        if (appTotal > 0) {
            entries.add(new PieEntry(appTotal, "Approved"));
            colors.add(Color.parseColor("#219653"));
        }

        if (entries.isEmpty()) {
            bookingChart.setCenterText("No bookings");
            bookingChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(bookingChart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        String centerText = "Bookings\n";
        if (selectedMonthPos > 0) {
            String[] months = getResources().getStringArray(R.array.months_array);
            centerText += months[selectedMonthPos] + "\n";
        }
        centerText += "Total: " + (pendingTotal + approvedTotal + cancelledTotal + appTotal);

        bookingChart.setCenterText(centerText);
        bookingChart.setCenterTextSize(12f);
        bookingChart.setCenterTextColor(Color.BLACK);

        bookingChart.setData(data);
        bookingChart.highlightValues(null);
        bookingChart.invalidate();
        bookingChart.animateY(1000);
    }

    private void updateBookingsList(JSONArray recentBookings) throws JSONException {
        List<Booking> bookings = new ArrayList<>();

        if (recentBookings == null || recentBookings.length() == 0) {
            bookings.add(new Booking(-1, "No bookings available", ""));
        } else {
            for (int i = 0; i < recentBookings.length(); i++) {
                JSONObject booking = recentBookings.getJSONObject(i);
                bookings.add(new Booking(
                        booking.getInt("booking_id"),
                        booking.getString("booking_date"),
                        booking.getString("approval")
                ));
            }
        }
        bookingAdapter.updateData(bookings);
    }

    private void clearAllData() {
        tvPendingCount.setText("0");
        tvApprovedCount.setText("0");
        tvCancelledCount.setText("0");
        tvAppCount.setText("0");
        bookingChart.clear();
        bookingChart.invalidate();
        bookingAdapter.updateData(new ArrayList<>());
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadCompleteReceiver != null) {
            try {
                unregisterReceiver(downloadCompleteReceiver);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Receiver not registered");
            }
        }
    }

    private class Booking {
        private int bookingId;
        private String bookingDate;
        private String status;

        public Booking(int bookingId, String bookingDate, String status) {
            this.bookingId = bookingId;
            this.bookingDate = bookingDate;
            this.status = status;
        }

        public int getBookingId() { return bookingId; }
        public String getBookingDate() { return bookingDate; }
        public String getStatus() { return status; }
    }

    private class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
        private List<Booking> bookings;

        public BookingAdapter(List<Booking> bookings) {
            this.bookings = bookings;
        }

        public void updateData(List<Booking> newBookings) {
            this.bookings = newBookings;
            notifyDataSetChanged();
        }

        @Override
        public BookingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_booking_report, parent, false);
            return new BookingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BookingViewHolder holder, int position) {
            Booking booking = bookings.get(position);
            holder.tvBookingId.setText("Booking ID: " + booking.getBookingId());
            holder.tvBookingDate.setText("Date: " + booking.getBookingDate());
            holder.tvStatus.setText("Status: " + booking.getStatus());
            holder.tvStatus.setTextColor(getStatusColor(booking.getStatus()));
        }

        private int getStatusColor(String status) {
            switch (status.toLowerCase()) {
                case "pending": return Color.parseColor("#FFA500");
                case "approved": return Color.parseColor("#219653");
                case "completed": return Color.parseColor("#2196F3");
                case "cancelled": return Color.parseColor("#F44336");
                default: return Color.BLACK;
            }
        }

        @Override
        public int getItemCount() {
            return bookings.size();
        }

        class BookingViewHolder extends RecyclerView.ViewHolder {
            TextView tvBookingId, tvBookingDate, tvStatus;

            public BookingViewHolder(View itemView) {
                super(itemView);
                tvBookingId = itemView.findViewById(R.id.tvBookingId);
                tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }
}