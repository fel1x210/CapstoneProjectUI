package ca.gbc.comp3074.uiprototype.ui.animation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.ui.main.MainActivity;

/**
 * Full-screen welcome animation activity
 * Shows shuffle text animation after successful login
 */
public class WelcomeAnimationActivity extends AppCompatActivity {

    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_IS_FIRST_TIME = "is_first_time";

    private ShuffleTextView shuffleTextView;
    private TextView subtitleTextView;
    private TextView userNameTextView;
    private View backgroundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make it full screen and immersive
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_welcome_animation);

        // Hide system UI for full immersive experience
        hideSystemUI();

        // Initialize views
        backgroundView = findViewById(R.id.welcomeBackground);
        shuffleTextView = findViewById(R.id.shuffleTextView);
        subtitleTextView = findViewById(R.id.welcomeSubtitle);
        userNameTextView = findViewById(R.id.welcomeUserName);

        // Get data from intent
        String userName = getIntent().getStringExtra(EXTRA_USER_NAME);
        boolean isFirstTime = getIntent().getBooleanExtra(EXTRA_IS_FIRST_TIME, false);

        // Set default name if null
        if (userName == null || userName.isEmpty()) {
            userName = "User";
        }
        
        // Log for debugging
        android.util.Log.d("WelcomeAnimation", "User Name: " + userName + ", First Time: " + isFirstTime);

        // Prepare animation
        setupAnimation(userName, isFirstTime);
    }

    private void setupAnimation(String userName, boolean isFirstTime) {
        // Start with everything invisible
        shuffleTextView.setAlpha(0f);
        subtitleTextView.setAlpha(0f);
        userNameTextView.setAlpha(0f);
        backgroundView.setAlpha(0f);

        // Set the text to shuffle
        shuffleTextView.setText("WELCOME TO QUIETSPACE");

        // Set subtitle and username with better formatting
        if (isFirstTime) {
            subtitleTextView.setText("Hello, " + userName + "! 👋");
            userNameTextView.setText("Let's find your perfect quiet space");
        } else {
            // For returning users, show: "Welcome back, [Name]!"
            subtitleTextView.setText("Welcome back,");
            userNameTextView.setText(userName + "! 🎉");
        }

        // Start the animation sequence
        startAnimationSequence();
    }

    private void startAnimationSequence() {
        // Fade in background first (300ms)
        AlphaAnimation fadeInBackground = new AlphaAnimation(0f, 1f);
        fadeInBackground.setDuration(300);
        fadeInBackground.setFillAfter(true);
        backgroundView.startAnimation(fadeInBackground);

        // Show shuffle text after background (400ms delay)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            shuffleTextView.setAlpha(1f);
            shuffleTextView.startAnimation();
        }, 400);

        // Fade in subtitle (after shuffle starts + 1200ms)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            subtitleTextView.setAlpha(0f);
            subtitleTextView.animate()
                    .alpha(1f)
                    .setDuration(600)
                    .start();
        }, 1600);

        // Fade in username (after subtitle + 500ms)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            userNameTextView.setAlpha(0f);
            userNameTextView.animate()
                    .alpha(1f)
                    .setDuration(600)
                    .start();
        }, 2100);

        // Navigate to MainActivity after animation completes (6000ms total - gives more time to see name)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToMainActivity();
        }, 6000);
    }

    private void navigateToMainActivity() {
        // Fade out animation before transition
        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(400);
        fadeOut.setFillAfter(true);

        backgroundView.startAnimation(fadeOut);

        // Navigate after fade out
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(WelcomeAnimationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            // Use fade transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 400);
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button during animation
        // User must wait for animation to complete
    }
}
