package com.cookpilot.university.features.auth.presentation.viewmodel;

import androidx.annotation.Nullable;

public final class LoginUiState {

    private final boolean valid;
    private final boolean submitted;
    @Nullable
    private final String emailError;
    @Nullable
    private final String passwordError;

    private LoginUiState(
            boolean valid,
            boolean submitted,
            @Nullable String emailError,
            @Nullable String passwordError
    ) {
        this.valid = valid;
        this.submitted = submitted;
        this.emailError = emailError;
        this.passwordError = passwordError;
    }

    public static LoginUiState idle() {
        return new LoginUiState(false, false, null, null);
    }

    public static LoginUiState validationError(
            @Nullable String emailError,
            @Nullable String passwordError
    ) {
        return new LoginUiState(false, true, emailError, passwordError);
    }

    public static LoginUiState validSubmission() {
        return new LoginUiState(true, true, null, null);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    @Nullable
    public String getEmailError() {
        return emailError;
    }

    @Nullable
    public String getPasswordError() {
        return passwordError;
    }
}
