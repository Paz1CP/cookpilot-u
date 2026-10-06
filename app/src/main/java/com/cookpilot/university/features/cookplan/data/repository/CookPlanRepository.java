package com.cookpilot.university.features.cookplan.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.cookpilot.university.features.cookplan.data.local.PlannedRecipeDao;
import com.cookpilot.university.features.cookplan.data.local.PlannedRecipeEntity;
import com.cookpilot.university.features.cookplan.data.remote.CookPlanRemoteDataSource;
import com.cookpilot.university.features.cookplan.data.remote.PlanEntryDto;
import com.cookpilot.university.features.cookplan.domain.model.MealMoment;
import com.cookpilot.university.features.cookplan.domain.model.PlannedRecipe;
import com.cookpilot.university.features.recipes.data.repository.RecipeRepository;
import com.cookpilot.university.features.recipes.domain.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CookPlanRepository {

    private final PlannedRecipeDao dao;
    private final RecipeRepository recipeRepository;
    private final CookPlanRemoteDataSource remoteDataSource;
    private final FirebaseAuth firebaseAuth;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public CookPlanRepository(
            @NonNull PlannedRecipeDao dao,
            @NonNull RecipeRepository recipeRepository,
            @NonNull CookPlanRemoteDataSource remoteDataSource,
            @NonNull FirebaseAuth firebaseAuth
    ) {
        this.dao = dao;
        this.recipeRepository = recipeRepository;
        this.remoteDataSource = remoteDataSource;
        this.firebaseAuth = firebaseAuth;
    }

    public LiveData<List<PlannedRecipe>> observeDay(@NonNull LocalDate date) {
        String userId = currentUserId();
        if (userId == null) {
            return new MutableLiveData<>(Collections.emptyList());
        }

        return Transformations.map(
                dao.observeByDate(userId, date.toString()),
                this::toDomain
        );
    }

    public LiveData<List<PlannedRecipe>> observeWeek(
            @NonNull LocalDate weekStart
    ) {
        String userId = currentUserId();
        if (userId == null) {
            return new MutableLiveData<>(Collections.emptyList());
        }

        return Transformations.map(
                dao.observeRange(
                        userId,
                        weekStart.toString(),
                        weekStart.plusDays(6).toString()
                ),
                this::toDomain
        );
    }

    public void addRecipes(
            @NonNull LocalDate date,
            @NonNull MealMoment mealMoment,
            @NonNull List<Recipe> recipes
    ) {
        String userId = currentUserId();
        if (userId == null || recipes.isEmpty()) {
            return;
        }

        executor.execute(() -> {
            long now = System.currentTimeMillis();

            for (int index = 0; index < recipes.size(); index++) {
                Recipe recipe = recipes.get(index);
                long createdAt = now + index;

                dao.upsert(new PlannedRecipeEntity(
                        UUID.randomUUID().toString(),
                        userId,
                        date.toString(),
                        mealMoment.getStorageKey(),
                        recipe.getId(),
                        recipe.getBaseServings(),
                        createdAt,
                        createdAt,
                        PlannedRecipeEntity.PENDING_CREATE
                ));
            }

            syncPending();
        });
    }

    public void replaceRecipe(
            @NonNull PlannedRecipe plannedRecipe,
            @NonNull Recipe replacement
    ) {
        executor.execute(() -> {
            dao.replaceRecipe(
                    plannedRecipe.getId(),
                    replacement.getId(),
                    replacement.getBaseServings(),
                    System.currentTimeMillis()
            );
            syncPending();
        });
    }

    public void removeRecipe(@NonNull PlannedRecipe plannedRecipe) {
        executor.execute(() -> {
            PlannedRecipeEntity entity = dao.getById(plannedRecipe.getId());
            if (entity == null) {
                return;
            }

            if (PlannedRecipeEntity.PENDING_CREATE.equals(entity.syncState)) {
                dao.hardDelete(entity.id);
            } else {
                dao.markDeleted(entity.id, System.currentTimeMillis());
                syncPending();
            }
        });
    }

    public void updateServings(
            @NonNull PlannedRecipe plannedRecipe,
            int servings
    ) {
        executor.execute(() -> {
            dao.updateServings(
                    plannedRecipe.getId(),
                    Math.max(1, servings),
                    System.currentTimeMillis()
            );
            syncPending();
        });
    }

    public void refreshWeek(@NonNull LocalDate weekStart) {
        String userId = currentUserId();
        if (userId == null) {
            return;
        }

        LocalDate weekEnd = weekStart.plusDays(6);

        remoteDataSource.getRange(
                weekStart.toString(),
                weekEnd.toString(),
                new CookPlanRemoteDataSource.ListCallback() {
                    @Override
                    public void onSuccess(
                            @NonNull List<PlanEntryDto> entries
                    ) {
                        executor.execute(() -> {
                            Set<String> pendingIds = new HashSet<>();
                            for (PlannedRecipeEntity pending
                                    : dao.getPending(userId)) {
                                pendingIds.add(pending.id);
                            }

                            List<PlannedRecipeEntity> remoteEntries =
                                    new ArrayList<>();
                            for (PlanEntryDto entry : entries) {
                                if (!pendingIds.contains(entry.getId())) {
                                    remoteEntries.add(
                                            fromRemote(entry, userId)
                                    );
                                }
                            }

                            dao.replaceSyncedRange(
                                    userId,
                                    weekStart.toString(),
                                    weekEnd.toString(),
                                    remoteEntries
                            );
                        });
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        // Room mantiene la planificación disponible sin red.
                    }
                }
        );
    }

    public void syncPending() {
        String userId = currentUserId();
        if (userId == null) {
            return;
        }

        executor.execute(() -> {
            List<PlannedRecipeEntity> pending = dao.getPending(userId);
            for (PlannedRecipeEntity entity : pending) {
                syncEntity(entity);
            }
        });
    }

    private void syncEntity(@NonNull PlannedRecipeEntity entity) {
        if (PlannedRecipeEntity.PENDING_DELETE.equals(entity.syncState)) {
            remoteDataSource.delete(
                    entity.id,
                    new CookPlanRemoteDataSource.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            executor.execute(() -> dao.hardDelete(entity.id));
                        }

                        @Override
                        public void onError(@NonNull Exception exception) {
                        }
                    }
            );
            return;
        }

        PlanEntryDto dto = toRemote(entity);
        CookPlanRemoteDataSource.EntryCallback callback =
                new CookPlanRemoteDataSource.EntryCallback() {
                    @Override
                    public void onSuccess(@NonNull PlanEntryDto entry) {
                        executor.execute(() -> {
                            dao.markSyncedIfVersion(
                                    entity.id,
                                    entity.updatedAt,
                                    System.currentTimeMillis()
                            );
                        });
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                    }
                };

        if (PlannedRecipeEntity.PENDING_CREATE.equals(entity.syncState)) {
            remoteDataSource.create(dto, callback);
        } else {
            remoteDataSource.update(dto, callback);
        }
    }

    @NonNull
    private PlanEntryDto toRemote(@NonNull PlannedRecipeEntity entity) {
        return new PlanEntryDto(
                entity.id,
                entity.planDate,
                entity.mealMoment,
                entity.recipeId,
                entity.servings
        );
    }

    @NonNull
    private PlannedRecipeEntity fromRemote(
            @NonNull PlanEntryDto dto,
            @NonNull String userId
    ) {
        long now = System.currentTimeMillis();

        return new PlannedRecipeEntity(
                dto.getId(),
                userId,
                dto.getDate(),
                dto.getMealMoment(),
                dto.getRecipeId(),
                dto.getServings(),
                parseTime(dto.getCreatedAt(), now),
                parseTime(dto.getUpdatedAt(), now),
                PlannedRecipeEntity.SYNCED
        );
    }

    private long parseTime(String value, long fallback) {
        if (value == null || value.isEmpty()) {
            return fallback;
        }

        try {
            return Instant.parse(value).toEpochMilli();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private List<PlannedRecipe> toDomain(
            List<PlannedRecipeEntity> entities
    ) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        List<PlannedRecipe> result = new ArrayList<>();
        for (PlannedRecipeEntity entity : entities) {
            MealMoment mealMoment =
                    MealMoment.fromStorageKey(entity.mealMoment);
            Optional<Recipe> recipe =
                    recipeRepository.findById(entity.recipeId);

            if (mealMoment == null || !recipe.isPresent()) {
                continue;
            }

            result.add(new PlannedRecipe(
                    entity.id,
                    recipe.get(),
                    mealMoment,
                    entity.servings
            ));
        }

        return Collections.unmodifiableList(result);
    }

    private String currentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user == null ? null : user.getUid();
    }
}
