package com.cookpilot.university.features.auth.presentation.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cookpilot.university.R;

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

        int emailError = 0;
        int passwordError = 0;

        if (email.isEmpty()) {
            emailError = R.string.login_error_email_required;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = R.string.login_error_email_invalid;
        }

        if (password.isEmpty()) {
            passwordError = R.string.login_error_password_required;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordError = R.string.login_error_password_short;
        }

        if (emailError != 0 || passwordError != 0) {
            uiState.setValue(
                    LoginUiState.validationError(emailError, passwordError)
            );
            return;
        }

        uiState.setValue(LoginUiState.validSubmission());
    }
}
