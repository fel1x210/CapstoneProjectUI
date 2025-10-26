package ca.gbc.comp3074.uiprototype.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseAuthHelper;

public class PrivacySettingsActivity extends AppCompatActivity {

    private SupabaseAuthHelper authHelper;
    private CardView changePasswordCard;
    private CardView updateEmailCard;
    private CardView linkFacebookCard;
    private CardView linkGoogleCard;
    private CardView linkTwitterCard;
    private CardView deleteAccountCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        // Set up toolbar with back button
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Handle back button click
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Supabase
        authHelper = new SupabaseAuthHelper();

        // Initialize views
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        changePasswordCard = findViewById(R.id.changePasswordCard);
        updateEmailCard = findViewById(R.id.updateEmailCard);
        linkFacebookCard = findViewById(R.id.linkFacebookCard);
        linkGoogleCard = findViewById(R.id.linkGoogleCard);
        linkTwitterCard = findViewById(R.id.linkTwitterCard);
        deleteAccountCard = findViewById(R.id.deleteAccountCard);
    }

    private void setupClickListeners() {
        changePasswordCard.setOnClickListener(v -> showChangePasswordDialog());
        updateEmailCard.setOnClickListener(v -> showUpdateEmailDialog());
        linkFacebookCard.setOnClickListener(v -> handleLinkFacebook());
        linkGoogleCard.setOnClickListener(v -> handleLinkGoogle());
        linkTwitterCard.setOnClickListener(v -> handleLinkTwitter());
        deleteAccountCard.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

        TextInputLayout currentPasswordLayout = dialogView.findViewById(R.id.currentPasswordLayout);
        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.newPasswordLayout);
        TextInputLayout confirmPasswordLayout = dialogView.findViewById(R.id.confirmPasswordLayout);

        TextInputEditText currentPasswordInput = dialogView.findViewById(R.id.currentPasswordInput);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);

        builder.setView(dialogView)
                .setTitle("Change Password")
                .setPositiveButton("Change", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String currentPassword = currentPasswordInput.getText().toString().trim();
                String newPassword = newPasswordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                // Reset errors
                currentPasswordLayout.setError(null);
                newPasswordLayout.setError(null);
                confirmPasswordLayout.setError(null);

                // Validation
                if (TextUtils.isEmpty(currentPassword)) {
                    currentPasswordLayout.setError("Current password is required");
                    return;
                }

                if (TextUtils.isEmpty(newPassword)) {
                    newPasswordLayout.setError("New password is required");
                    return;
                }

                if (newPassword.length() < 6) {
                    newPasswordLayout.setError("Password must be at least 6 characters");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    confirmPasswordLayout.setError("Passwords don't match");
                    return;
                }

                // Change password
                button.setEnabled(false);
                button.setText("Changing...");

                authHelper.changePassword(currentPassword, newPassword, new SupabaseAuthHelper.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(PrivacySettingsActivity.this,
                                    "Password changed successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            button.setEnabled(true);
                            button.setText("Change");
                            currentPasswordLayout.setError("Current password is incorrect");
                        });
                    }
                });
            });
        });

        dialog.show();
    }

    private void showUpdateEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_update_email, null);

        TextInputLayout newEmailLayout = dialogView.findViewById(R.id.newEmailLayout);
        TextInputLayout passwordLayout = dialogView.findViewById(R.id.passwordLayout);

        TextInputEditText newEmailInput = dialogView.findViewById(R.id.newEmailInput);
        TextInputEditText passwordInput = dialogView.findViewById(R.id.passwordInput);

        builder.setView(dialogView)
                .setTitle("Update Email")
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String newEmail = newEmailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                // Reset errors
                newEmailLayout.setError(null);
                passwordLayout.setError(null);

                // Validation
                if (TextUtils.isEmpty(newEmail)) {
                    newEmailLayout.setError("Email is required");
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    newEmailLayout.setError("Invalid email format");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordLayout.setError("Password is required for verification");
                    return;
                }

                // Update email
                button.setEnabled(false);
                button.setText("Updating...");

                authHelper.updateEmail(newEmail, password, new SupabaseAuthHelper.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(PrivacySettingsActivity.this,
                                    "Email updated successfully. Please check your new email for verification.",
                                    Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            button.setEnabled(true);
                            button.setText("Update");
                            if (error.contains("password")) {
                                passwordLayout.setError("Incorrect password");
                            } else {
                                newEmailLayout.setError(error);
                            }
                        });
                    }
                });
            });
        });

        dialog.show();
    }

    private void handleLinkFacebook() {
        Toast.makeText(this, "Facebook linking - Coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement Facebook OAuth linking
    }

    private void handleLinkGoogle() {
        Toast.makeText(this, "Google linking - Coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement Google OAuth linking
    }

    private void handleLinkTwitter() {
        Toast.makeText(this, "Twitter linking - Coming soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement Twitter OAuth linking
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage(
                        "Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO: Implement account deletion
                    Toast.makeText(this, "Account deletion - Coming soon", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void finish() {
        super.finish();
        // Add smooth exit transition
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
