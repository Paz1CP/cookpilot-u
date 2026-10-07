package com.cookpilot.university.features.cookplan.data.remote;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.android.gms.tasks.Tasks;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class CookPlanRemoteDataSource {

    public interface ListCallback {
        void onSuccess(@NonNull List<PlanEntryDto> entries);
        void onError(@NonNull Exception exception);
    }

    public interface EntryCallback {
        void onSuccess(@NonNull PlanEntryDto entry);
        void onError(@NonNull Exception exception);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(@NonNull Exception exception);
    }

    private final FirebaseAuth firebaseAuth;
    private final CookPlanApiService apiService;

    public CookPlanRemoteDataSource(
            @NonNull FirebaseAuth firebaseAuth,
            @NonNull CookPlanApiService apiService
    ) {
        this.firebaseAuth = firebaseAuth;
        this.apiService = apiService;
    }

    public void getRange(
            @NonNull String from,
            @NonNull String to,
            @NonNull ListCallback callback
    ) {
        withToken(token -> apiService
                .getPlanEntries("Bearer " + token, from, to)
                .enqueue(new Callback<List<PlanEntryDto>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<List<PlanEntryDto>> call,
                            @NonNull Response<List<PlanEntryDto>> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onError(httpError(response.code()));
                            return;
                        }
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<PlanEntryDto>> call,
                            @NonNull Throwable throwable
                    ) {
                        callback.onError(asException(throwable));
                    }
                }), callback::onError);
    }

    public void create(
            @NonNull PlanEntryDto entry,
            @NonNull EntryCallback callback
    ) {
        withToken(token -> apiService
                .createPlanEntry("Bearer " + token, entry)
                .enqueue(entryCallback(callback)), callback::onError);
    }

    public void update(
            @NonNull PlanEntryDto entry,
            @NonNull EntryCallback callback
    ) {
        withToken(token -> apiService
                .updatePlanEntry(
                        "Bearer " + token,
                        entry.getId(),
                        entry
                )
                .enqueue(entryCallback(callback)), callback::onError);
    }

    public void delete(
            @NonNull String entryId,
            @NonNull ActionCallback callback
    ) {
        withToken(token -> apiService
                .deletePlanEntry("Bearer " + token, entryId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Void> call,
                            @NonNull Response<Void> response
                    ) {
                        if (!response.isSuccessful()) {
                            callback.onError(httpError(response.code()));
                            return;
                        }
                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Void> call,
                            @NonNull Throwable throwable
                    ) {
                        callback.onError(asException(throwable));
                    }
                }), callback::onError);
    }

    @NonNull
    public List<PlanEntryDto> getRangeBlocking(
            @NonNull String from,
            @NonNull String to
    ) throws Exception {
        Response<List<PlanEntryDto>> response = apiService
                .getPlanEntries(
                        "Bearer " + tokenBlocking(),
                        from,
                        to
                )
                .execute();

        if (!response.isSuccessful() || response.body() == null) {
            throw httpError(response.code());
        }
        return response.body();
    }

    @NonNull
    public PlanEntryDto createBlocking(
            @NonNull PlanEntryDto entry
    ) throws Exception {
        Response<PlanEntryDto> response = apiService
                .createPlanEntry(
                        "Bearer " + tokenBlocking(),
                        entry
                )
                .execute();

        if (!response.isSuccessful() || response.body() == null) {
            throw httpError(response.code());
        }
        return response.body();
    }

    @NonNull
    public PlanEntryDto updateBlocking(
            @NonNull PlanEntryDto entry
    ) throws Exception {
        Response<PlanEntryDto> response = apiService
                .updatePlanEntry(
                        "Bearer " + tokenBlocking(),
                        entry.getId(),
                        entry
                )
                .execute();

        if (!response.isSuccessful() || response.body() == null) {
            throw httpError(response.code());
        }
        return response.body();
    }

    public void deleteBlocking(
            @NonNull String entryId
    ) throws Exception {
        Response<Void> response = apiService
                .deletePlanEntry(
                        "Bearer " + tokenBlocking(),
                        entryId
                )
                .execute();

        if (!response.isSuccessful()) {
            throw httpError(response.code());
        }
    }

    @NonNull
    private String tokenBlocking() throws Exception {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException(
                    "No existe una sesión activa."
            );
        }

        GetTokenResult result = Tasks.await(
                user.getIdToken(false),
                20,
                TimeUnit.SECONDS
        );
        String token = result.getToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException(
                    "No se pudo obtener el token de sesión."
            );
        }
        return token;
    }

    @NonNull
    private Callback<PlanEntryDto> entryCallback(
            @NonNull EntryCallback callback
    ) {
        return new Callback<PlanEntryDto>() {
            @Override
            public void onResponse(
                    @NonNull Call<PlanEntryDto> call,
                    @NonNull Response<PlanEntryDto> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError(httpError(response.code()));
                    return;
                }
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(
                    @NonNull Call<PlanEntryDto> call,
                    @NonNull Throwable throwable
            ) {
                callback.onError(asException(throwable));
            }
        };
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