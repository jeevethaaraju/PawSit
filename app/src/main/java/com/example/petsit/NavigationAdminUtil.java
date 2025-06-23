package com.example.petsit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class NavigationAdminUtil {
    private static final String TAG = "NavigationAdminUtil";
    public static final String PREFS_NAME = "UserPrefs";
    public static final String KEY_SITTER_ID = "sitter_id";
    public static final String KEY_SESSION_TOKEN = "session_token";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_PROFILE_IMAGE = "profile_image";

    public static void setupBottomNavigation(Activity activity) {
        try {
            ImageView homeBtn = activity.findViewById(R.id.home);
            ImageView reportBtn = activity.findViewById(R.id.message);
            ImageView graphBtn = activity.findViewById(R.id.graph);
            ImageView profileBtn = activity.findViewById(R.id.profilehome);

            if (homeBtn != null) homeBtn.setOnClickListener(v -> navigateToHome(activity));
            if (reportBtn != null) reportBtn.setOnClickListener(v -> navigateToReport(activity));
            if (graphBtn != null) graphBtn.setOnClickListener(v -> navigateToGraph(activity));
            if (profileBtn != null) profileBtn.setOnClickListener(v -> navigateToProfile(activity));

        } catch (Exception e) {
            Log.e(TAG, "Admin navigation setup failed", e);
            showErrorToast(activity);
        }
    }

    public static void navigateToHome(Activity activity) {
        if (isAlreadyInActivity(activity, AdminHomeActivity.class)) return;
        startActivity(activity, AdminHomeActivity.class);
    }
    public static void navigateToProfile(Activity activity) {
        if (isAlreadyInActivity(activity, AdminHomeActivity.class)) return;
        startActivity(activity, AdminProfile.class);
    }

    public static void navigateToReport(Activity activity) {
        if (isAlreadyInActivity(activity, AdminViewReport.class)) return;
        startActivity(activity, AdminViewReport.class);
    }

    public static void navigateToGraph(Activity activity) {
        if (isAlreadyInActivity(activity, ReportingGraphActivity.class)) return;
        startActivity(activity, ReportingGraphActivity.class);
    }

    // Helper methods
    private static void startActivity(Activity activity, Class<?> destination) {
        Intent intent = new Intent(activity, destination);
        activity.startActivity(intent);
    }

    private static boolean isAlreadyInActivity(Activity activity, Class<?> activityClass) {
        return activity.getClass().equals(activityClass);
    }

    private static void showErrorToast(Context context) {
        Toast.makeText(context, "Navigation error occurred", Toast.LENGTH_SHORT).show();
    }
}
