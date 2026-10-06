package com.cookpilot.university.features.recipes.domain.model;

public final class NutritionInfo {

    private final double calories;
    private final double proteinG;
    private final double carbsG;
    private final double fatG;
    private final double fiberG;

    public NutritionInfo(
            double calories,
            double proteinG,
            double carbsG,
            double fatG,
            double fiberG
    ) {
        this.calories = calories;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatG = fatG;
        this.fiberG = fiberG;
    }

    public double getCalories() {
        return calories;
    }

    public double getProteinG() {
        return proteinG;
    }

    public double getCarbsG() {
        return carbsG;
    }

    public double getFatG() {
        return fatG;
    }

    public double getFiberG() {
        return fiberG;
    }
}
