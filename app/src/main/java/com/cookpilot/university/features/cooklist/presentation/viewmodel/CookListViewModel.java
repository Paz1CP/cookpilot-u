package com.cookpilot.university.features.cooklist.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.cookpilot.university.features.cooklist.data.repository.ShoppingRepository;
import com.cookpilot.university.features.cooklist.domain.model.ShoppingItem;
import com.cookpilot.university.features.cookplan.data.repository.CookPlanRepository;
import com.cookpilot.university.features.cookplan.domain.model.PlannedRecipe;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class CookListViewModel extends ViewModel {

    private final ShoppingRepository shoppingRepository;
    private final LocalDate weekStart;
    private final MediatorLiveData<List<ShoppingItem>> items =
            new MediatorLiveData<>();
    private final MediatorLiveData<Double> estimatedTotal =
            new MediatorLiveData<>();

    private List<ShoppingItem> sourceItems = Collections.emptyList();
    private String query = "";

    public CookListViewModel(
            @NonNull ShoppingRepository shoppingRepository,
            @NonNull CookPlanRepository cookPlanRepository
    ) {
        this.shoppingRepository = shoppingRepository;
        weekStart = LocalDate.now().with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        items.addSource(
                shoppingRepository.observeWeek(weekStart),
                values -> {
                    sourceItems = values == null
                            ? Collections.emptyList()
                            : values;
                    applyQuery();
                }
        );

        LiveData<List<PlannedRecipe>> plan =
                cookPlanRepository.observeWeek(weekStart);

        estimatedTotal.addSource(plan, values -> {
            List<PlannedRecipe> safe = values == null
                    ? Collections.emptyList()
                    : values;

            shoppingRepository.regenerateFromPlan(
                    weekStart,
                    safe
            );
            estimatedTotal.setValue(totalCost(safe));
        });

        cookPlanRepository.refreshWeek(weekStart);
    }

    public LiveData<List<ShoppingItem>> getItems() {
        return items;
    }

    public LiveData<Double> getEstimatedTotal() {
        return estimatedTotal;
    }

    public void setQuery(@NonNull String query) {
        this.query = query;
        applyQuery();
    }

    public void addManual(
            @NonNull String name,
            double quantity,
            @NonNull String unit
    ) {
        shoppingRepository.addManual(
                weekStart,
                name,
                quantity,
                unit
        );
    }

    public void updateItem(
            @NonNull ShoppingItem item,
            @NonNull String name,
            double quantity,
            @NonNull String unit
    ) {
        shoppingRepository.updateItem(
                item,
                name,
                quantity,
                unit
        );
    }

    public void togglePurchased(@NonNull ShoppingItem item) {
        shoppingRepository.togglePurchased(item);
    }

    public void delete(@NonNull ShoppingItem item) {
        shoppingRepository.delete(item);
    }

    public void clearPurchased() {
        shoppingRepository.clearPurchased(weekStart);
    }

    private void applyQuery() {
        String normalizedQuery = normalize(query).trim();

        if (normalizedQuery.isEmpty()) {
            items.setValue(
                    Collections.unmodifiableList(
                            new ArrayList<>(sourceItems)
                    )
            );
            return;
        }

        List<ShoppingItem> filtered = new ArrayList<>();
        for (ShoppingItem item : sourceItems) {
            if (normalize(item.getName()).contains(normalizedQuery)) {
                filtered.add(item);
            }
        }

        items.setValue(Collections.unmodifiableList(filtered));
    }

    private double totalCost(
            @NonNull List<PlannedRecipe> plan
    ) {
        double total = 0;

        for (PlannedRecipe plannedRecipe : plan) {
            Recipe recipe = plannedRecipe.getRecipe();
            total += recipe.getEstimatedCostPen()
                    * plannedRecipe.getServings()
                    / Math.max(1, recipe.getBaseServings());
        }

        return total;
    }

    @NonNull
    private String normalize(@NonNull String value) {
        return Normalizer.normalize(
                        value,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }
}
