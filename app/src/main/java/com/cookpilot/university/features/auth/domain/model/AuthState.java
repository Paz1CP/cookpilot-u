package com.cookpilot.university.features.auth.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class AuthState {

    public enum Status {
        AUTHENTICATED,
        SIGNED_OUT
    }

    @NonNull
    private final Status status;

    @Nullable
    private final User user;

    private AuthState(
            @NonNull Status status,
            @Nullable User user
    ) {
        this.status = status;
        this.user = user;
    }

    public static AuthState authenticated(@NonNull User user) {
        return new AuthState(Status.AUTHENTICATED, user);
    }

    public static AuthState signedOut() {
        return new AuthState(Status.SIGNED_OUT, null);
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @Nullable
    public User getUser() {
        return user;
    }

    public boolean isAuthenticated() {
        return status == Status.AUTHENTICATED && user != null;
    }
}
