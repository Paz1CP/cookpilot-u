package com.cookpilot.university.core.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cookpilot.university.features.cookplan.data.local.PlannedRecipeDao;
import com.cookpilot.university.features.cookplan.data.local.PlannedRecipeEntity;

@Database(
        entities = {PlannedRecipeEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class CookPilotDatabase extends RoomDatabase {

    public abstract PlannedRecipeDao plannedRecipeDao();

    public static CookPilotDatabase create(Context context) {
        return Room.databaseBuilder(
                context.getApplicationContext(),
                CookPilotDatabase.class,
                "cookpilot_university.db"
        ).build();
    }
}
