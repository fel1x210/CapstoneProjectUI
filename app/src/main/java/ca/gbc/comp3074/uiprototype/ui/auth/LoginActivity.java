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
import ca.gbc.comp3074.uiprototype.data.supabase.models.UserProfile;
import ca.gbc.comp3074.uiprototype.ui.animation.WelcomeAnimationActivity;
import ca.gbc.comp3074.uiprototype.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private CardView cardLogin;
    private View loginIllustration;
    private View loginForm;
    private View socialLogin;
    private TextView loginFooter;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private MaterialButton buttonLogin;
    private TextView textViewRegister;
    private LinearLayout buttonGoogle;
    private LinearLayout buttonApple;

    private SupabaseAuthHelper authHelper;
    private boolean isLoggingIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Supabase
        SupabaseClientManager.INSTANCE.initialize();
        authHelper = new SupabaseAuthHelper();

        initViews();
        setupInteractions();
        animateEntrance();
    }

    private void initViews() {
        cardLogin = findViewById(R.id.cardLogin);
        loginForm = findViewById(R.id.loginForm);
        socialLogin = findViewById(R.id.socialLogin);
        loginFooter = findViewById(R.id.loginFooter);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        buttonGoogle = findViewById(R.id.buttonGoogle);
        buttonApple = findViewById(R.id.buttonApple);
    }

    private void setupInteractions() {
        buttonLogin.setOnClickListener(v -> {
            if (isLoggingIn)
                return; // Prevent double clicks

            String email = editTextEmail.getText() != null ? editTextEmail.getText().toString().trim() : "";
            String password = editTextPassword.getText() != null ? editTextPassword.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                showAlert("Please enter both email and password");
                shakeCard();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showAlert("Please enter a valid email address");
                shakeCard();
                return;
            }

            performLogin(email, password);
        });

        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        buttonGoogle.setOnClickListener(v -> showInfoDialog(getString(R.string.social_google)));
        buttonApple.setOnClickListener(v -> showInfoDialog(getString(R.string.social_apple)));
    }

    private void performLogin(String email, String password) {
        isLoggingIn = true;
        buttonLogin.setEnabled(false);
        buttonLogin.setText("Signing in...");

        authHelper.signIn(email, password, new SupabaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                isLoggingIn = false;
                buttonLogin.setEnabled(true);
                buttonLogin.setText(getString(R.string.action_log_in));
                showSuccessDialog(profile);
            }

            @Override
            public void onError(String error) {
                isLoggingIn = false;
                buttonLogin.setEnabled(true);
                buttonLogin.setText(getString(R.string.action_log_in));
                showAlert("Login failed: " + error);
                shakeCard();
            }
        });
    }

    private void animateEntrance() {
        cardLogin.post(() -> {
            cardLogin.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            loginForm.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(250)
                    .setDuration(550)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            socialLogin.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(350)
                    .setDuration(550)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            loginFooter.animate()
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
        ObjectAnimator animator = ObjectAnimator.ofFloat(cardLogin, View.TRANSLATION_X, 0f, -10f, 10f, -10f, 10f, 0f);
        animator.setDuration(300);
        animator.start();
    }

    private void showAlert(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.login_error))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showInfoDialog(String provider) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(provider)
                .setMessage(getString(R.string.social_apple).equals(provider)
                        ? getString(R.string.social_apple) + " will be implemented"
                        : getString(R.string.social_google) + " will be implemented")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showSuccessDialog(UserProfile profile) {
        // Launch full-screen welcome animation
        String userName = profile != null && profile.getFullName() != null
                ? profile.getFullName()
                : "User";

        Intent intent = new Intent(LoginActivity.this, WelcomeAnimationActivity.class);
        intent.putExtra(WelcomeAnimationActivity.EXTRA_USER_NAME, userName);
        intent.putExtra(WelcomeAnimationActivity.EXTRA_IS_FIRST_TIME, false);
        startActivity(intent);
        finish();

        // Use smooth custom transition
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    
    @Override
    public void finish() {
        super.finish();
        // Add smooth exit transition
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
