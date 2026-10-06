package com.cookpilot.university.features.recipes.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Recipe {

    @NonNull
    private final String id;
    @NonNull
    private final String title;
    @NonNull
    private final String canonicalName;
    @NonNull
    private final String description;
    @Nullable
    private final String imageUrl;
    private final int baseServings;
    @NonNull
    private final String difficulty;
    private final int totalMinutes;
    private final int activeMinutes;
    private final int passiveMinutes;
    private final double estimatedCostPen;
    private final double estimatedSavingsPen;
    @NonNull
    private final NutritionInfo nutrition;
    @NonNull
    private final List<String> categorySlugs;
    @NonNull
    private final List<Ingredient> ingredients;
    @NonNull
    private final List<RecipeStep> steps;

    public Recipe(
            @NonNull String id,
            @NonNull String title,
            @NonNull String canonicalName,
            @NonNull String description,
            @Nullable String imageUrl,
            int baseServings,
            @NonNull String difficulty,
            int totalMinutes,
            int activeMinutes,
            int passiveMinutes,
            double estimatedCostPen,
            double estimatedSavingsPen,
            @NonNull NutritionInfo nutrition,
            @NonNull List<String> categorySlugs,
            @NonNull List<Ingredient> ingredients,
            @NonNull List<RecipeStep> steps
    ) {
        this.id = id;
        this.title = title;
        this.canonicalName = canonicalName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.baseServings = Math.max(1, baseServings);
        this.difficulty = difficulty;
        this.totalMinutes = Math.max(0, totalMinutes);
        this.activeMinutes = Math.max(0, activeMinutes);
        this.passiveMinutes = Math.max(0, passiveMinutes);
        this.estimatedCostPen = Math.max(0, estimatedCostPen);
        this.estimatedSavingsPen = Math.max(0, estimatedSavingsPen);
        this.nutrition = nutrition;
        this.categorySlugs = Collections.unmodifiableList(
                new ArrayList<>(categorySlugs)
        );
        this.ingredients = Collections.unmodifiableList(
                new ArrayList<>(ingredients)
        );
        this.steps = Collections.unmodifiableList(
                new ArrayList<>(steps)
        );
    }

    @NonNull
    public String getId() { return id; }

    @NonNull
    public String getTitle() { return title; }

    @NonNull
    public String getCanonicalName() { return canonicalName; }

    @NonNull
    public String getDescription() { return description; }

    @Nullable
    public String getImageUrl() { return imageUrl; }

    public int getBaseServings() { return baseServings; }

    @NonNull
    public String getDifficulty() { return difficulty; }

    public int getTotalMinutes() { return totalMinutes; }

    public int getActiveMinutes() { return activeMinutes; }

    public int getPassiveMinutes() { return passiveMinutes; }

    @NonNull
    public NutritionInfo getNutrition() { return nutrition; }

    @NonNull
    public List<String> getCategorySlugs() { return categorySlugs; }

    @NonNull
    public List<Ingredient> getIngredients() { return ingredients; }

    @NonNull
    public List<RecipeStep> getSteps() { return steps; }

    public double getEstimatedCostPen() { return estimatedCostPen; }

    public double getEstimatedSavingsPen() { return estimatedSavingsPen; }
}