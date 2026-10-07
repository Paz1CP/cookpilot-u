package com.cookpilot.university.features.recipes.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cookpilot.university.core.sync.SyncScheduler;
import com.cookpilot.university.features.recipes.data.local.SavedRecipeDao;
import com.cookpilot.university.features.recipes.data.local.SavedRecipeEntity;
import com.cookpilot.university.features.recipes.data.remote.SavedRecipeFirestoreDataSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SavedRecipeRepository {

    private final SavedRecipeDao dao;
    private final FirebaseAuth firebaseAuth;
    private final SavedRecipeFirestoreDataSource remoteDataSource;
    private final Context appContext;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public SavedRecipeRepository(
            @NonNull SavedRecipeDao dao,
            @NonNull FirebaseAuth firebaseAuth,
            @NonNull SavedRecipeFirestoreDataSource remoteDataSource,
            @NonNull Context context
    ) {
        this.dao = dao;
        this.firebaseAuth = firebaseAuth;
        this.remoteDataSource = remoteDataSource;
        appContext = context.getApplicationContext();
    }

    @NonNull
    public LiveData<List<String>> observeSavedRecipeIds() {
        String userId = currentUserId();
        if (userId == null) {
            return new MutableLiveData<>(Collections.emptyList());
        }
        return dao.observeRecipeIds(userId);
    }

    public void toggle(@NonNull String recipeId) {
        String userId = currentUserId();
        if (userId == null) {
            return;
        }

        executor.execute(() -> {
            SavedRecipeEntity current =
                    dao.getById(userId, recipeId);

            if (current == null
                    || SavedRecipeEntity.PENDING_DELETE.equals(
                            current.syncState
                    )) {
                dao.upsert(new SavedRecipeEntity(
                        userId,
                        recipeId,
                        System.currentTimeMillis(),
                        SavedRecipeEntity.PENDING_CREATE
                ));
            } else if (SavedRecipeEntity.PENDING_CREATE.equals(
                    current.syncState
            )) {
                dao.hardDelete(userId, recipeId);
            } else {
                dao.markDeleted(userId, recipeId);
            }

            SyncScheduler.requestSync(appContext);
        });
    }

    public void syncPendingBlocking() throws Exception {
        String userId = currentUserId();
        if (userId == null) {
            return;
        }

        for (SavedRecipeEntity entity : dao.getPending(userId)) {
            if (SavedRecipeEntity.PENDING_DELETE.equals(
                    entity.syncState
            )) {
                remoteDataSource.deleteBlocking(
                        userId,
                        entity.recipeId
                );
                dao.hardDelete(userId, entity.recipeId);
                continue;
            }

            remoteDataSource.writeBlocking(entity);
            dao.markSyncedIfVersion(
                    userId,
                    entity.recipeId,
                    entity.savedAt
            );
        }
    }

    public void refreshBlocking() throws Exception {
        String userId = currentUserId();
        if (userId == null) {
            return;
        }

        Set<String> pendingIds = new HashSet<>();
        for (SavedRecipeEntity pending : dao.getPending(userId)) {
            pendingIds.add(pending.recipeId);
        }

        List<SavedRecipeEntity> safeRemote = new ArrayList<>();
        for (SavedRecipeEntity remote
                : remoteDataSource.getAllBlocking(userId)) {
            if (!pendingIds.contains(remote.recipeId)) {
                safeRemote.add(remote);
            }
        }

        dao.replaceSynced(userId, safeRemote);
    }

    private String currentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user == null ? null : user.getUid();
    }
}
