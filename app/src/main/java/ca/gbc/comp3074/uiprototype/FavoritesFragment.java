package ca.gbc.comp3074.uiprototype;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FavoritesFragment extends Fragment {

    public FavoritesFragment() {
        super(R.layout.fragment_favorites);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View header = view.findViewById(R.id.favoritesHeader);
        View scroll = view.findViewById(R.id.favoritesScroll);
        LinearLayout favoritesList = view.findViewById(R.id.favoritesList);
        LinearLayout emptyState = view.findViewById(R.id.emptyState);
        TextView subtitle = view.findViewById(R.id.textFavoritesSubtitle);
        TextView statFav = view.findViewById(R.id.textStatFavorites);
        TextView statCheckins = view.findViewById(R.id.textStatCheckins);
        TextView statReviews = view.findViewById(R.id.textStatReviews);
        MaterialButton buttonExplore = view.findViewById(R.id.buttonExplore);

        List<FavoritePlace> favorites = buildFavorites();

        int favoritesCount = favorites.size();
        int checkins = 0;
        int reviews = 0;
        for (FavoritePlace place : favorites) {
            checkins += place.checkins;
            reviews += place.reviewCount;
        }

        subtitle.setText(getString(R.string.favorites_subtitle, favoritesCount));
        statFav.setText(String.valueOf(favoritesCount));
        statCheckins.setText(String.valueOf(checkins));
        statReviews.setText(String.valueOf(reviews));

        if (favoritesCount == 0) {
            emptyState.setVisibility(View.VISIBLE);
            favoritesList.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            favoritesList.setVisibility(View.VISIBLE);
            renderFavorites(favoritesList, favorites);
        }

        buttonExplore.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToSearch();
            }
        });

        animate(header, scroll);
    }

    private List<FavoritePlace> buildFavorites() {
        List<FavoritePlace> favorites = new ArrayList<>();
        favorites.add(new FavoritePlace(
                "☕",
                "The Urban Reader Café",
                "Café",
                "0.2 miles",
                4.7f,
                362,
                18,
                "2 days ago",
                Arrays.asList("Quiet corners", "Power outlets", "Specialty brews")));
        favorites.add(new FavoritePlace(
                "📚",
                "Central Library",
                "Library",
                "0.5 miles",
                4.8f,
                128,
                32,
                "Last week",
                Arrays.asList("Study rooms", "Very quiet", "Research help")));
        favorites.add(new FavoritePlace(
                "🌇",
                "Sunset Study Lounge",
                "Lounge",
                "1.2 miles",
                4.6f,
                54,
                9,
                "Yesterday evening",
                Arrays.asList("Sunset view", "Soft seating", "Snacks")));
        return favorites;
    }

    private void renderFavorites(LinearLayout container, List<FavoritePlace> favorites) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        container.removeAllViews();

        for (FavoritePlace place : favorites) {
            CardView card = (CardView) inflater.inflate(R.layout.item_favorite_card, container, false);

            TextView emoji = card.findViewById(R.id.textEmoji);
            TextView name = card.findViewById(R.id.textName);
            TextView type = card.findViewById(R.id.textType);
            TextView rating = card.findViewById(R.id.textRating);
            TextView reviews = card.findViewById(R.id.textReviews);
            TextView lastVisited = card.findViewById(R.id.textLastVisited);
            TextView remove = card.findViewById(R.id.textRemove);
            MaterialButton buttonCheckIn = card.findViewById(R.id.buttonCheckIn);
            MaterialButton buttonDirections = card.findViewById(R.id.buttonDirections);
            ChipGroup tagGroup = card.findViewById(R.id.tagContainer);

            emoji.setText(place.emoji);
            name.setText(place.name);
            type.setText(String.format(Locale.getDefault(), "%s • %s", place.type, place.distance));
            rating.setText(String.format(Locale.getDefault(), "⭐ %.1f", place.rating));
            reviews.setText(getString(R.string.favorites_reviews_format, place.reviewCount));
            lastVisited.setText(getString(R.string.favorites_last_visited_format, place.lastVisited));

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

            remove.setOnClickListener(v -> Toast.makeText(requireContext(),
                    getString(R.string.favorites_remove_message, place.name),
                    Toast.LENGTH_SHORT).show());

            buttonCheckIn.setOnClickListener(v -> Toast.makeText(requireContext(),
                    getString(R.string.favorites_check_in_message, place.name),
                    Toast.LENGTH_SHORT).show());

            buttonDirections.setOnClickListener(v -> Toast.makeText(requireContext(),
                    getString(R.string.favorites_directions_message, place.name),
                    Toast.LENGTH_SHORT).show());

            container.addView(card);
        }
    }

    private static class FavoritePlace {
        final String emoji;
        final String name;
        final String type;
        final String distance;
        final float rating;
        final int reviewCount;
        final int checkins;
        final String lastVisited;
        final List<String> tags;

        FavoritePlace(String emoji, String name, String type, String distance, float rating,
                int reviewCount, int checkins, String lastVisited, List<String> tags) {
            this.emoji = emoji;
            this.name = name;
            this.type = type;
            this.distance = distance;
            this.rating = rating;
            this.reviewCount = reviewCount;
            this.checkins = checkins;
            this.lastVisited = lastVisited;
            this.tags = tags;
        }
    }

    private void animate(View header, View scroll) {
        header.post(() -> {
            header.animate().alpha(1f).translationY(0f).setDuration(600)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
            // Root linear content inside scroll (first child of NestedScrollView)
            if (scroll instanceof androidx.core.widget.NestedScrollView) {
                androidx.core.widget.NestedScrollView nsv = (androidx.core.widget.NestedScrollView) scroll;
                if (nsv.getChildCount() > 0) {
                    View content = nsv.getChildAt(0);
                    content.animate().alpha(1f).translationY(0f).setStartDelay(200).setDuration(600)
                            .setInterpolator(new android.view.animation.OvershootInterpolator()).start();
                }
            }
        });
    }
}
