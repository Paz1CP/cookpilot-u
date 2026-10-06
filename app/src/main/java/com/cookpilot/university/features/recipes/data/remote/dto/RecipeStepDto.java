package com.cookpilot.university.features.recipes.data.remote.dto;

public final class RecipeStepDto {

    private int number;
    private String instruction;

    public int getNumber() {
        return number;
    }

    public String getInstruction() {
        return instruction == null ? "" : instruction;
    }
}
