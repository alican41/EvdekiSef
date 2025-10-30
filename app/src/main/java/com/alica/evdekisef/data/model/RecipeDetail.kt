package com.alica.evdekisef.data.model // Sizin paket adınız

data class RecipeDetail(
    val id: Int,
    val title: String,
    val image: String,
    val summary: String, // Yemeğin özeti (HTML içerebilir)
    val instructions: String, // Hazırlanışı (HTML içerebilir)
    val extendedIngredients: List<IngredientDetail>, // Malzeme listesi
    val readyInMinutes: Int, // Hazırlanma süresi
    val servings: Int // Kaç kişilik
)

data class IngredientDetail(
    val id: Int,
    val original: String, // "1 cup sugar"
    val name: String, // "sugar"
    val nameClean: String, // "sugar" (bazen daha temiz)
    val amount: Double, // 1.0
    val unit: String // "cup"
)