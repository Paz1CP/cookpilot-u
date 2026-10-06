package com.cookpilot.university.features.cookplan.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(
        tableName = "planned_recipes",
        primaryKeys = {"plan_date", "meal_moment", "recipe_id"}
)
public final class PlannedRecipeEntity {

    @NonNull
    @ColumnInfo(name = "plan_date")
    public final String planDate;

    @NonNull
    @ColumnInfo(name = "meal_moment")
    public final String mealMoment;

    @NonNull
    @ColumnInfo(name = "recipe_id")
    public final String recipeId;

    @ColumnInfo(name = "servings")
    public final int servings;

    @ColumnInfo(name = "created_at")
    public final long createdAt;

    public PlannedRecipeEntity(
            @NonNull String planDate,
            @NonNull String mealMoment,
            @NonNull String recipeId,
            int servings,
            long createdAt
    ) {
        this.planDate = planDate;
        this.mealMoment = mealMoment;
        this.recipeId = recipeId;
        this.servings = Math.max(1, servings);
        this.createdAt = createdAt;
    }
}
