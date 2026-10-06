package com.cookpilot.university.features.recipes.data.repository;

import androidx.annotation.NonNull;

import com.cookpilot.university.features.recipes.data.local.RecipeFixtures;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.util.List;
import java.util.Optional;

public final class RecipeRepository {

    @NonNull
    public List<Recipe> getAllRecipes() {
        return RecipeFixtures.all();
    }

    @NonNull
    public Optional<Recipe> findById(@NonNull String recipeId) {
        for (Recipe recipe : RecipeFixtures.all()) {
            if (recipe.getId().equals(recipeId)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }
}
