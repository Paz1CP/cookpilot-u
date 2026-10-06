package com.cookpilot.university.features.auth.data.repository;

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
        authenticateAndPersist(true, email, password, callback);
    }

    public void login(
            @NonNull String email,
            @NonNull String password,
            @NonNull Callback callback
    ) {
        authenticateAndPersist(false, email, password, callback);
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

    private void authenticateAndPersist(
            boolean register,
            @NonNull String email,
            @NonNull String password,
            @NonNull Callback callback
    ) {
        FirebaseAuthDataSource.Callback authCallback =
                new FirebaseAuthDataSource.Callback() {
                    @Override
                    public void onSuccess(@NonNull FirebaseUser firebaseUser) {
                        persistUser(firebaseUser, callback);
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

    private void persistUser(
            @NonNull FirebaseUser firebaseUser,
            @NonNull Callback callback
    ) {
        User user = toDomain(firebaseUser);

        userDataSource.ensureUser(
                user,
                new FirebaseUserDataSource.Callback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess(user);
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        callback.onError(exception);
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
