package com.cookpilot.university.features.recipes.presentation.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.cookpilot.university.CookPilotApplication;
import com.cookpilot.university.R;
import com.cookpilot.university.core.design.icons.CookIcons;
import com.cookpilot.university.databinding.ActivityRecipeDetailBinding;
import com.cookpilot.university.features.recipes.domain.model.Ingredient;
import com.cookpilot.university.features.recipes.domain.model.NutritionInfo;
import com.cookpilot.university.features.recipes.domain.model.Recipe;
import com.cookpilot.university.features.recipes.domain.model.RecipeDetail;
import com.cookpilot.university.features.recipes.domain.model.RecipeStep;
import com.cookpilot.university.features.recipes.presentation.viewmodel.RecipeDetailViewModel;
import com.cookpilot.university.features.recipes.presentation.viewmodel.RecipeDetailViewModelFactory;

import java.util.Locale;

public final class RecipeDetailActivity extends AppCompatActivity {

    private static final String EXTRA_RECIPE_ID = "recipe_id";
    private static final String EXTRA_SERVINGS = "servings";

    private ActivityRecipeDetailBinding binding;
    private RecipeDetailViewModel viewModel;
    private RecipeDetail currentDetail;
    private int currentServings = 1;

    @NonNull
    public static Intent createIntent(
            @NonNull Context context,
            @NonNull String recipeId,
            int servings
    ) {
        return new Intent(context, RecipeDetailActivity.class)
                .putExtra(EXTRA_RECIPE_ID, recipeId)
                .putExtra(EXTRA_SERVINGS, Math.max(1, servings));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableFullscreen();

        binding = ActivityRecipeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String recipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        if (recipeId == null || recipeId.trim().isEmpty()) {
            finish();
            return;
        }

        CookPilotApplication application =
                (CookPilotApplication) getApplication();

        viewModel = new ViewModelProvider(
                this,
                new RecipeDetailViewModelFactory(
                        application.getRecipeRepository(),
                        recipeId,
                        getIntent().getIntExtra(EXTRA_SERVINGS, 1)
                )
        ).get(RecipeDetailViewModel.class);

        configureUi();
        observeState();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars();
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

    private void configureUi() {
        binding.backButton.setImageResource(CookIcons.back());
        binding.metricMoneyIcon.setImageResource(CookIcons.money());
        binding.metricTimeIcon.setImageResource(CookIcons.timer());
        binding.metricNutritionIcon.setImageResource(CookIcons.nutrition());
        binding.ingredientsIcon.setImageResource(CookIcons.ingredients());
        binding.stepsIcon.setImageResource(CookIcons.steps());
        binding.nutritionIcon.setImageResource(CookIcons.nutrition());

        binding.backButton.setOnClickListener(
                view -> getOnBackPressedDispatcher().onBackPressed()
        );
        binding.decreaseServingsButton.setOnClickListener(
                view -> viewModel.decreaseServings()
        );
        binding.increaseServingsButton.setOnClickListener(
                view -> viewModel.increaseServings()
        );

        binding.tabGeneral.setOnClickListener(view -> selectTab(0));
        binding.tabRecipe.setOnClickListener(view -> selectTab(1));
        binding.tabNutrition.setOnClickListener(view -> selectTab(2));
        selectTab(0);

        binding.cookButton.setOnClickListener(view ->
                Toast.makeText(
                        this,
                        R.string.recipe_detail_cookmode_next,
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    private void observeState() {
        viewModel.getDetail().observe(this, detail -> {
            currentDetail = detail;
            renderDetail();
        });
        viewModel.getServings().observe(this, servings -> {
            currentServings = servings == null ? 1 : Math.max(1, servings);
            renderScaledContent();
        });
    }

    private void renderDetail() {
        if (currentDetail == null) {
            return;
        }

        Recipe recipe = currentDetail.getRecipe();
        binding.recipeTitle.setText(recipe.getTitle());
        binding.recipeDescription.setText(recipe.getDescription());

        Glide.with(binding.heroImage)
                .load(recipe.getImageUrl())
                .centerCrop()
                .into(binding.heroImage);

        renderSteps(currentDetail);
        renderScaledContent();
    }

    private void renderScaledContent() {
        if (currentDetail == null) {
            return;
        }

        Recipe recipe = currentDetail.getRecipe();
        double scale = (double) currentServings
                / Math.max(1, recipe.getBaseServings());

        binding.servingsValue.setText(
                getString(R.string.recipe_detail_servings, currentServings)
        );
        binding.decreaseServingsButton.setEnabled(currentServings > 1);
        binding.decreaseServingsButton.setAlpha(
                currentServings > 1 ? 1f : 0.45f
        );

        binding.metricMoneyValue.setText(
                String.format(
                        Locale.US,
                        "S/ %.1f",
                        recipe.getEstimatedCostPen() * scale
                )
        );
        binding.metricMoneySubtitle.setText(
                getString(
                        R.string.recipe_detail_savings,
                        recipe.getEstimatedSavingsPen() * scale
                )
        );
        binding.metricTimeValue.setText(
                getString(
                        R.string.recipe_detail_minutes,
                        recipe.getTotalMinutes()
                )
        );
        binding.metricTimeSubtitle.setText(
                getString(
                        R.string.recipe_detail_rest_minutes,
                        recipe.getPassiveMinutes()
                )
        );
        binding.metricNutritionValue.setText(
                formatNumber(
                        currentDetail.getNutrition().getCalories() * scale
                ) + " kcal"
        );
        binding.metricNutritionSubtitle.setText(
                R.string.recipe_detail_nutrition
        );

        renderIngredients(currentDetail, scale);
        renderNutrition(currentDetail.getNutrition(), scale);
    }

    private void renderIngredients(
            @NonNull RecipeDetail detail,
            double scale
    ) {
        binding.ingredientsList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Ingredient ingredient : detail.getIngredients()) {
            View row = inflater.inflate(
                    R.layout.item_recipe_detail_ingredient,
                    binding.ingredientsList,
                    false
            );
            ImageView image = row.findViewById(R.id.ingredientImage);
            TextView name = row.findViewById(R.id.ingredientName);
            TextView quantity = row.findViewById(R.id.ingredientQuantity);
            TextView optional = row.findViewById(R.id.ingredientOptional);

            name.setText(ingredient.getName());
            quantity.setText(
                    formatQuantity(
                            ingredient.getQuantity() * scale,
                            ingredient.getUnit()
                    )
            );
            optional.setVisibility(
                    ingredient.isOptional() ? View.VISIBLE : View.GONE
            );

            Glide.with(image)
                    .load(ingredient.getImageUrl())
                    .centerCrop()
                    .into(image);

            binding.ingredientsList.addView(row);
        }
    }

    private void renderSteps(@NonNull RecipeDetail detail) {
        binding.stepsList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (RecipeStep step : detail.getSteps()) {
            View row = inflater.inflate(
                    R.layout.item_recipe_detail_step,
                    binding.stepsList,
                    false
            );
            ((TextView) row.findViewById(R.id.stepLabel)).setText(
                    getString(
                            R.string.recipe_detail_step_number,
                            step.getNumber()
                    )
            );
            ((TextView) row.findViewById(R.id.stepInstruction))
                    .setText(step.getInstruction());
            binding.stepsList.addView(row);
        }
    }

    private void renderNutrition(
            @NonNull NutritionInfo nutrition,
            double scale
    ) {
        binding.nutritionList.removeAllViews();
        addNutrition(
                R.string.recipe_detail_calories,
                formatNumber(nutrition.getCalories() * scale) + " kcal"
        );
        addNutrition(
                R.string.recipe_detail_protein,
                formatNumber(nutrition.getProteinG() * scale) + " g"
        );
        addNutrition(
                R.string.recipe_detail_carbs,
                formatNumber(nutrition.getCarbsG() * scale) + " g"
        );
        addNutrition(
                R.string.recipe_detail_fat,
                formatNumber(nutrition.getFatG() * scale) + " g"
        );
        addNutrition(
                R.string.recipe_detail_fiber,
                formatNumber(nutrition.getFiberG() * scale) + " g"
        );
    }

    private void addNutrition(int labelRes, @NonNull String value) {
        View row = LayoutInflater.from(this).inflate(
                R.layout.item_recipe_detail_nutrition,
                binding.nutritionList,
                false
        );
        ((TextView) row.findViewById(R.id.nutritionLabel))
                .setText(labelRes);
        ((TextView) row.findViewById(R.id.nutritionValue))
                .setText(value);
        binding.nutritionList.addView(row);
    }

    private void selectTab(int selectedTab) {
        boolean general = selectedTab == 0;
        boolean recipe = selectedTab == 1;
        boolean nutrition = selectedTab == 2;

        binding.generalSection.setVisibility(
                general ? View.VISIBLE : View.GONE
        );
        binding.recipeSection.setVisibility(
                recipe ? View.VISIBLE : View.GONE
        );
        binding.nutritionSection.setVisibility(
                nutrition ? View.VISIBLE : View.GONE
        );

        applyTabState(binding.tabGeneral, general);
        applyTabState(binding.tabRecipe, recipe);
        applyTabState(binding.tabNutrition, nutrition);
    }

    private void applyTabState(
            @NonNull TextView tab,
            boolean selected
    ) {
        tab.setBackgroundResource(
                selected
                        ? R.drawable.bg_recipe_detail_tab_selected
                        : android.R.color.transparent
        );
        tab.setTextColor(
                ContextCompat.getColor(
                        this,
                        selected
                                ? R.color.cook_dark
                                : R.color.cook_text_primary
                )
        );
    }

    @NonNull
    private String formatQuantity(double value, @NonNull String unit) {
        String displayUnit = "unit".equals(unit) ? "u" : unit;
        return formatNumber(value) + " " + displayUnit;
    }

    @NonNull
    private String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.01) {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(Locale.US, "%.1f", value);
    }
}
