package com.cookpilot.university.features.recipes.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "recipe_steps",
        primaryKeys = {"recipe_id", "step_number"},
        foreignKeys = @ForeignKey(
                entity = RecipeEntity.class,
                parentColumns = "id",
                childColumns = "recipe_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("recipe_id")
)
public final class RecipeStepEntity {

    @NonNull
    @ColumnInfo(name = "recipe_id")
    public final String recipeId;

    @ColumnInfo(name = "step_number")
    public final int stepNumber;

    @NonNull
    public final String instruction;

    public RecipeStepEntity(
            @NonNull String recipeId,
            int stepNumber,
            @NonNull String instruction
    ) {
        this.recipeId = recipeId;
        this.stepNumber = stepNumber;
        this.instruction = instruction;
    }
}
