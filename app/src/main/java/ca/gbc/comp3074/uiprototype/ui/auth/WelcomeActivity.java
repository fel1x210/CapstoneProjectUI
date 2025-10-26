package ca.gbc.comp3074.uiprototype.ui.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseAuthHelper;
import ca.gbc.comp3074.uiprototype.data.supabase.SupabaseClientManager;
import ca.gbc.comp3074.uiprototype.ui.main.MainActivity;

public class WelcomeActivity extends AppCompatActivity {

    private CardView cardContainer;
    private View headerGroup;
    private View illustrationGroup;
    private View buttonGroup;
    private TextView textFooter;
    private MaterialButton buttonSignUp;
    private MaterialButton buttonLogin;
    private SupabaseAuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Supabase first
        SupabaseClientManager.INSTANCE.initialize();
        authHelper = new SupabaseAuthHelper();
        
        // Show welcome screen (auto-login disabled)
        setContentView(R.layout.activity_welcome);
        
        // Always show welcome screen with animations
        initViews();
        setupInteractions();
        animateEntrance();
        
        /* AUTO-LOGIN DISABLED FOR TESTING
        // Check if user is already logged in (async)
        authHelper.checkIfUserLoggedIn(isLoggedIn -> {
            if (isLoggedIn) {
                // User is already authenticated, go directly to MainActivity
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                // User not logged in, show welcome screen with animations
                initViews();
                setupInteractions();
                animateEntrance();
            }
            return null; // Required for Kotlin Unit lambda
        });
        */
    }

    private void initViews() {
        cardContainer = findViewById(R.id.cardContainer);
        headerGroup = findViewById(R.id.headerGroup);
        illustrationGroup = findViewById(R.id.illustrationGroup);
        buttonGroup = findViewById(R.id.buttonGroup);
        textFooter = findViewById(R.id.textFooter);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonLogin = findViewById(R.id.buttonLogin);
    }

    private void setupInteractions() {
        buttonSignUp.setOnClickListener(v -> playButtonPressAnimation(buttonSignUp, () -> {
            Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }));

        buttonLogin.setOnClickListener(v -> playButtonPressAnimation(buttonLogin, () -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }));
    }

    private void animateEntrance() {
        cardContainer.post(() -> {
            cardContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            headerGroup.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(150)
                    .setDuration(600)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            illustrationGroup.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(250)
                    .setDuration(600)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            buttonGroup.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(350)
                    .setDuration(600)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            textFooter.animate()
                    .alpha(1f)
                    .setStartDelay(500)
                    .setDuration(600)
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
}
