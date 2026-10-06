package com.cookpilot.university.features.cookplan.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.cookpilot.university.features.cookplan.data.local.PlannedRecipeDao;
import com.cookpilot.university.features.cookplan.data.local.PlannedRecipeEntity;
import com.cookpilot.university.features.cookplan.domain.model.MealMoment;
import com.cookpilot.university.features.cookplan.domain.model.PlannedRecipe;
import com.cookpilot.university.features.recipes.data.local.RecipeFixtures;
import com.cookpilot.university.features.recipes.data.repository.RecipeRepository;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CookPlanRepository {

    private final PlannedRecipeDao dao;
    private final RecipeRepository recipeRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CookPlanRepository(
            @NonNull PlannedRecipeDao dao,
            @NonNull RecipeRepository recipeRepository
    ) {
        this.dao = dao;
        this.recipeRepository = recipeRepository;
    }

    public LiveData<List<PlannedRecipe>> observeDay(@NonNull LocalDate date) {
        return Transformations.map(
                dao.observeByDate(date.toString()),
                this::toDomain
        );
    }

    public void addRecipes(
            @NonNull LocalDate date,
            @NonNull MealMoment mealMoment,
            @NonNull List<Recipe> recipes
    ) {
        if (recipes.isEmpty()) {
            return;
        }

        executor.execute(() -> {
            long now = System.currentTimeMillis();
            List<PlannedRecipeEntity> entities = new ArrayList<>();

            for (int index = 0; index < recipes.size(); index++) {
                Recipe recipe = recipes.get(index);
                entities.add(new PlannedRecipeEntity(
                        date.toString(),
                        mealMoment.getStorageKey(),
                        recipe.getId(),
                        1,
                        now + index
                ));
            }

            dao.upsertAll(entities);
        });
    }

    public void removeRecipe(
            @NonNull LocalDate date,
            @NonNull MealMoment mealMoment,
            @NonNull String recipeId
    ) {
        executor.execute(() -> dao.delete(
                date.toString(),
                mealMoment.getStorageKey(),
                recipeId
        ));
    }

    public void updateServings(
            @NonNull LocalDate date,
            @NonNull MealMoment mealMoment,
            @NonNull String recipeId,
            int servings
    ) {
        executor.execute(() -> dao.updateServings(
                date.toString(),
                mealMoment.getStorageKey(),
                recipeId,
                Math.max(1, servings)
        ));
    }

    public void seedDemoDayIfEmpty(@NonNull LocalDate date) {
        executor.execute(() -> {
            if (dao.countForDate(date.toString()) > 0) {
                return;
            }

            List<PlannedRecipeEntity> initial = Arrays.asList(
                    entity(date, MealMoment.BREAKFAST, RecipeFixtures.PANQUEQUES_ID, 1, 0),
                    entity(date, MealMoment.LUNCH, RecipeFixtures.LOMO_SALTADO_ID, 1, 1),
                    entity(date, MealMoment.LUNCH, RecipeFixtures.ARROZ_CON_POLLO_ID, 1, 2),
                    entity(date, MealMoment.LUNCH, RecipeFixtures.TALLARINES_ROJOS_ID, 1, 3),
                    entity(date, MealMoment.DINNER, RecipeFixtures.ENSALADA_PASTA_POLLO_ID, 1, 4)
            );

            dao.upsertAll(initial);
        });
    }

    private PlannedRecipeEntity entity(
            LocalDate date,
            MealMoment mealMoment,
            String recipeId,
            int servings,
            int offset
    ) {
        return new PlannedRecipeEntity(
                date.toString(),
                mealMoment.getStorageKey(),
                recipeId,
                servings,
                System.currentTimeMillis() + offset
        );
    }

    private List<PlannedRecipe> toDomain(List<PlannedRecipeEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        List<PlannedRecipe> result = new ArrayList<>();
        for (PlannedRecipeEntity entity : entities) {
            MealMoment mealMoment = MealMoment.fromStorageKey(entity.mealMoment);
            Optional<Recipe> recipe = recipeRepository.findById(entity.recipeId);

            if (mealMoment == null || !recipe.isPresent()) {
                continue;
            }

            result.add(new PlannedRecipe(
                    recipe.get(),
                    mealMoment,
                    entity.servings
            ));
        }
        return Collections.unmodifiableList(result);
    }
}
