package com.cookpilot.university.features.auth.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cookpilot.university.features.auth.data.repository.AuthRepository;

public final class LoginViewModelFactory implements ViewModelProvider.Factory {

    private final AuthRepository repository;

    public LoginViewModelFactory(@NonNull AuthRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(repository);
        }
        throw new IllegalArgumentException(
                "ViewModel no soportado: " + modelClass.getName()
        );
    }
}
