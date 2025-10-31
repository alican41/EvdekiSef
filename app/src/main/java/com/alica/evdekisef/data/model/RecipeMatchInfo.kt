package com.alica.evdekisef.data.model // Sizin paket adınız

// Bu sınıf, UI'da gösterilecek veriyi temsil eder:
// Tarif + O tarif için bende olmayan (eksik) malzemeler
data class RecipeMatchInfo(
    val recipe: FirestoreRecipe,
    val missingIngredients: List<String> // ["Soğan", "Krema"]
) {
    val missingCount: Int = missingIngredients.size
}