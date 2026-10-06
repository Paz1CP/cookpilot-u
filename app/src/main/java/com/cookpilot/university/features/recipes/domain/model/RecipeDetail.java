package com.cookpilot.university.features.recipes.domain.model;

import androidx.annotation.NonNull;

import java.util.List;

public final class RecipeDetail {

    @NonNull
    private final Recipe recipe;

    public RecipeDetail(@NonNull Recipe recipe) {
        this.recipe = recipe;
    }

    @NonNull
    public Recipe getRecipe() {
        return recipe;
    }

    @NonNull
    public List<Ingredient> getIngredients() {
        return recipe.getIngredients();
    }

    @NonNull
    public List<RecipeStep> getSteps() {
        return recipe.getSteps();
    }

    @NonNull
    public NutritionInfo getNutrition() {
        return recipe.getNutrition();
    }
}
