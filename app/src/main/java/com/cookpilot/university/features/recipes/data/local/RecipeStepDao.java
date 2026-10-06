package com.cookpilot.university.features.recipes.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeStepDao {

    @Query(
            "SELECT * FROM recipe_steps "
                    + "WHERE recipe_id = :recipeId "
                    + "ORDER BY step_number"
    )
    List<RecipeStepEntity> getForRecipe(String recipeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<RecipeStepEntity> steps);

    @Query("DELETE FROM recipe_steps")
    void deleteAll();
}
