package com.cookpilot.university.features.recipes.domain.model;

import androidx.annotation.NonNull;

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
    private final String imageUrl;
    private final int baseServings;
    private final int totalMinutes;
    private final double estimatedCostPen;
    private final double estimatedSavingsPen;
    @NonNull
    private final NutritionInfo nutrition;
    @NonNull
    private final List<Ingredient> ingredients;

    public Recipe(
            @NonNull String id,
            @NonNull String title,
            @NonNull String canonicalName,
            @NonNull String imageUrl,
            int baseServings,
            int totalMinutes,
            double estimatedCostPen,
            double estimatedSavingsPen,
            @NonNull NutritionInfo nutrition,
            @NonNull List<Ingredient> ingredients
    ) {
        this.id = id;
        this.title = title;
        this.canonicalName = canonicalName;
        this.imageUrl = imageUrl;
        this.baseServings = baseServings;
        this.totalMinutes = totalMinutes;
        this.estimatedCostPen = estimatedCostPen;
        this.estimatedSavingsPen = estimatedSavingsPen;
        this.nutrition = nutrition;
        this.ingredients = Collections.unmodifiableList(
                new ArrayList<>(ingredients)
        );
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getCanonicalName() {
        return canonicalName;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    public int getBaseServings() {
        return baseServings;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public double getEstimatedCostPen() {
        return estimatedCostPen;
    }

    public double getEstimatedSavingsPen() {
        return estimatedSavingsPen;
    }

    @NonNull
    public NutritionInfo getNutrition() {
        return nutrition;
    }

    @NonNull
    public List<Ingredient> getIngredients() {
        return ingredients;
    }
}
