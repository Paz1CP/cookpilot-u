package com.cookpilot.university.features.cooklist.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cookpilot.university.features.cooklist.data.local.ShoppingItemEntity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ShoppingFirestoreDataSource {

    private final FirebaseFirestore firestore;

    public ShoppingFirestoreDataSource(
            @NonNull FirebaseFirestore firestore
    ) {
        this.firestore = firestore;
    }

    @Nullable
    public ShoppingItemEntity getByIdBlocking(
            @NonNull String userId,
            @NonNull String itemId
    ) throws Exception {
        DocumentSnapshot snapshot = Tasks.await(
                firestore.collection("users")
                        .document(userId)
                        .collection("shoppingItems")
                        .document(itemId)
                        .get(Source.SERVER),
                30,
                TimeUnit.SECONDS
        );

        return snapshot.exists()
                ? fromDocument(snapshot, userId)
                : null;
    }

    @NonNull
    public List<ShoppingItemEntity> getWeekBlocking(
            @NonNull String userId,
            @NonNull String weekStart
    ) throws Exception {
        QuerySnapshot snapshot = Tasks.await(
                firestore.collection("users")
                        .document(userId)
                        .collection("shoppingItems")
                        .whereEqualTo("weekStart", weekStart)
                        .get(Source.SERVER),
                30,
                TimeUnit.SECONDS
        );

        List<ShoppingItemEntity> result = new ArrayList<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            ShoppingItemEntity entity = fromDocument(
                    document,
                    userId
            );
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }

    public void writeBlocking(
            @NonNull ShoppingItemEntity entity
    ) throws Exception {
        Tasks.await(
                firestore.collection("users")
                        .document(entity.userId)
                        .collection("shoppingItems")
                        .document(entity.id)
                        .set(toMap(entity)),
                30,
                TimeUnit.SECONDS
        );
    }

    public void deleteBlocking(
            @NonNull String userId,
            @NonNull String itemId
    ) throws Exception {
        Tasks.await(
                firestore.collection("users")
                        .document(userId)
                        .collection("shoppingItems")
                        .document(itemId)
                        .delete(),
                30,
                TimeUnit.SECONDS
        );
    }

    @NonNull
    private Map<String, Object> toMap(
            @NonNull ShoppingItemEntity entity
    ) {
        Map<String, Object> value = new HashMap<>();
        value.put("id", entity.id);
        value.put("weekStart", entity.weekStart);
        value.put("ingredientId", entity.ingredientId);
        value.put("name", entity.name);
        value.put("quantity", entity.quantity);
        value.put("unit", entity.unit);
        value.put("purchased", entity.purchased);
        value.put("manual", entity.manual);
        value.put(
                "sourceRecipeIds",
                decodeIds(entity.sourceRecipeIds)
        );
        value.put("createdAt", entity.createdAt);
        value.put("updatedAt", entity.updatedAt);
        return value;
    }

    @Nullable
    private ShoppingItemEntity fromDocument(
            @NonNull DocumentSnapshot document,
            @NonNull String userId
    ) {
        String id = string(document, "id", document.getId());
        String weekStart = string(document, "weekStart", "");
        String name = string(document, "name", "");
        String unit = string(document, "unit", "unit");

        if (id.isEmpty() || weekStart.isEmpty() || name.isEmpty()) {
            return null;
        }

        Object sourceIds = document.get("sourceRecipeIds");
        JSONArray encodedSources = new JSONArray();
        if (sourceIds instanceof List<?>) {
            for (Object value : (List<?>) sourceIds) {
                if (value instanceof String) {
                    encodedSources.put(value);
                }
            }
        }

        return new ShoppingItemEntity(
                id,
                userId,
                weekStart,
                nullableString(document.get("ingredientId")),
                name,
                number(document.get("quantity"), 0),
                unit,
                Boolean.TRUE.equals(document.getBoolean("purchased")),
                Boolean.TRUE.equals(document.getBoolean("manual")),
                encodedSources.toString(),
                longNumber(document.get("createdAt"), System.currentTimeMillis()),
                longNumber(document.get("updatedAt"), System.currentTimeMillis()),
                ShoppingItemEntity.SYNCED
        );
    }

    @NonNull
    private List<String> decodeIds(@NonNull String raw) {
        List<String> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(raw);
            for (int index = 0; index < array.length(); index++) {
                String value = array.optString(index, "").trim();
                if (!value.isEmpty()) {
                    result.add(value);
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    @NonNull
    private String string(
            @NonNull DocumentSnapshot document,
            @NonNull String field,
            @NonNull String fallback
    ) {
        String value = document.getString(field);
        return value == null ? fallback : value;
    }

    @Nullable
    private String nullableString(Object value) {
        return value instanceof String && !((String) value).isEmpty()
                ? (String) value
                : null;
    }

    private double number(Object value, double fallback) {
        return value instanceof Number
                ? ((Number) value).doubleValue()
                : fallback;
    }

    private long longNumber(Object value, long fallback) {
        return value instanceof Number
                ? ((Number) value).longValue()
                : fallback;
    }
}
