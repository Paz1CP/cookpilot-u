package com.cookpilot.university.features.cooklist.data.local;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "shopping_items",
        indices = {
                @Index(
                        value = {"user_id", "week_start"},
                        name = "index_shopping_items_user_id_week_start"
                ),
                @Index(
                        value = {"ingredient_id"},
                        name = "index_shopping_items_ingredient_id"
                )
        }
)
public final class ShoppingItemEntity {

    public static final String LOCAL = "LOCAL";

    @PrimaryKey
    @NonNull
    public final String id;

    @NonNull
    @ColumnInfo(name = "user_id")
    public final String userId;

    @NonNull
    @ColumnInfo(name = "week_start")
    public final String weekStart;

    @Nullable
    @ColumnInfo(name = "ingredient_id")
    public final String ingredientId;

    @NonNull
    public final String name;

    public final double quantity;

    @NonNull
    public final String unit;

    public final boolean purchased;

    public final boolean manual;

    @NonNull
    @ColumnInfo(name = "source_recipe_ids")
    public final String sourceRecipeIds;

    @ColumnInfo(name = "created_at")
    public final long createdAt;

    @ColumnInfo(name = "updated_at")
    public final long updatedAt;

    @NonNull
    @ColumnInfo(name = "sync_state")
    public final String syncState;

    public ShoppingItemEntity(
            @NonNull String id,
            @NonNull String userId,
            @NonNull String weekStart,
            @Nullable String ingredientId,
            @NonNull String name,
            double quantity,
            @NonNull String unit,
            boolean purchased,
            boolean manual,
            @NonNull String sourceRecipeIds,
            long createdAt,
            long updatedAt,
            @NonNull String syncState
    ) {
        this.id = id;
        this.userId = userId;
        this.weekStart = weekStart;
        this.ingredientId = ingredientId;
        this.name = name;
        this.quantity = Math.max(0, quantity);
        this.unit = unit;
        this.purchased = purchased;
        this.manual = manual;
        this.sourceRecipeIds = sourceRecipeIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.syncState = syncState;
    }
}
