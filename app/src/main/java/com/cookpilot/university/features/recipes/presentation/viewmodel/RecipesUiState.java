package com.cookpilot.university.features.recipes.presentation.viewmodel;

import androidx.annotation.Nullable;

public final class RecipesUiState {

    private final boolean loading;
    private final boolean showingCachedData;

    @Nullable
    private final String errorMessage;

    private RecipesUiState(
            boolean loading,
            boolean showingCachedData,
            @Nullable String errorMessage
    ) {
        this.loading = loading;
        this.showingCachedData = showingCachedData;
        this.errorMessage = errorMessage;
    }

    public static RecipesUiState loading() {
        return new RecipesUiState(true, false, null);
    }

    public static RecipesUiState ready() {
        return new RecipesUiState(false, false, null);
    }

    public static RecipesUiState error(
            @Nullable String message,
            boolean showingCachedData
    ) {
        return new RecipesUiState(
                false,
                showingCachedData,
                message
        );
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isShowingCachedData() {
        return showingCachedData;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }
}
