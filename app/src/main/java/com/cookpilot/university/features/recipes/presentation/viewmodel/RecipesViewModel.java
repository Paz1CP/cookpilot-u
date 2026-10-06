package com.cookpilot.university.features.recipes.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cookpilot.university.features.recipes.data.repository.RecipeRepository;
import com.cookpilot.university.features.recipes.domain.model.Ingredient;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class RecipesViewModel extends ViewModel {

    public static final String DIFFICULTY_ALL = "all";
    public static final String DIFFICULTY_EASY = "easy";
    public static final String DIFFICULTY_MEDIUM = "medium";
    public static final String DIFFICULTY_HARD = "hard";

    private final RecipeRepository repository;
    private final MediatorLiveData<List<Recipe>> recipes =
            new MediatorLiveData<>();
    private final MutableLiveData<RecipesUiState> uiState =
            new MutableLiveData<>(RecipesUiState.loading());

    private List<Recipe> sourceRecipes = Collections.emptyList();
    private String query = "";
    private String difficulty = DIFFICULTY_ALL;
    private int maxMinutes;

    public RecipesViewModel(@NonNull RecipeRepository repository) {
        this.repository = repository;

        recipes.addSource(
                repository.observeRecipes(),
                values -> {
                    sourceRecipes = values == null
                            ? Collections.emptyList()
                            : values;
                    applyFilters();

                    if (!sourceRecipes.isEmpty()) {
                        uiState.setValue(RecipesUiState.ready());
                    }
                }
        );

        if (repository.hasCachedRecipes()) {
            uiState.setValue(RecipesUiState.ready());
        } else {
            refresh();
        }
    }

    public LiveData<List<Recipe>> getRecipes() {
        return recipes;
    }

    public LiveData<RecipesUiState> getUiState() {
        return uiState;
    }

    public void setQuery(@NonNull String query) {
        this.query = query;
        applyFilters();
    }

    public void setDifficulty(@NonNull String difficulty) {
        this.difficulty = difficulty;
        applyFilters();
    }

    public void setMaxMinutes(int maxMinutes) {
        this.maxMinutes = Math.max(0, maxMinutes);
        applyFilters();
    }

    @NonNull
    public String getDifficulty() {
        return difficulty;
    }

    public int getMaxMinutes() {
        return maxMinutes;
    }

    public boolean hasActiveFilters() {
        return !DIFFICULTY_ALL.equals(difficulty)
                || maxMinutes > 0;
    }

    public void refresh() {
        if (!repository.hasCachedRecipes()) {
            uiState.setValue(RecipesUiState.loading());
        }

        repository.refresh(new RecipeRepository.RefreshCallback() {
            @Override
            public void onSuccess() {
                uiState.postValue(RecipesUiState.ready());
            }

            @Override
            public void onError(@NonNull Exception exception) {
                boolean cached = repository.hasCachedRecipes();
                uiState.postValue(
                        RecipesUiState.error(
                                cached
                                        ? null
                                        : "No se pudo cargar el catálogo.",
                                cached
                        )
                );
            }
        });
    }

    private void applyFilters() {
        List<Recipe> filtered = new ArrayList<>();

        for (Recipe recipe : sourceRecipes) {
            if (!matchesDifficulty(recipe)
                    || !matchesTime(recipe)
                    || !matchesQuery(recipe)) {
                continue;
            }
            filtered.add(recipe);
        }

        recipes.setValue(
                Collections.unmodifiableList(filtered)
        );
    }

    private boolean matchesDifficulty(@NonNull Recipe recipe) {
        return DIFFICULTY_ALL.equals(difficulty)
                || difficulty.equals(recipe.getDifficulty());
    }

    private boolean matchesTime(@NonNull Recipe recipe) {
        return maxMinutes == 0
                || recipe.getTotalMinutes() <= maxMinutes;
    }

    private boolean matchesQuery(@NonNull Recipe recipe) {
        String normalizedQuery = normalize(query).trim();
        if (normalizedQuery.isEmpty()) {
            return true;
        }

        StringBuilder searchable = new StringBuilder()
                .append(recipe.getTitle()).append(' ')
                .append(recipe.getCanonicalName()).append(' ')
                .append(recipe.getDescription()).append(' ');

        for (String category : recipe.getCategorySlugs()) {
            searchable.append(category).append(' ');
        }

        for (Ingredient ingredient : recipe.getIngredients()) {
            searchable.append(ingredient.getName()).append(' ');
        }

        String normalizedSearchable = normalize(
                searchable.toString()
        );

        String[] terms = normalizedQuery.split("\\s+");
        for (String term : terms) {
            if (!normalizedSearchable.contains(term)) {
                return false;
            }
        }

        return true;
    }

    @NonNull
    private String normalize(@NonNull String value) {
        String normalized = Normalizer.normalize(
                value,
                Normalizer.Form.NFD
        );
        return normalized
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }
}
