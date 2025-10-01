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
import ca.gbc.comp3074.uiprototype.ui.favorites.FavoritesFragment;
import ca.gbc.comp3074.uiprototype.ui.home.HomeFragment;
import ca.gbc.comp3074.uiprototype.ui.profile.ProfileFragment;
import ca.gbc.comp3074.uiprototype.ui.search.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment searchFragment = new SearchFragment();
    private final Fragment favoritesFragment = new FavoritesFragment();
    private final Fragment profileFragment = new ProfileFragment();

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
                switchFragment(homeFragment);
                return true;
            } else if (itemId == R.id.navigation_search) {
                switchFragment(searchFragment);
                return true;
            } else if (itemId == R.id.navigation_favorites) {
                switchFragment(favoritesFragment);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
            switchFragment(homeFragment);
        }
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void navigateToProfile() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        switchFragment(profileFragment);
    }

    public void navigateToSearch() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_search);
        switchFragment(searchFragment);
    }
}
