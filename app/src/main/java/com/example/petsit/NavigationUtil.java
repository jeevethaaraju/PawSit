package com.example.petsit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class NavigationUtil {
    private static final String TAG = "NavigationUtil";
    public static final String PREFS_NAME = "UserPrefs";
    public static final String KEY_OWNER_ID = "owner_id";
    public static final String KEY_SESSION_TOKEN = "session_token";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_PROFILE_IMAGE = "profile_image";

    // Main navigation setup method
    public static void setupBottomNavigation(Activity activity) {
        try {
            ImageView homeBtn = activity.findViewById(R.id.home);
            ImageView ordersBtn = activity.findViewById(R.id.ordericon);
            ImageView mapBtn = activity.findViewById(R.id.mapicon);
            ImageView reportsBtn = activity.findViewById(R.id.reporticon);
            ImageView profileBtn = activity.findViewById(R.id.profilehome);

            if (homeBtn != null) homeBtn.setOnClickListener(v -> navigateToHome(activity));
            if (ordersBtn != null) ordersBtn.setOnClickListener(v -> navigateToOrders(activity));
            if (mapBtn != null) mapBtn.setOnClickListener(v -> navigateToMap(activity));
            if (reportsBtn != null) reportsBtn.setOnClickListener(v -> navigateToReports(activity));
            if (profileBtn != null) profileBtn.setOnClickListener(v -> navigateToProfile(activity));

        } catch (Exception e) {
            Log.e(TAG, "Navigation setup failed", e);
            showErrorToast(activity);
        }
    }

    // Navigation methods for each destination
    public static void navigateToHome(Activity activity) {
        if (isAlreadyInActivity(activity, HomeActivity.class)) return;
        startActivity(activity, HomeActivity.class);
    }

    public static void navigateToOrders(Activity activity) {
        if (!validateSession(activity)) return;
        startActivityWithExtras(activity, ViewOrderActivity.class,
                new String[]{KEY_OWNER_ID, KEY_SESSION_TOKEN});
    }

    public static void navigateToMap(Activity activity) {
        if (!validateSession(activity)) return;
        startActivityWithExtras(activity, MapActivity.class,
                new String[]{KEY_OWNER_ID});
    }

    public static void navigateToReports(Activity activity) {
        if (!validateSession(activity)) return;
        startActivityWithExtras(activity, ReportActivity.class,
                new String[]{KEY_OWNER_ID, KEY_SESSION_TOKEN});
    }

    public static void navigateToProfile(Activity activity) {
        if (!validateSession(activity)) return;
        Intent intent = new Intent(activity, ProfileActivity.class);
        addProfileExtras(activity, intent);
        startActivityWithAnimation(activity, intent);
    }

    // Helper methods
    private static boolean validateSession(Activity activity) {
        SharedPreferences prefs = getSharedPreferences(activity);
        int ownerId = prefs.getInt(KEY_OWNER_ID, -1);
        String token = prefs.getString(KEY_SESSION_TOKEN, null);

        if (ownerId == -1 || token == null) {
            redirectToLogin(activity);
            return false;
        }
        return true;
    }

    private static void startActivity(Activity activity, Class<?> destination) {
        startActivityWithAnimation(activity, new Intent(activity, destination));
    }

    private static void startActivityWithExtras(Activity activity, Class<?> destination, String[] extraKeys) {
        Intent intent = new Intent(activity, destination);
        SharedPreferences prefs = getSharedPreferences(activity);

        for (String key : extraKeys) {
            if (key.equals(KEY_OWNER_ID)) {
                intent.putExtra(key, prefs.getInt(key, -1));
            } else {
                intent.putExtra(key, prefs.getString(key, null));
            }
        }

        startActivityWithAnimation(activity, intent);
    }

    private static void addProfileExtras(Activity activity, Intent intent) {
        SharedPreferences prefs = getSharedPreferences(activity);
        intent.putExtra(KEY_OWNER_ID, prefs.getInt(KEY_OWNER_ID, -1));
        intent.putExtra(KEY_FIRST_NAME, prefs.getString(KEY_FIRST_NAME, ""));
        intent.putExtra(KEY_PROFILE_IMAGE, prefs.getString(KEY_PROFILE_IMAGE, ""));
    }

    private static void startActivityWithAnimation(Activity activity, Intent intent) {
        activity.startActivity(intent);

    }

    private static void redirectToLogin(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static boolean isAlreadyInActivity(Activity activity, Class<?> activityClass) {
        return activity.getClass().equals(activityClass);
    }

    private static void showErrorToast(Context context) {
        Toast.makeText(context, "Navigation error occurred", Toast.LENGTH_SHORT).show();
    }
}