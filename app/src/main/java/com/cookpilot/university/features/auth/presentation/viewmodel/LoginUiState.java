package com.cookpilot.university.features.auth.presentation.viewmodel;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public final class LoginUiState {

    private static final int NO_ERROR = 0;

    private final boolean registerMode;
    private final boolean loading;
    private final boolean authenticated;

    @StringRes
    private final int emailErrorResId;

    @StringRes
    private final int passwordErrorResId;

    @Nullable
    private final String authError;

    private LoginUiState(
            boolean registerMode,
            boolean loading,
            boolean authenticated,
            @StringRes int emailErrorResId,
            @StringRes int passwordErrorResId,
            @Nullable String authError
    ) {
        this.registerMode = registerMode;
        this.loading = loading;
        this.authenticated = authenticated;
        this.emailErrorResId = emailErrorResId;
        this.passwordErrorResId = passwordErrorResId;
        this.authError = authError;
    }

    public static LoginUiState initial() {
        return idle(false);
    }

    public static LoginUiState idle(boolean registerMode) {
        return new LoginUiState(
                registerMode,
                false,
                false,
                NO_ERROR,
                NO_ERROR,
                null
        );
    }

    public static LoginUiState loading(boolean registerMode) {
        return new LoginUiState(
                registerMode,
                true,
                false,
                NO_ERROR,
                NO_ERROR,
                null
        );
    }

    public static LoginUiState validationError(
            boolean registerMode,
            @StringRes int emailErrorResId,
            @StringRes int passwordErrorResId
    ) {
        return new LoginUiState(
                registerMode,
                false,
                false,
                emailErrorResId,
                passwordErrorResId,
                null
        );
    }

    public static LoginUiState authError(
            boolean registerMode,
            @Nullable String message
    ) {
        return new LoginUiState(
                registerMode,
                false,
                false,
                NO_ERROR,
                NO_ERROR,
                message
        );
    }

    public static LoginUiState authenticated(boolean registerMode) {
        return new LoginUiState(
                registerMode,
                false,
                true,
                NO_ERROR,
                NO_ERROR,
                null
        );
    }

    public boolean isRegisterMode() {
        return registerMode;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    @StringRes
    public int getEmailErrorResId() {
        return emailErrorResId;
    }

    @StringRes
    public int getPasswordErrorResId() {
        return passwordErrorResId;
    }

    @Nullable
    public String getAuthError() {
        return authError;
    }
}
