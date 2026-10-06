package com.cookpilot.university.features.recipes.data.local;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;

public final class RecipeIngredientRow {

    @NonNull
    @ColumnInfo(name = "ingredient_id")
    public String ingredientId;

    @NonNull
    public String name;

    @Nullable
    @ColumnInfo(name = "image_url")
    public String imageUrl;

    public double quantity;

    @NonNull
    public String unit;

    @ColumnInfo(name = "is_optional")
    public boolean optional;
}
