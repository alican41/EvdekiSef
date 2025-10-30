package com.alica.evdekisef.ui.detail // Sizin paket adınız

// ... (tüm import'lar aynı kalacak)
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alica.evdekisef.data.model.IngredientDetail
import com.alica.evdekisef.data.model.RecipeDetail
import com.alica.evdekisef.data.repository.MealRepository
import com.alica.evdekisef.di.IngredientMap
import com.alica.evdekisef.di.UnitMap
import com.google.mlkit.nl.translate.Translator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import android.text.Html

// (DetailUiState sealed class'ı aynı, değişmedi)
sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(
        val recipe: RecipeDetail,
        val trTitle: String,
        val trSummary: String,
        val trInstructions: String,
        val trIngredients: List<String>
    ) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}


@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: MealRepository,
    private val translator: Translator,
    @IngredientMap private val ingredientMap: Map<String, String>,
    @UnitMap private val unitMap: Map<String, String>,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        val recipeId: Int? = savedStateHandle["recipeId"]
        if (recipeId != null) {
            fetchAndTranslateDetails(recipeId)
        } else {
            _uiState.value = DetailUiState.Error("Tarif ID'si bulunamadı.")
        }
    }

    private fun stripHtml(html: String?): String {
        if (html.isNullOrBlank()) return ""
        return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString().trim()
    }

    private fun fetchAndTranslateDetails(id: Int) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                // ---!!! YENİ ve EN ÖNEMLİ ADIM !!!---
                // Çeviriye başlamadan önce, modelin indirilmesini BEKLE.
                // Eğer inmişse, anında devam eder.
                // İnmemişse, bu satırda bekler (ve internet gerekebilir).
                translator.downloadModelIfNeeded().await()
                // ------------------------------------

                // 1. Model hazır, API'dan İngilizce detayları çek
                val recipe = repository.getRecipeDetails(id)

                // 2. DÖRT FARKLI ÇEVİRİ İŞİNİ PARALEL BAŞLAT (async)
                val titleJob = async {
                    try { translator.translate(recipe.title).await() }
                    catch (e: Exception) { recipe.title }
                }
                val summaryJob = async {
                    try { translator.translate(stripHtml(recipe.summary)).await() }
                    catch (e: Exception) { "Özet çevrilemedi." }
                }
                val instructionsJob = async {
                    try { translator.translate(stripHtml(recipe.instructions)).await() }
                    catch (e: Exception) { "Hazırlanışı çevrilemedi." }
                }
                val ingredientsJob = async {
                    translateIngredients(recipe.extendedIngredients)
                }

                // 3. Tüm işlerin bitmesini bekle
                _uiState.value = DetailUiState.Success(
                    recipe = recipe,
                    trTitle = titleJob.await(),
                    trSummary = summaryJob.await(),
                    trInstructions = instructionsJob.await(),
                    trIngredients = ingredientsJob.await()
                )

            } catch (e: Exception) {
                // Hata artık daha net: Model indirilememiş veya API hatası.
                _uiState.value = DetailUiState.Error("Çeviri modeli indirilemedi veya API hatası: ${e.message}. Lütfen internet bağlantınızı kontrol edip tekrar deneyin.")
            }
        }
    }

    // (translateIngredients fonksiyonu aynı, değişmedi)
    private suspend fun translateIngredients(ingredients: List<IngredientDetail>): List<String> {
        val translatedList = mutableListOf<String>()
        val inverseIngredientMap = ingredientMap.entries.associate { (k, v) -> v.lowercase() to k }

        for (item in ingredients) {
            val name = item.nameClean.lowercase(Locale.ENGLISH)
            val unit = item.unit.lowercase(Locale.ENGLISH)
            var trName = inverseIngredientMap[name]
            if (trName == null) {
                trName = try {
                    translator.translate(name).await().replaceFirstChar { it.titlecase() }
                } catch (e: Exception) { name }
            }
            var trUnit = unitMap[unit] ?: unit
            val amount = if (item.amount % 1 == 0.0) item.amount.toInt().toString() else item.amount.toString()
            translatedList.add("$amount $trUnit $trName")
        }
        return translatedList
    }

    override fun onCleared() {
        super.onCleared()
        translator.close()
    }
}