package ca.gbc.comp3074.uiprototype.ui.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseAuthHelper;
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseClientManager;
import ca.gbc.comp3074.uiprototype.ui.main.MainActivity;

public class RegisterActivity extends AppCompatActivity {

    private CardView cardRegister;
    private View registerIllustration;
    private View registerForm;
    private View registerSocialLogin;
    private TextView registerFooter;
    private TextInputEditText editTextFullName;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextConfirmPassword;
    private MaterialButton buttonRegister;
    private TextView textViewLogin;
    private com.google.android.material.checkbox.MaterialCheckBox checkboxTerms;

    private SupabaseAuthHelper authHelper;
    private boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Supabase
        SupabaseClientManager.INSTANCE.initialize();
        authHelper = new SupabaseAuthHelper();

        initViews();
        setupInteractions();
        animateEntrance();
    }

    private void initViews() {
        cardRegister = findViewById(R.id.cardRegister);
        cardRegister = findViewById(R.id.cardRegister);
        registerIllustration = findViewById(R.id.registerHeader);
        registerForm = findViewById(R.id.registerForm);
        registerFooter = findViewById(R.id.registerFooter);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
        checkboxTerms = findViewById(R.id.checkboxTerms);
    }

    private void setupInteractions() {
        buttonRegister.setOnClickListener(v -> {
            if (isRegistering)
                return; // Prevent double clicks

            String fullName = textOrEmpty(editTextFullName);
            String email = textOrEmpty(editTextEmail);
            String password = textOrEmpty(editTextPassword);
            String confirmPassword = textOrEmpty(editTextConfirmPassword);

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(getString(R.string.register_error_fields));
                shakeCard();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showAlert("Please enter a valid email address");
                shakeCard();
                return;
            }

            if (!checkboxTerms.isChecked()) {
                showAlert("Please agree to Terms & Privacy Policy");
                shakeCard();
                return;
            }

            if (!password.equals(confirmPassword)) {
                showAlert(getString(R.string.register_error_password_match));
                shakeCard();
                return;
            }

            if (password.length() < 6) {
                showAlert(getString(R.string.register_error_password_length));
                shakeCard();
                return;
            }

            performRegistration(email, password, fullName);
        });

        textViewLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void performRegistration(String email, String password, String fullName) {
        isRegistering = true;
        buttonRegister.setEnabled(false);
        buttonRegister.setText("Creating account...");

        authHelper.signUp(email, password, fullName, new SupabaseAuthHelper.SignUpCallback() {
            @Override
            public void onSuccess() {
                isRegistering = false;
                buttonRegister.setEnabled(true);
                buttonRegister.setText(getString(R.string.action_sign_up));
                showSuccessDialog();
            }

            @Override
            public void onError(String error) {
                isRegistering = false;
                buttonRegister.setEnabled(true);
                buttonRegister.setText(getString(R.string.action_sign_up));
                showAlert("Registration failed: " + error);
                shakeCard();
            }
        });
    }

    private void animateEntrance() {
        cardRegister.post(() -> {
            cardRegister.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            registerForm.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(250)
                    .setDuration(550)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            registerFooter.animate()
                    .alpha(1f)
                    .setStartDelay(500)
                    .setDuration(400)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        });
    }

    private void playButtonPressAnimation(View view, Runnable onEndAction) {
        view.animate().cancel();
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .setInterpolator(new OvershootInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        if (onEndAction != null) {
                                            onEndAction.run();
                                        }
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }

    private void shakeCard() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(cardRegister, View.TRANSLATION_X, 0f, -10f, 10f, -10f, 10f,
                0f);
        animator.setDuration(300);
        animator.start();
    }

    private void showAlert(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.action_sign_up)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showInfoDialog(String provider) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(provider)
                .setMessage(provider + " will be implemented")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.action_sign_up)
                .setMessage(R.string.register_success)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                })
                .show();
    }

    private String textOrEmpty(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
    
    @Override
    public void finish() {
        super.finish();
        // Add smooth exit transition
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
