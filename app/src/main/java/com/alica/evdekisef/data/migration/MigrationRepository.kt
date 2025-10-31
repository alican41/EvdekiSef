package com.alica.evdekisef.data.migration // Sizin paket adınız

import com.alica.evdekisef.data.model.FirestoreRecipe
import com.alica.evdekisef.data.network.MealApi
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MigrationRepository @Inject constructor(
    private val mealApi: MealApi,
    private val firestore: FirebaseFirestore
) {

    /**
     * Spoonacular'dan veri çeker ve Firestore'a "eğer yoksa" ekler.
     * @param searchTerms API'da aranacak kelimeler (örn: "chicken,pasta,potato")
     * @param count Çekilecek maksimum tarif sayısı (API limiti genelde 100'dür)
     * @param onProgress UI'a log basmak için bir callback fonksiyonu
     */
    suspend fun seedDatabaseFromSpoonacular(
        searchTerms: String,
        count: Int,
        onProgress: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val collectionRef = firestore.collection("yemekler")

            try {
                onProgress("Başlatılıyor... '$searchTerms' için $count tarif aranacak.")

                // 1. Spoonacular'dan tarif listesini al
                val searchResults = mealApi.findRecipesByIngredients(
                    ingredients = searchTerms,
                    number = count,
                    ranking = 1 // Popülerliğe göre
                )

                val recipeIds = searchResults.map { it.id }.distinct()
                onProgress("${recipeIds.size} adet benzersiz tarif bulundu. Firestore kontrol ediliyor...")

                var successCount = 0
                var skippedCount = 0

                recipeIds.forEachIndexed { index, id ->
                    val docId = id.toString()
                    val docRef = collectionRef.document(docId)

                    // 2. KONTROL ADIMI: Bu ID Firestore'da var mı?
                    if (docRef.get().await().exists()) {
                        // Eğer varsa, atla ve API kotasını harcama
                        skippedCount++
                        onProgress("($index/${recipeIds.size}) ATLANDI: $id ID'li tarif zaten var.")
                    } else {
                        // 3. EĞER YOKSA: Detayları çek ve kaydet
                        try {
                            val recipeDetail = mealApi.getRecipeDetails(id)
                            onProgress("($index/${recipeIds.size}) ÇEKİLİYOR: ${recipeDetail.title}...")

                            val firestoreRecipe = FirestoreRecipe(
                                id = recipeDetail.id,
                                title_en = recipeDetail.title,
                                summary_en = recipeDetail.summary,
                                instructions_en = recipeDetail.instructions,
                                imageUrl = recipeDetail.image, // Sadece URL'i kaydet
                                readyInMinutes = recipeDetail.readyInMinutes,
                                servings = recipeDetail.servings,
                                ingredients_en = recipeDetail.extendedIngredients.mapNotNull { it.nameClean }.filter { it.isNotBlank() }.distinct(),
                                full_ingredients_en = recipeDetail.extendedIngredients.map { it.original }.distinct(),
                                title_tr = "",
                                summary_tr = "",
                                instructions_tr = "",
                                ingredients_tr = emptyList()
                            )

                            docRef.set(firestoreRecipe).await()
                            successCount++
                            onProgress("($index/${recipeIds.size}) KAYDEDİLDİ: ${recipeDetail.title}")

                        } catch (e: Exception) {
                            onProgress("($index/${recipeIds.size}) DETAY ÇEKME HATASI ($id): ${e.message}")
                        }
                    }
                }
                onProgress("--- İŞLEM TAMAMLANDI ---")
                onProgress("$successCount YENİ tarif eklendi.")
                onProgress("$skippedCount tarif zaten mevcut olduğu için atlandı.")

            } catch (e: Exception) {
                onProgress("ANA HATA: ${e.message}")
            }
        }
    }
}