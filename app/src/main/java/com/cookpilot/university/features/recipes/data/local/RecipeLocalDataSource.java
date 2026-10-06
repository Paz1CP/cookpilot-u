package com.cookpilot.university.features.recipes.data.local;

import androidx.annotation.NonNull;

import com.cookpilot.university.core.database.CookPilotDatabase;
import com.cookpilot.university.features.recipes.data.mapper.RecipeMapper;
import com.cookpilot.university.features.recipes.data.remote.dto.RecipeDto;
import com.cookpilot.university.features.recipes.data.remote.dto.RecipeIngredientDto;
import com.cookpilot.university.features.recipes.data.remote.dto.RecipeStepDto;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RecipeLocalDataSource {

    private final CookPilotDatabase database;
    private final RecipeDao recipeDao;
    private final IngredientDao ingredientDao;
    private final RecipeIngredientDao recipeIngredientDao;
    private final RecipeStepDao recipeStepDao;

    public RecipeLocalDataSource(@NonNull CookPilotDatabase database) {
        this.database = database;
        recipeDao = database.recipeDao();
        ingredientDao = database.ingredientDao();
        recipeIngredientDao = database.recipeIngredientDao();
        recipeStepDao = database.recipeStepDao();
    }

    @NonNull
    public List<Recipe> loadAll() {
        List<RecipeEntity> recipeEntities = recipeDao.getAll();
        List<Recipe> recipes = new ArrayList<>();

        for (RecipeEntity entity : recipeEntities) {
            recipes.add(RecipeMapper.toDomain(
                    entity,
                    recipeIngredientDao.getForRecipe(entity.id),
                    recipeStepDao.getForRecipe(entity.id)
            ));
        }

        return recipes;
    }

    public void replaceCatalog(@NonNull List<RecipeDto> dtos) {
        long now = System.currentTimeMillis();

        List<RecipeEntity> recipes = new ArrayList<>();
        Map<String, IngredientEntity> ingredients = new LinkedHashMap<>();
        List<RecipeIngredientEntity> compositions = new ArrayList<>();
        List<RecipeStepEntity> steps = new ArrayList<>();

        for (RecipeDto dto : dtos) {
            recipes.add(RecipeMapper.toRecipeEntity(dto, now));

            List<RecipeIngredientDto> dtoIngredients = dto.getIngredients();
            for (int index = 0; index < dtoIngredients.size(); index++) {
                RecipeIngredientDto ingredient = dtoIngredients.get(index);

                ingredients.put(
                        ingredient.getIngredientId(),
                        RecipeMapper.toIngredientEntity(ingredient, now)
                );
                compositions.add(
                        RecipeMapper.toRecipeIngredientEntity(
                                dto.getId(),
                                ingredient,
                                index
                        )
                );
            }

            for (RecipeStepDto step : dto.getSteps()) {
                steps.add(RecipeMapper.toStepEntity(dto.getId(), step));
            }
        }

        database.runInTransaction(() -> {
            recipeIngredientDao.deleteAll();
            recipeStepDao.deleteAll();
            recipeDao.deleteAll();
            ingredientDao.deleteAll();

            recipeDao.upsertAll(recipes);
            ingredientDao.upsertAll(new ArrayList<>(ingredients.values()));
            recipeIngredientDao.upsertAll(compositions);
            recipeStepDao.upsertAll(steps);
        });
    }
}
