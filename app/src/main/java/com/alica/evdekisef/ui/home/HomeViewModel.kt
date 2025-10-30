package com.alica.evdekisef.ui.home // Sizin paket adınız

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alica.evdekisef.data.model.RecipeSearchResult
import com.alica.evdekisef.data.repository.MealRepository
import com.alica.evdekisef.di.IngredientMap
import com.google.mlkit.nl.translate.Translator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Bu import çok önemli!
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MealRepository,
    @IngredientMap private val ingredientMap: Map<String, String>,
    private val translator: Translator // YENİ: ML Kit Çevirmen
) : ViewModel() {

    // --- STATES ---

    // UI'ın genel durumu (Yükleniyor, Başarılı, Hata)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Empty)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Popüler malzemeler (Hızlı Ekle LazyRow'u için)
    private val _popularIngredients = MutableStateFlow<List<String>>(emptyList())
    val popularIngredients: StateFlow<List<String>> = _popularIngredients.asStateFlow()

    // Kullanıcının seçtiği malzemeler ("Sepet" FlowRow'u için)
    private val _selectedIngredients = MutableStateFlow<Set<String>>(emptySet())
    val selectedIngredients: StateFlow<Set<String>> = _selectedIngredients.asStateFlow()

    // Özel malzeme giriş TextField'ının metni
    private val _customIngredientText = MutableStateFlow("")
    val customIngredientText: StateFlow<String> = _customIngredientText.asStateFlow()

    // (HomeUiState sealed class'ı öncekiyle aynı)
    sealed class HomeUiState {
        object Loading : HomeUiState()
        data class Success(val recipes: List<RecipeSearchResult>) : HomeUiState()
        data class Error(val message: String) : HomeUiState()
        object Empty : HomeUiState()
    }

    init {
        _uiState.value = HomeUiState.Empty
        // Popüler malzemeleri sözlüğümüzden alıyoruz
        _popularIngredients.value = ingredientMap.keys.sorted()
    }

    // --- UI EVENTS (Kullanıcı Aksiyonları) ---

    // TextField metni değiştiğinde
    fun onCustomTextChanged(text: String) {
        _customIngredientText.value = text
    }

    // "Ekle" butonuna basıldığında
    fun addCustomIngredient() {
        // Metni temizle, baş harfini büyüt (örn: "pırasa" -> "Pırasa")
        val ingredient = _customIngredientText.value.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }

        if (ingredient.isNotBlank()) {
            _selectedIngredients.value = _selectedIngredients.value + ingredient
            _customIngredientText.value = "" // TextField'ı temizle
        }
    }

    // "Hızlı Ekle"den (LazyRow) seçim yapıldığında
    fun addPopularIngredient(ingredient: String) {
        _selectedIngredients.value = _selectedIngredients.value + ingredient
    }

    // "Sepet"ten (FlowRow) malzeme çıkarıldığında
    fun removeIngredient(ingredient: String) {
        _selectedIngredients.value = _selectedIngredients.value - ingredient
    }

    // "Tarif Bul" butonuna basıldığında (HİBRİT MANTIK)
    fun performSearch() {
        val turkishIngredients = _selectedIngredients.value
        if (turkishIngredients.isEmpty()) {
            _uiState.value = HomeUiState.Error("Lütfen en az bir malzeme seçin.")
            return
        }

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // Çevrilecek özel kelime var mı? (Performans için kontrol)
            val needsTranslation = turkishIngredients.any { !ingredientMap.containsKey(it) }

            try {
                // ---!!! YENİ ADIM !!!---
                // Eğer "Pırasa" gibi özel bir kelime varsa (sözlükte olmayan)
                // ve çeviri gerekiyorsa, önce modelin inmesini BEKLE.
                if (needsTranslation) {
                    translator.downloadModelIfNeeded().await()
                }
                // -------------------------

                val englishIngredients = mutableListOf<String>()

                // 1. Seçilen tüm malzemeler üzerinde tek tek döngüye gir
                turkishIngredients.forEach { turkish ->
                    val knownTranslation = ingredientMap[turkish]

                    if (knownTranslation != null) {
                        englishIngredients.add(knownTranslation)
                    } else {
                        // 2b. Sözlükte yoksa (örn: "Pırasa"), ML Kit ile çevir
                        try {
                            val translated = translator.translate(turkish).await()
                            if (translated != null) {
                                englishIngredients.add(translated.lowercase())
                            }
                        } catch (e: Exception) {
                            println("Çeviri hatası ($turkish): ${e.message}")
                            // Hata olursa o kelimeyi atla
                        }
                    }
                }

                // ... (Aramanın geri kalanı aynı)
                if (englishIngredients.isEmpty()) {
                    _uiState.value = HomeUiState.Error("Geçerli malzeme bulunamadı veya çevrilemedi.")
                    return@launch
                }
                val response = repository.findRecipesByIngredients(englishIngredients)
                if (response.isEmpty()) {
                    _uiState.value = HomeUiState.Error("Seçilen malzemelerle tarif bulunamadı.")
                } else {
                    _uiState.value = HomeUiState.Success(response)
                }

            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Çeviri modeli indirilemedi veya API hatası: ${e.message}. Lütfen internet bağlantınızı kontrol edip tekrar deneyin.")
            }
        }
    }

    // ViewModel ölürken çevirmeni de hafızadan temizle
    override fun onCleared() {
        super.onCleared()
        translator.close()
    }
}