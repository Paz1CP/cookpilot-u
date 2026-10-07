package com.cookpilot.university.features.cookmode.presentation.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.cookpilot.university.CookPilotApplication;
import com.cookpilot.university.R;
import com.cookpilot.university.core.design.icons.CookIcons;
import com.cookpilot.university.core.utils.TextUtil;
import com.cookpilot.university.databinding.ActivityCookModeBinding;
import com.cookpilot.university.features.recipes.domain.model.Ingredient;
import com.cookpilot.university.features.recipes.domain.model.Recipe;
import com.cookpilot.university.features.recipes.domain.model.RecipeDetail;
import com.cookpilot.university.features.recipes.domain.model.RecipeStep;
import com.cookpilot.university.features.recipes.domain.usecase.ScaleRecipeServings;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class CookModeActivity extends AppCompatActivity {

    private static final String EXTRA_RECIPE_IDS = "recipe_ids";
    private static final String EXTRA_SERVINGS = "servings";
    private static final int MAX_TIMERS = 4;

    private ActivityCookModeBinding binding;
    private final List<CookingStep> steps = new ArrayList<>();
    private final List<ManualTimer> timers = new ArrayList<>();
    private final Map<String, Integer> servingsByRecipe =
            new LinkedHashMap<>();

    private int currentStepIndex;
    private Handler timerHandler;
    private final ToneGenerator timerTone =
            new ToneGenerator(AudioManager.STREAM_ALARM, 80);

    private final Runnable timerTicker = new Runnable() {
        @Override
        public void run() {
            renderTimers();
            timerHandler.postDelayed(this, 1000);
        }
    };

    @NonNull
    public static Intent createIntent(
            @NonNull Context context,
            @NonNull List<String> recipeIds,
            @NonNull List<Integer> servings
    ) {
        return new Intent(context, CookModeActivity.class)
                .putStringArrayListExtra(
                        EXTRA_RECIPE_IDS,
                        new ArrayList<>(recipeIds)
                )
                .putIntegerArrayListExtra(
                        EXTRA_SERVINGS,
                        new ArrayList<>(servings)
                );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableFullscreen();
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        binding = ActivityCookModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!loadSession()) {
            finish();
            return;
        }

        timerHandler = new Handler(Looper.getMainLooper());
        configureUi();
        renderStep();
        timerHandler.post(timerTicker);
    }

    @Override
    protected void onDestroy() {
        if (timerHandler != null) {
            timerHandler.removeCallbacksAndMessages(null);
        }
        timerTone.release();
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars();
        }
    }

    private boolean loadSession() {
        ArrayList<String> recipeIds =
                getIntent().getStringArrayListExtra(EXTRA_RECIPE_IDS);
        ArrayList<Integer> portions =
                getIntent().getIntegerArrayListExtra(EXTRA_SERVINGS);

        if (recipeIds == null || recipeIds.isEmpty()) {
            return false;
        }
        if (portions == null) {
            portions = new ArrayList<>();
        }

        CookPilotApplication application =
                (CookPilotApplication) getApplication();

        for (int index = 0; index < recipeIds.size(); index++) {
            String recipeId = recipeIds.get(index);
            int requestedServings = index < portions.size()
                    ? portions.get(index)
                    : 0;

            RecipeDetail detail = application
                    .getRecipeRepository()
                    .getRecipeDetail(recipeId)
                    .orElse(null);

            if (detail == null) {
                continue;
            }

            Recipe recipe = detail.getRecipe();
            int servings = requestedServings > 0
                    ? requestedServings
                    : recipe.getBaseServings();

            servingsByRecipe.put(
                    recipe.getId(),
                    Math.max(1, servings)
            );

            for (RecipeStep step : detail.getSteps()) {
                steps.add(new CookingStep(detail, step));
            }
        }

        return !steps.isEmpty();
    }

    private void configureUi() {
        binding.exitIcon.setImageResource(CookIcons.back());
        binding.headerTimerIcon.setImageResource(CookIcons.timer());
        binding.previousStepButton.setIconResource(
                CookIcons.previous()
        );
        binding.nextStepButton.setIconResource(CookIcons.next());
        binding.timerAction.setIconResource(CookIcons.timer());
        binding.ingredientsAction.setIconResource(
                CookIcons.ingredients()
        );
        binding.cleanAction.setIconResource(
                CookIcons.cleanScreen()
        );
        binding.finishAction.setIconResource(CookIcons.finish());

        binding.exitButton.setOnClickListener(
                view -> confirmExit()
        );
        binding.previousStepButton.setOnClickListener(
                view -> moveToStep(currentStepIndex - 1)
        );
        binding.nextStepButton.setOnClickListener(view -> {
            if (currentStepIndex >= steps.size() - 1) {
                confirmFinish();
            } else {
                moveToStep(currentStepIndex + 1);
            }
        });

        binding.timerAction.setOnClickListener(
                view -> showTimerSheet(null)
        );
        binding.ingredientsAction.setOnClickListener(
                view -> showIngredients()
        );
        binding.cleanAction.setOnClickListener(
                view -> showCleanScreen()
        );
        binding.finishAction.setOnClickListener(
                view -> confirmFinish()
        );

        loadMascot();
    }

    private void renderStep() {
        if (steps.isEmpty()) {
            return;
        }

        CookingStep current = steps.get(currentStepIndex);
        Recipe recipe = current.detail.getRecipe();

        binding.currentRecipeTitle.setText(recipe.getTitle());
        binding.currentStepMeta.setText(
                getString(
                        R.string.cooking_step_progress,
                        currentStepIndex + 1,
                        steps.size()
                )
        );
        binding.headerMinutes.setText(
                getString(
                        R.string.cooking_minutes,
                        recipe.getTotalMinutes()
                )
        );

        binding.instructionText.setText(
                TextUtil.boldMarkdown(
                        current.step.getInstruction()
                )
        );

        Glide.with(binding.recipeThumb)
                .load(recipe.getImageUrl())
                .circleCrop()
                .into(binding.recipeThumb);

        binding.previousStepButton.setEnabled(currentStepIndex > 0);
        binding.previousStepButton.setAlpha(
                currentStepIndex > 0 ? 1f : 0.4f
        );

        boolean last = currentStepIndex == steps.size() - 1;
        binding.nextStepButton.setText(
                last
                        ? R.string.cooking_finish
                        : R.string.cooking_next
        );
        binding.nextStepButton.setIconResource(
                last ? CookIcons.flag() : CookIcons.next()
        );

        renderStepRail();
    }

    private void renderStepRail() {
        binding.stepRail.removeAllViews();

        for (int index = 0; index < steps.size(); index++) {
            TextView cell = new TextView(this);
            cell.setGravity(Gravity.CENTER);
            cell.setTextAppearance(
                    R.style.TextAppearance_CookPilot_TitleL
            );

            boolean completed = index < currentStepIndex;
            boolean current = index == currentStepIndex;

            int backgroundColor;
            if (current) {
                backgroundColor = R.color.cook_primary;
            } else if (completed) {
                backgroundColor = R.color.cook_secondary;
            } else {
                backgroundColor = R.color.cook_layer_floating;
            }

            cell.setTextColor(
                    ContextCompat.getColor(
                            this,
                            current || completed
                                    ? R.color.cook_dark
                                    : R.color.cook_text_primary
                    )
            );
            cell.setBackground(
                    roundedBackground(
                            ContextCompat.getColor(
                                    this,
                                    backgroundColor
                            ),
                            dp(36)
                    )
            );
            cell.setText(String.valueOf(index + 1));

            int target = index;
            cell.setOnClickListener(
                    view -> moveToStep(target)
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            dp(72),
                            dp(72)
                    );
            params.setMarginEnd(dp(12));
            binding.stepRail.addView(cell, params);
        }

        TextView finishCell = new TextView(this);
        finishCell.setGravity(Gravity.CENTER);
        finishCell.setBackground(
                roundedBackground(
                        ContextCompat.getColor(
                                this,
                                R.color.cook_accent
                        ),
                        dp(36)
                )
        );
        finishCell.setCompoundDrawablesWithIntrinsicBounds(
                0,
                CookIcons.flag(),
                0,
                0
        );
        finishCell.setOnClickListener(
                view -> confirmFinish()
        );

        LinearLayout.LayoutParams finishParams =
                new LinearLayout.LayoutParams(
                        dp(72),
                        dp(72)
                );
        binding.stepRail.addView(finishCell, finishParams);

        binding.stepRailScroll.post(() -> {
            int x = Math.max(
                    0,
                    (currentStepIndex * dp(84)) - dp(64)
            );
            binding.stepRailScroll.smoothScrollTo(x, 0);
        });
    }

    private void moveToStep(int index) {
        if (index < 0 || index >= steps.size()) {
            return;
        }
        currentStepIndex = index;
        renderStep();
    }

    private void showIngredients() {
        CookingStep current = steps.get(currentStepIndex);
        Recipe recipe = current.detail.getRecipe();
        int servings = servingsByRecipe.getOrDefault(
                recipe.getId(),
                recipe.getBaseServings()
        );
        double scale = ScaleRecipeServings.scaleQuantity(
                1.0,
                servings,
                recipe.getBaseServings()
        );

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(20), dp(24), dp(32));
        content.setBackgroundColor(
                ContextCompat.getColor(
                        this,
                        R.color.cook_layer_raised
                )
        );

        TextView title = new TextView(this);
        title.setText(R.string.cooking_ingredients);
        title.setTextAppearance(
                R.style.TextAppearance_CookPilot_TitleL
        );
        content.addView(title);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);

        for (Ingredient ingredient : current.detail.getIngredients()) {
            View row = LayoutInflater.from(this).inflate(
                    R.layout.item_recipe_detail_ingredient,
                    list,
                    false
            );
            ImageView image = row.findViewById(
                    R.id.ingredientImage
            );
            TextView name = row.findViewById(
                    R.id.ingredientName
            );
            TextView quantity = row.findViewById(
                    R.id.ingredientQuantity
            );
            TextView optional = row.findViewById(
                    R.id.ingredientOptional
            );

            name.setText(ingredient.getName());
            quantity.setText(
                    formatQuantity(
                            ingredient.getQuantity() * scale,
                            ingredient.getUnit()
                    )
            );
            optional.setVisibility(
                    ingredient.isOptional()
                            ? View.VISIBLE
                            : View.GONE
            );
            Glide.with(image)
                    .load(ingredient.getImageUrl())
                    .circleCrop()
                    .into(image);
            list.addView(row);
        }

        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(420)
                );
        scrollParams.topMargin = dp(16);
        content.addView(scroll, scrollParams);

        dialog.setContentView(content);
        dialog.show();
    }

    private void showCleanScreen() {
        Dialog dialog = new Dialog(
                this,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen
        );

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(dp(32), dp(48), dp(32), dp(48));
        content.setBackgroundColor(
                ContextCompat.getColor(this, R.color.cook_dark)
        );

        TextView bubble = new TextView(this);
        bubble.setText("🫧");
        bubble.setTextSize(112);
        bubble.setGravity(Gravity.CENTER);
        content.addView(bubble);

        TextView title = new TextView(this);
        title.setText(R.string.cooking_clean_title);
        title.setTextAppearance(
                R.style.TextAppearance_CookPilot_TitleXL
        );
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        titleParams.topMargin = dp(20);
        content.addView(title, titleParams);

        TextView subtitle = new TextView(this);
        subtitle.setText(R.string.cooking_clean_subtitle);
        subtitle.setTextAppearance(
                R.style.TextAppearance_CookPilot_Body
        );
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        subtitleParams.topMargin = dp(12);
        content.addView(subtitle, subtitleParams);

        MaterialButton unlock = new MaterialButton(
                this,
                null,
                com.google.android.material.R.attr.materialButtonStyle
        );
        unlock.setText(R.string.cooking_clean_unlock);
        unlock.setTextColor(Color.WHITE);
        unlock.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(this, R.color.cook_blue)
                )
        );
        unlock.setOnClickListener(view ->
                Toast.makeText(
                        this,
                        R.string.cooking_clean_hold_hint,
                        Toast.LENGTH_SHORT
                ).show()
        );
        unlock.setOnLongClickListener(view -> {
            dialog.dismiss();
            return true;
        });

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(64)
                );
        buttonParams.topMargin = dp(48);
        content.addView(unlock, buttonParams);

        dialog.setContentView(content);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showTimerSheet(@Nullable ManualTimer timer) {
        if (timer == null && timers.size() >= MAX_TIMERS) {
            Toast.makeText(
                    this,
                    R.string.cooking_timer_limit,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = LayoutInflater.from(this).inflate(
                R.layout.sheet_cooking_timer,
                null,
                false
        );

        EditText titleInput = sheet.findViewById(
                R.id.timerTitleInput
        );
        NumberPicker hoursPicker = sheet.findViewById(
                R.id.hoursPicker
        );
        NumberPicker minutesPicker = sheet.findViewById(
                R.id.minutesPicker
        );
        NumberPicker secondsPicker = sheet.findViewById(
                R.id.secondsPicker
        );
        MaterialButton deleteButton = sheet.findViewById(
                R.id.timerDeleteButton
        );
        MaterialButton primaryButton = sheet.findViewById(
                R.id.timerPrimaryButton
        );

        configurePicker(hoursPicker, 0, 12, " h");
        configurePicker(minutesPicker, 0, 59, " m");
        configurePicker(secondsPicker, 0, 59, " s");

        int initialSeconds = timer == null
                ? 60
                : timer.remainingSeconds(
                        System.currentTimeMillis()
                );

        hoursPicker.setValue(initialSeconds / 3600);
        minutesPicker.setValue((initialSeconds % 3600) / 60);
        secondsPicker.setValue(initialSeconds % 60);

        titleInput.setText(
                timer == null
                        ? getString(
                                R.string.cooking_timer_default_name,
                                timers.size() + 1
                        )
                        : timer.title
        );

        deleteButton.setVisibility(
                timer == null ? View.GONE : View.VISIBLE
        );
        primaryButton.setText(
                timer == null
                        ? R.string.cooking_timer_start
                        : R.string.cooking_timer_update
        );

        deleteButton.setOnClickListener(view -> {
            timers.remove(timer);
            dialog.dismiss();
            renderTimers();
        });

        primaryButton.setOnClickListener(view -> {
            int duration =
                    (hoursPicker.getValue() * 3600)
                            + (minutesPicker.getValue() * 60)
                            + secondsPicker.getValue();

            if (duration <= 0) {
                Toast.makeText(
                        this,
                        R.string.cooking_timer_invalid,
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String title = titleInput.getText()
                    .toString()
                    .trim();

            if (timer == null) {
                timers.add(
                        ManualTimer.create(
                                title.isEmpty()
                                        ? getString(
                                                R.string.cooking_timer_default_name,
                                                timers.size() + 1
                                        )
                                        : title,
                                duration
                        )
                );
            } else {
                timer.update(
                        title.isEmpty() ? timer.title : title,
                        duration
                );
            }

            dialog.dismiss();
            renderTimers();
        });

        dialog.setContentView(sheet);
        dialog.show();
    }

    private void configurePicker(
            @NonNull NumberPicker picker,
            int min,
            int max,
            @NonNull String suffix
    ) {
        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setWrapSelectorWheel(true);
        picker.setFormatter(value -> value + suffix);
    }

    private void renderTimers() {
        if (binding == null) {
            return;
        }

        binding.timerOverlay.removeAllViews();
        long now = System.currentTimeMillis();

        for (int index = 0; index < timers.size(); index++) {
            ManualTimer timer = timers.get(index);
            View card = LayoutInflater.from(this).inflate(
                    R.layout.item_cooking_timer,
                    binding.timerOverlay,
                    false
            );

            ImageView icon = card.findViewById(
                    R.id.timerControlIcon
            );
            TextView time = card.findViewById(
                    R.id.timerRemaining
            );
            TextView title = card.findViewById(
                    R.id.timerTitle
            );

            int remaining = timer.remainingSeconds(now);
            boolean warning = remaining > 0 && remaining <= 20;
            boolean expired = remaining == 0;

            icon.setImageResource(
                    expired
                            ? CookIcons.timer()
                            : timer.paused
                            ? CookIcons.play()
                            : CookIcons.timerPause()
            );
            icon.setColorFilter(
                    ContextCompat.getColor(
                            this,
                            expired || warning
                                    ? R.color.cook_warning
                                    : timer.paused
                                    ? R.color.cook_blue
                                    : R.color.cook_accent
                    )
            );
            time.setText(formatTimer(remaining));
            time.setTextColor(
                    ContextCompat.getColor(
                            this,
                            warning || expired
                                    ? R.color.cook_warning
                                    : R.color.cook_text_primary
                    )
            );
            title.setText(timer.title);

            icon.setOnClickListener(view -> {
                timer.togglePause();
                renderTimers();
            });
            card.setOnClickListener(
                    view -> showTimerSheet(timer)
            );

            if (expired && !timer.alertPlayed) {
                timer.alertPlayed = true;
                timerTone.startTone(
                        ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
                        700
                );
            }

            FrameLayout.LayoutParams params =
                    new FrameLayout.LayoutParams(
                            dp(188),
                            dp(76),
                            Gravity.END | Gravity.BOTTOM
                    );
            params.bottomMargin =
                    dp(188) + (index * dp(84));
            binding.timerOverlay.addView(card, params);
        }
    }

    private void confirmExit() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cooking_exit_title)
                .setMessage(R.string.cooking_exit_message)
                .setNegativeButton(R.string.auth_cancel, null)
                .setPositiveButton(
                        R.string.cooking_exit,
                        (dialog, which) -> finish()
                )
                .show();
    }

    private void confirmFinish() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cooking_finish_title)
                .setMessage(R.string.cooking_finish_message)
                .setNegativeButton(R.string.auth_cancel, null)
                .setPositiveButton(
                        R.string.cooking_finish,
                        (dialog, which) -> finish()
                )
                .show();
    }

    private void loadMascot() {
        try (InputStream stream = getAssets().open(
                "images/cookpilot.png"
        )) {
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            binding.mascotImage.setImageBitmap(bitmap);
        } catch (IOException ignored) {
        }
    }

    private void enableFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
            getWindow().setStatusBarContrastEnforced(false);
        }

        hideSystemBars();
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        getWindow(),
                        getWindow().getDecorView()
                );
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
    }

    @NonNull
    private android.graphics.drawable.GradientDrawable roundedBackground(
            int color,
            int radius
    ) {
        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    @NonNull
    private String formatQuantity(
            double value,
            @NonNull String unit
    ) {
        String number = Math.abs(value - Math.rint(value)) < 0.01
                ? String.valueOf((int) Math.rint(value))
                : String.format(Locale.US, "%.1f", value);
        return number + " " + ("unit".equals(unit) ? "u" : unit);
    }

    @NonNull
    private String formatTimer(int seconds) {
        int safe = Math.max(0, seconds);
        int hours = safe / 3600;
        int minutes = (safe % 3600) / 60;
        int rest = safe % 60;

        return hours > 0
                ? String.format(
                        Locale.US,
                        "%d:%02d:%02d",
                        hours,
                        minutes,
                        rest
                )
                : String.format(
                        Locale.US,
                        "%02d:%02d",
                        minutes,
                        rest
                );
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }

    private static final class CookingStep {
        final RecipeDetail detail;
        final RecipeStep step;

        CookingStep(
                @NonNull RecipeDetail detail,
                @NonNull RecipeStep step
        ) {
            this.detail = detail;
            this.step = step;
        }
    }

    private static final class ManualTimer {
        final String id;
        String title;
        int totalDurationSeconds;
        long startedAtMillis;
        boolean paused;
        int pausedRemainingSeconds;
        boolean alertPlayed;

        private ManualTimer(
                @NonNull String id,
                @NonNull String title,
                int totalDurationSeconds,
                long startedAtMillis
        ) {
            this.id = id;
            this.title = title;
            this.totalDurationSeconds = totalDurationSeconds;
            this.startedAtMillis = startedAtMillis;
        }

        static ManualTimer create(
                @NonNull String title,
                int durationSeconds
        ) {
            return new ManualTimer(
                    UUID.randomUUID().toString(),
                    title,
                    Math.max(1, durationSeconds),
                    System.currentTimeMillis()
            );
        }

        int remainingSeconds(long now) {
            if (paused) {
                return Math.max(0, pausedRemainingSeconds);
            }

            long elapsed = Math.max(
                    0,
                    (now - startedAtMillis) / 1000
            );
            return Math.max(
                    0,
                    totalDurationSeconds - (int) elapsed
            );
        }

        void togglePause() {
            long now = System.currentTimeMillis();

            if (remainingSeconds(now) == 0) {
                return;
            }

            if (paused) {
                int elapsed =
                        totalDurationSeconds
                                - pausedRemainingSeconds;
                startedAtMillis =
                        now - (elapsed * 1000L);
                paused = false;
            } else {
                pausedRemainingSeconds =
                        remainingSeconds(now);
                paused = true;
            }
        }

        void update(
                @NonNull String title,
                int durationSeconds
        ) {
            this.title = title;
            totalDurationSeconds = Math.max(1, durationSeconds);
            startedAtMillis = System.currentTimeMillis();
            paused = false;
            pausedRemainingSeconds = 0;
            alertPlayed = false;
        }
    }
}