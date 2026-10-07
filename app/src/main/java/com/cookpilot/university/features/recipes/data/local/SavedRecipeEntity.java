package com.cookpilot.university.features.recipes.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "saved_recipes",
        primaryKeys = {"user_id", "recipe_id"},
        indices = @Index(
                value = {"user_id"},
                name = "index_saved_recipes_user_id"
        )
)
public final class SavedRecipeEntity {

    public static final String SYNCED = "SYNCED";
    public static final String PENDING_CREATE = "PENDING_CREATE";
    public static final String PENDING_DELETE = "PENDING_DELETE";

    @NonNull
    @ColumnInfo(name = "user_id")
    public final String userId;

    @NonNull
    @ColumnInfo(name = "recipe_id")
    public final String recipeId;

    @ColumnInfo(name = "saved_at")
    public final long savedAt;

    @NonNull
    @ColumnInfo(name = "sync_state")
    public final String syncState;

    public SavedRecipeEntity(
            @NonNull String userId,
            @NonNull String recipeId,
            long savedAt,
            @NonNull String syncState
    ) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.savedAt = savedAt;
        this.syncState = syncState;
    }
}