package com.cookpilot.university.features.auth.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.cookpilot.university.features.auth.data.remote.FirebaseAuthDataSource;
import com.cookpilot.university.features.auth.data.remote.FirebaseUserDataSource;
import com.cookpilot.university.features.auth.domain.model.AuthState;
import com.cookpilot.university.features.auth.domain.model.User;
import com.google.firebase.auth.FirebaseUser;

public final class AuthRepository {

    public interface Callback {
        void onSuccess(@NonNull User user);
        void onError(@NonNull Exception exception);
    }

    private final FirebaseAuthDataSource authDataSource;
    private final FirebaseUserDataSource userDataSource;

    public AuthRepository(
            @NonNull FirebaseAuthDataSource authDataSource,
            @NonNull FirebaseUserDataSource userDataSource
    ) {
        this.authDataSource = authDataSource;
        this.userDataSource = userDataSource;
    }

    public void register(
            @NonNull String email,
            @NonNull String password,
            @NonNull Callback callback
    ) {
        authenticateAndSyncUser(true, email, password, callback);
    }

    public void login(
            @NonNull String email,
            @NonNull String password,
            @NonNull Callback callback
    ) {
        authenticateAndSyncUser(false, email, password, callback);
    }

    public void logout() {
        authDataSource.logout();
    }

    @NonNull
    public AuthState getAuthState() {
        FirebaseUser firebaseUser = authDataSource.currentUser();
        if (firebaseUser == null) {
            return AuthState.signedOut();
        }
        return AuthState.authenticated(toDomain(firebaseUser));
    }

    private void authenticateAndSyncUser(
            boolean register,
            @NonNull String email,
            @NonNull String password,
            @NonNull Callback callback
    ) {
        FirebaseAuthDataSource.Callback authCallback =
                new FirebaseAuthDataSource.Callback() {
                    @Override
                    public void onSuccess(@NonNull FirebaseUser firebaseUser) {
                        User user = toDomain(firebaseUser);
                        callback.onSuccess(user);
                        syncUser(user);
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        callback.onError(exception);
                    }
                };

        if (register) {
            authDataSource.register(email, password, authCallback);
        } else {
            authDataSource.login(email, password, authCallback);
        }
    }

    private void syncUser(@NonNull User user) {
        userDataSource.ensureUser(
                user,
                new FirebaseUserDataSource.Callback() {
                    @Override
                    public void onSuccess() {
                        // Auth already completed; profile sync does not control navigation.
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        Log.w("AuthRepository", "No se pudo sincronizar el perfil en Firestore.", exception);
                    }
                }
        );
    }

    @NonNull
    private User toDomain(@NonNull FirebaseUser firebaseUser) {
        String email = firebaseUser.getEmail();
        String displayName = firebaseUser.getDisplayName();

        return new User(
                firebaseUser.getUid(),
                email == null ? "" : email,
                displayName == null ? "" : displayName
        );
    }
}
