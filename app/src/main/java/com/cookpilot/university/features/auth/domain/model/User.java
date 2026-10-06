package com.cookpilot.university.features.auth.domain.model;

import androidx.annotation.NonNull;

public final class User {

    @NonNull
    private final String id;

    @NonNull
    private final String email;

    @NonNull
    private final String displayName;

    public User(
            @NonNull String id,
            @NonNull String email,
            @NonNull String displayName
    ) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }
}
