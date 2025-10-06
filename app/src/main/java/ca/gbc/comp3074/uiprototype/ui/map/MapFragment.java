package ca.gbc.comp3074.uiprototype.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.data.MapDataManager;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private MapDataManager mapDataManager;
    private boolean mapReady = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize data manager
        mapDataManager = new MapDataManager(requireContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {

            Bundle mapOptions = new Bundle();
            mapOptions.putBoolean("MapAttrs_liteMode", false);
            mapFragment.getMapAsync(this);
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;

        // Skip custom styling for now to avoid resource errors
        // Custom styling can be added later with proper resource setup

        // Performance settings - optimized for better performance
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); // Basic map type uses less resources
        mMap.getUiSettings().setZoomControlsEnabled(false); // Disable to reduce lag
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // Disable to reduce lag
        mMap.getUiSettings().setMapToolbarEnabled(false); // Disable unused controls
        mMap.getUiSettings().setRotateGesturesEnabled(false); // Disable unused gestures
        mMap.getUiSettings().setTiltGesturesEnabled(false); // Disable 3D view for better performance
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false); // Disable indoor maps
        mMap.getUiSettings().setCompassEnabled(false); // Disable compass
        mMap.setBuildingsEnabled(false); // Disable 3D buildings
        mMap.setTrafficEnabled(false); // Disable traffic overlay

        // Only call setMyLocationEnabled if we have permission check capability
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(false); // Disable until explicitly enabled later
            } catch (SecurityException e) {
                // Permission was revoked, ignore
            }
        }

        // Set initial camera position
        LatLng toronto = new LatLng(43.6532, -79.3832);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 10));

        // Check and request location permissions
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
                getCurrentLocation();
            } catch (SecurityException e) {
                // Handle the case where permission was revoked after the check
                Toast.makeText(requireContext(), "Location permission not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            requestLocationPermission();
        }

        // Initialize the data manager with this map instance
        mapDataManager.setGoogleMap(mMap);

        // Set up lifecycle-aware observer to prevent memory leaks
        mapDataManager.observePlaces(getViewLifecycleOwner());
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
                if (mMap != null && ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mMap.setMyLocationEnabled(true);
                        getCurrentLocation();
                    } catch (SecurityException e) {
                        Toast.makeText(requireContext(), "Unable to enable location", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null && mMap != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.addMarker(new MarkerOptions()
                                        .position(currentLocation)
                                        .title("My Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            }
                        }
                    });
        } catch (SecurityException e) {
            Toast.makeText(requireContext(), "Location access error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapDataManager != null) {
            mapDataManager.cleanup();
        }
        // Clear references
        mMap = null;
        fusedLocationClient = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause heavy resources when not visible
        if (mMap != null) {
            mMap.setMaxZoomPreference(15f); // Limit zoom level when not in active use
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restore functionality when visible
        if (mMap != null) {
            mMap.setMaxZoomPreference(21.0f); // Reset to maximum zoom level
        }
    }
}
