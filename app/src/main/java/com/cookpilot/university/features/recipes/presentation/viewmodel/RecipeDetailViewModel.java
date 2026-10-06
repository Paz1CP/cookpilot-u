package com.cookpilot.university.features.recipes.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cookpilot.university.features.recipes.data.repository.RecipeRepository;
import com.cookpilot.university.features.recipes.domain.model.RecipeDetail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RecipeDetailViewModel extends ViewModel {

    private final MutableLiveData<List<RecipeDetail>> details =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Map<String, Integer>> servingsByRecipe =
            new MutableLiveData<>(Collections.emptyMap());
    private final MutableLiveData<String> selectedRecipeId =
            new MutableLiveData<>();

    public RecipeDetailViewModel(
            @NonNull RecipeRepository repository,
            @NonNull List<String> recipeIds,
            @NonNull List<Integer> initialServings
    ) {
        List<RecipeDetail> loadedDetails = new ArrayList<>();
        Map<String, Integer> servings = new LinkedHashMap<>();

        for (int index = 0; index < recipeIds.size(); index++) {
            String recipeId = recipeIds.get(index);
            repository.getRecipeDetail(recipeId).ifPresent(detail -> {
                loadedDetails.add(detail);
                int value = index < initialServings.size()
                        ? initialServings.get(index)
                        : detail.getRecipe().getBaseServings();
                servings.put(recipeId, Math.max(1, value));
            });
        }

        details.setValue(
                Collections.unmodifiableList(loadedDetails)
        );
        servingsByRecipe.setValue(
                Collections.unmodifiableMap(
                        new LinkedHashMap<>(servings)
                )
        );

        if (!loadedDetails.isEmpty()) {
            selectedRecipeId.setValue(
                    loadedDetails.get(0).getRecipe().getId()
            );
        }
    }

    public LiveData<List<RecipeDetail>> getDetails() {
        return details;
    }

    public LiveData<Map<String, Integer>> getServingsByRecipe() {
        return servingsByRecipe;
    }

    public LiveData<String> getSelectedRecipeId() {
        return selectedRecipeId;
    }

    public void selectRecipe(@NonNull String recipeId) {
        selectedRecipeId.setValue(recipeId);
    }

    public void decreaseServings() {
        changeSelectedServings(-1);
    }

    public void increaseServings() {
        changeSelectedServings(1);
    }

    private void changeSelectedServings(int delta) {
        String recipeId = selectedRecipeId.getValue();
        Map<String, Integer> current = servingsByRecipe.getValue();

        if (recipeId == null || current == null) {
            return;
        }

        Integer currentServings = current.get(recipeId);
        if (currentServings == null) {
            return;
        }

        int next = Math.max(
                1,
                Math.min(24, currentServings + delta)
        );
        if (next == currentServings) {
            return;
        }

        Map<String, Integer> updated =
                new LinkedHashMap<>(current);
        updated.put(recipeId, next);
        servingsByRecipe.setValue(
                Collections.unmodifiableMap(updated)
        );
    }

    @Nullable
    public RecipeDetail findSelectedDetail() {
        String recipeId = selectedRecipeId.getValue();
        List<RecipeDetail> current = details.getValue();

        if (recipeId == null || current == null) {
            return null;
        }

        for (RecipeDetail detail : current) {
            if (recipeId.equals(detail.getRecipe().getId())) {
                return detail;
            }
        }

        return null;
    }
}
