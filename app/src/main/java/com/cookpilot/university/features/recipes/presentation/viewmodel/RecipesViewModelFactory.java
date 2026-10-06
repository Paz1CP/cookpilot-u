package com.cookpilot.university.features.recipes.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cookpilot.university.features.recipes.data.repository.RecipeRepository;

public final class RecipesViewModelFactory
        implements ViewModelProvider.Factory {

    private final RecipeRepository repository;

    public RecipesViewModelFactory(@NonNull RecipeRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RecipesViewModel.class)) {
            return (T) new RecipesViewModel(repository);
        }

        throw new IllegalArgumentException(
                "ViewModel no soportado: " + modelClass.getName()
        );
    }
}
