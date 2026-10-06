package com.cookpilot.university.features.auth.presentation.viewmodel;

import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cookpilot.university.R;
import com.cookpilot.university.features.auth.data.repository.AuthRepository;
import com.cookpilot.university.features.auth.domain.model.User;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public final class LoginViewModel extends ViewModel {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final AuthRepository repository;
    private final MutableLiveData<LoginUiState> uiState =
            new MutableLiveData<>(LoginUiState.initial());

    public LoginViewModel(@NonNull AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<LoginUiState> getUiState() {
        return uiState;
    }

    public void toggleMode() {
        LoginUiState current = currentState();
        if (current.isLoading()) {
            return;
        }
        uiState.setValue(LoginUiState.idle(!current.isRegisterMode()));
    }

    public void submit(String rawEmail, String rawPassword) {
        LoginUiState current = currentState();
        if (current.isLoading()) {
            return;
        }

        String email = rawEmail == null ? "" : rawEmail.trim();
        String password = rawPassword == null ? "" : rawPassword;

        int emailError = 0;
        int passwordError = 0;

        if (email.isEmpty()) {
            emailError = R.string.login_error_email_required;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = R.string.login_error_email_invalid;
        }

        if (password.isEmpty()) {
            passwordError = R.string.login_error_password_required;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordError = R.string.login_error_password_short;
        }

        if (emailError != 0 || passwordError != 0) {
            uiState.setValue(
                    LoginUiState.validationError(
                            current.isRegisterMode(),
                            emailError,
                            passwordError
                    )
            );
            return;
        }

        boolean registerMode = current.isRegisterMode();
        uiState.setValue(LoginUiState.loading(registerMode));

        AuthRepository.Callback callback = new AuthRepository.Callback() {
            @Override
            public void onSuccess(@NonNull User user) {
                uiState.postValue(
                        LoginUiState.authenticated(registerMode)
                );
            }

            @Override
            public void onError(@NonNull Exception exception) {
                uiState.postValue(
                        LoginUiState.authError(
                                registerMode,
                                userMessageFor(exception)
                        )
                );
            }
        };

        if (registerMode) {
            repository.register(email, password, callback);
        } else {
            repository.login(email, password, callback);
        }
    }

    @NonNull
    private LoginUiState currentState() {
        LoginUiState current = uiState.getValue();
        return current == null ? LoginUiState.initial() : current;
    }

    @NonNull
    private String userMessageFor(@NonNull Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            return "Ya existe una cuenta con ese correo.";
        }
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            return "La contraseña es demasiado débil.";
        }
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "Correo o contraseña incorrectos.";
        }
        if (exception instanceof FirebaseNetworkException) {
            return "No se pudo conectar. Revisa tu conexión.";
        }
        return "No se pudo completar la operación.";
    }
}
