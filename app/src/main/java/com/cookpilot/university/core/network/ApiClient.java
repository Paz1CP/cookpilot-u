package com.cookpilot.university.core.network;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static final String FUNCTION_REGION = "southamerica-west1";

    private ApiClient() {
    }

    @NonNull
    public static Retrofit create() {
        String projectId = FirebaseApp
                .getInstance()
                .getOptions()
                .getProjectId();

        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Firebase no contiene un projectId válido."
            );
        }

        String baseUrl = "https://"
                + FUNCTION_REGION
                + "-"
                + projectId
                + ".cloudfunctions.net/api/";

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
