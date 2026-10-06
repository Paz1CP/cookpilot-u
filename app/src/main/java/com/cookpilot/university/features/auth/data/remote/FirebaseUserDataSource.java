package com.cookpilot.university.features.auth.data.remote;

import androidx.annotation.NonNull;

import com.cookpilot.university.features.auth.domain.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public final class FirebaseUserDataSource {

    public interface Callback {
        void onSuccess();
        void onError(@NonNull Exception exception);
    }

    private final FirebaseFirestore firestore;

    public FirebaseUserDataSource(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void ensureUser(
            @NonNull User user,
            @NonNull Callback callback
    ) {
        DocumentReference reference = firestore
                .collection("users")
                .document(user.getId());

        reference.get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("displayName", user.getDisplayName());
                    data.put("email", user.getEmail());
                    data.put("updatedAt", FieldValue.serverTimestamp());

                    if (!snapshot.exists()) {
                        data.put("createdAt", FieldValue.serverTimestamp());
                    }

                    reference.set(data)
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }
}
