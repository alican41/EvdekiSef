package com.alica.evdekisef.ui.main // Sizin paket adınız

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alica.evdekisef.data.settings.SettingsRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Uygulamanın ana durumları
enum class AuthState {
    LOADING,
    LOGGED_OUT,
    LANGUAGE_NOT_SELECTED,
    APP_READY
}

data class MainUiState(
    val authState: AuthState = AuthState.LOADING
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkCurrentState()
    }

    fun checkCurrentState() {
        _uiState.update { it.copy(authState = AuthState.LOADING) }

        val user = auth.currentUser
        if (user == null) {
            // 1. Kullanıcı giriş yapmamış
            _uiState.update { it.copy(authState = AuthState.LOGGED_OUT) }
        } else {
            // 2. Kullanıcı giriş yapmış, dil seçmiş mi?
            val lang = settingsRepo.getLanguage()
            if (lang == null) {
                // 3. Dil seçmemiş
                _uiState.update { it.copy(authState = AuthState.LANGUAGE_NOT_SELECTED) }
            } else {
                // 4. Her şey hazır
                _uiState.update { it.copy(authState = AuthState.APP_READY) }
            }
        }
    }

    fun onLanguageSelected(langCode: String) {
        settingsRepo.saveLanguage(langCode)
        checkCurrentState() // Durumu tekrar kontrol et (APP_READY'e geçecek)
    }

    fun onLogout() {
        viewModelScope.launch {
            auth.signOut()
            settingsRepo.clearLanguageOnLogout()
            checkCurrentState() // Durumu tekrar kontrol et (LOGGED_OUT'a geçecek)
        }
    }
}