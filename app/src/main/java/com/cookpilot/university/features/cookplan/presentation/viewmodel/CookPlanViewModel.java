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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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

        repository.syncPending();
        repository.refreshWeek(weekStart(LocalDate.now()));
    }

    public LiveData<LocalDate> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<List<PlannedRecipe>> getPlannedRecipes() {
        return plannedRecipes;
    }

    public void selectDate(@NonNull LocalDate date) {
        LocalDate current = selectedDate.getValue();
        if (date.equals(current)) {
            return;
        }

        selectedDate.setValue(date);

        if (current == null
                || !weekStart(current).equals(weekStart(date))) {
            repository.refreshWeek(weekStart(date));
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

    public void replaceRecipe(
            @NonNull PlannedRecipe plannedRecipe,
            @NonNull Recipe replacement
    ) {
        repository.replaceRecipe(plannedRecipe, replacement);
    }

    public void removeRecipe(@NonNull PlannedRecipe plannedRecipe) {
        repository.removeRecipe(plannedRecipe);
    }

    public void updateServings(
            @NonNull PlannedRecipe plannedRecipe,
            int servings
    ) {
        repository.updateServings(
                plannedRecipe,
                Math.max(1, servings)
        );
    }

    @NonNull
    private LocalDate weekStart(@NonNull LocalDate date) {
        return date.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );
    }
}
