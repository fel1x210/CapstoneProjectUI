package ca.gbc.comp3074.uiprototype.data;

import android.util.Log;

import ca.gbc.comp3074.uiprototype.api.GooglePlacesService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts Google Places API data to our local PlaceEntity format
 */
public class PlaceDataConverter {
    private static final String TAG = "PlaceDataConverter";
    
    // Map Google Place types to our quiet space categories
    private static final Map<String, String> TYPE_MAPPING = new HashMap<>();
    
    static {
        TYPE_MAPPING.put("library", "Library");
        TYPE_MAPPING.put("park", "Park");
        TYPE_MAPPING.put("cafe", "Cafe");
        TYPE_MAPPING.put("museum", "Museum");
        TYPE_MAPPING.put("art_gallery", "Gallery");
        TYPE_MAPPING.put("spa", "Wellness");
        TYPE_MAPPING.put("church", "Spiritual");
        TYPE_MAPPING.put("university", "Study Space");
        TYPE_MAPPING.put("book_store", "Bookstore");
    }

    /**
     * Convert Google Places to PlaceEntity list
     */
    public static List<PlaceEntity> convertGooglePlacesToEntities(List<GooglePlacesService.GooglePlace> googlePlaces) {
        List<PlaceEntity> placeEntities = new ArrayList<>();
        
        for (GooglePlacesService.GooglePlace googlePlace : googlePlaces) {
            try {
                PlaceEntity entity = convertSinglePlace(googlePlace);
                if (entity != null) {
                    placeEntities.add(entity);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error converting place: " + googlePlace.name, e);
            }
        }
        
        return placeEntities;
    }

    /**
     * Convert a single Google Place to PlaceEntity
     */
    private static PlaceEntity convertSinglePlace(GooglePlacesService.GooglePlace googlePlace) {
        if (googlePlace.geometry == null || googlePlace.geometry.location == null) {
            return null;
        }

        PlaceEntity entity = new PlaceEntity();
        
        // Basic info
        entity.googlePlaceId = googlePlace.placeId;
        entity.name = googlePlace.name != null ? googlePlace.name : "Unknown Place";
        entity.latitude = googlePlace.geometry.location.lat;
        entity.longitude = googlePlace.geometry.location.lng;
        entity.address = googlePlace.vicinity != null ? googlePlace.vicinity : "";
        
        // Rating
        entity.rating = googlePlace.rating > 0 ? googlePlace.rating : 0.0f;
        entity.reviewCount = googlePlace.userRatingsTotal;
        
        // Determine place type/category
        entity.type = determineQuietSpaceType(googlePlace.types);
        
        // Price level (0-4 scale, convert to $ symbols)
        entity.priceLevel = convertPriceLevel(googlePlace.priceLevel);
        
        // Opening status
        entity.isOpen = googlePlace.openingHours != null ? googlePlace.openingHours.openNow : true;
        
        // Set quiet score based on place type and rating
        entity.quietScore = calculateQuietScore(entity.type, entity.rating);
        
        // Photo reference for later loading
        if (googlePlace.photos != null && !googlePlace.photos.isEmpty()) {
            entity.photoReference = googlePlace.photos.get(0).photoReference;
        }
        
        // Set description based on type
        entity.description = generateDescription(entity.type, entity.rating, entity.reviewCount);
        
        return entity;
    }

    /**
     * Determine the quiet space type from Google Place types
     */
    private static String determineQuietSpaceType(List<String> googleTypes) {
        if (googleTypes == null || googleTypes.isEmpty()) {
            return "Other";
        }
        
        // Check for exact matches first
        for (String googleType : googleTypes) {
            if (TYPE_MAPPING.containsKey(googleType)) {
                return TYPE_MAPPING.get(googleType);
            }
        }
        
        // Check for partial matches or related types
        for (String googleType : googleTypes) {
            if (googleType.contains("library")) return "Library";
            if (googleType.contains("park") || googleType.contains("garden")) return "Park";
            if (googleType.contains("cafe") || googleType.contains("coffee")) return "Cafe";
            if (googleType.contains("museum")) return "Museum";
            if (googleType.contains("gallery")) return "Gallery";
            if (googleType.contains("spa") || googleType.contains("wellness")) return "Wellness";
            if (googleType.contains("church") || googleType.contains("temple")) return "Spiritual";
            if (googleType.contains("university") || googleType.contains("school")) return "Study Space";
            if (googleType.contains("book")) return "Bookstore";
        }
        
        return "Other";
    }

    /**
     * Convert Google price level (0-4) to price symbols
     */
    private static String convertPriceLevel(Integer priceLevel) {
        if (priceLevel == null) return "";
        
        switch (priceLevel) {
            case 0: return "Free";
            case 1: return "$";
            case 2: return "$$";
            case 3: return "$$$";
            case 4: return "$$$$";
            default: return "";
        }
    }

    /**
     * Calculate a quiet score based on place type and rating
     */
    private static float calculateQuietScore(String type, float rating) {
        float baseScore = 3.0f; // Default quiet score
        
        // Adjust base score based on place type
        switch (type) {
            case "Library":
                baseScore = 4.8f;
                break;
            case "Park":
                baseScore = 4.2f;
                break;
            case "Museum":
            case "Gallery":
                baseScore = 4.0f;
                break;
            case "Spiritual":
                baseScore = 4.5f;
                break;
            case "Study Space":
                baseScore = 3.8f;
                break;
            case "Cafe":
                baseScore = 3.0f;
                break;
            case "Wellness":
                baseScore = 4.3f;
                break;
            case "Bookstore":
                baseScore = 3.5f;
                break;
            default:
                baseScore = 3.0f;
        }
        
        // Adjust based on Google rating (higher rated places tend to be better managed)
        if (rating > 0) {
            float ratingFactor = (rating - 2.5f) * 0.2f; // Scale rating impact
            baseScore = Math.max(1.0f, Math.min(5.0f, baseScore + ratingFactor));
        }
        
        return Math.round(baseScore * 10) / 10.0f; // Round to 1 decimal place
    }

    /**
     * Generate a description based on place data
     */
    private static String generateDescription(String type, float rating, int reviewCount) {
        StringBuilder description = new StringBuilder();
        
        switch (type) {
            case "Library":
                description.append("Quiet study space with books and peaceful atmosphere");
                break;
            case "Park":
                description.append("Natural outdoor space perfect for relaxation and quiet activities");
                break;
            case "Cafe":
                description.append("Cozy spot for coffee and quiet conversation");
                break;
            case "Museum":
                description.append("Cultural space with quiet galleries and exhibitions");
                break;
            case "Gallery":
                description.append("Artistic environment perfect for contemplation");
                break;
            case "Wellness":
                description.append("Relaxing space focused on health and tranquility");
                break;
            case "Spiritual":
                description.append("Peaceful place for reflection and meditation");
                break;
            case "Study Space":
                description.append("Dedicated area for learning and concentration");
                break;
            case "Bookstore":
                description.append("Quiet browsing among books and literature");
                break;
            default:
                description.append("A quiet space for relaxation and peace");
        }
        
        if (rating > 0 && reviewCount > 0) {
            description.append(String.format(" • Rated %.1f stars by %d visitors", rating, reviewCount));
        }
        
        return description.toString();
    }

    /**
     * Update PlaceEntity with detailed information from Google Place Details
     */
    public static void updatePlaceWithDetails(PlaceEntity entity, GooglePlacesService.GooglePlaceDetails details) {
        if (details == null) return;
        
        // Update address
        if (details.formattedAddress != null) {
            entity.address = details.formattedAddress;
        }
        
        // Update phone
        if (details.formattedPhoneNumber != null) {
            entity.phoneNumber = details.formattedPhoneNumber;
        }
        
        // Update opening hours
        if (details.openingHours != null) {
            entity.isOpen = details.openingHours.openNow;
            if (details.openingHours.weekdayText != null && !details.openingHours.weekdayText.isEmpty()) {
                entity.openingHours = String.join("\n", details.openingHours.weekdayText);
            }
        }
        
        // Update reviews - get the first few reviews
        if (details.reviews != null && !details.reviews.isEmpty()) {
            StringBuilder reviewsText = new StringBuilder();
            int maxReviews = Math.min(3, details.reviews.size());
            
            for (int i = 0; i < maxReviews; i++) {
                GooglePlacesService.GooglePlaceDetails.Review review = details.reviews.get(i);
                reviewsText.append("★★★★★".substring(0, review.rating))
                          .append(" ")
                          .append(review.authorName)
                          .append(": ")
                          .append(review.text.length() > 100 ? 
                                 review.text.substring(0, 100) + "..." : 
                                 review.text)
                          .append("\n\n");
            }
            
            entity.reviews = reviewsText.toString().trim();
        }
    }
}
