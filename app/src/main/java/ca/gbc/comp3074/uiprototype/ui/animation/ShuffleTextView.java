package ca.gbc.comp3074.uiprototype.ui.animation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.Random;

/**
 * Custom TextView that animates text with a sophisticated shuffle effect
 * Inspired by GSAP SplitText shuffle animations
 * Each character shuffles through random characters before revealing the final
 * text
 */
public class ShuffleTextView extends AppCompatTextView {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*";
    private static final int SHUFFLE_DURATION = 2500; // 2.5 seconds for smoother effect
    private static final int SHUFFLE_ITERATIONS = 8; // How many times each char shuffles

    private String targetText = "";
    private Random random = new Random();
    private ValueAnimator animator;

    public ShuffleTextView(Context context) {
        super(context);
        init();
    }

    public ShuffleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShuffleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTypeface(Typeface.MONOSPACE);
    }

    /**
     * Start the shuffle animation with the current text
     */
    public void startAnimation() {
        this.targetText = getText().toString();
        shuffleText(targetText);
    }

    /**
     * Start the shuffle animation with the given text
     */
    public void shuffleText(String text) {
        this.targetText = text;
        setText(text); // Set initial text

        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(SHUFFLE_DURATION);
        animator.setInterpolator(new DecelerateInterpolator(1.5f)); // Smoother deceleration

        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            setText(generateShuffledText(progress));
        });

        // Add slight delay before starting for dramatic effect
        animator.setStartDelay(200);
        animator.start();
    }

    /**
     * Generate shuffled text based on animation progress
     * Uses staggered reveal - each character reveals at a slightly different time
     */
    private String generateShuffledText(float progress) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < targetText.length(); i++) {
            char targetChar = targetText.charAt(i);

            // Keep spaces and punctuation as-is
            if (targetChar == ' ' || !Character.isLetterOrDigit(targetChar)) {
                result.append(targetChar);
                continue;
            }

            // Stagger the reveal - characters at the start reveal earlier
            // This creates a left-to-right wave effect
            float charProgress = calculateCharProgress(progress, i, targetText.length());

            if (charProgress >= 1.0f) {
                // Character is fully revealed
                result.append(targetChar);
            } else if (charProgress <= 0.0f) {
                // Character hasn't started shuffling yet
                result.append(getRandomChar());
            } else {
                // Character is shuffling
                // More iterations = more shuffling before reveal
                float shufflePhase = charProgress * SHUFFLE_ITERATIONS;
                int iteration = (int) shufflePhase;

                if (iteration >= SHUFFLE_ITERATIONS) {
                    result.append(targetChar);
                } else {
                    // Still shuffling
                    result.append(getRandomChar());
                }
            }
        }

        return result.toString();
    }

    /**
     * Calculate progress for individual character with stagger effect
     * Creates a wave-like reveal from left to right
     */
    private float calculateCharProgress(float overallProgress, int charIndex, int totalChars) {
        // Stagger amount: 0.0 = all at once, 1.0 = completely sequential
        float stagger = 0.6f;

        // Calculate when this character should start and finish
        float normalizedIndex = (float) charIndex / Math.max(1, totalChars - 1);
        float startProgress = normalizedIndex * stagger;
        float endProgress = startProgress + (1.0f - stagger);

        // Map overall progress to this character's progress range
        if (overallProgress < startProgress) {
            return 0.0f;
        } else if (overallProgress > endProgress) {
            return 1.0f;
        } else {
            return (overallProgress - startProgress) / (endProgress - startProgress);
        }
    }

    /**
     * Get a random character from the character set
     */
    private char getRandomChar() {
        return CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));
    }

    /**
     * Cancel any running animation
     */
    public void cancelAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnimation();
    }
}
