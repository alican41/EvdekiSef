package com.alica.evdekisef.data.settings // Sizin paket adınız

import android.content.SharedPreferences
import javax.inject.Inject

// Bu sınıf, dil ayarlarını yönetir
class SettingsRepository @Inject constructor(
    private val prefs: SharedPreferences
) {
    companion object {
        private const val KEY_LANGUAGE = "language_code"
    }

    fun saveLanguage(langCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
    }

    fun getLanguage(): String? {
        // Kayıtlı bir dil yoksa null döndür
        return prefs.getString(KEY_LANGUAGE, null)
    }

    fun clearLanguageOnLogout() {
        prefs.edit().remove(KEY_LANGUAGE).apply()
    }
}