package com.cookpilot.university.features.cookplan.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum MealMoment {
    BREAKFAST("breakfast"),
    LUNCH("lunch"),
    DINNER("dinner");

    @NonNull
    private final String storageKey;

    MealMoment(@NonNull String storageKey) {
        this.storageKey = storageKey;
    }

    @NonNull
    public String getStorageKey() {
        return storageKey;
    }

    @Nullable
    public static MealMoment fromStorageKey(@Nullable String value) {
        if (value == null) {
            return null;
        }

        for (MealMoment moment : values()) {
            if (moment.storageKey.equals(value)) {
                return moment;
            }
        }
        return null;
    }
}
