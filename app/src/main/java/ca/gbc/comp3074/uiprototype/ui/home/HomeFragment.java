package ca.gbc.comp3074.uiprototype.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Arrays;
import java.util.HashMap;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.api.GoogleDirectionsService;
import ca.gbc.comp3074.uiprototype.data.MapDataManager;
import ca.gbc.comp3074.uiprototype.data.PlaceEntity;
import ca.gbc.comp3074.uiprototype.ui.main.MainActivity;
import ca.gbc.comp3074.uiprototype.ui.details.PlaceDetailsActivity;
import ca.gbc.comp3074.uiprototype.ui.map.NearbyLocationAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private MapDataManager mapDataManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private RecyclerView nearbyLocationsRecyclerView;
    private NearbyLocationAdapter nearbyLocationAdapter;
    private HashMap<LatLng, Marker> locationMarkers = new HashMap<>();
    private Marker selectedMarker;

    private GoogleDirectionsService directionsService;
    private List<com.google.android.gms.maps.model.Polyline> currentPolylines = new ArrayList<>();
    private LatLng pendingNavigationTarget;
    private String pendingNavigationName;

    private Marker currentLocationMarker;

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize location request
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds

        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        currentLocation = userLocation;
                        updateCurrentLocationMarker(userLocation);
                    }
                }
            }
        };
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Find views that exist in the new layout
            EditText editSearch = view.findViewById(R.id.editSearch);
            View bottomSheet = view.findViewById(R.id.nearbyLocationsCard);
            nearbyLocationsRecyclerView = view.findViewById(R.id.nearbyLocationsRecyclerView);

            // Set up RecyclerView with horizontal layout
            if (nearbyLocationsRecyclerView != null) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false);
                nearbyLocationsRecyclerView.setLayoutManager(layoutManager);

                // Load sample nearby locations
                loadNearbyLocations();
            }

            // Initialize the map fragment
            try {
                SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.mapFragment);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                } else {
                    Log.e("HomeFragment", "Map fragment is null");
                }
            } catch (Exception e) {
                // Handle map initialization errors gracefully
                Log.e("HomeFragment", "Map initialization failed", e);
                Toast.makeText(requireContext(), "Map initialization failed: " + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }

            // Initialize location client
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

            // Initialize map data manager
            try {
                mapDataManager = new MapDataManager(requireContext());
            } catch (Exception e) {
                Log.e("HomeFragment", "MapDataManager initialization failed", e);
                Toast.makeText(requireContext(), "Failed to initialize map data: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }

            // Set up interactions
            if (editSearch != null) {
                editSearch.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus && getActivity() instanceof MainActivity) {
                        // Navigate to search when search bar is clicked
                        ((MainActivity) getActivity()).navigateToSearch();
                        editSearch.clearFocus();
                    }
                });
            }

            // Set up My Location button
            View btnMyLocation = view.findViewById(R.id.btnMyLocation);
            if (btnMyLocation != null) {
                btnMyLocation.setOnClickListener(v -> {
                    if (currentLocation != null && mMap != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        Toast.makeText(requireContext(), "Centering on your location", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Location not available yet", Toast.LENGTH_SHORT).show();
                        getCurrentLocationAndLoadPlaces(); // Try to get location again
                    }
                });
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error in onViewCreated", e);
            Toast.makeText(requireContext(), "Error loading home screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadNearbyLocations() {
        // Initialize adapter with click listener
        nearbyLocationAdapter = new NearbyLocationAdapter(location -> {
            // When a location is clicked, move camera to that location and highlight marker
            if (mMap != null) {
                // Reset previous selected marker to red
                if (selectedMarker != null) {
                    selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }

                // Find and highlight the marker for this location
                Marker marker = locationMarkers.get(location.getPosition());
                if (marker != null) {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    selectedMarker = marker;
                }

                // Animate camera to location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location.getPosition(), 15));
            }
            Toast.makeText(requireContext(), "Selected: " + location.getName(), Toast.LENGTH_SHORT).show();
        });

        nearbyLocationsRecyclerView.setAdapter(nearbyLocationAdapter);

        // Load real places from Google Places API via MapDataManager
        loadRealNearbyPlaces();
    }

    private void loadRealNearbyPlaces() {
        try {
            if (mapDataManager == null) {
                Log.e("HomeFragment", "MapDataManager is null, cannot load places");
                // Show fallback sample data
                showFallbackLocations();
                return;
            }

            // Observe real-time places data from Google Places API
            mapDataManager.getPlaces().observe(getViewLifecycleOwner(), places -> {
                try {
                    if (places != null && !places.isEmpty()) {
                        List<NearbyLocationAdapter.NearbyLocation> locations = new ArrayList<>();

                        // Convert PlaceEntity to NearbyLocation
                        for (PlaceEntity place : places) {
                            if (place.latitude != 0 && place.longitude != 0) {
                                LatLng position = new LatLng(place.latitude, place.longitude);

                                // Calculate distance from current location
                                double distance = calculateDistance(currentLocation, position);

                                // Get rating (use existing value or 0 if not set)
                                double rating = place.rating > 0 ? place.rating : 0.0;

                                locations.add(new NearbyLocationAdapter.NearbyLocation(
                                        place.name,
                                        place.type != null ? place.type : "Place",
                                        distance,
                                        (float) rating,
                                        position));
                            }
                        }

                        // Sort by distance (closest first)
                        locations.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));

                        // Limit to top 10 closest places
                        if (locations.size() > 10) {
                            locations = locations.subList(0, 10);
                        }

                        // Update adapter
                        if (nearbyLocationAdapter != null) {
                            nearbyLocationAdapter.setLocations(locations);
                        }

                        // Add markers to map if map is ready
                        if (mMap != null) {
                            addMarkersForLocations(locations);
                        }

                        Log.d("HomeFragment", "Loaded " + locations.size() + " real nearby places");
                    } else {
                        // No places found from API, show fallback
                        Log.d("HomeFragment", "No places from API, showing fallback data");
                        showFallbackLocations();
                    }
                } catch (Exception e) {
                    Log.e("HomeFragment", "Error processing places data", e);
                    showFallbackLocations();
                }
            });
        } catch (Exception e) {
            Log.e("HomeFragment", "Error in loadRealNearbyPlaces", e);
            showFallbackLocations();
        }
    }

    /**
     * Show sample fallback locations if API data is not available
     */
    private void showFallbackLocations() {
        List<NearbyLocationAdapter.NearbyLocation> locations = new ArrayList<>();

        // Sample nearby locations in Toronto area
        locations.add(new NearbyLocationAdapter.NearbyLocation(
                "Toronto Reference Library",
                "Library",
                0.8,
                4.5f,
                new LatLng(43.6719, -79.3863)));

        locations.add(new NearbyLocationAdapter.NearbyLocation(
                "The Quiet Bean Cafe",
                "Cafe",
                1.2,
                4.3f,
                new LatLng(43.6680, -79.3900)));

        locations.add(new NearbyLocationAdapter.NearbyLocation(
                "Central Study Commons",
                "Co-working",
                0.5,
                4.7f,
                new LatLng(43.6650, -79.3800)));

        locations.add(new NearbyLocationAdapter.NearbyLocation(
                "Robarts Library",
                "Library",
                2.1,
                4.4f,
                new LatLng(43.6645, -79.3995)));

        locations.add(new NearbyLocationAdapter.NearbyLocation(
                "Zen Reading Room",
                "Study Space",
                1.5,
                4.6f,
                new LatLng(43.6700, -79.3950)));

        // Update adapter
        if (nearbyLocationAdapter != null) {
            nearbyLocationAdapter.setLocations(locations);
        }

        // Add markers to map if map is ready
        if (mMap != null) {
            addMarkersForLocations(locations);
        }

        Log.d("HomeFragment", "Showing " + locations.size() + " fallback locations");
    }

    private LatLng currentLocation = new LatLng(43.6532, -79.3832); // Will be updated with real location

    private double calculateDistance(LatLng from, LatLng to) {
        // Calculate distance in kilometers using Haversine formula
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(to.latitude - from.latitude);
        double dLng = Math.toRadians(to.longitude - from.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(from.latitude)) * Math.cos(Math.toRadians(to.latitude)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private void addMarkersForLocations(List<NearbyLocationAdapter.NearbyLocation> locations) {
        // Clear existing markers from map
        for (Marker marker : locationMarkers.values()) {
            marker.remove();
        }
        locationMarkers.clear();

        // Add markers for each location
        for (NearbyLocationAdapter.NearbyLocation location : locations) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location.getPosition())
                    .title(location.getName())
                    .snippet(location.getType() + " • " + location.getDistance() + " km")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            Marker marker = mMap.addMarker(markerOptions);
            locationMarkers.put(location.getPosition(), marker);
        }
    }

    // Removed animation method since we have a simpler layout now

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mMap = googleMap;

            // Optimize map performance
            mMap.getUiSettings().setZoomControlsEnabled(false); // Disable to reduce lag
            mMap.getUiSettings().setMyLocationButtonEnabled(false); // Disable to reduce lag
            mMap.getUiSettings().setCompassEnabled(false); // Disable compass
            mMap.getUiSettings().setMapToolbarEnabled(false); // Disable toolbar
            mMap.getUiSettings().setRotateGesturesEnabled(false); // Disable rotation
            mMap.getUiSettings().setTiltGesturesEnabled(false); // Disable tilt

            // Check and request location permissions (simplified)
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                getCurrentLocationAndLoadPlaces();
            } else {
                // Use default location and load places
                LatLng toronto = new LatLng(43.6532, -79.3832);
                mapDataManager.updateLocation(toronto);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 12));
            }

            // Set up map data manager with real Google Places data
            mapDataManager.setGoogleMap(mMap);

            // Start observing places data
            mapDataManager.observePlaces(getViewLifecycleOwner());

            // Set up marker click listener
            mMap.setOnMarkerClickListener(marker -> {
                PlaceEntity place = mapDataManager.getPlaceForMarker(marker);
                if (place != null) {
                    openPlaceDetails(place);
                    return true; // Consume the event
                }
                return false; // Let default behavior happen
            });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Map setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(requireContext(), "Location permission is needed to show your current location",
                    Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(requireActivity(),
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    enableMyLocation();
                    getCurrentLocationAndLoadPlaces();
                }
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                // Use default location
                LatLng toronto = new LatLng(43.6532, -79.3832);
                mapDataManager.updateLocation(toronto);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 12));
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private void enableMyLocation() {
        if (mMap != null && ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void getCurrentLocationAndLoadPlaces() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null && mMap != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            // Update current location for distance calculations
                            currentLocation = userLocation;

                            // Update current location marker
                            updateCurrentLocationMarker(userLocation);

                            // Check for pending navigation
                            if (pendingNavigationTarget != null) {
                                startNavigation(currentLocation, pendingNavigationTarget, pendingNavigationName);
                                pendingNavigationTarget = null;
                            }

                            // Update map camera
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14));

                            // Update MapDataManager with current location and fetch Google Places
                            mapDataManager.updateLocation(userLocation);

                            Toast.makeText(requireContext(), "Loading quiet spaces near you...", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            // Fallback to default location
                            LatLng toronto = new LatLng(43.6532, -79.3832);
                            currentLocation = toronto;
                            mapDataManager.updateLocation(toronto);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 12));
                            Toast.makeText(requireContext(), "Using default location (Toronto)", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to default location on failure
                    LatLng toronto = new LatLng(43.6532, -79.3832);
                    currentLocation = toronto;
                    mapDataManager.updateLocation(toronto);
                    if (mMap != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 12));
                    }
                    Toast.makeText(requireContext(), "Using default location", Toast.LENGTH_SHORT).show();
                });
    }

    private void openPlaceDetails(PlaceEntity place) {
        Intent intent = new Intent(requireContext(), PlaceDetailsActivity.class);
        if (place.googlePlaceId != null && !place.googlePlaceId.isEmpty()) {
            intent.putExtra(PlaceDetailsActivity.EXTRA_GOOGLE_PLACE_ID, place.googlePlaceId);
        } else {
            intent.putExtra(PlaceDetailsActivity.EXTRA_PLACE_ID, place.id);
        }
        startActivity(intent);
    }

    public void prepareNavigation(double lat, double lng, String name) {
        pendingNavigationTarget = new LatLng(lat, lng);
        pendingNavigationName = name;

        if (mMap != null && currentLocation != null) {
            startNavigation(currentLocation, pendingNavigationTarget, pendingNavigationName);
            pendingNavigationTarget = null;
        } else {
            Toast.makeText(requireContext(), "Waiting for location to start navigation...", Toast.LENGTH_SHORT).show();
        }
    }

    private void startNavigation(LatLng origin, LatLng dest, String destName) {
        // Clear existing polylines
        for (com.google.android.gms.maps.model.Polyline polyline : currentPolylines) {
            polyline.remove();
        }
        currentPolylines.clear();

        if (directionsService == null) {
            directionsService = new GoogleDirectionsService();
        }

        Toast.makeText(requireContext(), "Calculating route to " + destName + "...", Toast.LENGTH_SHORT).show();

        directionsService.getDirections(origin.latitude, origin.longitude, dest.latitude, dest.longitude,
                new GoogleDirectionsService.DirectionsCallback() {
                    @Override
                    public void onSuccess(List<GoogleDirectionsService.Route> routes) {
                        if (getActivity() == null)
                            return;
                        getActivity().runOnUiThread(() -> {
                            if (routes.isEmpty()) {
                                Toast.makeText(requireContext(), "No routes found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Sort routes by duration (fastest first)
                            routes.sort((r1, r2) -> {
                                int d1 = 0, d2 = 0;
                                if (!r1.legs.isEmpty())
                                    d1 = r1.legs.get(0).duration.value;
                                if (!r2.legs.isEmpty())
                                    d2 = r2.legs.get(0).duration.value;
                                return Integer.compare(d1, d2);
                            });

                            // Draw routes
                            // Draw alternative routes first (so they are behind)
                            for (int i = routes.size() - 1; i >= 0; i--) {
                                GoogleDirectionsService.Route route = routes.get(i);
                                boolean isFastest = (i == 0);

                                List<LatLng> points = com.google.maps.android.PolyUtil
                                        .decode(route.overviewPolyline.points);

                                com.google.android.gms.maps.model.PolylineOptions polylineOptions = new com.google.android.gms.maps.model.PolylineOptions()
                                        .addAll(points)
                                        .width(isFastest ? 15 : 10)
                                        .color(isFastest
                                                ? ContextCompat.getColor(requireContext(), R.color.quiet_space_primary)
                                                : ContextCompat.getColor(requireContext(),
                                                        R.color.quiet_space_text_secondary))
                                        .geodesic(true);

                                if (!isFastest) {
                                    polylineOptions
                                            .pattern(Arrays.asList(new com.google.android.gms.maps.model.Dash(30),
                                                    new com.google.android.gms.maps.model.Gap(20)));
                                }

                                com.google.android.gms.maps.model.Polyline polyline = mMap.addPolyline(polylineOptions);
                                polyline.setTag(isFastest ? "fastest" : "alternative");
                                polyline.setClickable(true);
                                currentPolylines.add(polyline);

                                if (isFastest) {
                                    // Show duration info
                                    String duration = route.legs.get(0).duration.text;
                                    Toast.makeText(requireContext(), "Fastest route: " + duration, Toast.LENGTH_LONG)
                                            .show();
                                }
                            }

                            // Zoom to fit route
                            com.google.android.gms.maps.model.LatLngBounds.Builder builder = new com.google.android.gms.maps.model.LatLngBounds.Builder();
                            builder.include(origin);
                            builder.include(dest);
                            for (com.google.android.gms.maps.model.Polyline polyline : currentPolylines) {
                                for (LatLng point : polyline.getPoints()) {
                                    builder.include(point);
                                }
                            }
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
                        });
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() == null)
                            return;
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Error fetching directions: " + error, Toast.LENGTH_SHORT)
                                    .show();
                        });
                    }
                });
    }

    private void updateCurrentLocationMarker(LatLng location) {
        if (mMap == null)
            return;

        if (currentLocationMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location)
                    .title("You are here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .zIndex(100.0f); // Ensure it's above other markers
            currentLocationMarker = mMap.addMarker(markerOptions);
        } else {
            currentLocationMarker.setPosition(location);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                    android.os.Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
