package com.alica.evdekisef.data.network // Sizin paket adınız

import com.alica.evdekisef.data.model.RecipeDetail
import com.alica.evdekisef.data.model.RecipeSearchResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MealApi {

    @GET("recipes/findByIngredients")
    suspend fun findRecipesByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 20, // 20 tarif getir
        @Query("ranking") ranking: Int = 2  // Eksik malzemesi en az olanı getir
    ): List<RecipeSearchResult> // DİKKAT: Artık doğrudan bir Liste dönüyor

    @GET("recipes/{id}/information")
    suspend fun getRecipeDetails(
        @Path("id") id: Int
        // API Key'i Interceptor'ımız otomatik eklediği için buraya eklemiyoruz
    ): RecipeDetail



}