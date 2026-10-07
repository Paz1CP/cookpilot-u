package com.cookpilot.university.features.recipes.presentation.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.cookpilot.university.core.utils.TextUtil;
import com.cookpilot.university.databinding.ActivityRecipeDetailBinding;
import com.cookpilot.university.features.cookplan.domain.model.MealMoment;
import com.cookpilot.university.features.cookplan.domain.model.PlannedRecipe;
import com.cookpilot.university.features.cookmode.presentation.activity.CookModeActivity;
import com.cookpilot.university.features.recipes.domain.model.Ingredient;
import com.cookpilot.university.features.recipes.domain.model.NutritionInfo;
import com.cookpilot.university.features.recipes.domain.model.Recipe;
import com.cookpilot.university.features.recipes.domain.model.RecipeDetail;
import com.cookpilot.university.features.recipes.data.repository.SavedRecipeRepository;
import com.cookpilot.university.features.recipes.domain.model.RecipeStep;
import com.cookpilot.university.features.recipes.presentation.viewmodel.RecipeDetailViewModel;
import com.cookpilot.university.features.recipes.domain.usecase.ScaleRecipeServings;
import com.cookpilot.university.features.recipes.presentation.viewmodel.RecipeDetailViewModelFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class RecipeDetailActivity extends AppCompatActivity {

    private static final String EXTRA_RECIPE_IDS = "recipe_ids";
    private static final String EXTRA_SERVINGS = "servings";
    private static final String EXTRA_MENU_MODE = "menu_mode";
    private static final String EXTRA_MEAL_MOMENT = "meal_moment";

    private ActivityRecipeDetailBinding binding;
    private RecipeDetailViewModel viewModel;
    private SavedRecipeRepository savedRecipeRepository;

    private List<RecipeDetail> currentDetails = Collections.emptyList();
    private Map<String, Integer> currentServings =
            Collections.emptyMap();
    private Set<String> savedRecipeIds = Collections.emptySet();
    @Nullable
    private String selectedRecipeId;
    private boolean menuMode;

    @NonNull
    public static Intent createIntent(
            @NonNull Context context,
            @NonNull String recipeId,
            int servings
    ) {
        ArrayList<String> recipeIds = new ArrayList<>();
        recipeIds.add(recipeId);

        ArrayList<Integer> portions = new ArrayList<>();
        portions.add(Math.max(1, servings));

        return new Intent(context, RecipeDetailActivity.class)
                .putStringArrayListExtra(EXTRA_RECIPE_IDS, recipeIds)
                .putIntegerArrayListExtra(EXTRA_SERVINGS, portions)
                .putExtra(EXTRA_MENU_MODE, false);
    }

    @NonNull
    public static Intent createMenuIntent(
            @NonNull Context context,
            @NonNull MealMoment mealMoment,
            @NonNull List<PlannedRecipe> plannedRecipes
    ) {
        ArrayList<String> recipeIds = new ArrayList<>();
        ArrayList<Integer> portions = new ArrayList<>();

        for (PlannedRecipe plannedRecipe : plannedRecipes) {
            recipeIds.add(plannedRecipe.getRecipe().getId());
            portions.add(plannedRecipe.getServings());
        }

        return new Intent(context, RecipeDetailActivity.class)
                .putStringArrayListExtra(EXTRA_RECIPE_IDS, recipeIds)
                .putIntegerArrayListExtra(EXTRA_SERVINGS, portions)
                .putExtra(EXTRA_MENU_MODE, true)
                .putExtra(
                        EXTRA_MEAL_MOMENT,
                        mealMoment.getStorageKey()
                );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableFullscreen();

        binding = ActivityRecipeDetailBinding.inflate(
                getLayoutInflater()
        );
        setContentView(binding.getRoot());

        ArrayList<String> recipeIds =
                getIntent().getStringArrayListExtra(EXTRA_RECIPE_IDS);
        ArrayList<Integer> servings =
                getIntent().getIntegerArrayListExtra(EXTRA_SERVINGS);

        if (recipeIds == null || recipeIds.isEmpty()) {
            finish();
            return;
        }

        if (servings == null) {
            servings = new ArrayList<>();
        }

        menuMode = getIntent().getBooleanExtra(
                EXTRA_MENU_MODE,
                false
        );

        CookPilotApplication application =
                (CookPilotApplication) getApplication();
        savedRecipeRepository =
                application.getSavedRecipeRepository();

        viewModel = new ViewModelProvider(
                this,
                new RecipeDetailViewModelFactory(
                        application.getRecipeRepository(),
                        recipeIds,
                        servings
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
        binding.saveButton.setImageResource(CookIcons.heart());
        binding.metricMoneyIcon.setImageResource(CookIcons.money());
        binding.metricTimeIcon.setImageResource(CookIcons.timer());
        binding.metricNutritionIcon.setImageResource(
                CookIcons.nutrition()
        );
        binding.ingredientsIcon.setImageResource(
                CookIcons.ingredients()
        );
        binding.stepsIcon.setImageResource(CookIcons.steps());
        binding.nutritionIcon.setImageResource(
                CookIcons.nutrition()
        );

        binding.backButton.setOnClickListener(
                view -> getOnBackPressedDispatcher().onBackPressed()
        );
        binding.saveButton.setOnClickListener(view -> {
            if (selectedRecipeId != null) {
                savedRecipeRepository.toggle(selectedRecipeId);
            }
        });
        binding.decreaseServingsButton.setOnClickListener(
                view -> viewModel.decreaseServings()
        );
        binding.increaseServingsButton.setOnClickListener(
                view -> viewModel.increaseServings()
        );

        binding.tabGeneral.setOnClickListener(
                view -> selectTab(0)
        );
        binding.tabRecipe.setOnClickListener(
                view -> selectTab(1)
        );
        binding.tabNutrition.setOnClickListener(
                view -> selectTab(2)
        );
        selectTab(0);

        binding.cookButton.setText(
                menuMode
                        ? R.string.recipe_detail_cook_menu
                        : R.string.recipe_detail_cook_now
        );
        binding.cookButton.setOnClickListener(
                view -> startCooking()
        );
    }

    private void observeState() {
        viewModel.getDetails().observe(this, details -> {
            currentDetails = details == null
                    ? Collections.emptyList()
                    : details;
            renderAll();
        });

        viewModel.getServingsByRecipe().observe(this, servings -> {
            currentServings = servings == null
                    ? Collections.emptyMap()
                    : servings;
            renderAll();
        });

        viewModel.getSelectedRecipeId().observe(this, recipeId -> {
            selectedRecipeId = recipeId;
            renderAll();
        });

        savedRecipeRepository.observeSavedRecipeIds().observe(
                this,
                ids -> {
                    savedRecipeIds = ids == null
                            ? Collections.emptySet()
                            : Collections.unmodifiableSet(
                                    new HashSet<>(ids)
                            );
                    renderSaveState();
                }
        );
    }

    private void startCooking() {
        if (currentDetails.isEmpty()) {
            return;
        }

        List<String> recipeIds = new ArrayList<>();
        List<Integer> servings = new ArrayList<>();

        for (RecipeDetail detail : currentDetails) {
            Recipe recipe = detail.getRecipe();
            recipeIds.add(recipe.getId());
            servings.add(servingsFor(recipe));
        }

        startActivity(
                CookModeActivity.createIntent(
                        this,
                        recipeIds,
                        servings
                )
        );
    }

    private void renderAll() {
        RecipeDetail selected = selectedDetail();
        if (selected == null) {
            return;
        }

        renderRecipeSelector();
        renderSelectedRecipe(selected);
        renderMenuSummary();
        renderSaveState();
    }

    private void renderSaveState() {
        if (binding == null || selectedRecipeId == null) {
            return;
        }

        boolean saved = savedRecipeIds.contains(selectedRecipeId);
        binding.saveButton.setImageResource(
                saved
                        ? CookIcons.heartFilled()
                        : CookIcons.heart()
        );
        binding.saveButton.setColorFilter(
                ContextCompat.getColor(
                        this,
                        R.color.cook_accent
                )
        );
    }

    private void renderSelectedRecipe(
            @NonNull RecipeDetail detail
    ) {
        Recipe recipe = detail.getRecipe();
        int servings = servingsFor(recipe);
        double scale = scaleFor(recipe, servings);

        binding.recipeTitle.setText(recipe.getTitle());
        binding.recipeDescription.setText(
                TextUtil.boldMarkdown(recipe.getDescription())
        );

        Glide.with(binding.heroImage)
                .load(recipe.getImageUrl())
                .centerCrop()
                .into(binding.heroImage);

        binding.servingsValue.setText(
                getString(
                        R.string.recipe_detail_servings,
                        servings
                )
        );
        binding.decreaseServingsButton.setEnabled(servings > 1);
        binding.decreaseServingsButton.setAlpha(
                servings > 1 ? 1f : 0.45f
        );

        renderIngredients(detail, scale);
        renderSteps(detail);
        renderNutrition(detail.getNutrition(), scale);
    }

    private void renderMenuSummary() {
        if (currentDetails.isEmpty()) {
            return;
        }

        double cost = 0;
        double savings = 0;
        double calories = 0;
        int readyMinutes = 0;
        int restMinutes = 0;

        for (RecipeDetail detail : currentDetails) {
            Recipe recipe = detail.getRecipe();
            int servings = servingsFor(recipe);
            double scale = scaleFor(recipe, servings);

            cost += recipe.getEstimatedCostPen() * scale;
            savings += recipe.getEstimatedSavingsPen() * scale;
            calories += detail.getNutrition().getCalories() * scale;
            readyMinutes = Math.max(
                    readyMinutes,
                    recipe.getTotalMinutes()
            );
            restMinutes = Math.max(
                    restMinutes,
                    recipe.getPassiveMinutes()
            );
        }

        binding.metricMoneyValue.setText(
                String.format(Locale.US, "S/ %.1f", cost)
        );
        binding.metricMoneySubtitle.setText(
                getString(
                        R.string.recipe_detail_savings,
                        savings
                )
        );
        binding.metricTimeValue.setText(
                getString(
                        R.string.recipe_detail_minutes,
                        readyMinutes
                )
        );
        binding.metricTimeSubtitle.setText(
                getString(
                        R.string.recipe_detail_rest_minutes,
                        restMinutes
                )
        );
        binding.metricNutritionValue.setText(
                formatNumber(calories) + " kcal"
        );
        binding.metricNutritionSubtitle.setText(
                R.string.recipe_detail_nutrition
        );
    }

    private void renderRecipeSelector() {
        binding.recipeSelector.removeAllViews();

        boolean visible = menuMode && currentDetails.size() > 1;
        binding.recipeSelectorScroll.setVisibility(
                visible ? View.VISIBLE : View.GONE
        );

        if (!visible) {
            return;
        }

        for (RecipeDetail detail : currentDetails) {
            Recipe recipe = detail.getRecipe();
            int servings = servingsFor(recipe);
            boolean selected = recipe.getId().equals(
                    selectedRecipeId
            );

            TextView chip = new TextView(this);
            chip.setText(
                    recipe.getTitle() + " x" + servings
            );
            chip.setTextAppearance(
                    R.style.TextAppearance_CookPilot_Label
            );
            chip.setTextColor(
                    ContextCompat.getColor(
                            this,
                            selected
                                    ? R.color.cook_primary
                                    : R.color.cook_text_primary
                    )
            );
            chip.setGravity(android.view.Gravity.CENTER_VERTICAL);
            chip.setMaxLines(1);
            chip.setEllipsize(
                    android.text.TextUtils.TruncateAt.END
            );
            chip.setPadding(
                    dp(16),
                    dp(10),
                    dp(16),
                    dp(10)
            );
            chip.setBackgroundResource(
                    selected
                            ? R.drawable.bg_recipe_component_chip_selected
                            : R.drawable.bg_recipe_component_chip
            );
            chip.setOnClickListener(
                    view -> viewModel.selectRecipe(recipe.getId())
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            dp(220),
                            dp(48)
                    );
            params.setMarginEnd(dp(8));
            binding.recipeSelector.addView(chip, params);
        }
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
                    .setText(
                            TextUtil.boldMarkdown(
                                    step.getInstruction()
                            )
                    );
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
                formatNumber(nutrition.getCalories() * scale)
                        + " kcal"
        );
        addNutrition(
                R.string.recipe_detail_protein,
                formatNumber(nutrition.getProteinG() * scale)
                        + " g"
        );
        addNutrition(
                R.string.recipe_detail_carbs,
                formatNumber(nutrition.getCarbsG() * scale)
                        + " g"
        );
        addNutrition(
                R.string.recipe_detail_fat,
                formatNumber(nutrition.getFatG() * scale)
                        + " g"
        );
        addNutrition(
                R.string.recipe_detail_fiber,
                formatNumber(nutrition.getFiberG() * scale)
                        + " g"
        );
    }

    private void addNutrition(
            int labelRes,
            @NonNull String value
    ) {
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

    @Nullable
    private RecipeDetail selectedDetail() {
        if (selectedRecipeId == null) {
            return null;
        }

        for (RecipeDetail detail : currentDetails) {
            if (selectedRecipeId.equals(
                    detail.getRecipe().getId()
            )) {
                return detail;
            }
        }

        return null;
    }

    private int servingsFor(@NonNull Recipe recipe) {
        Integer value = currentServings.get(recipe.getId());
        return value == null
                ? recipe.getBaseServings()
                : Math.max(1, value);
    }

    private double scaleFor(
            @NonNull Recipe recipe,
            int servings
    ) {
        return ScaleRecipeServings.scaleQuantity(
                1.0,
                servings,
                recipe.getBaseServings()
        );
    }

    @NonNull
    private String formatQuantity(
            double value,
            @NonNull String unit
    ) {
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

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }
}