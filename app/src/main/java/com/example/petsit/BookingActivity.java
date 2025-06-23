package com.example.petsit;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import android.util.Log;




public class BookingActivity extends AppCompatActivity {


    private static final String TAG = "BookingActivity";

    private ArrayList<Long> bookedDatesMillis = new ArrayList<>();


    private ImageView calendarIcon1, calendarIcon2, clockIcon1, clockIcon2;
    private TextView textFromDate, textToDate, textFromTime, textToTime;

    private Calendar fromDateCalendar = null;
    private Calendar toDateCalendar = null;

    private String fromDateISO = null;
    private String toDateISO = null;

    private String serviceName;
    private int serviceId;
    private String servicePrice;
    private ArrayList<String> selectedPetTypes;

    private ArrayList<Integer> selectedPetIds;
    private ArrayList<String> selectedPetNames;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_OWNER_ID = "owner_id";
    private int petsitterId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_date);
        NavigationUtil.setupBottomNavigation(this);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check for valid session token
        String sessionToken = preferences.getString(KEY_SESSION_TOKEN, null);
        int ownerId = preferences.getInt(KEY_OWNER_ID, -1);
        //Toast.makeText(this, "Retrieved owner ID: " + ownerId, Toast.LENGTH_SHORT).show();


        if (sessionToken == null || ownerId == -1) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Get petsitter ID from intent

        petsitterId = getIntent().getIntExtra("petsitter_id", -1);
        //Toast.makeText(this, "Retrieved sitter ID: " + petsitterId, Toast.LENGTH_SHORT).show();

        if (petsitterId == -1) {
            showError("Invalid pet sitter ID");
            return;
        }



        // Receive data from intent
        serviceId = getIntent().getIntExtra("id", -1);
        serviceName = getIntent().getStringExtra("service_name");
        selectedPetIds = getIntent().getIntegerArrayListExtra("selected_pet_ids");
        selectedPetNames = getIntent().getStringArrayListExtra("selected_pet_names");
        selectedPetTypes = getIntent().getStringArrayListExtra("selected_pet_types");

        if (selectedPetTypes == null) selectedPetTypes = new ArrayList<>();

// Debug log
        //Toast.makeText(this, "Selected pet types: " + selectedPetTypes, Toast.LENGTH_SHORT).show();

        servicePrice = getIntent().getStringExtra("price");

        int totalPetsSelected = getIntent().getIntExtra("total_pets_selected", 0);

// Now you can use totalPetsSelected anywhere you want, e.g. display or calculation
        //Toast.makeText(this, "Total pets selected: " + totalPetsSelected, Toast.LENGTH_SHORT).show();

        if (selectedPetIds == null) selectedPetIds = new ArrayList<>();
        if (selectedPetNames == null) selectedPetNames = new ArrayList<>();

       // Toast.makeText(this, "Selected price(s): " + servicePrice, Toast.LENGTH_SHORT).show();

        // Link views
        calendarIcon1 = findViewById(R.id.img_calendar1);
        calendarIcon2 = findViewById(R.id.img_calendar2);
        clockIcon1 = findViewById(R.id.img_clock1);
        clockIcon2 = findViewById(R.id.img_clock2);
        textFromDate = findViewById(R.id.text_from_date);
        textToDate = findViewById(R.id.text_to_date);
        textFromTime = findViewById(R.id.text_from_time);
        textToTime = findViewById(R.id.text_to_time);

        SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfSend = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


        fetchBookedDates(petsitterId, () -> {
            // After booked dates are fetched, enable calendar icons
            calendarIcon1.setOnClickListener(v -> showDatePicker(true, sdfDisplay, sdfSend));
            calendarIcon2.setOnClickListener(v -> showDatePicker(false, sdfDisplay, sdfSend));
        });


        calendarIcon1.setOnClickListener(v -> showDatePicker(true, sdfDisplay, sdfSend));
        calendarIcon2.setOnClickListener(v -> showDatePicker(false, sdfDisplay, sdfSend));
        clockIcon1.setOnClickListener(v -> showTimePicker(textFromTime));
        clockIcon2.setOnClickListener(v -> showTimePicker(textToTime));



        findViewById(R.id.back_arrow).setOnClickListener(view -> finish());

        Button payNow = findViewById(R.id.button_book_now);
        payNow.setOnClickListener(view -> {
            if (fromDateCalendar == null || toDateCalendar == null
                    || textFromTime.getText().toString().isEmpty()
                    || textToTime.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please select all dates and times", Toast.LENGTH_SHORT).show();
                return;
            }

            long diffMillis = toDateCalendar.getTimeInMillis() - fromDateCalendar.getTimeInMillis();
            long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);
            long leftoverMillis = diffMillis - TimeUnit.DAYS.toMillis(diffDays);
            if (leftoverMillis > 0) {
                diffDays += 1;
            }
            if (diffDays < 1) diffDays = 1;

            Intent intent = new Intent(BookingActivity.this, ConfirmationActivity.class);
           // Toast.makeText(this, "Retrieved sitter ID: " + serviceId, Toast.LENGTH_SHORT).show();
            intent.putExtra("id", serviceId);
            intent.putExtra("service_name", serviceName);
            intent.putIntegerArrayListExtra("selected_pet_ids", selectedPetIds);
           // Toast.makeText(this, "Retrieved sitter ID: " + selectedPetIds, Toast.LENGTH_SHORT).show();
            intent.putStringArrayListExtra("selected_pet_names", selectedPetNames);
            intent.putStringArrayListExtra("selected_pet_types", selectedPetTypes);

            intent.putExtra("from_date", fromDateISO);
            intent.putExtra("to_date", toDateISO);
            intent.putExtra("from_time", textFromTime.getText().toString());
            intent.putExtra("to_time", textToTime.getText().toString());

            intent.putExtra("price", servicePrice);

            intent.putExtra("total_days", diffDays);

            intent.putExtra("total_pets_selected", selectedPetIds.size());
            intent.putExtra("petsitter_id",petsitterId);

            startActivity(intent);
        });
    }

    private void showDatePicker(boolean isFromDate, SimpleDateFormat sdfDisplay, SimpleDateFormat sdfSend) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.DatePickerDialogTheme,
                null, // We'll handle this in init()
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                try {
                    // Get the DatePicker instance
                    DatePicker datePicker = getDatePicker();

                    // Set minimum date to today
                    datePicker.setMinDate(System.currentTimeMillis() - 1000);

                    // Initialize with our custom validation
                    datePicker.init(
                            datePicker.getYear(),
                            datePicker.getMonth(),
                            datePicker.getDayOfMonth(),
                            new DatePicker.OnDateChangedListener() {
                                @Override
                                public void onDateChanged(DatePicker view, int year, int month, int day) {
                                    Calendar selectedDate = Calendar.getInstance();
                                    selectedDate.set(year, month, day);

                                    if (isDateBooked(selectedDate)) {
                                        Toast.makeText(BookingActivity.this, "This date is already booked. Please select another date.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    // Add validation for ToDate not being before FromDate
                                    if (!isFromDate && fromDateCalendar != null && selectedDate.before(fromDateCalendar)) {
                                        Toast.makeText(BookingActivity.this, "End date cannot be before start date", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    // Only handle valid selections
                                    String formattedDisplayDate = sdfDisplay.format(selectedDate.getTime());
                                    String formattedSendDate = sdfSend.format(selectedDate.getTime());

                                    if (isFromDate) {
                                        fromDateCalendar = selectedDate;
                                        textFromDate.setText(formattedDisplayDate);
                                        fromDateISO = formattedSendDate;

                                        // If ToDate is already set and now becomes invalid, clear it
                                        if (toDateCalendar != null && toDateCalendar.before(fromDateCalendar)) {
                                            toDateCalendar = null;
                                            textToDate.setText("");
                                            toDateISO = null;
                                        }
                                    } else {
                                        toDateCalendar = selectedDate;
                                        toDateISO = formattedSendDate;
                                        textToDate.setText(formattedDisplayDate);
                                    }
                                }
                            }
                    );

                } catch (Exception e) {
                    Log.e(TAG, "Error customizing date picker", e);
                }
            }
        };

        datePickerDialog.show();
    }

    // Custom delegate to disable booked dates
    private class CustomDayPickerDelegate {
        private final Object originalDelegate;

        CustomDayPickerDelegate(Object original) {
            this.originalDelegate = original;
        }

        public boolean isDayEnabled(int year, int month, int day) {
            Calendar checkDate = Calendar.getInstance();
            checkDate.set(year, month, day);
            return !isDateBooked(checkDate);
        }

        // Delegate all other methods to the original delegate
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("isDayEnabled")) {
                return isDayEnabled((Integer)args[0], (Integer)args[1], (Integer)args[2]);
            }
            return method.invoke(originalDelegate, args);
        }
    }

    private boolean isDateBooked(Calendar date) {
        // Convert to midnight for accurate comparison
        Calendar checkDate = Calendar.getInstance();
        checkDate.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        checkDate.set(Calendar.MILLISECOND, 0);

        for (Long bookedMillis : bookedDatesMillis) {
            Calendar bookedDate = Calendar.getInstance();
            bookedDate.setTimeInMillis(bookedMillis);
            bookedDate.set(Calendar.HOUR_OF_DAY, 0);
            bookedDate.set(Calendar.MINUTE, 0);
            bookedDate.set(Calendar.SECOND, 0);
            bookedDate.set(Calendar.MILLISECOND, 0);

            if (checkDate.equals(bookedDate)) {
                return true;
            }
        }
        return false;
    }
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
        finish();
    }
    private void showTimePicker(TextView targetTextView) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                    targetTextView.setText(selectedTime);
                }, hour, minute, true);
        timePickerDialog.show();
    }


    private void fetchBookedDates(int petsitterId, Runnable onComplete) {
        new Thread(() -> {
            try {
                String urlString = Constants.BASE_URL + "get_booked_dates.php?petsitter_id=" + petsitterId;
                java.net.URL url = new java.net.URL(urlString);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.io.InputStream inputStream = conn.getInputStream();
                    java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                    String response = s.hasNext() ? s.next() : "";

                    org.json.JSONObject json = new org.json.JSONObject(response);
                    if (json.getString("status").equals("success")) {
                        bookedDatesMillis.clear();

                        org.json.JSONArray bookedDates = json.getJSONArray("booked_dates");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                        for (int i = 0; i < bookedDates.length(); i++) {
                            org.json.JSONObject dateRange = bookedDates.getJSONObject(i);
                            String fromDateStr = dateRange.getString("from");
                            String toDateStr = dateRange.getString("to");

                            Calendar start = Calendar.getInstance();
                            start.setTime(sdf.parse(fromDateStr));

                            Calendar end = Calendar.getInstance();
                            end.setTime(sdf.parse(toDateStr));

                            // Add all dates in the range to bookedDatesMillis
                            while (!start.after(end)) {
                                bookedDatesMillis.add(start.getTimeInMillis());
                                start.add(Calendar.DATE, 1);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching booked dates", e);
            }

            // Run the onComplete runnable on UI thread after fetching
            runOnUiThread(onComplete);
        }).start();
    }

}
