package com.example.simpleexpensetracker.ui.theme;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    private static final String PREFERENCES_FILE_KEY = "com.example.simpleexpensetracker.THEME_PREFS";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static final int LIGHT_MODE = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int DARK_MODE = AppCompatDelegate.MODE_NIGHT_YES;
    public static final int SYSTEM_DEFAULT = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
    }

    public static void setThemeMode(Context context, int mode) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(KEY_THEME_MODE, mode);
        editor.apply();
    }

    public static int getThemeMode(Context context) {
        return getPreferences(context).getInt(KEY_THEME_MODE, SYSTEM_DEFAULT);
    }

    public static void applyTheme(Context context) {
        int themeMode = getThemeMode(context);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    public static void applyAndSaveTheme(Context context, int mode) {
        setThemeMode(context, mode);
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
