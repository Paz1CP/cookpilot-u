package com.cookpilot.university.features.recipes.data.remote;

import androidx.annotation.NonNull;

import com.cookpilot.university.features.recipes.data.remote.dto.RecipeDto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class RecipeRemoteDataSource {

    public interface CatalogCallback {
        void onSuccess(@NonNull List<RecipeDto> recipes);
        void onError(@NonNull Exception exception);
    }

    public interface RecipeCallback {
        void onSuccess(@NonNull RecipeDto recipe);
        void onError(@NonNull Exception exception);
    }

    private final FirebaseAuth firebaseAuth;
    private final RecipeApiService apiService;

    public RecipeRemoteDataSource(
            @NonNull FirebaseAuth firebaseAuth,
            @NonNull RecipeApiService apiService
    ) {
        this.firebaseAuth = firebaseAuth;
        this.apiService = apiService;
    }

    public void getRecipes(@NonNull CatalogCallback callback) {
        withToken(
                token -> apiService
                        .getRecipes("Bearer " + token)
                        .enqueue(new Callback<List<RecipeDto>>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<List<RecipeDto>> call,
                                    @NonNull Response<List<RecipeDto>> response
                            ) {
                                if (!response.isSuccessful()
                                        || response.body() == null) {
                                    callback.onError(httpError(response.code()));
                                    return;
                                }
                                callback.onSuccess(response.body());
                            }

                            @Override
                            public void onFailure(
                                    @NonNull Call<List<RecipeDto>> call,
                                    @NonNull Throwable throwable
                            ) {
                                callback.onError(asException(throwable));
                            }
                        }),
                callback::onError
        );
    }

    public void getRecipe(
            @NonNull String recipeId,
            @NonNull RecipeCallback callback
    ) {
        withToken(
                token -> apiService
                        .getRecipe("Bearer " + token, recipeId)
                        .enqueue(new Callback<RecipeDto>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<RecipeDto> call,
                                    @NonNull Response<RecipeDto> response
                            ) {
                                if (!response.isSuccessful()
                                        || response.body() == null) {
                                    callback.onError(httpError(response.code()));
                                    return;
                                }
                                callback.onSuccess(response.body());
                            }

                            @Override
                            public void onFailure(
                                    @NonNull Call<RecipeDto> call,
                                    @NonNull Throwable throwable
                            ) {
                                callback.onError(asException(throwable));
                            }
                        }),
                callback::onError
        );
    }

    private void withToken(
            @NonNull TokenConsumer consumer,
            @NonNull ErrorConsumer errorConsumer
    ) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            errorConsumer.accept(
                    new IllegalStateException("No existe una sesión activa.")
            );
            return;
        }

        user.getIdToken(false)
                .addOnSuccessListener(result -> {
                    String token = result.getToken();
                    if (token == null || token.isEmpty()) {
                        errorConsumer.accept(
                                new IllegalStateException(
                                        "No se pudo obtener el token de sesión."
                                )
                        );
                        return;
                    }
                    consumer.accept(token);
                })
                .addOnFailureListener(errorConsumer::accept);
    }

    private IOException httpError(int code) {
        return new IOException("Error HTTP " + code);
    }

    private Exception asException(Throwable throwable) {
        return throwable instanceof Exception
                ? (Exception) throwable
                : new IOException(throwable);
    }

    private interface TokenConsumer {
        void accept(String token);
    }

    private interface ErrorConsumer {
        void accept(Exception exception);
    }
}
