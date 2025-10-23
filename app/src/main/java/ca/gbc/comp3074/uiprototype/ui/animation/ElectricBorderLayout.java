package ca.gbc.comp3074.uiprototype.ui.animation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import java.util.Random;

/**
 * Electric Border effect inspired by modern web animations
 * Creates an animated, turbulent border with glow effects
 * Similar to GSAP turbulent displacement animations
 */
public class ElectricBorderLayout extends FrameLayout {

    private Paint strokePaint;
    private Paint glowPaint1;
    private Paint glowPaint2;
    private Paint backgroundGlowPaint;

    private float cornerRadius = 24f;
    private float borderWidth = 3f;
    private int borderColor = Color.parseColor("#5227FF");

    private float animationProgress = 0f;
    private ValueAnimator animator;

    private Random random = new Random();
    private float[] turbulenceOffsets;
    private static final int TURBULENCE_POINTS = 32; // Number of control points for turbulence

    private float speed = 1f;
    private float chaos = 1f;

    private Path borderPath;
    private RectF bounds;

    public ElectricBorderLayout(Context context) {
        super(context);
        init();
    }

    public ElectricBorderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ElectricBorderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        // Initialize turbulence offsets
        turbulenceOffsets = new float[TURBULENCE_POINTS];
        for (int i = 0; i < TURBULENCE_POINTS; i++) {
            turbulenceOffsets[i] = random.nextFloat() * 2f - 1f;
        }

        borderPath = new Path();
        bounds = new RectF();

        // Setup paints
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(borderWidth);
        strokePaint.setColor(borderColor);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);

        glowPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint1.setStyle(Paint.Style.STROKE);
        glowPaint1.setStrokeWidth(borderWidth * 1.5f);
        glowPaint1.setColor(adjustAlpha(borderColor, 0.6f));
        glowPaint1.setMaskFilter(new android.graphics.BlurMaskFilter(
                borderWidth * 2, android.graphics.BlurMaskFilter.Blur.NORMAL));

        glowPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint2.setStyle(Paint.Style.STROKE);
        glowPaint2.setStrokeWidth(borderWidth * 2f);
        glowPaint2.setColor(adjustAlpha(borderColor, 0.4f));
        glowPaint2.setMaskFilter(new android.graphics.BlurMaskFilter(
                borderWidth * 4, android.graphics.BlurMaskFilter.Blur.NORMAL));

        backgroundGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundGlowPaint.setStyle(Paint.Style.STROKE);
        backgroundGlowPaint.setStrokeWidth(borderWidth * 6f);
        backgroundGlowPaint.setColor(adjustAlpha(borderColor, 0.2f));
        backgroundGlowPaint.setMaskFilter(new android.graphics.BlurMaskFilter(
                32f, android.graphics.BlurMaskFilter.Blur.NORMAL));

        // Start animation
        startAnimation();

        // Don't add padding - let children define their own layout
        // This allows the border to be drawn around the actual child bounds

        // Enable hardware acceleration for better performance
        setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Enable drawing outside bounds for glow effect
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void setBorderColor(int color) {
        this.borderColor = color;
        strokePaint.setColor(color);
        glowPaint1.setColor(adjustAlpha(color, 0.6f));
        glowPaint2.setColor(adjustAlpha(color, 0.4f));
        backgroundGlowPaint.setColor(adjustAlpha(color, 0.2f));
        invalidate();
    }

    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        invalidate();
    }

    public void setBorderWidth(float width) {
        this.borderWidth = width;
        strokePaint.setStrokeWidth(width);
        glowPaint1.setStrokeWidth(width * 1.5f);
        glowPaint2.setStrokeWidth(width * 2f);
        backgroundGlowPaint.setStrokeWidth(width * 6f);
        invalidate();
    }

    public void setSpeed(float speed) {
        this.speed = Math.max(0.1f, speed);
        if (animator != null) {
            long duration = (long) (6000 / this.speed);
            animator.setDuration(duration);
        }
    }

    public void setChaos(float chaos) {
        this.chaos = Math.max(0f, Math.min(3f, chaos));
        invalidate();
    }

    private void startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration((long) (6000 / speed));
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);

        animator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();

            // Update turbulence offsets to create flowing effect
            for (int i = 0; i < TURBULENCE_POINTS; i++) {
                float phase = (animationProgress + (i / (float) TURBULENCE_POINTS)) % 1f;
                turbulenceOffsets[i] = (float) Math.sin(phase * Math.PI * 2) * chaos * 5f;
            }

            invalidate();
        });

        animator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateBounds();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateBounds();
    }

    private void updateBounds() {
        // If we have a child (the CardView), match its bounds
        if (getChildCount() > 0) {
            View child = getChildAt(0);

            // Try to get corner radius from CardView
            if (child instanceof androidx.cardview.widget.CardView) {
                androidx.cardview.widget.CardView cardView = (androidx.cardview.widget.CardView) child;
                cornerRadius = cardView.getRadius();
            }

            // Account for glow effect extension
            float glowExtension = borderWidth * 2;

            // Set bounds to match the child's actual position including margins
            bounds.set(
                    child.getLeft() - glowExtension,
                    child.getTop() - glowExtension,
                    child.getRight() + glowExtension,
                    child.getBottom() + glowExtension);
        } else {
            // Fallback if no child
            float glowExtension = borderWidth * 2;
            bounds.set(glowExtension, glowExtension,
                    getWidth() - glowExtension, getHeight() - glowExtension);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!bounds.isEmpty()) {
            // Create turbulent border path
            createTurbulentPath();

            // Draw layers from back to front (behind children)
            canvas.drawPath(borderPath, backgroundGlowPaint);
            canvas.drawPath(borderPath, glowPaint2);
            canvas.drawPath(borderPath, glowPaint1);
            canvas.drawPath(borderPath, strokePaint);
        }

        // Draw children on top
        super.dispatchDraw(canvas);
    }

    /**
     * Creates a path with turbulent displacement effect
     * Simulates the SVG turbulence filter with displacement mapping
     */
    private void createTurbulentPath() {
        borderPath.reset();

        float width = bounds.width();
        float height = bounds.height();
        float left = bounds.left;
        float top = bounds.top;
        float right = left + width;
        float bottom = top + height;

        // Create a rounded rectangle path as base
        RectF baseRect = new RectF(left, top, right, bottom);
        Path basePath = new Path();
        basePath.addRoundRect(baseRect, cornerRadius, cornerRadius, Path.Direction.CW);

        // Sample points along the rounded rectangle path
        android.graphics.PathMeasure pathMeasure = new android.graphics.PathMeasure(basePath, false);
        float pathLength = pathMeasure.getLength();
        float[] pos = new float[2];
        float[] tan = new float[2];

        boolean firstPoint = true;

        for (int i = 0; i <= TURBULENCE_POINTS; i++) {
            float t = (i / (float) TURBULENCE_POINTS);
            float distance = t * pathLength;

            // Get position and tangent at this point on the path
            pathMeasure.getPosTan(distance, pos, tan);

            float x = pos[0];
            float y = pos[1];

            // Calculate normal (perpendicular to tangent)
            float normalX = -tan[1];
            float normalY = tan[0];

            // Normalize the normal vector
            float normalLength = (float) Math.sqrt(normalX * normalX + normalY * normalY);
            if (normalLength > 0.001f) {
                normalX /= normalLength;
                normalY /= normalLength;
            }

            // Apply turbulence displacement
            int offsetIndex = i % TURBULENCE_POINTS;
            float displacement = turbulenceOffsets[offsetIndex];

            // Apply displacement perpendicular to the edge
            x += normalX * displacement;
            y += normalY * displacement;

            if (firstPoint) {
                borderPath.moveTo(x, y);
                firstPoint = false;
            } else {
                borderPath.lineTo(x, y);
            }
        }

        borderPath.close();
    }

    private int adjustAlpha(int color, float alpha) {
        int a = Math.round(Color.alpha(color) * alpha);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(a, r, g, b);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (animator != null && !animator.isRunning()) {
            animator.start();
        }
    }
}
