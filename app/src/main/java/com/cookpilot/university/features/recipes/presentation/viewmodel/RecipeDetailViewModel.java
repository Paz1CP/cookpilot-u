package com.cookpilot.university.features.recipes.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cookpilot.university.features.recipes.data.repository.RecipeRepository;
import com.cookpilot.university.features.recipes.domain.model.RecipeDetail;

public final class RecipeDetailViewModel extends ViewModel {

    private final MutableLiveData<RecipeDetail> detail =
            new MutableLiveData<>();
    private final MutableLiveData<Integer> servings =
            new MutableLiveData<>();

    public RecipeDetailViewModel(
            @NonNull RecipeRepository repository,
            @NonNull String recipeId,
            int initialServings
    ) {
        repository.getRecipeDetail(recipeId)
                .ifPresent(detail::setValue);
        servings.setValue(Math.max(1, initialServings));
    }

    public LiveData<RecipeDetail> getDetail() {
        return detail;
    }

    public LiveData<Integer> getServings() {
        return servings;
    }

    public void decreaseServings() {
        Integer current = servings.getValue();
        if (current == null || current <= 1) {
            return;
        }
        servings.setValue(current - 1);
    }

    public void increaseServings() {
        Integer current = servings.getValue();
        servings.setValue(
                Math.min(24, (current == null ? 1 : current) + 1)
        );
    }
}
