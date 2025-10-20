package ca.gbc.comp3074.uiprototype.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ca.gbc.comp3074.uiprototype.utils.AppConfig;

public class GooglePlacesService {
    private static final String TAG = "GooglePlacesService";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place";
    
    private final OkHttpClient httpClient;
    private final Gson gson;

    public GooglePlacesService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public interface PlacesCallback {
        void onSuccess(List<GooglePlace> places);
        void onError(String error);
    }

    /**
     * Search for quiet places near a location
     * Types: library, park, cafe, museum, etc.
     */
    public void searchQuietPlaces(double latitude, double longitude, int radius, PlacesCallback callback) {
        // Search for different types of quiet places
        String[] placeTypes = {"library", "park", "cafe", "museum", "art_gallery", "spa"};
        List<GooglePlace> allPlaces = new ArrayList<>();
        
        for (String type : placeTypes) {
            searchPlacesByType(latitude, longitude, radius, type, new PlacesCallback() {
                @Override
                public void onSuccess(List<GooglePlace> places) {
                    synchronized (allPlaces) {
                        allPlaces.addAll(places);
                        // If we've collected results from all types, return them
                        if (allPlaces.size() > 0) {
                            callback.onSuccess(allPlaces);
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error searching for " + type + ": " + error);
                }
            });
        }
    }

    /**
     * Search for places by specific type
     */
    public void searchPlacesByType(double latitude, double longitude, int radius, String type, PlacesCallback callback) {
        String url = BASE_URL + "/nearbysearch/json?" +
                "location=" + latitude + "," + longitude +
                "&radius=" + radius +
                "&type=" + type +
                "&key=" + AppConfig.GOOGLE_PLACES_API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network error: " + e.getMessage());
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("HTTP error: " + response.code());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    PlacesResponse placesResponse = gson.fromJson(responseBody, PlacesResponse.class);
                    
                    if ("OK".equals(placesResponse.status)) {
                        List<GooglePlace> places = placesResponse.results != null ? placesResponse.results : new ArrayList<>();
                        callback.onSuccess(places);
                    } else {
                        callback.onError("API error: " + placesResponse.status);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse error: " + e.getMessage());
                    callback.onError("Parse error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Get place details by place ID
     */
    public void getPlaceDetails(String placeId, PlaceDetailsCallback callback) {
        String url = BASE_URL + "/details/json?" +
                "place_id=" + placeId +
                "&fields=name,rating,formatted_address,formatted_phone_number,opening_hours,photos,geometry,price_level,reviews" +
                "&key=" + AppConfig.GOOGLE_PLACES_API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("HTTP error: " + response.code());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    PlaceDetailsResponse detailsResponse = gson.fromJson(responseBody, PlaceDetailsResponse.class);
                    
                    if ("OK".equals(detailsResponse.status)) {
                        callback.onSuccess(detailsResponse.result);
                    } else {
                        callback.onError("API error: " + detailsResponse.status);
                    }
                } catch (Exception e) {
                    callback.onError("Parse error: " + e.getMessage());
                }
            }
        });
    }

    public interface PlaceDetailsCallback {
        void onSuccess(GooglePlaceDetails placeDetails);
        void onError(String error);
    }

    // Response models for Google Places API
    public static class PlacesResponse {
        public List<GooglePlace> results;
        public String status;
        @SerializedName("next_page_token")
        public String nextPageToken;
    }

    public static class PlaceDetailsResponse {
        public GooglePlaceDetails result;
        public String status;
    }

    public static class GooglePlace {
        @SerializedName("place_id")
        public String placeId;
        public String name;
        public Geometry geometry;
        public float rating;
        @SerializedName("user_ratings_total")
        public int userRatingsTotal;
        public String vicinity;
        public List<String> types;
        @SerializedName("price_level")
        public Integer priceLevel;
        public List<Photo> photos;
        @SerializedName("opening_hours")
        public OpeningHours openingHours;

        public static class Geometry {
            public Location location;
            
            public static class Location {
                public double lat;
                public double lng;
            }
        }

        public static class Photo {
            @SerializedName("photo_reference")
            public String photoReference;
            public int height;
            public int width;
        }

        public static class OpeningHours {
            @SerializedName("open_now")
            public boolean openNow;
        }
    }

    public static class GooglePlaceDetails {
        @SerializedName("place_id")
        public String placeId;
        public String name;
        @SerializedName("formatted_address")
        public String formattedAddress;
        @SerializedName("formatted_phone_number")
        public String formattedPhoneNumber;
        public float rating;
        @SerializedName("user_ratings_total")
        public int userRatingsTotal;
        @SerializedName("price_level")
        public Integer priceLevel;
        public List<Review> reviews;
        public List<GooglePlace.Photo> photos;
        public GooglePlace.Geometry geometry;
        @SerializedName("opening_hours")
        public DetailedOpeningHours openingHours;

        public static class Review {
            @SerializedName("author_name")
            public String authorName;
            @SerializedName("author_url")
            public String authorUrl;
            public String language;
            @SerializedName("profile_photo_url")
            public String profilePhotoUrl;
            public int rating;
            @SerializedName("relative_time_description")
            public String relativeTimeDescription;
            public String text;
            public long time;
        }

        public static class DetailedOpeningHours {
            @SerializedName("open_now")
            public boolean openNow;
            public List<Period> periods;
            @SerializedName("weekday_text")
            public List<String> weekdayText;

            public static class Period {
                public TimeInfo open;
                public TimeInfo close;

                public static class TimeInfo {
                    public int day;
                    public String time;
                }
            }
        }
    }
}
