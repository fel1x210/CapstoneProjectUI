package ca.gbc.comp3074.uiprototype;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.libraries.places.api.Places;

import java.util.concurrent.Executors;

import ca.gbc.comp3074.uiprototype.data.QuietSpaceDatabase;
import ca.gbc.comp3074.uiprototype.utils.AppConfig;

public class QuietSpaceApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply saved theme (synchronous - needs to be immediate)
        applySavedTheme();

        // Initialize heavy operations on background thread for faster startup
        Executors.newSingleThreadExecutor().execute(() -> {
            // Initialize Room database in background
            QuietSpaceDatabase.getInstance(this);

            // Initialize Google Places API in background
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), AppConfig.GOOGLE_PLACES_API_KEY);
                Log.d("QuietSpaceApp", "Google Places API initialized");
            }
        });
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences("QuietSpacePrefs", MODE_PRIVATE);
        String theme = prefs.getString("theme", "System");

        int mode;
        switch (theme) {
            case "Light":
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case "Dark":
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case "System":
            default:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
