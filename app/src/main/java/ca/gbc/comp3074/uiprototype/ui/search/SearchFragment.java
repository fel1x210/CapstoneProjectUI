package ca.gbc.comp3074.uiprototype.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.data.PlaceEntity;
import ca.gbc.comp3074.uiprototype.ui.details.PlaceDetailsActivity;
import ca.gbc.comp3074.uiprototype.utils.AppConfig;

public class SearchFragment extends Fragment {

    private final List<Place> places = new ArrayList<>();

    private final List<String> recentSearches = Arrays.asList(
            "Quiet coffee shops",
            "Study rooms near me",
            "Late night workspace",
            "Places with phone booths",
            "Outdoor study spots");

    private final List<Category> categories = Arrays.asList(
            new Category("☕", "Cafés"),
            new Category("📚", "Libraries"),
            new Category("💼", "Coworking"),
            new Category("🌿", "Outdoor nooks"),
            new Category("🕘", "Late night"),
            new Category("💡", "Creative labs"));

    private EditText inputSearch;
    private TextView searchIcon;
    private LinearLayout recentList;
    private GridLayout categoriesGrid;
    private LinearLayout resultsSection;
    private LinearLayout resultsContainer;
    private LinearLayout noResultsView;
    private LinearLayout recentSection;
    private LinearLayout categoriesSection;
    private TextView textResultsTitle;
    private SearchViewModel viewModel;
    private String currentQuery = "";
    private SearchManager searchManager;

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);

        // Initialize SearchManager for Google Places API
        searchManager = new SearchManager(requireContext());

        populateRecentSearches();
        populateCategories();
        setupInteractions();

        // Setup ViewModel to observe database changes from Google Places API
        setupViewModel();

        animateEntrance(view);
    }

    private void bindViews(View view) {
        inputSearch = view.findViewById(R.id.inputSearch);
        searchIcon = view.findViewById(R.id.searchIcon);
        recentList = view.findViewById(R.id.recentList);
        categoriesGrid = view.findViewById(R.id.categoriesGrid);
        resultsSection = view.findViewById(R.id.resultsSection);
        resultsContainer = view.findViewById(R.id.resultsContainer);
        noResultsView = view.findViewById(R.id.noResults);
        recentSection = view.findViewById(R.id.recentSection);
        categoriesSection = view.findViewById(R.id.categoriesSection);
        textResultsTitle = view.findViewById(R.id.textResultsTitle);
    }

    private void populateRecentSearches() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        recentList.removeAllViews();
        for (String query : recentSearches) {
            View item = inflater.inflate(R.layout.item_recent_search, recentList, false);
            TextView textQuery = item.findViewById(R.id.textQuery);
            textQuery.setText(query);
            item.setOnClickListener(v -> performSearch(query));
            recentList.addView(item);
        }
    }

    private void populateCategories() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        categoriesGrid.removeAllViews();
        for (Category category : categories) {
            View item = inflater.inflate(R.layout.item_category_card, categoriesGrid, false);
            TextView icon = item.findViewById(R.id.textIcon);
            TextView label = item.findViewById(R.id.textLabel);
            icon.setText(category.icon);
            label.setText(category.label);
            item.setOnClickListener(v -> performSearch(category.label));
            categoriesGrid.addView(item);
        }
    }

    private void setupInteractions() {
        searchIcon.setOnClickListener(
                v -> performSearch(inputSearch.getText() != null ? inputSearch.getText().toString() : ""));

        inputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch(inputSearch.getText() != null ? inputSearch.getText().toString() : "");
                return true;
            }
            return false;
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        viewModel.getAllPlaces().observe(getViewLifecycleOwner(), placeEntities -> {
            // Update places from database when available
            if (placeEntities != null && !placeEntities.isEmpty()) {
                updatePlaces(placeEntities);
            }
            // Always show featured places or search results based on current state
            // This ensures dummy data is shown if database is empty
            if (TextUtils.isEmpty(currentQuery)) {
                showFeaturedPlaces();
            } else {
                List<Place> matches = filterPlaces(currentQuery);
                renderPlaces(matches,
                        getString(R.string.search_results_title, currentQuery, matches.size()),
                        true);
            }
        });
    }

    private void performSearch(String query) {
        String trimmed = query == null ? "" : query.trim();
        currentQuery = trimmed;
        inputSearch.setText(trimmed);
        if (TextUtils.isEmpty(trimmed)) {
            showFeaturedPlaces();
            return;
        }

        // Show loading state
        showLoadingState(trimmed);

        // Search using Google Places API
        searchManager.searchByText(trimmed, AppConfig.DEFAULT_LATITUDE, AppConfig.DEFAULT_LONGITUDE,
                new SearchManager.SearchCallback() {
                    @Override
                    public void onSearchResults(List<PlaceEntity> placeEntities) {
                        requireActivity().runOnUiThread(() -> {
                            List<Place> googlePlaces = convertToPlaces(placeEntities);
                            renderPlaces(googlePlaces,
                                    getString(R.string.search_results_title, trimmed, googlePlaces.size()),
                                    true);
                        });
                    }

                    @Override
                    public void onSearchError(String error) {
                        requireActivity().runOnUiThread(() -> {
                            // Fallback to local search
                            List<Place> matches = filterPlaces(trimmed);
                            renderPlaces(matches,
                                    getString(R.string.search_results_title, trimmed, matches.size()),
                                    true);
                        });
                    }
                });
    }

    private void showFeaturedPlaces() {
        if (places.isEmpty()) {
            resultsSection.setVisibility(View.GONE);
            recentSection.setVisibility(View.VISIBLE);
            categoriesSection.setVisibility(View.VISIBLE);
            return;
        }
        List<Place> featured = new ArrayList<>(places.subList(0, Math.min(places.size(), 4)));
        renderPlaces(featured, getString(R.string.search_featured_title), false);
    }

    private void renderPlaces(List<Place> data, String title, boolean fromSearch) {
        resultsContainer.removeAllViews();
        textResultsTitle.setText(title);
        noResultsView.setVisibility(View.GONE);
        resultsContainer.setVisibility(View.GONE);

        if (data == null || data.isEmpty()) {
            if (fromSearch) {
                noResultsView.setVisibility(View.VISIBLE);
                resultsSection.setVisibility(View.VISIBLE);
                recentSection.setVisibility(View.GONE);
                categoriesSection.setVisibility(View.GONE);
            } else {
                resultsSection.setVisibility(View.GONE);
                recentSection.setVisibility(View.VISIBLE);
                categoriesSection.setVisibility(View.VISIBLE);
            }
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Place place : data) {
            CardView card = (CardView) inflater.inflate(R.layout.item_place_card, resultsContainer, false);
            TextView name = card.findViewById(R.id.textPlaceName);
            TextView type = card.findViewById(R.id.textPlaceType);
            TextView rating = card.findViewById(R.id.textPlaceRating);
            TextView reviews = card.findViewById(R.id.textPlaceReviews);
            com.google.android.material.chip.ChipGroup tagGroup = card.findViewById(R.id.tagContainer);

            name.setText(place.name);
            type.setText(String.format(Locale.getDefault(), "%s • %s", place.type, place.distance));
            rating.setText(String.format(Locale.getDefault(), "⭐ %.1f", place.rating));
            reviews.setText(String.format(Locale.getDefault(), "(%d reviews)", place.reviewCount));

            tagGroup.removeAllViews();
            // Limit tags to improve performance (max 3 tags per place)
            int maxTags = Math.min(place.tags.size(), 3);
            for (int i = 0; i < maxTags; i++) {
                String tag = place.tags.get(i);
                Chip chip = new Chip(requireContext(), null,
                        com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated);
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.quiet_space_chip_inactive);
                chip.setTextColor(
                        getResources().getColor(R.color.quiet_space_text_secondary, requireContext().getTheme()));
                chip.setRippleColorResource(R.color.quiet_space_primary);
                chip.setEnsureMinTouchTargetSize(false);
                chip.setClickable(false); // Disable click for better performance
                tagGroup.addView(chip);
            }

            // Add click listener to open place details
            card.setOnClickListener(v -> openPlaceDetails(place));

            resultsContainer.addView(card);
        }

        resultsContainer.setVisibility(View.VISIBLE);
        resultsSection.setVisibility(View.VISIBLE);
        noResultsView.setVisibility(View.GONE);

        if (fromSearch) {
            recentSection.setVisibility(View.GONE);
            categoriesSection.setVisibility(View.GONE);
        } else {
            recentSection.setVisibility(View.VISIBLE);
            categoriesSection.setVisibility(View.VISIBLE);
        }
    }

    private List<Place> filterPlaces(String query) {
        final String lower = query.toLowerCase(Locale.getDefault());
        List<Place> filtered = new ArrayList<>();
        for (Place place : places) {
            boolean matchesName = place.name.toLowerCase(Locale.getDefault()).contains(lower);
            boolean matchesType = place.type.toLowerCase(Locale.getDefault()).contains(lower);
            boolean matchesTag = place.tags.stream()
                    .anyMatch(tag -> tag.toLowerCase(Locale.getDefault()).contains(lower));
            if (matchesName || matchesType || matchesTag) {
                filtered.add(place);
            }
        }
        return filtered;
    }

    private void updatePlaces(List<PlaceEntity> placeEntities) {
        places.clear();
        if (placeEntities == null) {
            return;
        }
        for (PlaceEntity entity : placeEntities) {
            places.add(new Place(
                    entity.name,
                    entity.type,
                    entity.distance,
                    entity.rating,
                    entity.reviewCount,
                    entity.tags));
        }
    }

    private void showLoadingState(String query) {
        resultsContainer.removeAllViews();
        textResultsTitle.setText("Searching for \"" + query + "\"...");
        noResultsView.setVisibility(View.GONE);
        resultsContainer.setVisibility(View.VISIBLE);
        resultsSection.setVisibility(View.VISIBLE);
        recentSection.setVisibility(View.GONE);
        categoriesSection.setVisibility(View.GONE);

        // Add a simple loading indicator
        TextView loadingText = new TextView(requireContext());
        loadingText.setText("🔍 Searching quiet spaces...");
        loadingText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        loadingText.setPadding(32, 32, 32, 32);
        resultsContainer.addView(loadingText);
    }

    private List<Place> convertToPlaces(List<PlaceEntity> placeEntities) {
        List<Place> converted = new ArrayList<>();
        for (PlaceEntity entity : placeEntities) {
            converted.add(new Place(
                    entity.name,
                    entity.type,
                    calculateDistance(entity.latitude, entity.longitude),
                    entity.rating,
                    entity.reviewCount,
                    entity.tags != null ? entity.tags : Arrays.asList("Quiet space"),
                    entity.googlePlaceId)); // Include Google Place ID
        }
        return converted;
    }

    private void openPlaceDetails(Place place) {
        if (place.googlePlaceId != null && !place.googlePlaceId.isEmpty()) {
            // Has Google Place ID - open directly
            Intent intent = new Intent(requireContext(), PlaceDetailsActivity.class);
            intent.putExtra(PlaceDetailsActivity.EXTRA_GOOGLE_PLACE_ID, place.googlePlaceId);
            startActivity(intent);
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            // No Google Place ID - search for it first
            searchAndOpenPlaceDetails(place.name);
        }
    }

    private void searchAndOpenPlaceDetails(String placeName) {
        // Show loading dialog
        android.app.ProgressDialog dialog = new android.app.ProgressDialog(requireContext());
        dialog.setMessage("Opening place details...");
        dialog.setCancelable(false);
        dialog.show();

        // Search for this place using Google Places API
        searchManager.searchByText(placeName, AppConfig.DEFAULT_LATITUDE, AppConfig.DEFAULT_LONGITUDE,
                new SearchManager.SearchCallback() {
                    @Override
                    public void onSearchResults(List<PlaceEntity> results) {
                        requireActivity().runOnUiThread(() -> {
                            dialog.dismiss();
                            if (results != null && !results.isEmpty()) {
                                // Found the place - open details with first result
                                PlaceEntity place = results.get(0);
                                if (place.googlePlaceId != null && !place.googlePlaceId.isEmpty()) {
                                    Intent intent = new Intent(requireContext(), PlaceDetailsActivity.class);
                                    intent.putExtra(PlaceDetailsActivity.EXTRA_GOOGLE_PLACE_ID, place.googlePlaceId);
                                    startActivity(intent);
                                    requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                } else {
                                    android.widget.Toast.makeText(requireContext(),
                                            "Unable to load place details",
                                            android.widget.Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                android.widget.Toast.makeText(requireContext(),
                                        "Place not found",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onSearchError(String error) {
                        requireActivity().runOnUiThread(() -> {
                            dialog.dismiss();
                            android.widget.Toast.makeText(requireContext(),
                                    "Error loading place: " + error,
                                    android.widget.Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private String calculateDistance(double lat, double lng) {
        // Simple distance calculation (you could use a proper distance formula)
        double distance = Math.sqrt(Math.pow(lat - AppConfig.DEFAULT_LATITUDE, 2) +
                Math.pow(lng - AppConfig.DEFAULT_LONGITUDE, 2)) * 111; // Rough km conversion
        if (distance < 1) {
            return String.format("%.0f m", distance * 1000);
        } else {
            return String.format("%.1f km", distance);
        }
    }

    private void animateEntrance(View root) {
        // Simplified animation for better performance
        View searchHeader = root.findViewById(R.id.searchHeader);
        View searchBar = root.findViewById(R.id.searchBar);
        View scroll = root.findViewById(R.id.searchScroll);

        // Quick fade-in only, no translation for smoother performance
        searchHeader.setAlpha(0f);
        searchBar.setAlpha(0f);
        scroll.setAlpha(0f);

        searchHeader.post(() -> {
            // Faster, simpler animations
            searchHeader.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();

            searchBar.animate()
                    .alpha(1f)
                    .setStartDelay(50)
                    .setDuration(150)
                    .start();

            scroll.animate()
                    .alpha(1f)
                    .setStartDelay(100)
                    .setDuration(150)
                    .start();
        });
    }

    private static class Place {
        final String name;
        final String type;
        final String distance;
        final float rating;
        final int reviewCount;
        final List<String> tags;
        final String googlePlaceId; // Add Google Place ID for details

        Place(String name, String type, String distance, float rating, int reviewCount, List<String> tags) {
            this(name, type, distance, rating, reviewCount, tags, null);
        }

        Place(String name, String type, String distance, float rating, int reviewCount, List<String> tags, String googlePlaceId) {
            this.name = name;
            this.type = type;
            this.distance = distance;
            this.rating = rating;
            this.reviewCount = reviewCount;
            this.tags = tags;
            this.googlePlaceId = googlePlaceId;
        }
    }

    private static class Category {
        final String icon;
        final String label;

        Category(String icon, String label) {
            this.icon = icon;
            this.label = label;
        }
    }
}
