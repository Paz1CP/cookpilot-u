package com.cookpilot.university.features.recipes.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cookpilot.university.features.recipes.data.repository.RecipeRepository;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.util.List;

public final class RecipesViewModel extends ViewModel {

    private final RecipeRepository repository;
    private final LiveData<List<Recipe>> recipes;
    private final MutableLiveData<RecipesUiState> uiState =
            new MutableLiveData<>(RecipesUiState.loading());

    public RecipesViewModel(@NonNull RecipeRepository repository) {
        this.repository = repository;
        recipes = repository.observeRecipes();
        refresh();
    }

    public LiveData<List<Recipe>> getRecipes() {
        return recipes;
    }

    public LiveData<RecipesUiState> getUiState() {
        return uiState;
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
}
