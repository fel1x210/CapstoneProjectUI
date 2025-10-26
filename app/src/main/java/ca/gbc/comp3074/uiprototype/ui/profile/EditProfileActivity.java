package ca.gbc.comp3074.uiprototype.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseAuthHelper;
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseClientManager;

public class EditProfileActivity extends AppCompatActivity {

    private SupabaseAuthHelper authHelper;
    private ImageView profileAvatar;
    private CardView avatarCard;
    private TextInputEditText editFullName;
    private TextInputEditText editEmail;
    private TextInputLayout fullNameLayout;
    private TextInputLayout emailLayout;
    private Button btnSave;
    private Button btnChangeAvatar;

    private Uri cameraImageUri;
    private String currentAvatarUrl;
    private Uri newAvatarUri;
    private boolean isInitialLoad = true;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize Supabase
        SupabaseClientManager.INSTANCE.initialize();
        authHelper = new SupabaseAuthHelper();

        // Initialize views
        initializeViews();
        setupActivityResultLaunchers();
        loadCurrentProfile();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Skip reloading on initial resume (already loaded in onCreate)
        if (isInitialLoad) {
            isInitialLoad = false;
            return;
        }

        // Reinitialize Supabase client to restore session after returning from photo
        // picker
        SupabaseClientManager.INSTANCE.initialize();
        loadCurrentProfile();
    }

    private void initializeViews() {
        profileAvatar = findViewById(R.id.profileAvatar);
        avatarCard = findViewById(R.id.avatarCard);
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        fullNameLayout = findViewById(R.id.fullNameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        btnSave = findViewById(R.id.btnSave);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
    }

    private void setupActivityResultLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (cameraImageUri != null) {
                            newAvatarUri = cameraImageUri;
                            displayAvatar(newAvatarUri);
                        }
                    }
                });

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            newAvatarUri = selectedImage;
                            displayAvatar(newAvatarUri);
                        }
                    }
                });
    }

    private void loadCurrentProfile() {
        authHelper.getCurrentUserProfile(new SupabaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(ca.gbc.comp3074.uiprototype.data.supabase.models.UserProfile profile) {
                runOnUiThread(() -> {
                    editFullName.setText(profile.getFullName());
                    editEmail.setText(profile.getEmail());
                    currentAvatarUrl = profile.getAvatarUrl();

                    if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                        // Use efficient caching for smooth performance
                        Glide.with(EditProfileActivity.this)
                                .load(currentAvatarUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(profileAvatar);
                    } else {
                        profileAvatar.setImageResource(R.drawable.ic_profile);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.util.Log.e("EditProfileActivity", "Load profile error: " + error);
                    Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupClickListeners() {
        btnChangeAvatar.setOnClickListener(v -> showAvatarOptions());
        avatarCard.setOnClickListener(v -> showAvatarOptions());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void showAvatarOptions() {
        String[] options = { "Take Photo", "Choose from Gallery", "Remove Photo" };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Change Avatar")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Take Photo
                            openCamera();
                            break;
                        case 1: // Choose from Gallery
                            openGallery();
                            break;
                        case 2: // Remove Photo
                            removeAvatar();
                            break;
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = new File(getCacheDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
            cameraImageUri = FileProvider.getUriForFile(this,
                    "ca.gbc.comp3074.uiprototype.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraLauncher.launch(intent);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void removeAvatar() {
        newAvatarUri = null;
        currentAvatarUrl = null;
        profileAvatar.setImageResource(R.drawable.ic_profile);
        Toast.makeText(this, "Avatar will be removed when you save", Toast.LENGTH_SHORT).show();
    }

    private void displayAvatar(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .into(profileAvatar);
    }

    private void saveProfile() {
        String newName = editFullName.getText() != null ? editFullName.getText().toString().trim() : "";
        String newEmail = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";

        // Reset errors
        fullNameLayout.setError(null);
        emailLayout.setError(null);

        // Validate
        if (TextUtils.isEmpty(newName)) {
            fullNameLayout.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(newEmail)) {
            emailLayout.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            emailLayout.setError("Invalid email format");
            return;
        }

        // Disable save button
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        // Save in sequence: avatar (if changed), then name, then email (if changed)
        if (newAvatarUri != null) {
            uploadAvatar(newName, newEmail);
        } else {
            updateProfile(newName, newEmail);
        }
    }

    private void uploadAvatar(String newName, String newEmail) {
        authHelper.uploadAvatar(this, newAvatarUri, new SupabaseAuthHelper.AvatarCallback() {
            @Override
            public void onSuccess(String avatarUrl) {
                currentAvatarUrl = avatarUrl;
                updateProfile(newName, newEmail);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to upload avatar: " + error, Toast.LENGTH_SHORT)
                            .show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                });
            }
        });
    }

    private void updateProfile(String newName, String newEmail) {
        authHelper.updateProfile(newName, new SupabaseAuthHelper.SignUpCallback() {
            @Override
            public void onSuccess() {
                // Check if email changed
                String currentEmail = editEmail.getHint() != null ? editEmail.getHint().toString() : "";
                if (!newEmail.equals(currentEmail)) {
                    // Email changed, need to update it (requires password)
                    showPasswordDialogForEmailUpdate(newEmail);
                } else {
                    // Success!
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT)
                                .show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + error, Toast.LENGTH_SHORT)
                            .show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                });
            }
        });
    }

    private void showPasswordDialogForEmailUpdate(String newEmail) {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_password_confirm, null);
        TextInputEditText passwordInput = dialogView.findViewById(R.id.passwordInput);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Email Change")
                .setMessage("Please enter your password to update your email address")
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
                    if (!TextUtils.isEmpty(password)) {
                        updateEmail(newEmail, password);
                    }
                })
                .setNegativeButton("Skip", (dialog, which) -> {
                    // Email not updated, but profile saved
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Profile updated (email unchanged)",
                                Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                })
                .show();
    }

    private void updateEmail(String newEmail, String password) {
        authHelper.updateEmail(newEmail, password, new SupabaseAuthHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Profile and email updated successfully",
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Email update failed: " + error, Toast.LENGTH_SHORT)
                            .show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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
