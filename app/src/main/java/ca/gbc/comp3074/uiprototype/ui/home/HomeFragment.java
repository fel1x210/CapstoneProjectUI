package ca.gbc.comp3074.uiprototype.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.data.MapDataManager;
import ca.gbc.comp3074.uiprototype.data.PlaceEntity;
import ca.gbc.comp3074.uiprototype.ui.main.MainActivity;
import ca.gbc.comp3074.uiprototype.ui.details.PlaceDetailsActivity;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private MapDataManager mapDataManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views that exist in the new layout
        EditText editSearch = view.findViewById(R.id.editSearch);
        View bottomSheet = view.findViewById(R.id.bottomSheet);
        View featuredPlaceCard = view.findViewById(R.id.featuredPlaceCard);

        // Initialize the map fragment
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapFragment);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        } catch (Exception e) {
            // Handle map initialization errors gracefully
            Toast.makeText(requireContext(), "Map initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        
        // Initialize map data manager
        mapDataManager = new MapDataManager(requireContext());

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

        if (featuredPlaceCard != null) {
            featuredPlaceCard.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("The Quiet Corner")
                        .setMessage("Opening place details...")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            });
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
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
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
            Toast.makeText(requireContext(), "Location permission is needed to show your current location", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null && mMap != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            
                            // Update map camera
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
                            
                            // Update MapDataManager with current location and fetch Google Places
                            mapDataManager.updateLocation(currentLocation);
                            
                            Toast.makeText(requireContext(), "Loading quiet spaces near you...", Toast.LENGTH_SHORT).show();
                        } else {
                            // Fallback to default location
                            LatLng toronto = new LatLng(43.6532, -79.3832);
                            mapDataManager.updateLocation(toronto);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 12));
                            Toast.makeText(requireContext(), "Using default location (Toronto)", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to default location on failure
                    LatLng toronto = new LatLng(43.6532, -79.3832);
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
}
