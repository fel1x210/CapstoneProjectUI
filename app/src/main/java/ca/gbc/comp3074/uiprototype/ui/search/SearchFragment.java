package ca.gbc.comp3074.uiprototype.ui.search;

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

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        populateRecentSearches();
        populateCategories();
        setupInteractions();

        // Load dummy data immediately as fallback
        loadDummyData();

        // Setup ViewModel to observe database changes
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

        List<Place> matches = filterPlaces(trimmed);
        renderPlaces(matches,
                getString(R.string.search_results_title, trimmed, matches.size()),
                true);
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
            for (String tag : place.tags) {
                Chip chip = new Chip(requireContext(), null,
                        com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated);
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.quiet_space_chip_inactive);
                chip.setTextColor(
                        getResources().getColor(R.color.quiet_space_text_secondary, requireContext().getTheme()));
                chip.setRippleColorResource(R.color.quiet_space_primary);
                chip.setEnsureMinTouchTargetSize(false);
                tagGroup.addView(chip);
            }

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

    private void loadDummyData() {
        places.clear();
        places.add(new Place("The Urban Reader Café", "Café", "0.2 miles", 4.7f, 362,
                Arrays.asList("Quiet corners", "WiFi", "Specialty brews")));
        places.add(new Place("Central Library", "Library", "0.5 miles", 4.8f, 128,
                Arrays.asList("Study rooms", "Very quiet", "Research help")));
        places.add(new Place("Peaceful Corner Coworking", "Coworking", "0.8 miles", 4.8f, 89,
                Arrays.asList("Professional", "Outlets", "Meeting rooms")));
        places.add(new Place("Sunset Study Lounge", "Lounge", "1.2 miles", 4.6f, 54,
                Arrays.asList("Sunset view", "Soft seating", "Snacks")));
        places.add(new Place("Aurora Reading Atrium", "Library", "1.5 miles", 4.9f, 204,
                Arrays.asList("Natural light", "Quiet zones", "Coffee bar")));
        places.add(new Place("Focus Hub Midtown", "Coworking", "2.0 miles", 4.4f, 178,
                Arrays.asList("24/7 access", "Phone booths", "Events")));
        places.add(new Place("Greenhouse Courtyard", "Garden", "2.3 miles", 4.3f, 96,
                Arrays.asList("Fresh air", "Shade", "Birdsong")));
        places.add(new Place("Midnight Study Café", "Café", "0.9 miles", 4.5f, 147,
                Arrays.asList("Late hours", "Cozy", "Music")));
        places.add(new Place("Riverside Writing Deck", "Outdoor", "3.3 miles", 4.2f, 63,
                Arrays.asList("River breeze", "Shade", "Picnic tables")));
        places.add(new Place("Innovation Loft", "Coworking", "1.9 miles", 4.6f, 112,
                Arrays.asList("Workshops", "Fast WiFi", "Community")));
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

    private void animateEntrance(View root) {
        View searchHeader = root.findViewById(R.id.searchHeader);
        View searchBar = root.findViewById(R.id.searchBar);
        View scroll = root.findViewById(R.id.searchScroll);

        searchHeader.post(() -> {
            searchHeader.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            searchBar.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(120)
                    .setDuration(500)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            if (scroll instanceof androidx.core.widget.NestedScrollView) {
                androidx.core.widget.NestedScrollView nsv = (androidx.core.widget.NestedScrollView) scroll;
				// Ensure the scroll view itself fades/translates in
				scroll.animate()
						.alpha(1f)
						.translationY(0f)
						.setStartDelay(200)
						.setDuration(550)
						.setInterpolator(new DecelerateInterpolator())
						.start();
                if (nsv.getChildCount() > 0) {
                    View content = nsv.getChildAt(0);
                    content.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setStartDelay(200)
                            .setDuration(550)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                }
            } else {
                scroll.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setStartDelay(200)
                        .setDuration(550)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        });
    }

    private static class Place {
        final String name;
        final String type;
        final String distance;
        final float rating;
        final int reviewCount;
        final List<String> tags;

        Place(String name, String type, String distance, float rating, int reviewCount, List<String> tags) {
            this.name = name;
            this.type = type;
            this.distance = distance;
            this.rating = rating;
            this.reviewCount = reviewCount;
            this.tags = tags;
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
