package com.cookpilot.university.features.cooklist.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.cookpilot.university.core.sync.SyncScheduler;
import com.cookpilot.university.features.cooklist.data.local.ShoppingItemDao;
import com.cookpilot.university.features.cooklist.data.local.ShoppingItemEntity;
import com.cookpilot.university.features.cooklist.data.remote.ShoppingFirestoreDataSource;
import com.cookpilot.university.features.cooklist.domain.model.GeneratedShoppingItem;
import com.cookpilot.university.features.cooklist.domain.model.ShoppingItem;
import com.cookpilot.university.features.cooklist.domain.usecase.GenerateShoppingList;
import com.cookpilot.university.features.cookplan.domain.model.PlannedRecipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ShoppingRepository {

    private final ShoppingItemDao dao;
    private final FirebaseAuth firebaseAuth;
    private final ShoppingFirestoreDataSource remoteDataSource;
    private final Context appContext;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public ShoppingRepository(
            @NonNull ShoppingItemDao dao,
            @NonNull FirebaseAuth firebaseAuth,
            @NonNull ShoppingFirestoreDataSource remoteDataSource,
            @NonNull Context context
    ) {
        this.dao = dao;
        this.firebaseAuth = firebaseAuth;
        this.remoteDataSource = remoteDataSource;
        appContext = context.getApplicationContext();
    }

    @NonNull
    public LiveData<List<ShoppingItem>> observeWeek(
            @NonNull LocalDate weekStart
    ) {
        String userId = currentUserId();
        if (userId == null) {
            return new MutableLiveData<>(Collections.emptyList());
        }

        return Transformations.map(
                dao.observeWeek(userId, weekStart.toString()),
                entities -> {
                    List<ShoppingItem> result = new ArrayList<>();
                    if (entities != null) {
                        for (ShoppingItemEntity entity : entities) {
                            result.add(toDomain(entity));
                        }
                    }
                    return Collections.unmodifiableList(result);
                }
        );
    }

    public void regenerateFromPlan(
            @NonNull LocalDate weekStart,
            @NonNull List<PlannedRecipe> plan
    ) {
        String userId = currentUserId();
        if (userId == null) {
            return;
        }

        executor.execute(() -> {
            long now = System.currentTimeMillis();
            List<ShoppingItemEntity> entities = new ArrayList<>();

            for (GeneratedShoppingItem item
                    : GenerateShoppingList.fromPlan(plan)) {
                String id = generatedId(
                        userId,
                        weekStart,
                        item.getIngredientId(),
                        item.getUnit()
                );

                entities.add(new ShoppingItemEntity(
                        id,
                        userId,
                        weekStart.toString(),
                        item.getIngredientId(),
                        item.getName(),
                        item.getQuantity(),
                        item.getUnit(),
                        false,
                        false,
                        encodeIds(item.getSourceRecipeIds()),
                        now,
                        now,
                        ShoppingItemEntity.PENDING_CREATE
                ));
            }

            dao.replaceGenerated(
                    userId,
                    weekStart.toString(),
                    entities
            );
            requestBackgroundSync();
        });
    }

    public void addManual(
            @NonNull LocalDate weekStart,
            @NonNull String name,
            double quantity,
            @NonNull String unit
    ) {
        String userId = currentUserId();
        String cleanedName = name.trim();
        if (userId == null || cleanedName.isEmpty()) {
            return;
        }

        executor.execute(() -> {
            long now = System.currentTimeMillis();
            dao.upsert(new ShoppingItemEntity(
                    UUID.randomUUID().toString(),
                    userId,
                    weekStart.toString(),
                    null,
                    cleanedName,
                    Math.max(0, quantity),
                    unit,
                    false,
                    true,
                    "[]",
                    now,
                    now,
                    ShoppingItemEntity.PENDING_CREATE
            ));
            requestBackgroundSync();
        });
    }

    public void updateItem(
            @NonNull ShoppingItem item,
            @NonNull String name,
            double quantity,
            @NonNull String unit
    ) {
        executor.execute(() -> {
            dao.updateDetails(
                    item.getId(),
                    name.trim(),
                    Math.max(0, quantity),
                    unit,
                    System.currentTimeMillis()
            );
            requestBackgroundSync();
        });
    }

    public void togglePurchased(@NonNull ShoppingItem item) {
        executor.execute(() -> {
            dao.updatePurchased(
                    item.getId(),
                    !item.isPurchased(),
                    System.currentTimeMillis()
            );
            requestBackgroundSync();
        });
    }

    public void delete(@NonNull ShoppingItem item) {
        executor.execute(() -> {
            markForDeletion(item.getId());
            requestBackgroundSync();
        });
    }

    public void clearPurchased(@NonNull LocalDate weekStart) {
        String userId = currentUserId();
        if (userId == null) {
            return;
        }

        executor.execute(() -> {
            for (ShoppingItemEntity entity
                    : dao.getWeekNow(userId, weekStart.toString())) {
                if (entity.purchased) {
                    markForDeletion(entity.id);
                }
            }
            requestBackgroundSync();
        });
    }

    public void clearWeek(@NonNull LocalDate weekStart) {
        String userId = currentUserId();
        if (userId == null) {
            return;
        }

        executor.execute(() -> {
            for (ShoppingItemEntity entity
                    : dao.getWeekNow(userId, weekStart.toString())) {
                markForDeletion(entity.id);
            }
            requestBackgroundSync();
        });
    }

    public void syncPendingBlocking() throws Exception {
        String userId = currentUserId();
        if (userId == null) {
            return;
        }

        for (ShoppingItemEntity entity : dao.getPending(userId)) {
            ShoppingItemEntity remote =
                    remoteDataSource.getByIdBlocking(userId, entity.id);

            if (remote != null && remote.updatedAt > entity.updatedAt) {
                dao.upsert(remote);
                continue;
            }

            if (ShoppingItemEntity.PENDING_DELETE.equals(
                    entity.syncState
            )) {
                remoteDataSource.deleteBlocking(userId, entity.id);
                dao.hardDelete(entity.id);
                continue;
            }

            remoteDataSource.writeBlocking(entity);
            dao.markSyncedIfVersion(entity.id, entity.updatedAt);
        }
    }

    public void refreshWeekBlocking(
            @NonNull LocalDate weekStart
    ) throws Exception {
        String userId = currentUserId();
        if (userId == null) {
            return;
        }

        Set<String> pendingIds = new HashSet<>();
        for (ShoppingItemEntity pending : dao.getPending(userId)) {
            pendingIds.add(pending.id);
        }

        List<ShoppingItemEntity> remoteItems =
                remoteDataSource.getWeekBlocking(
                        userId,
                        weekStart.toString()
                );
        List<ShoppingItemEntity> safeRemote = new ArrayList<>();

        for (ShoppingItemEntity remote : remoteItems) {
            if (!pendingIds.contains(remote.id)) {
                safeRemote.add(remote);
            }
        }

        dao.replaceSyncedWeek(
                userId,
                weekStart.toString(),
                safeRemote
        );
    }

    private void markForDeletion(@NonNull String id) {
        ShoppingItemEntity entity = dao.getById(id);
        if (entity == null) {
            return;
        }

        if (ShoppingItemEntity.PENDING_CREATE.equals(entity.syncState)) {
            dao.hardDelete(id);
        } else {
            dao.markDeleted(id, System.currentTimeMillis());
        }
    }

    private void requestBackgroundSync() {
        SyncScheduler.requestSync(appContext);
    }

    @NonNull
    private ShoppingItem toDomain(
            @NonNull ShoppingItemEntity entity
    ) {
        return new ShoppingItem(
                entity.id,
                entity.weekStart,
                entity.ingredientId,
                entity.name,
                entity.quantity,
                entity.unit,
                entity.purchased,
                entity.manual,
                decodeIds(entity.sourceRecipeIds)
        );
    }

    @NonNull
    private String generatedId(
            @NonNull String userId,
            @NonNull LocalDate weekStart,
            @NonNull String ingredientId,
            @NonNull String unit
    ) {
        String raw = userId
                + "|"
                + weekStart
                + "|"
                + ingredientId
                + "|"
                + unit;

        return UUID.nameUUIDFromBytes(
                raw.getBytes(StandardCharsets.UTF_8)
        ).toString();
    }

    @NonNull
    private String encodeIds(@NonNull List<String> ids) {
        return new JSONArray(ids).toString();
    }

    @NonNull
    private List<String> decodeIds(@NonNull String raw) {
        List<String> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(raw);
            for (int index = 0; index < array.length(); index++) {
                String value = array.optString(index, "").trim();
                if (!value.isEmpty()) {
                    result.add(value);
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private String currentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user == null ? null : user.getUid();
    }
}
