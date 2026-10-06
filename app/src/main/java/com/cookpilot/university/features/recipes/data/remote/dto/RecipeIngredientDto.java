package com.cookpilot.university.features.recipes.data.remote.dto;

import androidx.annotation.Nullable;

public final class RecipeIngredientDto {

    private String ingredientId;
    private String name;
    @Nullable
    private String imageUrl;
    private double quantity;
    private String unit;
    private boolean optional;

    public String getIngredientId() {
        return ingredientId == null ? "" : ingredientId;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit == null ? "" : unit;
    }

    public boolean isOptional() {
        return optional;
    }
}
