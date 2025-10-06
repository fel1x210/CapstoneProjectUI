package ca.gbc.comp3074.uiprototype.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapDataManager {

    private final PlaceRepository placeRepository;
    private final MutableLiveData<List<PlaceEntity>> placesLiveData = new MutableLiveData<>();
    private final Map<String, PlaceEntity> markerToPlaceMap = new HashMap<>();
    private GoogleMap googleMap;
    private Observer<List<PlaceEntity>> placesObserver;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public MapDataManager(Context context) {
        placeRepository = new PlaceRepository((android.app.Application) context.getApplicationContext());
    }

    public LiveData<List<PlaceEntity>> getPlaces() {
        return placeRepository.getAllPlaces();
    }

    public void setGoogleMap(GoogleMap map) {
        this.googleMap = map;
        loadPlacesOnMap();
    }

    public void observePlaces(LifecycleOwner lifecycleOwner) {
        // Remove any existing observer to prevent memory leaks
        if (placesObserver != null) {
            placeRepository.getAllPlaces().removeObserver(placesObserver);
            placesObserver = null;
        }

        // Create a new lifecycle-aware observer
        placesObserver = places -> {
            if (places != null && googleMap != null) {
                // Process markers in background to avoid UI blocking
                executorService.execute(() -> {
                    final List<PlaceEntity> placesCopy = new ArrayList<>(places);
                    mainHandler.post(() -> updateMarkersFromPlaces(placesCopy));
                });
            }
        };

        // Observe with lifecycle
        placeRepository.getAllPlaces().observe(lifecycleOwner, placesObserver);
    }

    private void loadPlacesOnMap() {
        if (googleMap == null)
            return;

        // Clear existing markers
        googleMap.clear();
        markerToPlaceMap.clear();

        // Load current places in background
        executorService.execute(() -> {
            final List<PlaceEntity> places = placeRepository.getAllPlacesSync();
            if (places != null && googleMap != null) {
                mainHandler.post(() -> updateMarkersFromPlaces(places));
            }
        });
    }

    private void updateMarkersFromPlaces(List<PlaceEntity> places) {
        if (googleMap == null || places == null)
            return;

        // Process in batches to avoid UI lag
        final int BATCH_SIZE = 10;
        final List<List<PlaceEntity>> batches = new ArrayList<>();

        // Create batches
        for (int i = 0; i < places.size(); i += BATCH_SIZE) {
            batches.add(places.subList(i, Math.min(i + BATCH_SIZE, places.size())));
        }

        // Process each batch with a delay
        for (int i = 0; i < batches.size(); i++) {
            final List<PlaceEntity> batch = batches.get(i);
            mainHandler.postDelayed(() -> addMarkersBatch(batch), i * 100);
        }
    }

    private void addMarkersBatch(List<PlaceEntity> batch) {
        if (googleMap == null)
            return;

        for (PlaceEntity place : batch) {
            LatLng location = new LatLng(place.latitude, place.longitude);
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(place.name)
                    .snippet(place.type + " • " + place.rating + "★"));

            if (marker != null) {
                markerToPlaceMap.put(marker.getId(), place);
            }
        }
    }

    public PlaceEntity getPlaceForMarker(Marker marker) {
        return markerToPlaceMap.get(marker.getId());
    }

    public void addPlace(PlaceEntity place) {
        // This would typically update the database
        // For now, we'll just reload the map with batching
        loadPlacesOnMap();
    }

    public void cleanup() {
        // Clean up resources to avoid memory leaks
        if (placesObserver != null) {
            placeRepository.getAllPlaces().removeObserver(placesObserver);
            placesObserver = null;
        }

        executorService.shutdown();
        googleMap = null;
        markerToPlaceMap.clear();
    }
}
