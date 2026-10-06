package com.cookpilot.university.features.auth.presentation.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public final class LoginViewModel extends ViewModel {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final MutableLiveData<LoginUiState> uiState =
            new MutableLiveData<>(LoginUiState.idle());

    public LiveData<LoginUiState> getUiState() {
        return uiState;
    }

    public void submit(String rawEmail, String rawPassword) {
        String email = rawEmail == null ? "" : rawEmail.trim();
        String password = rawPassword == null ? "" : rawPassword;

        String emailError = null;
        String passwordError = null;

        if (email.isEmpty()) {
            emailError = "Ingresa tu correo";
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Ingresa un correo válido";
        }

        if (password.isEmpty()) {
            passwordError = "Ingresa tu contraseña";
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordError = "Usa al menos 6 caracteres";
        }

        if (emailError != null || passwordError != null) {
            uiState.setValue(LoginUiState.validationError(emailError, passwordError));
            return;
        }

        uiState.setValue(LoginUiState.validSubmission());
    }
}
