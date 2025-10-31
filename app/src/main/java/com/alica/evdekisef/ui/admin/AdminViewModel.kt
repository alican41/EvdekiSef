package com.alica.evdekisef.ui.admin // Sizin paket adınız

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alica.evdekisef.data.migration.MigrationRepository
import com.alica.evdekisef.data.translation.TranslationRepository // YENİ İMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val migrationRepository: MigrationRepository,
    private val translationRepository: TranslationRepository // YENİ ENJEKSİYON
) : ViewModel() {

    private val _logLines = MutableStateFlow<List<String>>(emptyList())
    val logLines = _logLines.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // (addLog fonksiyonu aynı)
    private fun addLog(line: String) {
        _logLines.value = _logLines.value + line
    }

    // (startMigration fonksiyonu aynı, değişmedi)
    fun startMigration(searchTerms: String, countStr: String) {
        val count = countStr.toIntOrNull() ?: 100
        if (searchTerms.isBlank()) {
            addLog("HATA: Arama terimleri boş olamaz.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _logLines.value = emptyList()
            migrationRepository.seedDatabaseFromSpoonacular(
                searchTerms = searchTerms,
                count = count,
                onProgress = ::addLog // Fonksiyon referansı olarak yolla
            )
            _isLoading.value = false
        }
    }

    // --- YENİ FONKSİYON ---
    fun startBatchTranslation(forceOverwrite: Boolean) { // YENİ PARAMETRE
        viewModelScope.launch {
            _isLoading.value = true
            _logLines.value = emptyList()
            addLog("Toplu çeviri işi başlatılıyor...")

            translationRepository.batchTranslateFirestoreData(
                forceOverwrite = forceOverwrite, // YENİ
                onProgress = ::addLog
            )

            _isLoading.value = false
        }
    }
}