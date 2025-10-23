package ca.gbc.comp3074.uiprototype.ui.animation;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import ca.gbc.comp3074.uiprototype.R;

/**
 * Displays a welcome dialog with shuffle text animation
 */
public class WelcomeAnimationDialog {

    private Dialog dialog;
    private ShuffleTextView shuffleTextView;
    private TextView subtitleTextView;
    private TextView userNameTextView;
    private Context context;

    public WelcomeAnimationDialog(Context context) {
        this.context = context;
        setupDialog();
    }

    private void setupDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_welcome_animation, null);
        dialog.setContentView(view);

        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Find views
        shuffleTextView = view.findViewById(R.id.shuffleTextView);
        subtitleTextView = view.findViewById(R.id.welcomeSubtitle);
        userNameTextView = view.findViewById(R.id.welcomeUserName);

        // Make dialog non-cancelable during animation
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * Show the welcome dialog with animation
     * 
     * @param userName User's name to display
     */
    public void show(String userName) {
        dialog.show();

        // Start shuffle animation for main text
        shuffleTextView.shuffleText("WELCOME TO QUIETSPACE");

        // Animate subtitle after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            fadeIn(subtitleTextView, 500);
        }, 800);

        // Animate user name after another delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            userNameTextView.setText("Welcome back, " + userName + "!");
            fadeIn(userNameTextView, 500);
        }, 1200);

        // Auto dismiss after animation completes
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dismiss();
        }, 3500);
    }

    /**
     * Show the welcome dialog for new users
     */
    public void showFirstTime(String userName) {
        dialog.show();

        // Start shuffle animation for main text
        shuffleTextView.shuffleText("WELCOME TO QUIETSPACE");

        // Animate subtitle after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            subtitleTextView.setText("Let's find your perfect quiet space");
            fadeIn(subtitleTextView, 500);
        }, 800);

        // Animate user name after another delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            userNameTextView.setText("Hello, " + userName + "! 👋");
            fadeIn(userNameTextView, 500);
        }, 1200);

        // Auto dismiss after animation completes
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dismiss();
        }, 4000);
    }

    /**
     * Fade in animation for a view
     */
    private void fadeIn(View view, int duration) {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(duration);
        fadeIn.setFillAfter(true);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setAlpha(1.0f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeIn);
    }

    /**
     * Dismiss the dialog
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * Check if dialog is showing
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
