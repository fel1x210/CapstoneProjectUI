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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                if (homeFragment == null)
                    homeFragment = new HomeFragment();
                switchFragment(homeFragment);
                return true;
            } else if (itemId == R.id.navigation_search) {
                if (searchFragment == null)
                    searchFragment = new SearchFragment();
                switchFragment(searchFragment);
                return true;
            } else if (itemId == R.id.navigation_community) {
                if (communityFragment == null)
                    communityFragment = CommunityFragment.Companion.newInstance();
                switchFragment(communityFragment);
                return true;
            } else if (itemId == R.id.navigation_favorites) {
                if (favoritesFragment == null)
                    favoritesFragment = new FavoritesFragment();
                switchFragment(favoritesFragment);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                if (profileFragment == null)
                    profileFragment = new ProfileFragment();
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

        activeFragment = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow(); // Use commitNow() to avoid delays
    }

    public void navigateToProfile() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
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
        switchFragment(searchFragment);
    }
}
