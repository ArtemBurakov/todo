package com.example.todo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class InitApplicationTheme extends Application {
    public static void initAppTheme(Context context) {
        String theme_mode = getThemeMode(context);
        switch (theme_mode) {
            case "DARK_MODE":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return;
            case "LIGHT_MODE":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return;
            case "SYSTEM_DEFAULT_MODE":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static String getThemeMode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("THEME", Context.MODE_PRIVATE);
        return sharedPreferences.getString("THEME_MODE", "SYSTEM_DEFAULT_MODE");
    }

    public static void setThemeMode(String theme_mode, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("THEME", MODE_PRIVATE).edit();
        editor.putString("THEME_MODE", theme_mode);
        editor.apply();
    }
}
