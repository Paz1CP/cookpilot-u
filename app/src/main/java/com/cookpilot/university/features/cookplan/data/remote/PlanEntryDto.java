package com.cookpilot.university.features.cookplan.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class PlanEntryDto {

    private String id;
    private String date;
    private String mealMoment;
    private String recipeId;
    private int servings;

    @Nullable
    private String createdAt;

    @Nullable
    private String updatedAt;

    public PlanEntryDto(
            @NonNull String id,
            @NonNull String date,
            @NonNull String mealMoment,
            @NonNull String recipeId,
            int servings
    ) {
        this.id = id;
        this.date = date;
        this.mealMoment = mealMoment;
        this.recipeId = recipeId;
        this.servings = Math.max(1, servings);
    }

    @NonNull
    public String getId() {
        return id == null ? "" : id;
    }

    @NonNull
    public String getDate() {
        return date == null ? "" : date;
    }

    @NonNull
    public String getMealMoment() {
        return mealMoment == null ? "" : mealMoment;
    }

    @NonNull
    public String getRecipeId() {
        return recipeId == null ? "" : recipeId;
    }

    public int getServings() {
        return Math.max(1, servings);
    }

    @Nullable
    public String getCreatedAt() {
        return createdAt;
    }

    @Nullable
    public String getUpdatedAt() {
        return updatedAt;
    }
}
