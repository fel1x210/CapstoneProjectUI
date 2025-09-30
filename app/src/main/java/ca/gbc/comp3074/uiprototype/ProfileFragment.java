package ca.gbc.comp3074.uiprototype;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProfileFragment extends Fragment {

    public ProfileFragment() { super(R.layout.fragment_profile); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View menuBar = view.findViewById(R.id.menuBar);
        View content = view.findViewById(R.id.profileContent);
        View buttonLogout = view.findViewById(R.id.buttonLogout);
        View buttonToggleMenu = view.findViewById(R.id.buttonToggleMenu);
        View buttonSettings = view.findViewById(R.id.buttonSettings);
        Switch switchNotifications = view.findViewById(R.id.switchNotifications);
        Switch switchLocation = view.findViewById(R.id.switchLocation);

        buttonLogout.setOnClickListener(v -> showLogoutDialog());
        buttonToggleMenu.setOnClickListener(v -> Toast.makeText(requireContext(), "Menu coming soon", Toast.LENGTH_SHORT).show());
        buttonSettings.setOnClickListener(v -> Toast.makeText(requireContext(), "Settings quick access", Toast.LENGTH_SHORT).show());

        switchNotifications.setOnCheckedChangeListener((b, isChecked) -> Toast.makeText(requireContext(), isChecked ? "Notifications ON" : "Notifications OFF", Toast.LENGTH_SHORT).show());
        switchLocation.setOnCheckedChangeListener((b, isChecked) -> Toast.makeText(requireContext(), isChecked ? "Location ON" : "Location OFF", Toast.LENGTH_SHORT).show());

        animate(menuBar, content);
    }

    private void animate(View bar, View content) {
        bar.post(() -> {
            bar.animate().alpha(1f).translationY(0f).setDuration(600)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
            content.animate().alpha(1f).translationY(0f).setStartDelay(180).setDuration(650)
                    .setInterpolator(new android.view.animation.OvershootInterpolator()).start();
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
