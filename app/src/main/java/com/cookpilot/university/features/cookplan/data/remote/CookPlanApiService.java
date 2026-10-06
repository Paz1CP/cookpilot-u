package com.cookpilot.university.features.cookplan.data.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CookPlanApiService {

    @GET("plan-entries")
    Call<List<PlanEntryDto>> getPlanEntries(
            @Header("Authorization") String authorization,
            @Query("from") String from,
            @Query("to") String to
    );

    @POST("plan-entries")
    Call<PlanEntryDto> createPlanEntry(
            @Header("Authorization") String authorization,
            @Body PlanEntryDto entry
    );

    @PUT("plan-entries/{entryId}")
    Call<PlanEntryDto> updatePlanEntry(
            @Header("Authorization") String authorization,
            @Path("entryId") String entryId,
            @Body PlanEntryDto entry
    );

    @DELETE("plan-entries/{entryId}")
    Call<Void> deletePlanEntry(
            @Header("Authorization") String authorization,
            @Path("entryId") String entryId
    );
}
