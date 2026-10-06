package com.cookpilot.university.features.cookplan.domain.model;

import androidx.annotation.NonNull;

import com.cookpilot.university.features.recipes.domain.model.Recipe;

public final class PlannedRecipe {

    @NonNull
    private final String id;
    @NonNull
    private final Recipe recipe;
    @NonNull
    private final MealMoment mealMoment;
    private final int servings;

    public PlannedRecipe(
            @NonNull String id,
            @NonNull Recipe recipe,
            @NonNull MealMoment mealMoment,
            int servings
    ) {
        this.id = id;
        this.recipe = recipe;
        this.mealMoment = mealMoment;
        this.servings = Math.max(1, servings);
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public Recipe getRecipe() {
        return recipe;
    }

    @NonNull
    public MealMoment getMealMoment() {
        return mealMoment;
    }

    public int getServings() {
        return servings;
    }
}
