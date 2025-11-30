package ca.gbc.comp3074.uiprototype.utils;

import ca.gbc.comp3074.uiprototype.BuildConfig;

/**
 * Configuration class for API keys and app settings
 */
public class AppConfig {
    
    // Google Places API Key - Loaded from local.properties via BuildConfig
    public static final String GOOGLE_PLACES_API_KEY = BuildConfig.GOOGLE_PLACES_API_KEY;
    
    // Default locations
    public static final double DEFAULT_LATITUDE = 43.6532;  // Toronto
    public static final double DEFAULT_LONGITUDE = -79.3832;
    
    // Search settings
    public static final int DEFAULT_SEARCH_RADIUS_METERS = 5000; // 5km
    public static final int MAX_SEARCH_RESULTS = 20;
    
    // Map settings
    public static final float DEFAULT_ZOOM_LEVEL = 12.0f;
    public static final float USER_LOCATION_ZOOM_LEVEL = 14.0f;
    
    // Categories for quiet spaces
    public static final String[] QUIET_SPACE_CATEGORIES = {
        "Libraries", "Parks", "Cafes", "Museums", "Galleries", 
        "Wellness", "Spiritual", "Study Spaces"
    };
    
    // Google Places types for quiet spaces
    public static final String[] GOOGLE_PLACE_TYPES = {
        "library", "park", "cafe", "museum", "art_gallery", 
        "spa", "church", "university"
    };
}
