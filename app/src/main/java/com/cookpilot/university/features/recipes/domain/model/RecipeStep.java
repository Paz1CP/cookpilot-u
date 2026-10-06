package com.cookpilot.university.features.recipes.domain.model;

import androidx.annotation.NonNull;

public final class RecipeStep {

    private final int number;

    @NonNull
    private final String instruction;

    public RecipeStep(int number, @NonNull String instruction) {
        this.number = number;
        this.instruction = instruction;
    }

    public int getNumber() {
        return number;
    }

    @NonNull
    public String getInstruction() {
        return instruction;
    }
}
