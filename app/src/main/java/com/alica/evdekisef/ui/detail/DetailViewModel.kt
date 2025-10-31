package com.alica.evdekisef.ui.detail // Sizin paket adınız

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alica.evdekisef.data.model.FirestoreRecipe
import com.alica.evdekisef.data.settings.SettingsRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Artık çevrilmiş alanlara gerek yok, UI'da seçeceğiz
sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val recipe: FirestoreRecipe) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val settingsRepo: SettingsRepository, // Dili almak için
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Dili al
    val langCode = settingsRepo.getLanguage() ?: "TR"

    init {
        // ID artık bir String (documentId)
        val recipeId: String? = savedStateHandle["recipeId"]
        if (recipeId != null) {
            fetchRecipeDetails(recipeId)
        } else {
            _uiState.value = DetailUiState.Error("Tarif ID'si bulunamadı.")
        }
    }

    private fun fetchRecipeDetails(id: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val doc = firestore.collection("yemekler")
                    .document(id) // ID'ye göre dökümanı al
                    .get()
                    .await()

                val recipe = doc.toObject(FirestoreRecipe::class.java)
                if (recipe != null) {
                    _uiState.value = DetailUiState.Success(recipe)
                } else {
                    _uiState.value = DetailUiState.Error("Tarif bulunamadı.")
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error("Detaylar alınamadı: ${e.message}")
            }
        }
    }
}