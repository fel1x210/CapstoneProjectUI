package ca.gbc.comp3074.uiprototype.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ca.gbc.comp3074.uiprototype.R;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views
        View headerBar = view.findViewById(R.id.headerBar);
        View profileCard = view.findViewById(R.id.profileCard);
        View quickActionsCard = view.findViewById(R.id.quickActionsCard);
        View recentActivityCard = view.findViewById(R.id.recentActivityCard);
        View settingsCard = view.findViewById(R.id.settingsCard);
        View buttonLogout = view.findViewById(R.id.buttonLogout);

        // Quick actions
        View actionCheckin = view.findViewById(R.id.actionCheckin);
        View actionReview = view.findViewById(R.id.actionReview);
        View actionPhoto = view.findViewById(R.id.actionPhoto);
        View actionExplore = view.findViewById(R.id.actionExplore);

        // Settings
        Switch switchNotifications = view.findViewById(R.id.switchNotifications);
        Switch switchLocation = view.findViewById(R.id.switchLocation);
        View settingAppearance = view.findViewById(R.id.settingAppearance);
        View settingPrivacy = view.findViewById(R.id.settingPrivacy);
        View settingHelp = view.findViewById(R.id.settingHelp);
        View settingAbout = view.findViewById(R.id.settingAbout);

        // Set up click listeners
        buttonLogout.setOnClickListener(v -> showLogoutDialog());

        actionCheckin.setOnClickListener(
                v -> Toast.makeText(requireContext(), "Check-in feature", Toast.LENGTH_SHORT).show());
        actionReview
                .setOnClickListener(v -> Toast.makeText(requireContext(), "Review feature", Toast.LENGTH_SHORT).show());
        actionPhoto
                .setOnClickListener(v -> Toast.makeText(requireContext(), "Photo feature", Toast.LENGTH_SHORT).show());
        actionExplore.setOnClickListener(
                v -> Toast.makeText(requireContext(), "Explore feature", Toast.LENGTH_SHORT).show());

        settingAppearance.setOnClickListener(
                v -> Toast.makeText(requireContext(), "Appearance settings", Toast.LENGTH_SHORT).show());
        settingPrivacy.setOnClickListener(
                v -> Toast.makeText(requireContext(), "Privacy settings", Toast.LENGTH_SHORT).show());
        settingHelp
                .setOnClickListener(v -> Toast.makeText(requireContext(), "Help & Support", Toast.LENGTH_SHORT).show());
        settingAbout.setOnClickListener(
                v -> Toast.makeText(requireContext(), "About QuietSpace", Toast.LENGTH_SHORT).show());

        switchNotifications.setOnCheckedChangeListener((b, isChecked) -> Toast
                .makeText(requireContext(), isChecked ? "Notifications ON" : "Notifications OFF", Toast.LENGTH_SHORT)
                .show());
        switchLocation.setOnCheckedChangeListener((b, isChecked) -> Toast
                .makeText(requireContext(), isChecked ? "Location ON" : "Location OFF", Toast.LENGTH_SHORT).show());

        animateEntrance(headerBar, profileCard, quickActionsCard, recentActivityCard, settingsCard);
    }

    private void animateEntrance(View headerBar, View profileCard, View quickActionsCard,
            View recentActivityCard, View settingsCard) {
        headerBar.post(() -> {
            headerBar.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            profileCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(120)
                    .setDuration(650)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            quickActionsCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(200)
                    .setDuration(650)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            recentActivityCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(280)
                    .setDuration(650)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            settingsCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(360)
                    .setDuration(650)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
        });
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_confirmation_title)
                .setMessage(R.string.logout_confirmation_message)
                .setPositiveButton(R.string.logout, (d, w) -> {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
