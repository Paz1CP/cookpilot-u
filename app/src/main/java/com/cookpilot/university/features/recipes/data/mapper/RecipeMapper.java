package com.cookpilot.university.features.recipes.data.mapper;

import androidx.annotation.NonNull;

import com.cookpilot.university.features.recipes.data.local.IngredientEntity;
import com.cookpilot.university.features.recipes.data.local.RecipeEntity;
import com.cookpilot.university.features.recipes.data.local.RecipeIngredientEntity;
import com.cookpilot.university.features.recipes.data.local.RecipeIngredientRow;
import com.cookpilot.university.features.recipes.data.local.RecipeStepEntity;
import com.cookpilot.university.features.recipes.data.remote.dto.NutritionDto;
import com.cookpilot.university.features.recipes.data.remote.dto.RecipeDto;
import com.cookpilot.university.features.recipes.data.remote.dto.RecipeIngredientDto;
import com.cookpilot.university.features.recipes.data.remote.dto.RecipeStepDto;
import com.cookpilot.university.features.recipes.domain.model.Ingredient;
import com.cookpilot.university.features.recipes.domain.model.NutritionInfo;
import com.cookpilot.university.features.recipes.domain.model.Recipe;
import com.cookpilot.university.features.recipes.domain.model.RecipeStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RecipeMapper {

    private static final String CATEGORY_SEPARATOR = "";

    private RecipeMapper() {
    }

    @NonNull
    public static RecipeEntity toRecipeEntity(
            @NonNull RecipeDto dto,
            long updatedAt
    ) {
        NutritionDto nutrition = dto.getNutrition();

        return new RecipeEntity(
                dto.getId(),
                dto.getCanonicalName(),
                dto.getTitle(),
                dto.getDescription(),
                dto.getImageUrl(),
                Math.max(1, dto.getServings()),
                dto.getDifficulty(),
                dto.getTotalMinutes(),
                dto.getActiveMinutes(),
                dto.getPassiveMinutes(),
                nutrition.getCalories(),
                nutrition.getProteinG(),
                nutrition.getCarbsG(),
                nutrition.getFatG(),
                nutrition.getFiberG(),
                String.join(CATEGORY_SEPARATOR, dto.getCategorySlugs()),
                updatedAt
        );
    }

    @NonNull
    public static IngredientEntity toIngredientEntity(
            @NonNull RecipeIngredientDto dto,
            long updatedAt
    ) {
        return new IngredientEntity(
                dto.getIngredientId(),
                dto.getName(),
                dto.getImageUrl(),
                updatedAt
        );
    }

    @NonNull
    public static RecipeIngredientEntity toRecipeIngredientEntity(
            @NonNull String recipeId,
            @NonNull RecipeIngredientDto dto,
            int position
    ) {
        return new RecipeIngredientEntity(
                recipeId,
                dto.getIngredientId(),
                dto.getQuantity(),
                dto.getUnit(),
                dto.isOptional(),
                position
        );
    }

    @NonNull
    public static RecipeStepEntity toStepEntity(
            @NonNull String recipeId,
            @NonNull RecipeStepDto dto
    ) {
        return new RecipeStepEntity(
                recipeId,
                dto.getNumber(),
                dto.getInstruction()
        );
    }

    @NonNull
    public static Recipe toDomain(
            @NonNull RecipeEntity recipe,
            @NonNull List<RecipeIngredientRow> ingredientRows,
            @NonNull List<RecipeStepEntity> stepRows
    ) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (RecipeIngredientRow row : ingredientRows) {
            ingredients.add(new Ingredient(
                    row.ingredientId,
                    row.name,
                    row.imageUrl,
                    row.quantity,
                    row.unit,
                    row.optional
            ));
        }

        List<RecipeStep> steps = new ArrayList<>();
        for (RecipeStepEntity row : stepRows) {
            steps.add(new RecipeStep(row.stepNumber, row.instruction));
        }

        List<String> categories;
        if (recipe.categorySlugs.isEmpty()) {
            categories = Collections.emptyList();
        } else {
            categories = Arrays.asList(
                    recipe.categorySlugs.split(",")
            );
        }

        return new Recipe(
                recipe.id,
                recipe.title,
                recipe.canonicalName,
                recipe.description,
                recipe.imageUrl,
                recipe.baseServings,
                recipe.difficulty,
                recipe.totalMinutes,
                recipe.activeMinutes,
                recipe.passiveMinutes,
                new NutritionInfo(
                        recipe.calories,
                        recipe.proteinG,
                        recipe.carbsG,
                        recipe.fatG,
                        recipe.fiberG
                ),
                categories,
                ingredients,
                steps
        );
    }
}