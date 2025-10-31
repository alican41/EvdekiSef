package com.alica.evdekisef.data.translation

import android.text.Html
import com.alica.evdekisef.data.model.FirestoreRecipe
import com.alica.evdekisef.di.IngredientMap
import com.alica.evdekisef.di.UnitMap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class TranslationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val translator: Translator,
    @IngredientMap private val ingredientMap: Map<String, String>,
    @UnitMap private val unitMap: Map<String, String> // Bunu şimdilik kullanmıyoruz ama kalsın
) {

    private fun stripHtml(html: String?): String {
        if (html.isNullOrBlank()) return ""
        return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString().trim()
    }

    suspend fun batchTranslateFirestoreData(
        forceOverwrite: Boolean,
        onProgress: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val collectionRef = firestore.collection("yemekler")

            try {
                onProgress("Başlatılıyor... Çeviri modeli (EN->TR) kontrol ediliyor...")
                translator.downloadModelIfNeeded().await()
                onProgress("Model hazır. Tarifler Firestore'dan çekiliyor...")

                val query = if (forceOverwrite) {
                    onProgress("Zorla yeniden çevirme aktif. TÜM veritabanı çekiliyor...")
                    collectionRef
                } else {
                    onProgress("Standart mod. Sadece `title_tr` alanı boş olanlar çekiliyor...")
                    collectionRef.whereEqualTo("title_tr", "")
                }

                val snapshot = query.get().await()
                if (snapshot.isEmpty) {
                    onProgress("Harika! Çevrilecek yeni tarif bulunamadı.")
                    return@withContext
                }
                onProgress("${snapshot.size()} adet tarif bulundu. İşlem başlıyor...")

                val inverseIngredientMap = ingredientMap.entries.associate { (k, v) -> v.lowercase() to k }

                snapshot.documents.forEachIndexed { index, document ->
                    val recipe = document.toObject(FirestoreRecipe::class.java)
                    if (recipe == null) {
                        onProgress("HATA: (${document.id}) dökümanı okunamadı.")
                        return@forEachIndexed
                    }

                    onProgress("ÇEVRİLİYOR (${index + 1}/${snapshot.size()}): ${recipe.title_en}...")

                    // 5 FARKLI PARALEL ÇEVİRİ İŞİ
                    val titleJob = async {
                        try { translator.translate(recipe.title_en).await() }
                        catch (e: Exception) {
                            onProgress("HATA (title: ${recipe.id}): ${e.message}")
                            recipe.title_en
                        }
                    }
                    val summaryJob = async {
                        try { translator.translate(stripHtml(recipe.summary_en)).await() }
                        catch (e: Exception) { "" }
                    }
                    val instructionsJob = async {
                        try { translator.translate(stripHtml(recipe.instructions_en)).await() }
                        catch (e: Exception) { "" }
                    }

                    // --- İŞTE ÖNEMLİ DÜZELTME ---
                    // İŞ 4: Keyword listesini (ingredients_en) çevir -> ingredients_tr
                    val keywordsJob = async {
                        translateKeywordList(recipe.ingredients_en, inverseIngredientMap)
                    }

                    // İŞ 5: Tam listeyi (full_ingredients_en) çevir -> full_ingredients_tr
                    val fullListJob = async {
                        translateFullList(recipe.full_ingredients_en)
                    }

                    // Tüm işlerin bitmesini bekle
                    val trTitle = titleJob.await()
                    val trSummary = summaryJob.await()
                    val trInstructions = instructionsJob.await()
                    val trKeywords = keywordsJob.await() // ["Şeker", "Yumurta"]
                    val trFullList = fullListJob.await() // ["1 su bardağı şeker", "2 yumurta"]

                    // Dökümanı DÜZGÜN alanlara güncelle
                    val updates = mapOf(
                        "title_tr" to trTitle,
                        "summary_tr" to trSummary,
                        "instructions_tr" to trInstructions,
                        "ingredients_tr" to trKeywords,     // DÜZELTME
                        "full_ingredients_tr" to trFullList // DÜZELTME
                    )

                    document.reference.update(updates).await()
                    onProgress("GÜNCELLENDİ (${index + 1}/${snapshot.size()}): $trTitle")
                }

                onProgress("--- BATCH ÇEVİRİ TAMAMLANDI ---")

            } catch (e: Exception) {
                onProgress("KRİTİK HATA: ${e.message}")
            }
        }
    }

    // Keyword (filtre) listesini çevirir
    private suspend fun translateKeywordList(
        keywords_en: List<String>,
        inverseIngredientMap: Map<String, String>
    ): List<String> {
        val translatedList = mutableListOf<String>()
        for (keyword in keywords_en) {
            val name = keyword.lowercase(Locale.ENGLISH)
            var trName = inverseIngredientMap[name] // Önce yerel sözlükten bak
            if (trName == null) {
                trName = try {
                    translator.translate(name).await().replaceFirstChar { it.titlecase() }
                } catch (e: Exception) { name } // Hata olursa İngilizce kalsın
            }
            translatedList.add(trName)
        }
        return translatedList.distinct() // Tekrarları kaldır
    }

    // Tam ("1 cup sugar") listeyi çevirir
    private suspend fun translateFullList(fullList_en: List<String>): List<String> {
        // Performansı artırmak için tüm listeyi tek bir string olarak çevirmeyi deneyebiliriz
        // ancak bu, ML Kit'i zorlayabilir. Şimdilik tek tek çevirelim.
        val translatedList = mutableListOf<String>()
        for (line in fullList_en.filter { it.isNotBlank() }) {
            val trLine = try {
                translator.translate(line).await().replaceFirstChar { it.titlecase() }
            } catch (e: Exception) { line }
            translatedList.add(trLine)
        }
        return translatedList
    }
}