package ca.gbc.comp3074.uiprototype.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ca.gbc.comp3074.uiprototype.utils.AppConfig;

public class GoogleDirectionsService {
    private static final String TAG = "GoogleDirectionsService";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/json";

    private final OkHttpClient httpClient;
    private final Gson gson;

    public GoogleDirectionsService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public interface DirectionsCallback {
        void onSuccess(List<Route> routes);

        void onError(String error);
    }

    public void getDirections(double originLat, double originLng, double destLat, double destLng,
            DirectionsCallback callback) {
        String url = BASE_URL + "?" +
                "origin=" + originLat + "," + originLng +
                "&destination=" + destLat + "," + destLng +
                "&alternatives=true" +
                "&mode=walking" + // Default to walking for quiet spaces, or driving? User didn't specify, but
                                  // walking makes sense for "nearby". Let's stick to default (driving) or make it
                                  // configurable. Let's use driving for "fastest" usually implies driving, but
                                  // for campus/quiet spaces walking might be better. Let's use driving as default
                                  // for "fastest direction".
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
                    DirectionsResponse directionsResponse = gson.fromJson(responseBody, DirectionsResponse.class);

                    if ("OK".equals(directionsResponse.status)) {
                        callback.onSuccess(directionsResponse.routes);
                    } else {
                        callback.onError("API error: " + directionsResponse.status);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse error: " + e.getMessage());
                    callback.onError("Parse error: " + e.getMessage());
                }
            }
        });
    }

    public static class DirectionsResponse {
        public List<Route> routes;
        public String status;
    }

    public static class Route {
        public String summary;
        public List<Leg> legs;
        @SerializedName("overview_polyline")
        public Polyline overviewPolyline;
    }

    public static class Leg {
        public Distance distance;
        public Duration duration;
    }

    public static class Distance {
        public String text;
        public int value;
    }

    public static class Duration {
        public String text;
        public int value;
    }

    public static class Polyline {
        public String points;
    }
}
