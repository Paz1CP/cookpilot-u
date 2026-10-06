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
import java.util.List;
import java.util.Locale;

public final class CookPlanMealMomentView extends LinearLayout {

    public interface Listener {
        void onAddRecipe(@NonNull MealMoment mealMoment);
        void onRemoveRecipe(@NonNull PlannedRecipe plannedRecipe);
        void onCook(@NonNull MealMoment mealMoment);
    }

    private final ImageView momentIcon;
    private final TextView momentTitle;
    private final TextView priceText;
    private final TextView metaText;
    private final LinearLayout recipeRail;
    private final LinearLayout ingredientsList;
    private final MaterialButton ingredientsButton;
    private final MaterialButton addButton;
    private final MaterialButton cookButton;

    private MealMoment mealMoment = MealMoment.LUNCH;
    private Listener listener;
    private List<PlannedRecipe> currentRecipes = Collections.emptyList();
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
        ingredientsButton = findViewById(R.id.ingredientsButton);
        addButton = findViewById(R.id.addRecipeButton);
        cookButton = findViewById(R.id.cookButton);

        ImageView moreIcon = findViewById(R.id.mealMomentMoreIcon);
        moreIcon.setImageResource(CookIcons.more());
        moreIcon.setRotation(90f);

        ingredientsButton.setIconResource(CookIcons.ingredients());
        addButton.setIconResource(CookIcons.add());

        addButton.setOnClickListener(view -> requestAdd());
        ingredientsButton.setOnClickListener(view -> toggleIngredients());
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

        renderRecipeRail();
        renderIngredients();

        boolean hasRecipes = !currentRecipes.isEmpty();
        ingredientsButton.setVisibility(hasRecipes ? VISIBLE : GONE);
        cookButton.setVisibility(hasRecipes ? VISIBLE : GONE);
        priceText.setText(hasRecipes ? formatPrice(totalCost()) : "S/ —");
        metaText.setText(
                hasRecipes
                        ? formatMeta()
                        : getContext().getString(R.string.cookplan_empty_meal)
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

        for (PlannedRecipe plannedRecipe : currentRecipes) {
            View item = inflater.inflate(
                    R.layout.item_planned_recipe,
                    recipeRail,
                    false
            );

            ImageView image = item.findViewById(R.id.recipeImage);
            ImageView removeIcon = item.findViewById(R.id.removeRecipeIcon);
            TextView servings = item.findViewById(R.id.servingsBadge);
            View remove = item.findViewById(R.id.removeRecipeButton);

            removeIcon.setImageResource(CookIcons.close());
            removeIcon.setRotation(45f);
            servings.setText(String.valueOf(plannedRecipe.getServings()));

            Glide.with(image)
                    .load(plannedRecipe.getRecipe().getImageUrl())
                    .centerCrop()
                    .into(image);

            remove.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onRemoveRecipe(plannedRecipe);
                }
            });

            recipeRail.addView(item);
        }
    }

    private void renderIngredients() {
        ingredientsList.removeAllViews();
        ingredientsList.setVisibility(
                ingredientsVisible && !currentRecipes.isEmpty()
                        ? VISIBLE
                        : GONE
        );

        if (!ingredientsVisible || currentRecipes.isEmpty()) {
            return;
        }

        PlannedRecipe focused = currentRecipes.get(0);
        Recipe recipe = focused.getRecipe();
        double scale = (double) focused.getServings()
                / Math.max(1, recipe.getBaseServings());

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Ingredient ingredient : recipe.getIngredients()) {
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
            TextView quantity = row.findViewById(R.id.ingredientQuantity);

            selectedIcon.setImageResource(CookIcons.selected());
            name.setText(ingredient.getName());
            quantity.setText(formatQuantity(
                    ingredient.getQuantity() * scale,
                    ingredient.getUnit()
            ));

            Glide.with(icon)
                    .load(ingredient.getImageUrl())
                    .centerCrop()
                    .into(icon);

            ingredientsList.addView(row);
        }
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
        return getContext().getString(
                R.string.cookplan_meal_meta,
                readyMinutes(),
                totalSavings()
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
}
