package com.example.simpleexpensetracker;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private static final String PREFERENCES_FILE_KEY = "com.example.simpleexpensetracker.PREFERENCE_FILE_KEY";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
    }

    public static void setNotificationsEnabled(Context context, boolean isEnabled) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, isEnabled);
        editor.apply();
    }

    public static boolean areNotificationsEnabled(Context context) {
        return getPreferences(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, false);
    }
}
