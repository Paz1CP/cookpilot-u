package com.cookpilot.university.features.recipes.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cookpilot.university.features.recipes.data.repository.RecipeRepository;

import java.util.List;

public final class RecipeDetailViewModelFactory
        implements ViewModelProvider.Factory {

    private final RecipeRepository repository;
    private final List<String> recipeIds;
    private final List<Integer> initialServings;

    public RecipeDetailViewModelFactory(
            @NonNull RecipeRepository repository,
            @NonNull List<String> recipeIds,
            @NonNull List<Integer> initialServings
    ) {
        this.repository = repository;
        this.recipeIds = recipeIds;
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
                recipeIds,
                initialServings
        );
    }
}
