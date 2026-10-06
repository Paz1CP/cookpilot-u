package com.cookpilot.university.features.recipes.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeIngredientDao {

    @Query(
            "SELECT ri.ingredient_id AS ingredient_id, "
                    + "i.name AS name, "
                    + "i.image_url AS image_url, "
                    + "ri.quantity AS quantity, "
                    + "ri.unit AS unit, "
                    + "ri.is_optional AS is_optional "
                    + "FROM recipe_ingredients ri "
                    + "INNER JOIN ingredients i "
                    + "ON i.id = ri.ingredient_id "
                    + "WHERE ri.recipe_id = :recipeId "
                    + "ORDER BY ri.position"
    )
    List<RecipeIngredientRow> getForRecipe(String recipeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<RecipeIngredientEntity> rows);

    @Query("DELETE FROM recipe_ingredients")
    void deleteAll();
}
