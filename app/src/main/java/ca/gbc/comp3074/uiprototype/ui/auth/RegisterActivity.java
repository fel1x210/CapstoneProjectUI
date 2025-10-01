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
    private LinearLayout buttonGoogle;
    private LinearLayout buttonApple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupInteractions();
        animateEntrance();
    }

    private void initViews() {
        cardRegister = findViewById(R.id.cardRegister);
        registerIllustration = findViewById(R.id.registerIllustration);
        registerForm = findViewById(R.id.registerForm);
        registerSocialLogin = findViewById(R.id.registerSocialLogin);
        registerFooter = findViewById(R.id.registerFooter);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
        buttonGoogle = findViewById(R.id.buttonGoogle);
        buttonApple = findViewById(R.id.buttonApple);
    }

    private void setupInteractions() {
        buttonRegister.setOnClickListener(v -> {
            String fullName = textOrEmpty(editTextFullName);
            String email = textOrEmpty(editTextEmail);
            String password = textOrEmpty(editTextPassword);
            String confirmPassword = textOrEmpty(editTextConfirmPassword);

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(getString(R.string.register_error_fields));
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

            playButtonPressAnimation(buttonRegister, this::showSuccessDialog);
        });

        textViewLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        buttonGoogle.setOnClickListener(v -> showInfoDialog(getString(R.string.social_google)));
        buttonApple.setOnClickListener(v -> showInfoDialog(getString(R.string.social_apple)));
    }

    private void animateEntrance() {
        cardRegister.post(() -> {
            cardRegister.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            registerIllustration.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(150)
                    .setDuration(500)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            registerForm.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(250)
                    .setDuration(550)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            registerSocialLogin.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(350)
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
                    finish();
                })
                .show();
    }

    private String textOrEmpty(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
