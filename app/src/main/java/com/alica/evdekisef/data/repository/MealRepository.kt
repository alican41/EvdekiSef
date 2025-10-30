package com.alica.evdekisef.data.repository // Sizin paket adınız

import com.alica.evdekisef.data.model.RecipeDetail
import com.alica.evdekisef.data.model.RecipeSearchResult
import com.alica.evdekisef.data.network.MealApi
import javax.inject.Inject

class MealRepository @Inject constructor(
    private val mealApi: MealApi
) {
    // DEĞİŞTİ: Artık String değil, bir Liste alıyor
    suspend fun findRecipesByIngredients(ingredients: List<String>): List<RecipeSearchResult> {

        // Gelen listeyi "chicken,mushroom,onion" formatına çevir
        val ingredientsString = ingredients.joinToString(",")

        if (ingredientsString.isBlank()) {
            return emptyList()
        }

        return mealApi.findRecipesByIngredients(ingredients = ingredientsString)
    }

    suspend fun getRecipeDetails(id: Int): RecipeDetail {
        return mealApi.getRecipeDetails(id)
    }

}