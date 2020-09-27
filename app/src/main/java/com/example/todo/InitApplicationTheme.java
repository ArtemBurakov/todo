package com.example.todo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class InitApplicationTheme extends Application {

    public static boolean isNightModeEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("NIGHT_MODE", false);
    }

    public static void setNightMode(boolean isNightModeEnabled, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();
        editor.putBoolean("NIGHT_MODE", isNightModeEnabled);
        editor.apply();
    }
}
