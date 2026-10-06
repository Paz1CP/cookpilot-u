package com.cookpilot.university.features.auth.presentation.viewmodel;

import androidx.annotation.StringRes;

public final class LoginUiState {

    private static final int NO_ERROR = 0;

    private final boolean valid;
    private final boolean submitted;

    @StringRes
    private final int emailErrorResId;

    @StringRes
    private final int passwordErrorResId;

    private LoginUiState(
            boolean valid,
            boolean submitted,
            @StringRes int emailErrorResId,
            @StringRes int passwordErrorResId
    ) {
        this.valid = valid;
        this.submitted = submitted;
        this.emailErrorResId = emailErrorResId;
        this.passwordErrorResId = passwordErrorResId;
    }

    public static LoginUiState idle() {
        return new LoginUiState(false, false, NO_ERROR, NO_ERROR);
    }

    public static LoginUiState validationError(
            @StringRes int emailErrorResId,
            @StringRes int passwordErrorResId
    ) {
        return new LoginUiState(
                false,
                true,
                emailErrorResId,
                passwordErrorResId
        );
    }

    public static LoginUiState validSubmission() {
        return new LoginUiState(true, true, NO_ERROR, NO_ERROR);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    @StringRes
    public int getEmailErrorResId() {
        return emailErrorResId;
    }

    @StringRes
    public int getPasswordErrorResId() {
        return passwordErrorResId;
    }
}
