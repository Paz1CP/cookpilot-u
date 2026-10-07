package com.cookpilot.university.features.recipes.data.remote;

import androidx.annotation.NonNull;

import com.cookpilot.university.features.recipes.data.local.SavedRecipeEntity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class SavedRecipeFirestoreDataSource {

    private final FirebaseFirestore firestore;

    public SavedRecipeFirestoreDataSource(
            @NonNull FirebaseFirestore firestore
    ) {
        this.firestore = firestore;
    }

    @NonNull
    public List<SavedRecipeEntity> getAllBlocking(
            @NonNull String userId
    ) throws Exception {
        QuerySnapshot snapshot = Tasks.await(
                firestore.collection("users")
                        .document(userId)
                        .collection("savedRecipes")
                        .get(Source.SERVER),
                30,
                TimeUnit.SECONDS
        );

        List<SavedRecipeEntity> result = new ArrayList<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            String recipeId = document.getString("recipeId");
            if (recipeId == null || recipeId.trim().isEmpty()) {
                recipeId = document.getId();
            }

            Object savedAtValue = document.get("savedAt");
            long savedAt = savedAtValue instanceof Number
                    ? ((Number) savedAtValue).longValue()
                    : System.currentTimeMillis();

            result.add(new SavedRecipeEntity(
                    userId,
                    recipeId,
                    savedAt,
                    SavedRecipeEntity.SYNCED
            ));
        }

        return result;
    }

    public void writeBlocking(
            @NonNull SavedRecipeEntity entity
    ) throws Exception {
        Map<String, Object> value = new HashMap<>();
        value.put("recipeId", entity.recipeId);
        value.put("savedAt", entity.savedAt);

        Tasks.await(
                firestore.collection("users")
                        .document(entity.userId)
                        .collection("savedRecipes")
                        .document(entity.recipeId)
                        .set(value),
                30,
                TimeUnit.SECONDS
        );
    }

    public void deleteBlocking(
            @NonNull String userId,
            @NonNull String recipeId
    ) throws Exception {
        Tasks.await(
                firestore.collection("users")
                        .document(userId)
                        .collection("savedRecipes")
                        .document(recipeId)
                        .delete(),
                30,
                TimeUnit.SECONDS
        );
    }
}
