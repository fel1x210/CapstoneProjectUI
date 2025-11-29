package ca.gbc.comp3074.uiprototype.ui.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.ui.community.CommunityFragment;
import ca.gbc.comp3074.uiprototype.ui.favorites.FavoritesFragment;
import ca.gbc.comp3074.uiprototype.ui.home.HomeFragment;
import ca.gbc.comp3074.uiprototype.ui.profile.ProfileFragment;
import ca.gbc.comp3074.uiprototype.ui.search.SearchFragment;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_NAVIGATE_LAT = "navigate_lat";
    public static final String EXTRA_NAVIGATE_LNG = "navigate_lng";
    public static final String EXTRA_NAVIGATE_NAME = "navigate_name";

    private BottomNavigationView bottomNavigationView;

    // Lazy initialize fragments to reduce memory usage and startup time
    private Fragment homeFragment;
    private Fragment searchFragment;
    private Fragment communityFragment;
    private Fragment favoritesFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable high refresh rate for smoother UI (120Hz support)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getAttributes().preferredDisplayModeId = getDisplay().getMode().getModeId();
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Optimize window for performance
        getWindow().setBackgroundDrawable(null); // Remove window background overdraw

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }
                switchFragment(homeFragment);
                return true;
            } else if (itemId == R.id.navigation_search) {
                if (searchFragment == null) {
                    searchFragment = new SearchFragment();
                }
                switchFragment(searchFragment);
                return true;
            } else if (itemId == R.id.navigation_community) {
                if (communityFragment == null) {
                    communityFragment = CommunityFragment.Companion.newInstance();
                }
                switchFragment(communityFragment);
                return true;
            } else if (itemId == R.id.navigation_favorites) {
                if (favoritesFragment == null) {
                    favoritesFragment = new FavoritesFragment();
                }
                switchFragment(favoritesFragment);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                if (profileFragment == null) {
                    profileFragment = new ProfileFragment();
                }
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }

        // Sync favorites from cloud on startup
        checkAuthAndSyncFavorites();

        // Check for navigation intent on startup
        handleNavigationIntent(getIntent());
    }

    private void checkAuthAndSyncFavorites() {
        ca.gbc.comp3074.uiprototype.data.supabase.SupabaseAuthHelper authHelper = new ca.gbc.comp3074.uiprototype.data.supabase.SupabaseAuthHelper();

        // Use waitForSessionAndCheck to handle async session restoration on startup
        authHelper.waitForSessionAndCheck(isLoggedIn -> {
            if (isLoggedIn) {
                ca.gbc.comp3074.uiprototype.data.PlaceRepository repository = new ca.gbc.comp3074.uiprototype.data.PlaceRepository(
                        getApplication());
                repository.syncFavoritesFromCloud();
                android.util.Log.d("MainActivity", "Triggered favorites sync on startup");
            } else {
                android.util.Log.d("MainActivity", "User not logged in, skipping favorites sync");
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    private void switchFragment(Fragment fragment) {
        if (fragment == activeFragment)
            return; // Don't reload same fragment

        // Use show/hide instead of replace for better performance
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Hide current active fragment
        if (activeFragment != null) {
            transaction.hide(activeFragment);
        }

        // Show or add the new fragment
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.fragment_container, fragment);
        }

        transaction.commitNowAllowingStateLoss(); // Use commitNowAllowingStateLoss for better performance
        activeFragment = fragment;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear references to prevent memory leaks
        homeFragment = null;
        searchFragment = null;
        communityFragment = null;
        favoritesFragment = null;
        profileFragment = null;
        activeFragment = null;
    }

    public void navigateToSearch() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_search);
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNavigationIntent(intent);
    }

    private void handleNavigationIntent(android.content.Intent intent) {
        if (intent.hasExtra(EXTRA_NAVIGATE_LAT) && intent.hasExtra(EXTRA_NAVIGATE_LNG)) {
            double lat = intent.getDoubleExtra(EXTRA_NAVIGATE_LAT, 0);
            double lng = intent.getDoubleExtra(EXTRA_NAVIGATE_LNG, 0);
            String name = intent.getStringExtra(EXTRA_NAVIGATE_NAME);

            // Switch to home tab
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);

            // Pass navigation request to HomeFragment
            if (homeFragment instanceof HomeFragment) {
                ((HomeFragment) homeFragment).prepareNavigation(lat, lng, name);
            }
        }
    }
}
