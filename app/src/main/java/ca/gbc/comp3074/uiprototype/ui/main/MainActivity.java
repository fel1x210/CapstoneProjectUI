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
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragment_container, homeFragment, "home")
                            .commit();
                }
                switchFragment(homeFragment);
                return true;
            } else if (itemId == R.id.navigation_search) {
                if (searchFragment == null) {
                    searchFragment = new SearchFragment();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragment_container, searchFragment, "search")
                            .commit();
                }
                switchFragment(searchFragment);
                return true;
            } else if (itemId == R.id.navigation_community) {
                if (communityFragment == null) {
                    communityFragment = CommunityFragment.Companion.newInstance();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragment_container, communityFragment, "community")
                            .commit();
                }
                switchFragment(communityFragment);
                return true;
            } else if (itemId == R.id.navigation_favorites) {
                if (favoritesFragment == null) {
                    favoritesFragment = new FavoritesFragment();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragment_container, favoritesFragment, "favorites")
                            .commit();
                }
                switchFragment(favoritesFragment);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                if (profileFragment == null) {
                    profileFragment = new ProfileFragment();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragment_container, profileFragment, "profile")
                            .commit();
                }
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    private void switchFragment(Fragment fragment) {
        if (fragment == activeFragment)
            return; // Don't reload same fragment

        // Use setReorderingAllowed for better performance
        var transaction = getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true);

        if (homeFragment != null && homeFragment != fragment)
            transaction.hide(homeFragment);
        if (searchFragment != null && searchFragment != fragment)
            transaction.hide(searchFragment);
        if (communityFragment != null && communityFragment != fragment)
            transaction.hide(communityFragment);
        if (favoritesFragment != null && favoritesFragment != fragment)
            transaction.hide(favoritesFragment);
        if (profileFragment != null && profileFragment != fragment)
            transaction.hide(profileFragment);

        transaction.show(fragment).commit();
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
}
