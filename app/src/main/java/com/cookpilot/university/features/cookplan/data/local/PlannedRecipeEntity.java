package com.cookpilot.university.features.cookplan.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "planned_recipes")
public final class PlannedRecipeEntity {

    public static final String SYNCED = "SYNCED";
    public static final String PENDING_CREATE = "PENDING_CREATE";
    public static final String PENDING_UPDATE = "PENDING_UPDATE";
    public static final String PENDING_DELETE = "PENDING_DELETE";

    @PrimaryKey
    @NonNull
    public final String id;

    @NonNull
    @ColumnInfo(name = "user_id")
    public final String userId;

    @NonNull
    @ColumnInfo(name = "plan_date")
    public final String planDate;

    @NonNull
    @ColumnInfo(name = "meal_moment")
    public final String mealMoment;

    @NonNull
    @ColumnInfo(name = "recipe_id")
    public final String recipeId;

    public final int servings;

    @ColumnInfo(name = "created_at")
    public final long createdAt;

    @ColumnInfo(name = "updated_at")
    public final long updatedAt;

    @NonNull
    @ColumnInfo(name = "sync_state")
    public final String syncState;

    public PlannedRecipeEntity(
            @NonNull String id,
            @NonNull String userId,
            @NonNull String planDate,
            @NonNull String mealMoment,
            @NonNull String recipeId,
            int servings,
            long createdAt,
            long updatedAt,
            @NonNull String syncState
    ) {
        this.id = id;
        this.userId = userId;
        this.planDate = planDate;
        this.mealMoment = mealMoment;
        this.recipeId = recipeId;
        this.servings = Math.max(1, servings);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.syncState = syncState;
    }
}
