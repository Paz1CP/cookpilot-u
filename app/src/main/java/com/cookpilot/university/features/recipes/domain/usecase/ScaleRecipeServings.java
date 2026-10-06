package com.cookpilot.university.features.recipes.domain.usecase;

public final class ScaleRecipeServings {

    private ScaleRecipeServings() {
    }

    public static double scaleQuantity(
            double baseQuantity,
            int selectedServings,
            int baseServings
    ) {
        return baseQuantity
                * Math.max(1, selectedServings)
                / Math.max(1, baseServings);
    }
}
