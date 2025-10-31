package com.alica.evdekisef.data.model // Sizin paket adınız

import com.google.firebase.firestore.PropertyName

data class FirestoreRecipe(
    val id: Int = 0,

    val title_en: String = "",
    val title_tr: String = "",

    val summary_en: String = "",
    val summary_tr: String = "",

    val instructions_en: String = "",
    val instructions_tr: String = "",

    @get:PropertyName("image_url") @set:PropertyName("image_url")
    var imageUrl: String = "",

    val readyInMinutes: Int = 0,
    val servings: Int = 0,

    // FİLTRELEME İÇİN TEMİZ LİSTELER (Keywords)
    val ingredients_en: List<String> = emptyList(), // ["sugar", "egg"]
    val ingredients_tr: List<String> = emptyList(), // DÜZELTME: Bu yeni eklendi ["Şeker", "Yumurta"]

    // DETAYDA GÖSTERMEK İÇİN TAM LİSTELER
    val full_ingredients_en: List<String> = emptyList(), // ["1 cup sugar", "2 eggs"]
    val full_ingredients_tr: List<String> = emptyList() // DÜZELTME: Adı değişti ["1 su bardağı şeker", "2 yumurta"]
)