package com.cookpilot.university;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.cookpilot.university.core.database.CookPilotDatabase;
import com.cookpilot.university.features.cookplan.data.repository.CookPlanRepository;
import com.cookpilot.university.features.recipes.data.repository.RecipeRepository;

public final class CookPilotApplication extends Application {

    private CookPilotDatabase database;
    private RecipeRepository recipeRepository;
    private CookPlanRepository cookPlanRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        database = CookPilotDatabase.create(this);
        recipeRepository = new RecipeRepository();
        cookPlanRepository = new CookPlanRepository(
                database.plannedRecipeDao(),
                recipeRepository
        );
    }

    public RecipeRepository getRecipeRepository() {
        return recipeRepository;
    }

    public CookPlanRepository getCookPlanRepository() {
        return cookPlanRepository;
    }
}
