package com.cookpilot.university.core.design.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.cookpilot.university.R;

public final class CookTextRevealShimmerView extends AppCompatTextView {

    private static final float REVEAL_BAND_SIZE = 0.75f;
    private static final float AMBIENT_BAND_SIZE = 0.50f;
    private static final float SHIMMER_OPACITY = 0.85f;

    private ValueAnimator revealAnimator;
    private ValueAnimator ambientAnimator;

    private float revealProgress = 0f;
    private float shimmerProgress = 0f;
    private boolean revealPhase = true;
    private boolean revealCompletionDispatched = false;
    private boolean revealCancelled = false;

    private long revealDurationMs = CookAnimationTokens.SPLASH_REVEAL_MS;
    private long shimmerDurationMs = CookAnimationTokens.SPLASH_SHIMMER_MS;
    private int shimmerColor;
    private Runnable onRevealCompleted;

    public CookTextRevealShimmerView(Context context) {
        this(context, null);
    }

    public CookTextRevealShimmerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CookTextRevealShimmerView(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        shimmerColor = ContextCompat.getColor(context, R.color.cook_primary);
        setIncludeFontPadding(false);
    }

    public void setBrandTitle() {
        String cook = getContext().getString(R.string.brand_cook);
        String pilot = getContext().getString(R.string.brand_pilot);
        SpannableString title = new SpannableString(cook + pilot);

        title.setSpan(
                new ForegroundColorSpan(
                        ContextCompat.getColor(getContext(), R.color.cook_light)
                ),
                0,
                cook.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        title.setSpan(
                new ForegroundColorSpan(
                        ContextCompat.getColor(getContext(), R.color.cook_primary)
                ),
                cook.length(),
                title.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        setText(title);
    }

    public void setOnRevealCompleted(@Nullable Runnable listener) {
        onRevealCompleted = listener;
    }

    public void setRevealDurationMs(long durationMs) {
        revealDurationMs = Math.max(0L, durationMs);
    }

    public void setShimmerDurationMs(long durationMs) {
        shimmerDurationMs = Math.max(1L, durationMs);
    }

    public void setShimmerColor(int color) {
        shimmerColor = color;
        invalidate();
    }

    public void startReveal() {
        cancelAnimations();
        revealCompletionDispatched = false;
        revealCancelled = false;

        if (!ValueAnimator.areAnimatorsEnabled()) {
            revealProgress = 1f;
            shimmerProgress = 0.5f;
            revealPhase = false;
            invalidate();
            dispatchRevealCompletion();
            return;
        }

        revealPhase = true;
        revealProgress = 0f;
        shimmerProgress = 0f;

        revealAnimator = ValueAnimator.ofFloat(0f, 1f);
        revealAnimator.setDuration(revealDurationMs);
        revealAnimator.addUpdateListener(animator -> {
            float progress = (float) animator.getAnimatedValue();
            revealProgress = progress;
            shimmerProgress = progress;
            postInvalidateOnAnimation();
        });
        revealAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                revealCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (revealCancelled) {
                    return;
                }

                revealProgress = 1f;
                revealPhase = false;
                dispatchRevealCompletion();
                startAmbientShimmer();
            }
        });
        revealAnimator.start();
    }

    public void stopShimmer() {
        if (ambientAnimator != null) {
            ambientAnimator.cancel();
            ambientAnimator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelAnimations();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int revealSave = canvas.save();
        canvas.clipRect(0f, 0f, getWidth() * revealProgress, getHeight());

        super.onDraw(canvas);
        drawShimmerOverlay(canvas);

        canvas.restoreToCount(revealSave);
    }

    private void startAmbientShimmer() {
        if (!isAttachedToWindow() || !ValueAnimator.areAnimatorsEnabled()) {
            return;
        }

        ambientAnimator = ValueAnimator.ofFloat(0f, 1f);
        ambientAnimator.setDuration(shimmerDurationMs);
        ambientAnimator.setRepeatCount(ValueAnimator.INFINITE);
        ambientAnimator.addUpdateListener(animator -> {
            shimmerProgress = (float) animator.getAnimatedValue();
            postInvalidateOnAnimation();
        });
        ambientAnimator.start();
    }

    private void drawShimmerOverlay(Canvas canvas) {
        if (getLayout() == null || getWidth() <= 0) {
            return;
        }

        float bandSize = revealPhase
                ? REVEAL_BAND_SIZE
                : AMBIENT_BAND_SIZE;
        float sweepCenter = -0.20f + (shimmerProgress * 1.40f);
        float centerX = sweepCenter * getWidth();
        float halfBand = Math.max(
                getWidth() * bandSize * 0.5f,
                1f
        );

        int transparent = ColorUtils.setAlphaComponent(shimmerColor, 0);
        int soft = ColorUtils.setAlphaComponent(
                shimmerColor,
                Math.round(255f * SHIMMER_OPACITY * 0.30f)
        );
        int strong = ColorUtils.setAlphaComponent(
                shimmerColor,
                Math.round(255f * SHIMMER_OPACITY)
        );

        Shader previousShader = getPaint().getShader();
        getPaint().setShader(new LinearGradient(
                centerX - halfBand,
                0f,
                centerX + halfBand,
                0f,
                new int[]{transparent, soft, strong, soft, transparent},
                new float[]{0f, 0.25f, 0.5f, 0.75f, 1f},
                Shader.TileMode.CLAMP
        ));

        int layoutSave = canvas.save();
        canvas.translate(
                getCompoundPaddingLeft(),
                getExtendedPaddingTop()
        );
        getLayout().draw(canvas);
        canvas.restoreToCount(layoutSave);

        getPaint().setShader(previousShader);
    }

    private void dispatchRevealCompletion() {
        if (revealCompletionDispatched) {
            return;
        }

        revealCompletionDispatched = true;
        if (onRevealCompleted != null) {
            post(onRevealCompleted);
        }
    }

    private void cancelAnimations() {
        if (revealAnimator != null) {
            revealAnimator.cancel();
            revealAnimator = null;
        }

        if (ambientAnimator != null) {
            ambientAnimator.cancel();
            ambientAnimator = null;
        }
    }
}
