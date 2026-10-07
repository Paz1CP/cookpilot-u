package com.cookpilot.university.features.recipes.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface SavedRecipeDao {

    @Query(
            "SELECT recipe_id FROM saved_recipes "
                    + "WHERE user_id = :userId "
                    + "AND sync_state != 'PENDING_DELETE' "
                    + "ORDER BY saved_at DESC"
    )
    LiveData<List<String>> observeRecipeIds(String userId);

    @Query(
            "SELECT * FROM saved_recipes "
                    + "WHERE user_id = :userId "
                    + "AND recipe_id = :recipeId LIMIT 1"
    )
    SavedRecipeEntity getById(String userId, String recipeId);

    @Query(
            "SELECT * FROM saved_recipes "
                    + "WHERE user_id = :userId "
                    + "AND sync_state != 'SYNCED' "
                    + "ORDER BY saved_at ASC"
    )
    List<SavedRecipeEntity> getPending(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SavedRecipeEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<SavedRecipeEntity> entities);

    @Query(
            "UPDATE saved_recipes "
                    + "SET sync_state = 'PENDING_DELETE' "
                    + "WHERE user_id = :userId AND recipe_id = :recipeId"
    )
    void markDeleted(String userId, String recipeId);

    @Query(
            "UPDATE saved_recipes "
                    + "SET sync_state = 'SYNCED' "
                    + "WHERE user_id = :userId "
                    + "AND recipe_id = :recipeId "
                    + "AND saved_at = :savedAt"
    )
    int markSyncedIfVersion(
            String userId,
            String recipeId,
            long savedAt
    );

    @Query(
            "DELETE FROM saved_recipes "
                    + "WHERE user_id = :userId AND recipe_id = :recipeId"
    )
    void hardDelete(String userId, String recipeId);

    @Query(
            "DELETE FROM saved_recipes "
                    + "WHERE user_id = :userId AND sync_state = 'SYNCED'"
    )
    void deleteSynced(String userId);

    @Transaction
    default void replaceSynced(
            String userId,
            List<SavedRecipeEntity> remote
    ) {
        deleteSynced(userId);
        upsertAll(remote);
    }
}
