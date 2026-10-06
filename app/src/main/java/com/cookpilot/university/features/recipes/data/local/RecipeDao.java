package com.cookpilot.university.features.recipes.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeDao {

    @Query("SELECT * FROM recipes ORDER BY title")
    List<RecipeEntity> getAll();

    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    RecipeEntity getById(String recipeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<RecipeEntity> recipes);

    @Query("DELETE FROM recipes")
    void deleteAll();
}
