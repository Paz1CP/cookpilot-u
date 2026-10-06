package com.cookpilot.university.features.cookplan.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlannedRecipeDao {

    @Query(
            "SELECT * FROM planned_recipes " +
            "WHERE plan_date = :planDate " +
            "ORDER BY created_at ASC"
    )
    LiveData<List<PlannedRecipeEntity>> observeByDate(String planDate);

    @Query("SELECT COUNT(*) FROM planned_recipes WHERE plan_date = :planDate")
    int countForDate(String planDate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<PlannedRecipeEntity> entities);

    @Query(
            "DELETE FROM planned_recipes " +
            "WHERE plan_date = :planDate " +
            "AND meal_moment = :mealMoment " +
            "AND recipe_id = :recipeId"
    )
    void delete(String planDate, String mealMoment, String recipeId);

    @Query(
            "UPDATE planned_recipes SET servings = :servings " +
            "WHERE plan_date = :planDate " +
            "AND meal_moment = :mealMoment " +
            "AND recipe_id = :recipeId"
    )
    void updateServings(
            String planDate,
            String mealMoment,
            String recipeId,
            int servings
    );
}
