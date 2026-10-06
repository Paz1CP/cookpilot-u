package com.cookpilot.university;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public final class CookPilotApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}
