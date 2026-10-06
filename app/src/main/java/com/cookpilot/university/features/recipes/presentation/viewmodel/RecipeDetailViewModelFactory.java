package com.cookpilot.university.features.recipes.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cookpilot.university.features.recipes.data.repository.RecipeRepository;

public final class RecipeDetailViewModelFactory
        implements ViewModelProvider.Factory {

    private final RecipeRepository repository;
    private final String recipeId;
    private final int initialServings;

    public RecipeDetailViewModelFactory(
            @NonNull RecipeRepository repository,
            @NonNull String recipeId,
            int initialServings
    ) {
        this.repository = repository;
        this.recipeId = recipeId;
        this.initialServings = initialServings;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (!modelClass.isAssignableFrom(
                RecipeDetailViewModel.class
        )) {
            throw new IllegalArgumentException(
                    "ViewModel no soportado"
            );
        }

        return (T) new RecipeDetailViewModel(
                repository,
                recipeId,
                initialServings
        );
    }
}
