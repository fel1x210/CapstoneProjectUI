package ca.gbc.comp3074.uiprototype;

import android.app.Application;
import android.util.Log;

import com.google.android.libraries.places.api.Places;

import ca.gbc.comp3074.uiprototype.data.QuietSpaceDatabase;
import ca.gbc.comp3074.uiprototype.utils.AppConfig;

public class QuietSpaceApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Room database
        QuietSpaceDatabase.getInstance(this);
        
        // Initialize Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), AppConfig.GOOGLE_PLACES_API_KEY);
            Log.d("QuietSpaceApp", "Google Places API initialized");
        }
    }
}
