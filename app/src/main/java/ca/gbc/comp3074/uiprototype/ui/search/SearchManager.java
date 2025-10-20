package ca.gbc.comp3074.uiprototype.ui.search;

import android.content.Context;
import android.util.Log;

import ca.gbc.comp3074.uiprototype.api.GooglePlacesService;
import ca.gbc.comp3074.uiprototype.data.PlaceDataConverter;
import ca.gbc.comp3074.uiprototype.data.PlaceEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles search functionality for quiet spaces using Google Places API
 */
public class SearchManager {
    private static final String TAG = "SearchManager";
    
    private final GooglePlacesService placesService;
    private final Context context;

    public SearchManager(Context context) {
        this.context = context;
        this.placesService = new GooglePlacesService();
    }

    public interface SearchCallback {
        void onSearchResults(List<PlaceEntity> places);
        void onSearchError(String error);
    }

    /**
     * Search for places by text query
     */
    public void searchByText(String query, double latitude, double longitude, SearchCallback callback) {
        // For text search, we'll search across different categories that match the query
        String searchType = determineSearchType(query);
        
        placesService.searchPlacesByType(latitude, longitude, 10000, searchType, new GooglePlacesService.PlacesCallback() {
            @Override
            public void onSuccess(List<GooglePlacesService.GooglePlace> places) {
                List<PlaceEntity> entities = PlaceDataConverter.convertGooglePlacesToEntities(places);
                
                // Filter results by query if needed
                List<PlaceEntity> filteredResults = filterByQuery(entities, query);
                callback.onSearchResults(filteredResults);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Search error: " + error);
                callback.onSearchError(error);
            }
        });
    }

    /**
     * Search for places by category
     */
    public void searchByCategory(String category, double latitude, double longitude, SearchCallback callback) {
        String googlePlaceType = mapCategoryToGoogleType(category);
        
        placesService.searchPlacesByType(latitude, longitude, 10000, googlePlaceType, new GooglePlacesService.PlacesCallback() {
            @Override
            public void onSuccess(List<GooglePlacesService.GooglePlace> places) {
                List<PlaceEntity> entities = PlaceDataConverter.convertGooglePlacesToEntities(places);
                callback.onSearchResults(entities);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Category search error: " + error);
                callback.onSearchError(error);
            }
        });
    }

    /**
     * Get recent searches (this would typically come from local storage)
     */
    public List<String> getRecentSearches() {
        // For now, return some sample recent searches
        List<String> recentSearches = new ArrayList<>();
        recentSearches.add("Quiet cafe");
        recentSearches.add("Library study space");
        recentSearches.add("Park near me");
        recentSearches.add("Meditation center");
        return recentSearches;
    }

    /**
     * Get popular search suggestions
     */
    public List<String> getPopularSearches() {
        List<String> popularSearches = new ArrayList<>();
        popularSearches.add("Coffee shops");
        popularSearches.add("Public libraries");
        popularSearches.add("Quiet parks");
        popularSearches.add("Study spaces");
        popularSearches.add("Museums");
        popularSearches.add("Art galleries");
        return popularSearches;
    }

    /**
     * Determine Google Places type from search query
     */
    private String determineSearchType(String query) {
        query = query.toLowerCase();
        
        if (query.contains("coffee") || query.contains("cafe")) {
            return "cafe";
        } else if (query.contains("library")) {
            return "library";
        } else if (query.contains("park")) {
            return "park";
        } else if (query.contains("museum")) {
            return "museum";
        } else if (query.contains("gallery")) {
            return "art_gallery";
        } else if (query.contains("spa") || query.contains("wellness")) {
            return "spa";
        } else if (query.contains("church") || query.contains("temple")) {
            return "church";
        } else if (query.contains("study") || query.contains("university")) {
            return "university";
        } else {
            // Default to cafe for general searches
            return "cafe";
        }
    }

    /**
     * Map our app categories to Google Places types
     */
    private String mapCategoryToGoogleType(String category) {
        switch (category.toLowerCase()) {
            case "cafes":
            case "cafe":
                return "cafe";
            case "libraries":
            case "library":
                return "library";
            case "parks":
            case "park":
                return "park";
            case "museums":
            case "museum":
                return "museum";
            case "galleries":
            case "gallery":
                return "art_gallery";
            case "wellness":
            case "spa":
                return "spa";
            case "spiritual":
            case "church":
                return "church";
            case "study spaces":
            case "university":
                return "university";
            default:
                return "cafe";
        }
    }

    /**
     * Filter search results by query string
     */
    private List<PlaceEntity> filterByQuery(List<PlaceEntity> places, String query) {
        if (query == null || query.trim().isEmpty()) {
            return places;
        }
        
        List<PlaceEntity> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (PlaceEntity place : places) {
            if (place.name.toLowerCase().contains(lowerQuery) ||
                place.type.toLowerCase().contains(lowerQuery) ||
                place.description.toLowerCase().contains(lowerQuery)) {
                filtered.add(place);
            }
        }
        
        return filtered.isEmpty() ? places : filtered; // Return all if no matches
    }
}
