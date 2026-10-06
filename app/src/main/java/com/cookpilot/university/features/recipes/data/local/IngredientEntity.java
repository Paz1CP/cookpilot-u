package com.cookpilot.university.features.recipes.data.local;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ingredients")
public final class IngredientEntity {

    @PrimaryKey
    @NonNull
    public final String id;

    @NonNull
    public final String name;

    @Nullable
    @ColumnInfo(name = "image_url")
    public final String imageUrl;

    @ColumnInfo(name = "updated_at")
    public final long updatedAt;

    public IngredientEntity(
            @NonNull String id,
            @NonNull String name,
            @Nullable String imageUrl,
            long updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.updatedAt = updatedAt;
    }
}
