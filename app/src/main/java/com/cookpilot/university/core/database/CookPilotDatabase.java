package com.cookpilot.university.core.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cookpilot.university.features.cookplan.data.local.PlannedRecipeDao;
import com.cookpilot.university.features.cookplan.data.local.PlannedRecipeEntity;
import com.cookpilot.university.features.recipes.data.local.IngredientDao;
import com.cookpilot.university.features.recipes.data.local.IngredientEntity;
import com.cookpilot.university.features.recipes.data.local.RecipeDao;
import com.cookpilot.university.features.recipes.data.local.RecipeEntity;
import com.cookpilot.university.features.recipes.data.local.RecipeIngredientDao;
import com.cookpilot.university.features.recipes.data.local.RecipeIngredientEntity;
import com.cookpilot.university.features.recipes.data.local.RecipeStepDao;
import com.cookpilot.university.features.recipes.data.local.RecipeStepEntity;

@Database(
        entities = {
                PlannedRecipeEntity.class,
                RecipeEntity.class,
                IngredientEntity.class,
                RecipeIngredientEntity.class,
                RecipeStepEntity.class
        },
        version = 3,
        exportSchema = false
)
public abstract class CookPilotDatabase extends RoomDatabase {

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS recipes ("
                            + "id TEXT NOT NULL, "
                            + "canonical_name TEXT NOT NULL, "
                            + "title TEXT NOT NULL, "
                            + "description TEXT NOT NULL, "
                            + "image_url TEXT, "
                            + "base_servings INTEGER NOT NULL, "
                            + "difficulty TEXT NOT NULL, "
                            + "total_minutes INTEGER NOT NULL, "
                            + "active_minutes INTEGER NOT NULL, "
                            + "passive_minutes INTEGER NOT NULL, "
                            + "calories REAL NOT NULL, "
                            + "protein_g REAL NOT NULL, "
                            + "carbs_g REAL NOT NULL, "
                            + "fat_g REAL NOT NULL, "
                            + "fiber_g REAL NOT NULL, "
                            + "category_slugs TEXT NOT NULL, "
                            + "updated_at INTEGER NOT NULL, "
                            + "PRIMARY KEY(id))"
            );

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS ingredients ("
                            + "id TEXT NOT NULL, "
                            + "name TEXT NOT NULL, "
                            + "image_url TEXT, "
                            + "updated_at INTEGER NOT NULL, "
                            + "PRIMARY KEY(id))"
            );

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS recipe_ingredients ("
                            + "recipe_id TEXT NOT NULL, "
                            + "ingredient_id TEXT NOT NULL, "
                            + "quantity REAL NOT NULL, "
                            + "unit TEXT NOT NULL, "
                            + "is_optional INTEGER NOT NULL, "
                            + "position INTEGER NOT NULL, "
                            + "PRIMARY KEY(recipe_id, ingredient_id), "
                            + "FOREIGN KEY(recipe_id) REFERENCES recipes(id) "
                            + "ON UPDATE NO ACTION ON DELETE CASCADE, "
                            + "FOREIGN KEY(ingredient_id) REFERENCES ingredients(id) "
                            + "ON UPDATE NO ACTION ON DELETE CASCADE)"
            );

            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_recipe_ingredients_recipe_id "
                            + "ON recipe_ingredients(recipe_id)"
            );
            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_recipe_ingredients_ingredient_id "
                            + "ON recipe_ingredients(ingredient_id)"
            );

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS recipe_steps ("
                            + "recipe_id TEXT NOT NULL, "
                            + "step_number INTEGER NOT NULL, "
                            + "instruction TEXT NOT NULL, "
                            + "PRIMARY KEY(recipe_id, step_number), "
                            + "FOREIGN KEY(recipe_id) REFERENCES recipes(id) "
                            + "ON UPDATE NO ACTION ON DELETE CASCADE)"
            );

            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_recipe_steps_recipe_id "
                            + "ON recipe_steps(recipe_id)"
            );
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // La versión anterior contenía datos de demostración sin usuario.
            database.execSQL("DROP TABLE IF EXISTS planned_recipes");
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS planned_recipes ("
                            + "id TEXT NOT NULL, "
                            + "user_id TEXT NOT NULL, "
                            + "plan_date TEXT NOT NULL, "
                            + "meal_moment TEXT NOT NULL, "
                            + "recipe_id TEXT NOT NULL, "
                            + "servings INTEGER NOT NULL, "
                            + "created_at INTEGER NOT NULL, "
                            + "updated_at INTEGER NOT NULL, "
                            + "sync_state TEXT NOT NULL, "
                            + "PRIMARY KEY(id))"
            );
            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_planned_recipes_user_id_plan_date "
                            + "ON planned_recipes(user_id, plan_date)"
            );
        }
    };

    public abstract PlannedRecipeDao plannedRecipeDao();
    public abstract RecipeDao recipeDao();
    public abstract IngredientDao ingredientDao();
    public abstract RecipeIngredientDao recipeIngredientDao();
    public abstract RecipeStepDao recipeStepDao();

    public static CookPilotDatabase create(Context context) {
        return Room.databaseBuilder(
                context.getApplicationContext(),
                CookPilotDatabase.class,
                "cookpilot_university.db"
        )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build();
    }
}
