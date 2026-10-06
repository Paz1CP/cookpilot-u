package com.cookpilot.university.features.cooklist.domain.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GeneratedShoppingItem {

    @NonNull
    private final String ingredientId;
    @NonNull
    private final String name;
    private final double quantity;
    @NonNull
    private final String unit;
    @NonNull
    private final List<String> sourceRecipeIds;

    public GeneratedShoppingItem(
            @NonNull String ingredientId,
            @NonNull String name,
            double quantity,
            @NonNull String unit,
            @NonNull List<String> sourceRecipeIds
    ) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.quantity = Math.max(0, quantity);
        this.unit = unit;
        this.sourceRecipeIds = Collections.unmodifiableList(
                new ArrayList<>(sourceRecipeIds)
        );
    }

    @NonNull
    public String getIngredientId() { return ingredientId; }

    @NonNull
    public String getName() { return name; }

    public double getQuantity() { return quantity; }

    @NonNull
    public String getUnit() { return unit; }

    @NonNull
    public List<String> getSourceRecipeIds() {
        return sourceRecipeIds;
    }
}
