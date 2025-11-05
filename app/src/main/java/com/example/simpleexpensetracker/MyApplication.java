package com.example.simpleexpensetracker;

import android.app.Application;

import com.example.simpleexpensetracker.ui.theme.ThemeManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applyTheme(this);
    }
}
