package com.cookpilot.university.features.auth.data.remote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class FirebaseAuthDataSource {

    public interface Callback {
        void onSuccess(@NonNull FirebaseUser user);
        void onError(@NonNull Exception exception);
    }

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthDataSource(@NonNull FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public void register(
            @NonNull String email,
            @NonNull String password,
            @NonNull Callback callback
    ) {
        firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError(
                                new IllegalStateException(
                                        "No se pudo obtener el usuario creado."
                                )
                        );
                        return;
                    }
                    callback.onSuccess(user);
                })
                .addOnFailureListener(callback::onError);
    }

    public void login(
            @NonNull String email,
            @NonNull String password,
            @NonNull Callback callback
    ) {
        firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError(
                                new IllegalStateException(
                                        "No se pudo obtener el usuario autenticado."
                                )
                        );
                        return;
                    }
                    callback.onSuccess(user);
                })
                .addOnFailureListener(callback::onError);
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    @Nullable
    public FirebaseUser currentUser() {
        return firebaseAuth.getCurrentUser();
    }
}
