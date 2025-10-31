package com.alica.evdekisef.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alica.evdekisef.data.settings.SettingsRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val settingsRepo: SettingsRepository // YENİ: Dil ayarları için
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    // YENİ: Artık hangi dili seçtiğini de alıyoruz
    private fun onAuthSuccess(langCode: String) {
        settingsRepo.saveLanguage(langCode) // 1. Dili kaydet
        _uiState.value = AuthUiState.Success  // 2. Başarı sinyali gönder
    }

    fun login(email: String, password: String, langCode: String) {
        if (email.isBlank() || password.isBlank()) { /* ... hata ... */ return }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                onAuthSuccess(langCode) // YENİ
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Giriş hatası")
            }
        }
    }

    fun register(email: String, password: String, langCode: String) {
        if (email.isBlank() || password.isBlank()) { /* ... hata ... */ return }
        if (password.length < 6) { /* ... hata ... */ return }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                onAuthSuccess(langCode) // YENİ
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Kayıt hatası")
            }
        }
    }
}