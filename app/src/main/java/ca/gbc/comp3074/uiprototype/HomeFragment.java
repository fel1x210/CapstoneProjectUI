package ca.gbc.comp3074.uiprototype;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class HomeFragment extends Fragment {

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
}
