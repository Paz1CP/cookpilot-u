package com.cookpilot.university.features.cooklist.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Dao
public interface ShoppingItemDao {

    @Query(
            "SELECT * FROM shopping_items "
                    + "WHERE user_id = :userId "
                    + "AND week_start = :weekStart "
                    + "ORDER BY purchased ASC, name COLLATE NOCASE ASC"
    )
    LiveData<List<ShoppingItemEntity>> observeWeek(
            String userId,
            String weekStart
    );

    @Query(
            "SELECT * FROM shopping_items "
                    + "WHERE user_id = :userId "
                    + "AND week_start = :weekStart "
                    + "AND manual = 0"
    )
    List<ShoppingItemEntity> getGeneratedForWeek(
            String userId,
            String weekStart
    );

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ShoppingItemEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ShoppingItemEntity> entities);

    @Query(
            "DELETE FROM shopping_items "
                    + "WHERE user_id = :userId "
                    + "AND week_start = :weekStart "
                    + "AND manual = 0"
    )
    void deleteGeneratedForWeek(
            String userId,
            String weekStart
    );

    @Query("DELETE FROM shopping_items WHERE id = :id")
    void deleteById(String id);

    @Query(
            "DELETE FROM shopping_items "
                    + "WHERE user_id = :userId "
                    + "AND week_start = :weekStart "
                    + "AND purchased = 1"
    )
    void deletePurchased(
            String userId,
            String weekStart
    );

    @Query(
            "DELETE FROM shopping_items "
                    + "WHERE user_id = :userId "
                    + "AND week_start = :weekStart"
    )
    void deleteWeek(
            String userId,
            String weekStart
    );

    @Query(
            "UPDATE shopping_items "
                    + "SET purchased = :purchased, "
                    + "updated_at = :updatedAt "
                    + "WHERE id = :id"
    )
    void updatePurchased(
            String id,
            boolean purchased,
            long updatedAt
    );

    @Query(
            "UPDATE shopping_items "
                    + "SET name = :name, "
                    + "quantity = :quantity, "
                    + "unit = :unit, "
                    + "updated_at = :updatedAt "
                    + "WHERE id = :id"
    )
    void updateDetails(
            String id,
            String name,
            double quantity,
            String unit,
            long updatedAt
    );

    @Transaction
    default void replaceGenerated(
            String userId,
            String weekStart,
            List<ShoppingItemEntity> generated
    ) {
        List<ShoppingItemEntity> existing =
                getGeneratedForWeek(userId, weekStart);
        Map<String, ShoppingItemEntity> existingById =
                new HashMap<>();

        for (ShoppingItemEntity item : existing) {
            existingById.put(item.id, item);
        }

        deleteGeneratedForWeek(userId, weekStart);

        List<ShoppingItemEntity> merged = new ArrayList<>();
        for (ShoppingItemEntity item : generated) {
            ShoppingItemEntity previous = existingById.get(item.id);

            if (previous == null) {
                merged.add(item);
                continue;
            }

            merged.add(new ShoppingItemEntity(
                    item.id,
                    item.userId,
                    item.weekStart,
                    item.ingredientId,
                    item.name,
                    item.quantity,
                    item.unit,
                    previous.purchased,
                    false,
                    item.sourceRecipeIds,
                    previous.createdAt,
                    item.updatedAt,
                    item.syncState
            ));
        }

        upsertAll(merged);
    }
}
