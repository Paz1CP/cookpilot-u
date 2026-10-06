package com.cookpilot.university.features.cookplan.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface PlannedRecipeDao {

    @Query(
            "SELECT * FROM planned_recipes "
                    + "WHERE user_id = :userId "
                    + "AND plan_date = :planDate "
                    + "AND sync_state != 'PENDING_DELETE' "
                    + "ORDER BY created_at ASC"
    )
    LiveData<List<PlannedRecipeEntity>> observeByDate(
            String userId,
            String planDate
    );

    @Query(
            "SELECT * FROM planned_recipes "
                    + "WHERE user_id = :userId "
                    + "AND plan_date BETWEEN :from AND :to "
                    + "AND sync_state != 'PENDING_DELETE' "
                    + "ORDER BY plan_date, created_at ASC"
    )
    LiveData<List<PlannedRecipeEntity>> observeRange(
            String userId,
            String from,
            String to
    );

    @Query(
            "SELECT * FROM planned_recipes "
                    + "WHERE user_id = :userId "
                    + "AND sync_state != 'SYNCED' "
                    + "ORDER BY updated_at ASC"
    )
    List<PlannedRecipeEntity> getPending(String userId);

    @Query("SELECT * FROM planned_recipes WHERE id = :id LIMIT 1")
    PlannedRecipeEntity getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(PlannedRecipeEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<PlannedRecipeEntity> entities);

    @Query(
            "UPDATE planned_recipes "
                    + "SET servings = :servings, "
                    + "updated_at = :updatedAt, "
                    + "sync_state = CASE "
                    + "WHEN sync_state = 'PENDING_CREATE' THEN 'PENDING_CREATE' "
                    + "ELSE 'PENDING_UPDATE' END "
                    + "WHERE id = :id"
    )
    void updateServings(
            String id,
            int servings,
            long updatedAt
    );

    @Query(
            "UPDATE planned_recipes "
                    + "SET recipe_id = :recipeId, "
                    + "servings = :servings, "
                    + "updated_at = :updatedAt, "
                    + "sync_state = CASE "
                    + "WHEN sync_state = 'PENDING_CREATE' THEN 'PENDING_CREATE' "
                    + "ELSE 'PENDING_UPDATE' END "
                    + "WHERE id = :id"
    )
    void replaceRecipe(
            String id,
            String recipeId,
            int servings,
            long updatedAt
    );

    @Query(
            "UPDATE planned_recipes "
                    + "SET updated_at = :updatedAt, "
                    + "sync_state = 'PENDING_DELETE' "
                    + "WHERE id = :id"
    )
    void markDeleted(String id, long updatedAt);

    @Query(
            "UPDATE planned_recipes "
                    + "SET sync_state = 'SYNCED', updated_at = :syncedAt "
                    + "WHERE id = :id "
                    + "AND updated_at = :expectedUpdatedAt "
                    + "AND sync_state != 'PENDING_DELETE'"
    )
    int markSyncedIfVersion(
            String id,
            long expectedUpdatedAt,
            long syncedAt
    );

    @Query("DELETE FROM planned_recipes WHERE id = :id")
    void hardDelete(String id);

    @Query(
            "DELETE FROM planned_recipes "
                    + "WHERE user_id = :userId "
                    + "AND plan_date BETWEEN :from AND :to "
                    + "AND sync_state = 'SYNCED'"
    )
    void deleteSyncedRange(
            String userId,
            String from,
            String to
    );

    @Transaction
    default void replaceSyncedRange(
            String userId,
            String from,
            String to,
            List<PlannedRecipeEntity> entries
    ) {
        deleteSyncedRange(userId, from, to);
        upsertAll(entries);
    }
}
