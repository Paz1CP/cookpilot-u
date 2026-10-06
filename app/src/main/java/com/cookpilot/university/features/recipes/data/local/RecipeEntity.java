package com.cookpilot.university.features.recipes.data.local;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recipes")
public final class RecipeEntity {

    @PrimaryKey
    @NonNull
    public final String id;

    @NonNull
    @ColumnInfo(name = "canonical_name")
    public final String canonicalName;

    @NonNull
    public final String title;

    @NonNull
    public final String description;

    @Nullable
    @ColumnInfo(name = "image_url")
    public final String imageUrl;

    @ColumnInfo(name = "base_servings")
    public final int baseServings;

    @NonNull
    public final String difficulty;

    @ColumnInfo(name = "total_minutes")
    public final int totalMinutes;

    @ColumnInfo(name = "active_minutes")
    public final int activeMinutes;

    @ColumnInfo(name = "passive_minutes")
    public final int passiveMinutes;

    @ColumnInfo(name = "estimated_cost_pen")
    public final double estimatedCostPen;

    @ColumnInfo(name = "estimated_savings_pen")
    public final double estimatedSavingsPen;

    public final double calories;

    @ColumnInfo(name = "protein_g")
    public final double proteinG;

    @ColumnInfo(name = "carbs_g")
    public final double carbsG;

    @ColumnInfo(name = "fat_g")
    public final double fatG;

    @ColumnInfo(name = "fiber_g")
    public final double fiberG;

    @NonNull
    @ColumnInfo(name = "category_slugs")
    public final String categorySlugs;

    @ColumnInfo(name = "updated_at")
    public final long updatedAt;

    public RecipeEntity(
            @NonNull String id,
            @NonNull String canonicalName,
            @NonNull String title,
            @NonNull String description,
            @Nullable String imageUrl,
            int baseServings,
            @NonNull String difficulty,
            int totalMinutes,
            int activeMinutes,
            int passiveMinutes,
            double estimatedCostPen,
            double estimatedSavingsPen,
            double calories,
            double proteinG,
            double carbsG,
            double fatG,
            double fiberG,
            @NonNull String categorySlugs,
            long updatedAt
    ) {
        this.id = id;
        this.canonicalName = canonicalName;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.baseServings = baseServings;
        this.difficulty = difficulty;
        this.totalMinutes = totalMinutes;
        this.activeMinutes = activeMinutes;
        this.passiveMinutes = passiveMinutes;
        this.estimatedCostPen = estimatedCostPen;
        this.estimatedSavingsPen = estimatedSavingsPen;
        this.calories = calories;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatG = fatG;
        this.fiberG = fiberG;
        this.categorySlugs = categorySlugs;
        this.updatedAt = updatedAt;
    }
}