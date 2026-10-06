package com.cookpilot.university.features.recipes.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class Ingredient {

    @NonNull
    private final String id;
    @NonNull
    private final String name;
    @Nullable
    private final String imageUrl;
    private final double quantity;
    @NonNull
    private final String unit;

    public Ingredient(
            @NonNull String id,
            @NonNull String name,
            @Nullable String imageUrl,
            double quantity,
            @NonNull String unit
    ) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.unit = unit;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    public double getQuantity() {
        return quantity;
    }

    @NonNull
    public String getUnit() {
        return unit;
    }
}
