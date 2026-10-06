package com.cookpilot.university.features.recipes.data.remote.dto;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

public final class RecipeDto {

    private String id;
    private String canonicalName;
    private String title;
    private String description;
    @Nullable
    private String imageUrl;
    private int servings;
    private String difficulty;
    private int totalMinutes;
    private int activeMinutes;
    private int passiveMinutes;
    private List<String> categorySlugs;
    private NutritionDto nutrition;
    private List<RecipeIngredientDto> ingredients;
    private List<RecipeStepDto> steps;
    @Nullable
    private String updatedAt;

    public String getId() { return id == null ? "" : id; }
    public String getCanonicalName() {
        return canonicalName == null ? "" : canonicalName;
    }
    public String getTitle() { return title == null ? "" : title; }
    public String getDescription() {
        return description == null ? "" : description;
    }
    @Nullable
    public String getImageUrl() { return imageUrl; }
    public int getServings() { return servings; }
    public String getDifficulty() {
        return difficulty == null ? "medium" : difficulty;
    }
    public int getTotalMinutes() { return totalMinutes; }
    public int getActiveMinutes() { return activeMinutes; }
    public int getPassiveMinutes() { return passiveMinutes; }
    public List<String> getCategorySlugs() {
        return categorySlugs == null
                ? Collections.emptyList()
                : categorySlugs;
    }
    public NutritionDto getNutrition() {
        return nutrition == null ? new NutritionDto() : nutrition;
    }
    public List<RecipeIngredientDto> getIngredients() {
        return ingredients == null
                ? Collections.emptyList()
                : ingredients;
    }
    public List<RecipeStepDto> getSteps() {
        return steps == null ? Collections.emptyList() : steps;
    }
    @Nullable
    public String getUpdatedAt() { return updatedAt; }
}
