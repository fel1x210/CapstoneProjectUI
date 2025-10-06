package ca.gbc.comp3074.uiprototype.ui.home;

import android.Manifest;
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

        View headerContainer = view.findViewById(R.id.headerContainer);
        View searchContainer = view.findViewById(R.id.searchContainer);
        View filterScroll = view.findViewById(R.id.filterScroll);
        CardView mapCard = view.findViewById(R.id.mapCard);
        CardView bottomCard = view.findViewById(R.id.bottomCard);
        MaterialButton buttonProfile = view.findViewById(R.id.buttonProfile);
        MaterialButton buttonAddSpot = view.findViewById(R.id.buttonAddSpot);
        MaterialButton buttonCheckIn = view.findViewById(R.id.buttonCheckIn);
        MaterialButton buttonFavorite = view.findViewById(R.id.buttonFavorite);
        EditText editSearch = view.findViewById(R.id.editSearch);

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

        animateEntrance(headerContainer, searchContainer, filterScroll, mapCard, bottomCard);

        buttonProfile.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToProfile();
            }
        });

        buttonAddSpot.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add a Spot")
                .setMessage("Spot creation will be implemented")
                .setPositiveButton(android.R.string.ok, null)
                .show());

        buttonCheckIn.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Check-in")
                .setMessage("You're checked in at The Urban Reader Café")
                .setPositiveButton(android.R.string.ok, null)
                .show());

        buttonFavorite.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Favorites")
                .setMessage("Added to favorites")
                .setPositiveButton(android.R.string.ok, null)
                .show());

        editSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                editSearch.clearFocus();
            }
        });
    }

    private void animateEntrance(View headerContainer,
            View searchContainer,
            View filterScroll,
            CardView mapCard,
            CardView bottomCard) {
        headerContainer.post(() -> {
            headerContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            searchContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(150)
                    .setDuration(500)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            filterScroll.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(250)
                    .setDuration(500)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            mapCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(300)
                    .setDuration(550)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            bottomCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(450)
                    .setDuration(600)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        });
    }

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
                mMap.setMyLocationEnabled(true);
                getCurrentLocation();
            }

            // Set up map data manager with real data
            mapDataManager.setGoogleMap(mMap);

            // Set default location (Toronto, Canada) with optimized zoom
            LatLng toronto = new LatLng(43.6532, -79.3832);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 12)); // Reduced zoom level
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
                    mMap.setMyLocationEnabled(true);
                    getCurrentLocation();
                }
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
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
                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("My Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    }
                });
    }
}
