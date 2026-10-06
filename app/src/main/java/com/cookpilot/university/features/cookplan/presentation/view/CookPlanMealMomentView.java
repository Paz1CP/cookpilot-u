package com.cookpilot.university.features.cookplan.presentation.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cookpilot.university.R;
import com.cookpilot.university.core.design.icons.CookIcons;
import com.cookpilot.university.features.cookplan.domain.model.MealMoment;
import com.cookpilot.university.features.cookplan.domain.model.PlannedRecipe;
import com.cookpilot.university.features.recipes.domain.model.Ingredient;
import com.cookpilot.university.features.recipes.domain.model.Recipe;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CookPlanMealMomentView extends LinearLayout {

    public interface Listener {
        void onAddRecipe(@NonNull MealMoment mealMoment);
        void onRemoveRecipe(@NonNull PlannedRecipe plannedRecipe);
        void onUpdateServings(
                @NonNull PlannedRecipe plannedRecipe,
                int servings
        );
        void onCook(@NonNull MealMoment mealMoment);
    }

    private final ImageView momentIcon;
    private final TextView momentTitle;
    private final TextView priceText;
    private final TextView metaText;
    private final LinearLayout recipeRail;
    private final LinearLayout ingredientsList;
    private final View ingredientsDivider;
    private final LinearLayout focusedRecipeControls;
    private final TextView focusedRecipeTitle;
    private final TextView focusedServings;
    private final TextView decreaseServingsButton;
    private final TextView increaseServingsButton;
    private final MaterialButton ingredientsButton;
    private final MaterialButton addButton;
    private final MaterialButton cookButton;

    private final Set<String> uncheckedIngredientKeys = new HashSet<>();

    private MealMoment mealMoment = MealMoment.LUNCH;
    private Listener listener;
    private List<PlannedRecipe> currentRecipes = Collections.emptyList();
    private String focusedRecipeId;
    private boolean ingredientsVisible;

    public CookPlanMealMomentView(Context context) {
        this(context, null);
    }

    public CookPlanMealMomentView(
            Context context,
            @Nullable AttributeSet attrs
    ) {
        this(context, attrs, 0);
    }

    public CookPlanMealMomentView(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);

        LayoutInflater.from(context).inflate(
                R.layout.view_cook_plan_meal_moment,
                this,
                true
        );

        momentIcon = findViewById(R.id.mealMomentIcon);
        momentTitle = findViewById(R.id.mealMomentTitle);
        priceText = findViewById(R.id.mealPrice);
        metaText = findViewById(R.id.mealMeta);
        recipeRail = findViewById(R.id.recipeRail);
        ingredientsList = findViewById(R.id.ingredientsList);
        ingredientsDivider = findViewById(R.id.ingredientsDivider);
        focusedRecipeControls = findViewById(R.id.focusedRecipeControls);
        focusedRecipeTitle = findViewById(R.id.focusedRecipeTitle);
        focusedServings = findViewById(R.id.focusedServings);
        decreaseServingsButton = findViewById(
                R.id.decreaseServingsButton
        );
        increaseServingsButton = findViewById(
                R.id.increaseServingsButton
        );
        ingredientsButton = findViewById(R.id.ingredientsButton);
        addButton = findViewById(R.id.addRecipeButton);
        cookButton = findViewById(R.id.cookButton);

        ingredientsButton.setIconResource(CookIcons.ingredients());
        addButton.setIconResource(CookIcons.add());

        addButton.setOnClickListener(view -> requestAdd());
        ingredientsButton.setOnClickListener(
                view -> toggleIngredients()
        );
        decreaseServingsButton.setOnClickListener(
                view -> changeFocusedServings(-1)
        );
        increaseServingsButton.setOnClickListener(
                view -> changeFocusedServings(1)
        );
        cookButton.setOnClickListener(view -> {
            if (listener != null && !currentRecipes.isEmpty()) {
                listener.onCook(mealMoment);
            }
        });
    }

    public void configure(
            @NonNull MealMoment mealMoment,
            @NonNull Listener listener
    ) {
        this.mealMoment = mealMoment;
        this.listener = listener;
        applyMomentAppearance();
    }

    public void render(@NonNull List<PlannedRecipe> recipes) {
        currentRecipes = Collections.unmodifiableList(
                new ArrayList<>(recipes)
        );

        if (focusedRecipeId != null && focusedRecipeById() == null) {
            focusedRecipeId = null;
        }

        renderRecipeRail();
        renderFocusedControls();
        renderIngredients();

        boolean hasRecipes = !currentRecipes.isEmpty();
        ingredientsButton.setVisibility(hasRecipes ? VISIBLE : GONE);
        cookButton.setVisibility(hasRecipes ? VISIBLE : GONE);

        double cost = totalCost();
        priceText.setText(
                hasRecipes && cost > 0.0
                        ? formatPrice(cost)
                        : getContext().getString(
                                R.string.cookplan_price_unavailable
                        )
        );
        metaText.setText(
                hasRecipes
                        ? formatMeta()
                        : getContext().getString(
                                R.string.cookplan_empty_meal
                        )
        );
    }

    private void applyMomentAppearance() {
        int icon;
        int tint;
        int title;

        switch (mealMoment) {
            case BREAKFAST:
                icon = CookIcons.breakfast();
                tint = R.color.cook_primary;
                title = R.string.meal_breakfast;
                break;
            case DINNER:
                icon = CookIcons.dinner();
                tint = R.color.cook_accent_soft;
                title = R.string.meal_dinner;
                break;
            case LUNCH:
            default:
                icon = CookIcons.lunch();
                tint = R.color.cook_primary_strong;
                title = R.string.meal_lunch;
                break;
        }

        momentIcon.setImageResource(icon);
        momentIcon.setColorFilter(
                ContextCompat.getColor(getContext(), tint)
        );
        momentTitle.setText(title);
    }

    private void renderRecipeRail() {
        recipeRail.removeAllViews();

        if (currentRecipes.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText(R.string.cookplan_choose_recipes);
            empty.setTextAppearance(
                    R.style.TextAppearance_CookPilot_BodySmall
            );
            empty.setTextColor(ContextCompat.getColor(
                    getContext(),
                    R.color.cook_text_tertiary
            ));
            empty.setPadding(0, dp(16), 0, dp(16));
            recipeRail.addView(empty);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        PlannedRecipe focused = focusedRecipe();

        for (PlannedRecipe plannedRecipe : currentRecipes) {
            View item = inflater.inflate(
                    R.layout.item_planned_recipe,
                    recipeRail,
                    false
            );

            ImageView image = item.findViewById(R.id.recipeImage);
            ImageView removeIcon = item.findViewById(
                    R.id.removeRecipeIcon
            );
            TextView servings = item.findViewById(R.id.servingsBadge);
            View remove = item.findViewById(R.id.removeRecipeButton);

            boolean dimmed = focused != null
                    && currentRecipes.size() > 1
                    && !focused.getId().equals(plannedRecipe.getId());
            item.setAlpha(dimmed ? 0.5f : 1f);

            removeIcon.setImageResource(CookIcons.close());
            removeIcon.setRotation(45f);
            servings.setText(
                    String.valueOf(plannedRecipe.getServings())
            );

            Glide.with(image)
                    .load(plannedRecipe.getRecipe().getImageUrl())
                    .centerCrop()
                    .into(image);

            item.setOnClickListener(view -> {
                if (currentRecipes.size() <= 1) {
                    return;
                }

                focusedRecipeId = plannedRecipe.getId().equals(
                        focusedRecipeId
                )
                        ? null
                        : plannedRecipe.getId();

                renderRecipeRail();
                renderFocusedControls();
                renderIngredients();
            });

            remove.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onRemoveRecipe(plannedRecipe);
                }
            });

            recipeRail.addView(item);
        }
    }

    private void renderFocusedControls() {
        PlannedRecipe focused = focusedRecipe();
        focusedRecipeControls.setVisibility(
                focused == null ? GONE : VISIBLE
        );

        if (focused == null) {
            return;
        }

        focusedRecipeTitle.setText(
                focused.getRecipe().getTitle()
        );
        focusedServings.setText(
                String.valueOf(focused.getServings())
        );
        decreaseServingsButton.setEnabled(
                focused.getServings() > 1
        );
        decreaseServingsButton.setAlpha(
                focused.getServings() > 1 ? 1f : 0.45f
        );
    }

    private void renderIngredients() {
        ingredientsList.removeAllViews();

        boolean visible = ingredientsVisible
                && !currentRecipes.isEmpty();

        ingredientsList.setVisibility(visible ? VISIBLE : GONE);
        ingredientsDivider.setVisibility(visible ? VISIBLE : GONE);

        if (!visible) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (IngredientDisplay ingredient : visibleIngredients()) {
            View row = inflater.inflate(
                    R.layout.item_ingredient_line,
                    ingredientsList,
                    false
            );

            ImageView icon = row.findViewById(R.id.ingredientIcon);
            ImageView selectedIcon = row.findViewById(
                    R.id.ingredientSelectedIcon
            );
            TextView name = row.findViewById(R.id.ingredientName);
            TextView quantity = row.findViewById(
                    R.id.ingredientQuantity
            );

            String key = ingredient.key();
            boolean checked = !uncheckedIngredientKeys.contains(key);

            name.setText(ingredient.name);
            quantity.setText(formatQuantity(
                    ingredient.quantity,
                    ingredient.unit
            ));

            selectedIcon.setImageResource(CookIcons.selected());
            selectedIcon.setColorFilter(
                    ContextCompat.getColor(
                            getContext(),
                            checked
                                    ? R.color.cook_secondary
                                    : R.color.cook_border_default
                    )
            );
            selectedIcon.setAlpha(checked ? 1f : 0.55f);
            selectedIcon.setOnClickListener(view -> {
                if (!uncheckedIngredientKeys.add(key)) {
                    uncheckedIngredientKeys.remove(key);
                }
                renderIngredients();
            });

            Glide.with(icon)
                    .load(ingredient.imageUrl)
                    .centerCrop()
                    .into(icon);

            ingredientsList.addView(row);
        }
    }

    @NonNull
    private List<IngredientDisplay> visibleIngredients() {
        PlannedRecipe focused = focusedRecipe();
        if (focused != null) {
            return scaledIngredients(focused);
        }

        Map<String, IngredientDisplay> merged =
                new LinkedHashMap<>();

        for (PlannedRecipe plannedRecipe : currentRecipes) {
            for (IngredientDisplay ingredient
                    : scaledIngredients(plannedRecipe)) {
                IngredientDisplay current = merged.get(
                        ingredient.key()
                );

                if (current == null) {
                    merged.put(ingredient.key(), ingredient);
                } else {
                    current.quantity += ingredient.quantity;
                }
            }
        }

        return new ArrayList<>(merged.values());
    }

    @NonNull
    private List<IngredientDisplay> scaledIngredients(
            @NonNull PlannedRecipe plannedRecipe
    ) {
        Recipe recipe = plannedRecipe.getRecipe();
        double scale = (double) plannedRecipe.getServings()
                / Math.max(1, recipe.getBaseServings());

        List<IngredientDisplay> result = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            result.add(new IngredientDisplay(
                    ingredient.getId(),
                    ingredient.getName(),
                    ingredient.getImageUrl(),
                    ingredient.getQuantity() * scale,
                    ingredient.getUnit()
            ));
        }
        return result;
    }

    private void changeFocusedServings(int delta) {
        PlannedRecipe focused = focusedRecipe();
        if (focused == null || listener == null) {
            return;
        }

        int servings = Math.max(
                1,
                focused.getServings() + delta
        );
        if (servings == focused.getServings()) {
            return;
        }

        listener.onUpdateServings(focused, servings);
    }

    private void requestAdd() {
        if (listener != null) {
            listener.onAddRecipe(mealMoment);
        }
    }

    private void toggleIngredients() {
        ingredientsVisible = !ingredientsVisible;
        renderIngredients();

        int background = ContextCompat.getColor(
                getContext(),
                ingredientsVisible
                        ? R.color.cook_accent_tint_strong
                        : R.color.cook_layer_floating
        );
        int foreground = ContextCompat.getColor(
                getContext(),
                ingredientsVisible
                        ? R.color.cook_accent
                        : R.color.cook_text_primary
        );

        ingredientsButton.setBackgroundTintList(
                ColorStateList.valueOf(background)
        );
        ingredientsButton.setIconTint(
                ColorStateList.valueOf(foreground)
        );
    }

    @Nullable
    private PlannedRecipe focusedRecipe() {
        if (currentRecipes.size() == 1) {
            return currentRecipes.get(0);
        }
        return focusedRecipeById();
    }

    @Nullable
    private PlannedRecipe focusedRecipeById() {
        if (focusedRecipeId == null) {
            return null;
        }

        for (PlannedRecipe plannedRecipe : currentRecipes) {
            if (plannedRecipe.getId().equals(focusedRecipeId)) {
                return plannedRecipe;
            }
        }
        return null;
    }

    private double totalCost() {
        double total = 0;
        for (PlannedRecipe plannedRecipe : currentRecipes) {
            Recipe recipe = plannedRecipe.getRecipe();
            total += recipe.getEstimatedCostPen()
                    * plannedRecipe.getServings()
                    / Math.max(1, recipe.getBaseServings());
        }
        return total;
    }

    private double totalSavings() {
        double total = 0;
        for (PlannedRecipe plannedRecipe : currentRecipes) {
            Recipe recipe = plannedRecipe.getRecipe();
            total += recipe.getEstimatedSavingsPen()
                    * plannedRecipe.getServings()
                    / Math.max(1, recipe.getBaseServings());
        }
        return total;
    }

    private int readyMinutes() {
        int result = 0;
        for (PlannedRecipe plannedRecipe : currentRecipes) {
            result = Math.max(
                    result,
                    plannedRecipe.getRecipe().getTotalMinutes()
            );
        }
        return result;
    }

    private String formatPrice(double value) {
        return String.format(Locale.US, "S/ %.1f", value);
    }

    private String formatMeta() {
        double savings = totalSavings();
        if (savings > 0.0) {
            return getContext().getString(
                    R.string.cookplan_meal_meta,
                    readyMinutes(),
                    savings
            );
        }

        return getContext().getString(
                R.string.cookplan_meal_time,
                readyMinutes()
        );
    }

    private String formatQuantity(double value, String unit) {
        String number = Math.abs(value - Math.rint(value)) < 0.01
                ? String.valueOf((int) Math.rint(value))
                : String.format(Locale.US, "%.1f", value);
        String displayUnit = "unit".equals(unit) ? "u" : unit;
        return number + " " + displayUnit;
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }

    private static final class IngredientDisplay {

        @NonNull
        final String id;
        @NonNull
        final String name;
        @Nullable
        final String imageUrl;
        double quantity;
        @NonNull
        final String unit;

        IngredientDisplay(
                @NonNull String id,
                @NonNull String name,
                @Nullable String imageUrl,
                double quantity,
                @NonNull String unit
        ) {
            this.id = id;
            this.name = name;
            this.imageUrl = imageUrl;
            this.quantity = quantity;
            this.unit = unit;
        }

        @NonNull
        String key() {
            return id + "|" + unit;
        }
    }
}
