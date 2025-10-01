package ca.gbc.comp3074.uiprototype.ui.favorites;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.data.PlaceEntity;
import ca.gbc.comp3074.uiprototype.ui.main.MainActivity;

public class FavoritesFragment extends Fragment {

    public FavoritesFragment() {
        super(R.layout.fragment_favorites);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View header = view.findViewById(R.id.favoritesHeader);
        View scroll = view.findViewById(R.id.favoritesScroll);
        RecyclerView favoritesList = view.findViewById(R.id.recyclerFavorites);
        LinearLayout emptyState = view.findViewById(R.id.emptyState);
        TextView subtitle = view.findViewById(R.id.textFavoritesSubtitle);
        TextView statFav = view.findViewById(R.id.textStatFavorites);
        TextView statCheckins = view.findViewById(R.id.textStatCheckins);
        TextView statReviews = view.findViewById(R.id.textStatReviews);
        MaterialButton buttonExplore = view.findViewById(R.id.buttonExplore);

        favoritesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        FavoritesAdapter adapter = new FavoritesAdapter();
        favoritesList.setAdapter(adapter);

        FavoritesViewModel viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);
        viewModel.getFavorites().observe(getViewLifecycleOwner(), favoritePlaces -> {
            updateStats(subtitle, statFav, statCheckins, statReviews, favoritePlaces);
            if (favoritePlaces == null || favoritePlaces.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                favoritesList.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                favoritesList.setVisibility(View.VISIBLE);
                adapter.submitList(favoritePlaces);
            }
        });

        buttonExplore.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToSearch();
            } else {
                Snackbar.make(v, R.string.search_title, Snackbar.LENGTH_SHORT).show();
            }
        });

        animate(header, scroll);
    }

    private void updateStats(TextView subtitle,
            TextView statFav,
            TextView statCheckins,
            TextView statReviews,
            List<PlaceEntity> favorites) {
        int favoritesCount = favorites == null ? 0 : favorites.size();
        int totalCheckins = 0;
        int totalReviews = 0;
        if (favorites != null) {
            for (PlaceEntity place : favorites) {
                totalCheckins += place.checkins;
                totalReviews += place.reviewCount;
            }
        }

        subtitle.setText(getString(R.string.favorites_subtitle, favoritesCount));
        statFav.setText(String.valueOf(favoritesCount));
        statCheckins.setText(String.valueOf(totalCheckins));
        statReviews.setText(String.valueOf(totalReviews));
    }

    private void animate(View header, View scroll) {
        header.post(() -> {
            header.animate().alpha(1f).translationY(0f).setDuration(600)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
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
