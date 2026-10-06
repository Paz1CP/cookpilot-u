package com.cookpilot.university.features.cookplan.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cookpilot.university.features.cookplan.data.repository.CookPlanRepository;

public final class CookPlanViewModelFactory implements ViewModelProvider.Factory {

    private final CookPlanRepository repository;

    public CookPlanViewModelFactory(CookPlanRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CookPlanViewModel.class)) {
            return (T) new CookPlanViewModel(repository);
        }
        throw new IllegalArgumentException(
                "Unsupported ViewModel: " + modelClass.getName()
        );
    }
}
