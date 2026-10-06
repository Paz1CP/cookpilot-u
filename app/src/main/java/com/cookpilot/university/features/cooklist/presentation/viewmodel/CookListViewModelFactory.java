package com.cookpilot.university.features.cooklist.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cookpilot.university.features.cooklist.data.repository.ShoppingRepository;
import com.cookpilot.university.features.cookplan.data.repository.CookPlanRepository;

public final class CookListViewModelFactory
        implements ViewModelProvider.Factory {

    private final ShoppingRepository shoppingRepository;
    private final CookPlanRepository cookPlanRepository;

    public CookListViewModelFactory(
            @NonNull ShoppingRepository shoppingRepository,
            @NonNull CookPlanRepository cookPlanRepository
    ) {
        this.shoppingRepository = shoppingRepository;
        this.cookPlanRepository = cookPlanRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (!modelClass.isAssignableFrom(
                CookListViewModel.class
        )) {
            throw new IllegalArgumentException(
                    "ViewModel no soportado"
            );
        }

        return (T) new CookListViewModel(
                shoppingRepository,
                cookPlanRepository
        );
    }
}
