package com.alica.evdekisef.ui.home // Sizin paket adınız

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alica.evdekisef.data.model.FirestoreRecipe
import com.alica.evdekisef.data.model.RecipeMatchInfo
import com.alica.evdekisef.data.settings.SettingsRepository
import com.alica.evdekisef.ui.core.AppStrings
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Empty : HomeUiState()
    data class Success(val matchedRecipes: List<RecipeMatchInfo>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    // --- Dil ve Metinler ---
    val langCode = settingsRepo.getLanguage() ?: "TR"
    val strings = AppStrings.getHomeStrings(langCode)
    private val ingredientField = if (langCode == "TR") "ingredients_tr" else "ingredients_en"

    // --- State'ler ---
    private val _allRecipes = MutableStateFlow<List<FirestoreRecipe>>(emptyList())

    private val _allIngredientKeywords = MutableStateFlow<List<String>>(emptyList())
    val allIngredientKeywords = _allIngredientKeywords.asStateFlow()

    private val _selectedIngredients = MutableStateFlow<Set<String>>(emptySet())
    val selectedIngredients = _selectedIngredients.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> =
        combine(
            _allRecipes,
            _selectedIngredients,
            _errorState
        ) { allRecipes, selectedPantry, error ->

            if (error != null) {
                return@combine HomeUiState.Error(error)
            }

            // Tarifler yüklenmediyse (ve hata yoksa) Yükleniyor...
            // (fetchAllDataFromFirestore bittiğinde bu durum değişecek)
            if (allRecipes.isEmpty() && _errorState.value == null) {
                return@combine HomeUiState.Loading
            }

            // Sepet (Pantry) boşsa, "Malzeme seçin" ekranını göster
            if (selectedPantry.isEmpty()) {
                return@combine HomeUiState.Empty
            }

            // Sepet doluysa, FİLTRELE
            val filtered = allRecipes.mapNotNull { recipe ->
                val recipeNeeds = (if (langCode == "TR") recipe.ingredients_tr else recipe.ingredients_en).toSet()
                if (recipeNeeds.isEmpty()) return@mapNotNull null

                val matches = recipeNeeds.intersect(selectedPantry)
                val missing = recipeNeeds - selectedPantry

                if (matches.isNotEmpty()) {
                    RecipeMatchInfo(recipe = recipe, missingIngredients = missing.toList())
                } else {
                    null
                }
            }
                .sortedBy { it.missingCount }

            if (filtered.isEmpty()) {
                HomeUiState.Error(strings.noResults)
            } else {
                HomeUiState.Success(filtered)
            }

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState.Loading)


    init {
        fetchAllDataFromFirestore()
    }

    private fun fetchAllDataFromFirestore() {
        viewModelScope.launch {
            _errorState.value = null
            try {
                val snapshot = firestore.collection("yemekler").get().await()
                val recipes = snapshot.toObjects(FirestoreRecipe::class.java)
                _allRecipes.value = recipes

                val allKeywordsSet = mutableSetOf<String>()
                recipes.forEach { recipe ->
                    // Veritabanındaki 'normal' alanı okuyoruz
                    val keywords = if (langCode == "TR") recipe.ingredients_tr else recipe.ingredients_en
                    allKeywordsSet.addAll(keywords)
                }
                _allIngredientKeywords.value = allKeywordsSet.filter { it.isNotBlank() }.sorted()

                // !!! HATA DÜZELTMESİ BURADA !!!
                // 'combine' bloğu sepetin boş olduğunu zaten bilecek
                // ve 'Empty' durumunu otomatik olarak tetikleyecek.
                // Buradaki 'if' bloğunu sildim.

            } catch (e: Exception) {
                _errorState.value = e.message ?: "Veritabanı okuma hatası"
            }
        }
    }

    fun performSearch() {
        // Bu fonksiyon artık sadece 'combine' operatörünü tetikler
        if (_selectedIngredients.value.isEmpty()) {
            // _uiState.value = HomeUiState.Empty
            // Buna bile gerek yok, 'combine' hallediyor.
        }
    }

    fun addIngredient(ingredient: String) {
        val cleanIngredient = ingredient.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        if (cleanIngredient.isNotBlank()) {
            _selectedIngredients.update { it + cleanIngredient }
        }
    }

    fun removeIngredient(ingredient: String) {
        _selectedIngredients.update { it - ingredient }
    }
}