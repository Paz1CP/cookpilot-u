package com.cookpilot.university.features.recipes.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cookpilot.university.features.recipes.data.local.RecipeLocalDataSource;
import com.cookpilot.university.features.recipes.data.remote.RecipeRemoteDataSource;
import com.cookpilot.university.features.recipes.data.remote.dto.RecipeDto;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RecipeRepository {

    public interface RefreshCallback {
        void onSuccess();
        void onError(@NonNull Exception exception);
    }

    private final RecipeLocalDataSource localDataSource;
    private final RecipeRemoteDataSource remoteDataSource;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<Recipe>> recipes =
            new MutableLiveData<>(Collections.emptyList());

    private volatile Map<String, Recipe> recipeCache =
            Collections.emptyMap();

    public RecipeRepository(
            @NonNull RecipeLocalDataSource localDataSource,
            @NonNull RecipeRemoteDataSource remoteDataSource
    ) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        loadLocalCatalog();
    }

    @NonNull
    public LiveData<List<Recipe>> observeRecipes() {
        return recipes;
    }

    @NonNull
    public List<Recipe> getAllRecipes() {
        List<Recipe> current = recipes.getValue();
        return current == null
                ? Collections.emptyList()
                : current;
    }

    @NonNull
    public Optional<Recipe> findById(@NonNull String recipeId) {
        return Optional.ofNullable(recipeCache.get(recipeId));
    }

    public boolean hasCachedRecipes() {
        return !recipeCache.isEmpty();
    }

    public void refresh(@NonNull RefreshCallback callback) {
        remoteDataSource.getRecipes(new RecipeRemoteDataSource.CatalogCallback() {
            @Override
            public void onSuccess(@NonNull List<RecipeDto> remoteRecipes) {
                executor.execute(() -> {
                    try {
                        localDataSource.replaceCatalog(remoteRecipes);
                        publish(localDataSource.loadAll());
                        callback.onSuccess();
                    } catch (Exception exception) {
                        callback.onError(exception);
                    }
                });
            }

            @Override
            public void onError(@NonNull Exception exception) {
                callback.onError(exception);
            }
        });
    }

    private void loadLocalCatalog() {
        executor.execute(() -> {
            try {
                publish(localDataSource.loadAll());
            } catch (Exception ignored) {
                publish(Collections.emptyList());
            }
        });
    }

    private void publish(@NonNull List<Recipe> values) {
        List<Recipe> immutable = Collections.unmodifiableList(
                new ArrayList<>(values)
        );
        Map<String, Recipe> byId = new LinkedHashMap<>();

        for (Recipe recipe : immutable) {
            byId.put(recipe.getId(), recipe);
        }

        recipeCache = Collections.unmodifiableMap(byId);
        recipes.postValue(immutable);
    }
}
