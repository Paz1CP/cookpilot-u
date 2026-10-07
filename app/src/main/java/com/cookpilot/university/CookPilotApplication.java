package com.cookpilot.university;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.cookpilot.university.core.database.CookPilotDatabase;
import com.cookpilot.university.core.network.ApiClient;
import com.cookpilot.university.core.sync.SyncScheduler;
import com.cookpilot.university.features.auth.data.remote.FirebaseAuthDataSource;
import com.cookpilot.university.features.auth.data.remote.FirebaseUserDataSource;
import com.cookpilot.university.features.auth.data.repository.AuthRepository;
import com.cookpilot.university.features.cookplan.data.remote.CookPlanApiService;
import com.cookpilot.university.features.cookplan.data.remote.CookPlanRemoteDataSource;
import com.cookpilot.university.features.cookplan.data.repository.CookPlanRepository;
import com.cookpilot.university.features.cooklist.data.remote.ShoppingFirestoreDataSource;
import com.cookpilot.university.features.cooklist.data.repository.ShoppingRepository;
import com.cookpilot.university.features.recipes.data.local.RecipeLocalDataSource;
import com.cookpilot.university.features.recipes.data.remote.RecipeApiService;
import com.cookpilot.university.features.recipes.data.remote.RecipeRemoteDataSource;
import com.cookpilot.university.features.recipes.data.remote.SavedRecipeFirestoreDataSource;
import com.cookpilot.university.features.recipes.data.repository.RecipeRepository;
import com.cookpilot.university.features.recipes.data.repository.SavedRecipeRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import retrofit2.Retrofit;

public final class CookPilotApplication extends Application {

    private CookPilotDatabase database;
    private AuthRepository authRepository;
    private RecipeRepository recipeRepository;
    private CookPlanRepository cookPlanRepository;
    private ShoppingRepository shoppingRepository;
    private SavedRecipeRepository savedRecipeRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        database = CookPilotDatabase.create(this);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        Retrofit retrofit = ApiClient.create();

        authRepository = new AuthRepository(
                new FirebaseAuthDataSource(firebaseAuth),
                new FirebaseUserDataSource(FirebaseFirestore.getInstance())
        );

        RecipeApiService recipeApiService =
                retrofit.create(RecipeApiService.class);
        recipeRepository = new RecipeRepository(
                new RecipeLocalDataSource(database),
                new RecipeRemoteDataSource(
                        firebaseAuth,
                        recipeApiService
                )
        );

        CookPlanApiService cookPlanApiService =
                retrofit.create(CookPlanApiService.class);
        cookPlanRepository = new CookPlanRepository(
                database.plannedRecipeDao(),
                recipeRepository,
                new CookPlanRemoteDataSource(
                        firebaseAuth,
                        cookPlanApiService
                ),
                firebaseAuth,
                this
        );

        shoppingRepository = new ShoppingRepository(
                database.shoppingItemDao(),
                firebaseAuth,
                new ShoppingFirestoreDataSource(
                        FirebaseFirestore.getInstance()
                ),
                this
        );

        savedRecipeRepository = new SavedRecipeRepository(
                database.savedRecipeDao(),
                firebaseAuth,
                new SavedRecipeFirestoreDataSource(
                        FirebaseFirestore.getInstance()
                ),
                this
        );

        SyncScheduler.ensurePeriodicSync(this);
        SyncScheduler.requestSync(this);
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

    public ShoppingRepository getShoppingRepository() {
        return shoppingRepository;
    }

    public SavedRecipeRepository getSavedRecipeRepository() {
        return savedRecipeRepository;
    }
}