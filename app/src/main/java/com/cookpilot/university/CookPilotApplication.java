package com.cookpilot.university;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.cookpilot.university.core.database.CookPilotDatabase;
import com.cookpilot.university.core.network.ApiClient;
import com.cookpilot.university.features.auth.data.remote.FirebaseAuthDataSource;
import com.cookpilot.university.features.auth.data.remote.FirebaseUserDataSource;
import com.cookpilot.university.features.auth.data.repository.AuthRepository;
import com.cookpilot.university.features.cookplan.data.repository.CookPlanRepository;
import com.cookpilot.university.features.recipes.data.local.RecipeLocalDataSource;
import com.cookpilot.university.features.recipes.data.remote.RecipeApiService;
import com.cookpilot.university.features.recipes.data.remote.RecipeRemoteDataSource;
import com.cookpilot.university.features.recipes.data.repository.RecipeRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public final class CookPilotApplication extends Application {

    private CookPilotDatabase database;
    private AuthRepository authRepository;
    private RecipeRepository recipeRepository;
    private CookPlanRepository cookPlanRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        database = CookPilotDatabase.create(this);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        authRepository = new AuthRepository(
                new FirebaseAuthDataSource(firebaseAuth),
                new FirebaseUserDataSource(FirebaseFirestore.getInstance())
        );

        RecipeApiService recipeApiService = ApiClient
                .create()
                .create(RecipeApiService.class);

        recipeRepository = new RecipeRepository(
                new RecipeLocalDataSource(database),
                new RecipeRemoteDataSource(
                        firebaseAuth,
                        recipeApiService
                )
        );

        cookPlanRepository = new CookPlanRepository(
                database.plannedRecipeDao(),
                recipeRepository
        );
    }

    public AuthRepository getAuthRepository() {
        return authRepository;
    }

    public RecipeRepository getRecipeRepository() {
        return recipeRepository;
    }

    public CookPlanRepository getCookPlanRepository() {
        return cookPlanRepository;
    }
}
