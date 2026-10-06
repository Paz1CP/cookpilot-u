package com.cookpilot.university.features.recipes.data.remote;

import com.cookpilot.university.features.recipes.data.remote.dto.RecipeDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface RecipeApiService {

    @GET("recipes")
    Call<List<RecipeDto>> getRecipes(
            @Header("Authorization") String authorization
    );

    @GET("recipes/{recipeId}")
    Call<RecipeDto> getRecipe(
            @Header("Authorization") String authorization,
            @Path("recipeId") String recipeId
    );
}
