package com.cookpilot.university.features.cooklist.domain.usecase;

import androidx.annotation.NonNull;

import com.cookpilot.university.features.cooklist.domain.model.GeneratedShoppingItem;
import com.cookpilot.university.features.cookplan.domain.model.PlannedRecipe;
import com.cookpilot.university.features.recipes.domain.model.Ingredient;
import com.cookpilot.university.features.recipes.domain.model.Recipe;
import com.cookpilot.university.features.recipes.domain.usecase.ScaleRecipeServings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GenerateShoppingList {

    private GenerateShoppingList() {
    }

    @NonNull
    public static List<GeneratedShoppingItem> fromPlan(
            @NonNull List<PlannedRecipe> plan
    ) {
        Map<String, Accumulator> grouped = new LinkedHashMap<>();

        for (PlannedRecipe plannedRecipe : plan) {
            Recipe recipe = plannedRecipe.getRecipe();

            for (Ingredient ingredient : recipe.getIngredients()) {
                String key = ingredient.getId()
                        + "|"
                        + ingredient.getUnit();

                double scaledQuantity =
                        ScaleRecipeServings.scaleQuantity(
                                ingredient.getQuantity(),
                                plannedRecipe.getServings(),
                                recipe.getBaseServings()
                        );

                Accumulator current = grouped.get(key);
                if (current == null) {
                    current = new Accumulator(
                            ingredient.getId(),
                            ingredient.getName(),
                            ingredient.getUnit()
                    );
                    grouped.put(key, current);
                }

                current.quantity += scaledQuantity;
                current.sourceRecipeIds.add(recipe.getId());
            }
        }

        List<GeneratedShoppingItem> result = new ArrayList<>();
        for (Accumulator item : grouped.values()) {
            result.add(new GeneratedShoppingItem(
                    item.ingredientId,
                    item.name,
                    item.quantity,
                    item.unit,
                    new ArrayList<>(item.sourceRecipeIds)
            ));
        }

        return result;
    }

    private static final class Accumulator {

        @NonNull
        final String ingredientId;
        @NonNull
        final String name;
        @NonNull
        final String unit;
        double quantity;
        final Set<String> sourceRecipeIds = new LinkedHashSet<>();

        Accumulator(
                @NonNull String ingredientId,
                @NonNull String name,
                @NonNull String unit
        ) {
            this.ingredientId = ingredientId;
            this.name = name;
            this.unit = unit;
        }
    }
}
