package com.cookpilot.university.features.recipes.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface IngredientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<IngredientEntity> ingredients);

    @Query("DELETE FROM ingredients")
    void deleteAll();
}
