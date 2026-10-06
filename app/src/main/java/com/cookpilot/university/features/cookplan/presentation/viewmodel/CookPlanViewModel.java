package com.cookpilot.university.features.cookplan.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.cookpilot.university.features.cookplan.data.repository.CookPlanRepository;
import com.cookpilot.university.features.cookplan.domain.model.MealMoment;
import com.cookpilot.university.features.cookplan.domain.model.PlannedRecipe;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.time.LocalDate;
import java.util.List;

public final class CookPlanViewModel extends ViewModel {

    private final CookPlanRepository repository;
    private final MutableLiveData<LocalDate> selectedDate =
            new MutableLiveData<>(LocalDate.now());
    private final LiveData<List<PlannedRecipe>> plannedRecipes;

    public CookPlanViewModel(@NonNull CookPlanRepository repository) {
        this.repository = repository;
        plannedRecipes = Transformations.switchMap(
                selectedDate,
                repository::observeDay
        );
        repository.seedDemoDayIfEmpty(LocalDate.now());
    }

    public LiveData<LocalDate> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<List<PlannedRecipe>> getPlannedRecipes() {
        return plannedRecipes;
    }

    public void selectDate(@NonNull LocalDate date) {
        if (!date.equals(selectedDate.getValue())) {
            selectedDate.setValue(date);
        }
    }

    public void addRecipes(
            @NonNull MealMoment mealMoment,
            @NonNull List<Recipe> recipes
    ) {
        LocalDate date = selectedDate.getValue();
        if (date != null) {
            repository.addRecipes(date, mealMoment, recipes);
        }
    }

    public void removeRecipe(@NonNull PlannedRecipe plannedRecipe) {
        LocalDate date = selectedDate.getValue();
        if (date != null) {
            repository.removeRecipe(
                    date,
                    plannedRecipe.getMealMoment(),
                    plannedRecipe.getRecipe().getId()
            );
        }
    }

    public void updateServings(
            @NonNull PlannedRecipe plannedRecipe,
            int servings
    ) {
        LocalDate date = selectedDate.getValue();
        if (date != null) {
            repository.updateServings(
                    date,
                    plannedRecipe.getMealMoment(),
                    plannedRecipe.getRecipe().getId(),
                    servings
            );
        }
    }
}
