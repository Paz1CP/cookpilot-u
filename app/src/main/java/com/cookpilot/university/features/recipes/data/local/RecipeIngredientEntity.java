package com.cookpilot.university.features.recipes.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "recipe_ingredients",
        primaryKeys = {"recipe_id", "ingredient_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = RecipeEntity.class,
                        parentColumns = "id",
                        childColumns = "recipe_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = IngredientEntity.class,
                        parentColumns = "id",
                        childColumns = "ingredient_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("recipe_id"),
                @Index("ingredient_id")
        }
)
public final class RecipeIngredientEntity {

    @NonNull
    @ColumnInfo(name = "recipe_id")
    public final String recipeId;

    @NonNull
    @ColumnInfo(name = "ingredient_id")
    public final String ingredientId;

    public final double quantity;

    @NonNull
    public final String unit;

    @ColumnInfo(name = "is_optional")
    public final boolean optional;

    public final int position;

    public RecipeIngredientEntity(
            @NonNull String recipeId,
            @NonNull String ingredientId,
            double quantity,
            @NonNull String unit,
            boolean optional,
            int position
    ) {
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unit = unit;
        this.optional = optional;
        this.position = position;
    }
}
