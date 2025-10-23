package ca.gbc.comp3074.uiprototype.ui.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseAuthHelper;
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseClientManager;
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseStorageRepository;
import ca.gbc.comp3074.uiprototype.data.supabase.models.UserProfile;
import ca.gbc.comp3074.uiprototype.ui.auth.WelcomeActivity;

public class ProfileFragment extends Fragment {

        private TextView profileName;
        private TextView profileEmail;
        private ImageView profileAvatar;
        private CardView profileAvatarCard;
        private LinearLayout statsContainer;
        private LinearLayout activityIconsContainer;
        private TextView statsLabel;
        private Switch switchNotifications;
        private Switch switchLocation;

        private SupabaseAuthHelper authHelper;
        private Uri cameraImageUri;

        // Activity result launchers for camera and gallery
        private ActivityResultLauncher<Intent> cameraLauncher;
        private ActivityResultLauncher<String> galleryLauncher;
        private ActivityResultLauncher<Intent> editProfileLauncher;

        public ProfileFragment() {
                super(R.layout.fragment_profile);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);

                // Initialize Supabase
                SupabaseClientManager.INSTANCE.initialize();
                authHelper = new SupabaseAuthHelper();

                // Initialize activity result launchers
                setupActivityResultLaunchers();

                // Find views
                View headerBar = view.findViewById(R.id.headerBar);
                View profileCard = view.findViewById(R.id.profileCard);
                View quickActionsCard = view.findViewById(R.id.quickActionsCard);
                View recentActivityCard = view.findViewById(R.id.recentActivityCard);
                View settingsCard = view.findViewById(R.id.settingsCard);
                View buttonLogout = view.findViewById(R.id.buttonLogout);

                // Header icons
                View menuIcon = view.findViewById(R.id.menuIcon);
                View settingsIcon = view.findViewById(R.id.settingsIcon);

                // Profile views
                profileName = view.findViewById(R.id.profileName);
                profileEmail = view.findViewById(R.id.profileEmail);
                profileAvatar = view.findViewById(R.id.profileAvatar);
                profileAvatarCard = view.findViewById(R.id.profileAvatarCard);
                statsContainer = view.findViewById(R.id.statsContainer);
                activityIconsContainer = view.findViewById(R.id.activityIconsContainer);
                statsLabel = view.findViewById(R.id.statsLabel);
                View buttonEditProfile = view.findViewById(R.id.buttonEditProfile);

                // Quick actions
                View actionCheckin = view.findViewById(R.id.actionCheckin);
                View actionReview = view.findViewById(R.id.actionReview);
                View actionPhoto = view.findViewById(R.id.actionPhoto);
                View actionExplore = view.findViewById(R.id.actionExplore);

                // Settings
                switchNotifications = view.findViewById(R.id.switchNotifications);
                switchLocation = view.findViewById(R.id.switchLocation);
                View settingAppearance = view.findViewById(R.id.settingAppearance);
                View settingPrivacy = view.findViewById(R.id.settingPrivacy);
                View settingHelp = view.findViewById(R.id.settingHelp);
                View settingAbout = view.findViewById(R.id.settingAbout);

                // Set up click listeners
                buttonLogout.setOnClickListener(v -> showLogoutDialog());
                buttonEditProfile.setOnClickListener(v -> showEditProfileDialog());
                profileAvatarCard.setOnClickListener(v -> showAvatarOptions());

                // Header icon click listeners
                menuIcon.setOnClickListener(v -> showMenuOptions());
                settingsIcon.setOnClickListener(v -> showQuickSettings());

                // Quick Actions - Make them functional
                actionCheckin.setOnClickListener(v -> handleCheckIn());
                actionReview.setOnClickListener(v -> handleReview());
                actionPhoto.setOnClickListener(v -> handlePhoto());
                actionExplore.setOnClickListener(v -> handleExplore());

                // Settings - Make them functional
                settingAppearance.setOnClickListener(v -> handleAppearanceSettings());
                settingPrivacy.setOnClickListener(v -> handlePrivacySettings());
                settingHelp.setOnClickListener(v -> handleHelpSupport());
                settingAbout.setOnClickListener(v -> handleAbout());

                // Load and apply saved preferences
                loadPreferences();

                switchNotifications.setOnCheckedChangeListener((b, isChecked) -> {
                        savePreference("notifications", isChecked);
                        Toast.makeText(requireContext(), isChecked ? "Notifications ON" : "Notifications OFF",
                                        Toast.LENGTH_SHORT).show();
                });

                switchLocation.setOnCheckedChangeListener((b, isChecked) -> {
                        savePreference("location", isChecked);
                        Toast.makeText(requireContext(), isChecked ? "Location ON" : "Location OFF",
                                        Toast.LENGTH_SHORT).show();
                });

                // Load user profile
                loadUserProfile();

                animateEntrance(headerBar, profileCard, quickActionsCard, recentActivityCard, settingsCard);
        }

        private void loadUserProfile() {
                // Get current user profile from Supabase
                authHelper.getCurrentUserProfile(new SupabaseAuthHelper.AuthCallback() {
                        @Override
                        public void onSuccess(UserProfile profile) {
                                // Update UI with real user data
                                profileName.setText(profile.getFullName() != null ? profile.getFullName() : "User");

                                // Show email if available (add email TextView to layout if needed)
                                if (profileEmail != null && profile.getEmail() != null) {
                                        profileEmail.setText(profile.getEmail());
                                        profileEmail.setVisibility(View.VISIBLE);
                                }

                                // Hide stats for new users (no activity yet)
                                if (statsContainer != null) {
                                        statsContainer.setVisibility(View.GONE);
                                }
                                if (statsLabel != null) {
                                        statsLabel.setVisibility(View.GONE);
                                }
                                if (activityIconsContainer != null) {
                                        activityIconsContainer.setVisibility(View.GONE);
                                }
                        }

                        @Override
                        public void onError(String error) {
                                // Show error or default values
                                profileName.setText("User");
                                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();

                                // Hide stats containers
                                if (statsContainer != null) {
                                        statsContainer.setVisibility(View.GONE);
                                }
                                if (statsLabel != null) {
                                        statsLabel.setVisibility(View.GONE);
                                }
                                if (activityIconsContainer != null) {
                                        activityIconsContainer.setVisibility(View.GONE);
                                }
                        }
                });
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
                                .setPositiveButton(R.string.logout, (d, w) -> performLogout())
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
        }

        private void performLogout() {
                authHelper.signOut(new SupabaseAuthHelper.SignUpCallback() {
                        @Override
                        public void onSuccess() {
                                // Navigate back to welcome screen
                                Intent intent = new Intent(requireContext(), WelcomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                if (getActivity() != null) {
                                        getActivity().finish();
                                }
                        }

                        @Override
                        public void onError(String error) {
                                Toast.makeText(requireContext(), "Logout failed: " + error, Toast.LENGTH_SHORT).show();
                        }
                });
        }

        private void showMenuOptions() {
                new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Menu")
                                .setItems(new String[] {
                                                "Profile Settings",
                                                "Privacy Settings",
                                                "Help & Support",
                                                "About",
                                                "Logout"
                                }, (dialog, which) -> {
                                        switch (which) {
                                                case 0: // Profile Settings
                                                        showEditProfileDialog();
                                                        break;
                                                case 1: // Privacy Settings
                                                        handlePrivacySettings();
                                                        break;
                                                case 2: // Help & Support
                                                        handleHelpSupport();
                                                        break;
                                                case 3: // About
                                                        handleAbout();
                                                        break;
                                                case 4: // Logout
                                                        showLogoutDialog();
                                                        break;
                                        }
                                })
                                .show();
        }

        private void showQuickSettings() {
                new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Quick Settings")
                                .setItems(new String[] {
                                                "Appearance",
                                                "Privacy Settings",
                                                "Notifications",
                                                "Location Services"
                                }, (dialog, which) -> {
                                        switch (which) {
                                                case 0: // Appearance
                                                        handleAppearanceSettings();
                                                        break;
                                                case 1: // Privacy Settings
                                                        handlePrivacySettings();
                                                        break;
                                                case 2: // Notifications
                                                        switchNotifications
                                                                        .setChecked(!switchNotifications.isChecked());
                                                        break;
                                                case 3: // Location
                                                        switchLocation.setChecked(!switchLocation.isChecked());
                                                        break;
                                        }
                                })
                                .show();
        }

        private void showEditProfileDialog() {
                // Navigate to Edit Profile Activity
                Intent intent = new Intent(requireContext(), EditProfileActivity.class);
                editProfileLauncher.launch(intent);
        }

        private void updateProfile(String newName) {
                authHelper.updateProfile(newName, new SupabaseAuthHelper.SignUpCallback() {
                        @Override
                        public void onSuccess() {
                                profileName.setText(newName);
                                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT)
                                                .show();
                        }

                        @Override
                        public void onError(String error) {
                                Toast.makeText(requireContext(), "Failed to update profile: " + error,
                                                Toast.LENGTH_SHORT).show();
                        }
                });
        }

        // ===== Avatar Upload =====

        private void setupActivityResultLaunchers() {
                // Camera launcher
                cameraLauncher = registerForActivityResult(
                                new ActivityResultContracts.StartActivityForResult(),
                                result -> {
                                        if (result.getResultCode() == Activity.RESULT_OK && cameraImageUri != null) {
                                                uploadAvatar(cameraImageUri);
                                        }
                                });

                // Gallery launcher
                galleryLauncher = registerForActivityResult(
                                new ActivityResultContracts.GetContent(),
                                uri -> {
                                        if (uri != null) {
                                                uploadAvatar(uri);
                                        }
                                });

                // Edit Profile launcher - reload profile when returning
                editProfileLauncher = registerForActivityResult(
                                new ActivityResultContracts.StartActivityForResult(),
                                result -> {
                                        if (result.getResultCode() == Activity.RESULT_OK) {
                                                // Reload profile data
                                                loadUserProfile();
                                        }
                                });
        }

        private void showAvatarOptions() {
                new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Update Profile Picture")
                                .setItems(new String[] { "Take Photo", "Choose from Gallery", "Remove Photo" },
                                                (dialog, which) -> {
                                                        switch (which) {
                                                                case 0:
                                                                        openCamera();
                                                                        break;
                                                                case 1:
                                                                        openGallery();
                                                                        break;
                                                                case 2:
                                                                        removeAvatar();
                                                                        break;
                                                        }
                                                })
                                .show();
        }

        private void openCamera() {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile = new File(requireContext().getCacheDir(),
                                "avatar_" + System.currentTimeMillis() + ".jpg");
                cameraImageUri = FileProvider.getUriForFile(requireContext(),
                                requireContext().getPackageName() + ".fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                cameraLauncher.launch(intent);
        }

        private void openGallery() {
                galleryLauncher.launch("image/*");
        }

        private void uploadAvatar(Uri imageUri) {
                Toast.makeText(requireContext(), "Uploading avatar...", Toast.LENGTH_SHORT).show();

                authHelper.uploadAvatar(requireContext(), imageUri, new SupabaseAuthHelper.AvatarCallback() {
                        @Override
                        public void onSuccess(String avatarUrl) {
                                // Load the image with Glide
                                Glide.with(requireContext())
                                                .load(avatarUrl)
                                                .circleCrop()
                                                .into(profileAvatar);
                                Toast.makeText(requireContext(), "Avatar updated successfully!", Toast.LENGTH_SHORT)
                                                .show();
                        }

                        @Override
                        public void onError(String error) {
                                Toast.makeText(requireContext(), "Failed to upload avatar: " + error,
                                                Toast.LENGTH_SHORT).show();
                        }
                });
        }

        private void removeAvatar() {
                profileAvatar.setImageResource(R.drawable.ic_profile);
                Toast.makeText(requireContext(), "Avatar removed", Toast.LENGTH_SHORT).show();
        }

        // ===== Quick Actions =====

        private void handleCheckIn() {
                Toast.makeText(requireContext(),
                                "Check-in feature - Coming soon!\nYou'll be able to check in at quiet spaces you visit.",
                                Toast.LENGTH_LONG).show();
        }

        private void handleReview() {
                Toast.makeText(requireContext(),
                                "Review feature - Coming soon!\nYou'll be able to write reviews for places you've visited.",
                                Toast.LENGTH_LONG).show();
        }

        private void handlePhoto() {
                Toast.makeText(requireContext(),
                                "Photo feature - Coming soon!\nYou'll be able to upload photos of quiet spaces.",
                                Toast.LENGTH_LONG).show();
        }

        private void handleExplore() {
                // Navigate to search tab
                if (getActivity() != null) {
                        ((ca.gbc.comp3074.uiprototype.ui.main.MainActivity) getActivity()).navigateToSearch();
                }
        }

        // ===== Settings Handlers =====

        private void handleAppearanceSettings() {
                new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Appearance")
                                .setItems(new String[] { "Light Theme", "Dark Theme", "System Default" },
                                                (dialog, which) -> {
                                                        String theme = which == 0 ? "Light"
                                                                        : which == 1 ? "Dark" : "System";
                                                        savePreference("theme", theme);
                                                        applyTheme(theme);
                                                        Toast.makeText(requireContext(), theme + " theme selected",
                                                                        Toast.LENGTH_SHORT).show();
                                                })
                                .show();
        }

        private void handlePrivacySettings() {
                // Navigate to Privacy Settings Activity
                Intent intent = new Intent(requireContext(), PrivacySettingsActivity.class);
                startActivity(intent);
        }

        private void handleHelpSupport() {
                new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Help & Support")
                                .setMessage("Need help?\n\n📧 Email: support@quietspace.com\n📱 Phone: 1-800-QUIET-SPACE\n🌐 Website: www.quietspace.com/help\n\n"
                                                +
                                                "Common topics:\n• How to find quiet spaces\n• How to add reviews\n• Account management\n• Report an issue")
                                .setPositiveButton("OK", null)
                                .show();
        }

        private void handleAbout() {
                new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("About QuietSpace")
                                .setMessage("QuietSpace v1.0\n\n" +
                                                "Your companion for finding peaceful places to study, work, or relax.\n\n"
                                                +
                                                "© 2025 QuietSpace\n" +
                                                "Made with ❤️ for people who value peace and quiet.")
                                .setPositiveButton("OK", null)
                                .show();
        }

        // ===== Preferences Management =====

        private void loadPreferences() {
                android.content.SharedPreferences prefs = requireContext().getSharedPreferences("QuietSpacePrefs",
                                android.content.Context.MODE_PRIVATE);
                switchNotifications.setChecked(prefs.getBoolean("notifications", true));
                switchLocation.setChecked(prefs.getBoolean("location", true));
        }

        private void savePreference(String key, boolean value) {
                requireContext().getSharedPreferences("QuietSpacePrefs", android.content.Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean(key, value)
                                .apply();
        }

        private void savePreference(String key, String value) {
                requireContext().getSharedPreferences("QuietSpacePrefs", android.content.Context.MODE_PRIVATE)
                                .edit()
                                .putString(key, value)
                                .apply();
        }

        private void applyTheme(String theme) {
                int mode;
                switch (theme) {
                        case "Light":
                                mode = AppCompatDelegate.MODE_NIGHT_NO;
                                break;
                        case "Dark":
                                mode = AppCompatDelegate.MODE_NIGHT_YES;
                                break;
                        case "System":
                        default:
                                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                                break;
                }
                AppCompatDelegate.setDefaultNightMode(mode);
        }
}
