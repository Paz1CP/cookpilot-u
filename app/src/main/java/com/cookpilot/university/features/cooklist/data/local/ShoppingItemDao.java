package com.cookpilot.university.features.cooklist.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Dao
public interface ShoppingItemDao {

    @Query(
            "SELECT * FROM shopping_items "
                    + "WHERE user_id = :userId "
                    + "AND week_start = :weekStart "
                    + "AND sync_state != 'PENDING_DELETE' "
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
                    + "AND sync_state != 'PENDING_DELETE'"
    )
    List<ShoppingItemEntity> getWeekNow(
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

    @Query(
            "SELECT * FROM shopping_items "
                    + "WHERE user_id = :userId "
                    + "AND sync_state != 'SYNCED' "
                    + "ORDER BY updated_at ASC"
    )
    List<ShoppingItemEntity> getPending(String userId);

    @Query("SELECT * FROM shopping_items WHERE id = :id LIMIT 1")
    ShoppingItemEntity getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ShoppingItemEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ShoppingItemEntity> entities);

    @Query("DELETE FROM shopping_items WHERE id = :id")
    void hardDelete(String id);

    @Query(
            "UPDATE shopping_items "
                    + "SET purchased = :purchased, "
                    + "updated_at = :updatedAt, "
                    + "sync_state = CASE "
                    + "WHEN sync_state = 'PENDING_CREATE' THEN 'PENDING_CREATE' "
                    + "ELSE 'PENDING_UPDATE' END "
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
                    + "updated_at = :updatedAt, "
                    + "sync_state = CASE "
                    + "WHEN sync_state = 'PENDING_CREATE' THEN 'PENDING_CREATE' "
                    + "ELSE 'PENDING_UPDATE' END "
                    + "WHERE id = :id"
    )
    void updateDetails(
            String id,
            String name,
            double quantity,
            String unit,
            long updatedAt
    );

    @Query(
            "UPDATE shopping_items "
                    + "SET updated_at = :updatedAt, "
                    + "sync_state = 'PENDING_DELETE' "
                    + "WHERE id = :id"
    )
    void markDeleted(String id, long updatedAt);

    @Query(
            "UPDATE shopping_items "
                    + "SET sync_state = 'SYNCED' "
                    + "WHERE id = :id "
                    + "AND updated_at = :expectedUpdatedAt "
                    + "AND sync_state != 'PENDING_DELETE'"
    )
    int markSyncedIfVersion(
            String id,
            long expectedUpdatedAt
    );

    @Query(
            "DELETE FROM shopping_items "
                    + "WHERE user_id = :userId "
                    + "AND week_start = :weekStart "
                    + "AND sync_state = 'SYNCED'"
    )
    void deleteSyncedWeek(String userId, String weekStart);

    @Transaction
    default void replaceSyncedWeek(
            String userId,
            String weekStart,
            List<ShoppingItemEntity> remoteItems
    ) {
        deleteSyncedWeek(userId, weekStart);
        upsertAll(remoteItems);
    }

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
        Set<String> nextIds = new HashSet<>();

        for (ShoppingItemEntity item : existing) {
            existingById.put(item.id, item);
        }

        List<ShoppingItemEntity> merged = new ArrayList<>();
        for (ShoppingItemEntity item : generated) {
            nextIds.add(item.id);
            ShoppingItemEntity previous = existingById.get(item.id);

            if (previous == null) {
                merged.add(item);
                continue;
            }

            boolean changed =
                    Double.compare(previous.quantity, item.quantity) != 0
                            || !previous.name.equals(item.name)
                            || !previous.unit.equals(item.unit)
                            || !previous.sourceRecipeIds.equals(
                                    item.sourceRecipeIds
                            )
                            || ShoppingItemEntity.PENDING_DELETE.equals(
                                    previous.syncState
                            );

            String nextState;
            long updatedAt;

            if (ShoppingItemEntity.PENDING_CREATE.equals(
                    previous.syncState
            )) {
                nextState = ShoppingItemEntity.PENDING_CREATE;
                updatedAt = item.updatedAt;
            } else if (changed) {
                nextState = ShoppingItemEntity.PENDING_UPDATE;
                updatedAt = item.updatedAt;
            } else {
                nextState = previous.syncState;
                updatedAt = previous.updatedAt;
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
                    updatedAt,
                    nextState
            ));
        }

        upsertAll(merged);

        long now = System.currentTimeMillis();
        for (ShoppingItemEntity previous : existing) {
            if (nextIds.contains(previous.id)) {
                continue;
            }

            if (ShoppingItemEntity.PENDING_CREATE.equals(
                    previous.syncState
            )) {
                hardDelete(previous.id);
            } else {
                markDeleted(previous.id, now);
            }
        }
    }
}
