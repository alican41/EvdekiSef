package com.alica.evdekisef.data.model // Sizin paket adınız

import com.google.gson.annotations.SerializedName

// Spoonacular'dan dönen tarif listesinin modeli
data class RecipeSearchResult(
    val id: Int,
    val title: String,
    val image: String,
    val imageType: String,

    // Projemiz için EN ÖNEMLİ VERİ:
    // Bu tarif için kaç tane malzememiz eksik?
    val missedIngredientCount: Int,

    val usedIngredientCount: Int,

    @SerializedName("unusedIngredients") // Kullanılmayan malzemeler
    val unusedIngredients: List<Ingredient>,

    @SerializedName("usedIngredients") // Kullanılan malzemeler
    val usedIngredients: List<Ingredient>
)

data class Ingredient(
    val id: Int,
    val name: String,
    val amount: Double,
    val unit: String
)