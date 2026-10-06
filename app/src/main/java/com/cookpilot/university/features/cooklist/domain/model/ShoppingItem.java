package com.cookpilot.university.features.cooklist.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ShoppingItem {

    @NonNull
    private final String id;
    @NonNull
    private final String weekStart;
    @Nullable
    private final String ingredientId;
    @NonNull
    private final String name;
    private final double quantity;
    @NonNull
    private final String unit;
    private final boolean purchased;
    private final boolean manual;
    @NonNull
    private final List<String> sourceRecipeIds;

    public ShoppingItem(
            @NonNull String id,
            @NonNull String weekStart,
            @Nullable String ingredientId,
            @NonNull String name,
            double quantity,
            @NonNull String unit,
            boolean purchased,
            boolean manual,
            @NonNull List<String> sourceRecipeIds
    ) {
        this.id = id;
        this.weekStart = weekStart;
        this.ingredientId = ingredientId;
        this.name = name;
        this.quantity = Math.max(0, quantity);
        this.unit = unit;
        this.purchased = purchased;
        this.manual = manual;
        this.sourceRecipeIds = Collections.unmodifiableList(
                new ArrayList<>(sourceRecipeIds)
        );
    }

    @NonNull
    public String getId() { return id; }

    @NonNull
    public String getWeekStart() { return weekStart; }

    @Nullable
    public String getIngredientId() { return ingredientId; }

    @NonNull
    public String getName() { return name; }

    public double getQuantity() { return quantity; }

    @NonNull
    public String getUnit() { return unit; }

    public boolean isPurchased() { return purchased; }

    public boolean isManual() { return manual; }

    @NonNull
    public List<String> getSourceRecipeIds() {
        return sourceRecipeIds;
    }
}
