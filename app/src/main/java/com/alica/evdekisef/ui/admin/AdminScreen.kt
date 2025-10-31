package com.alica.evdekisef.ui.admin // Sizin paket adınız

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    // ViewModel'den gelen state'leri izliyoruz
    val logLines by viewModel.logLines.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Ekranın kendi içinde tuttuğu state'ler (TextField'lar ve Checkbox için)
    var searchTerms by remember { mutableStateOf("chicken,beef,pasta,potato,egg,tomato,onion,garlic,rice,salmon") }
    var count by remember { mutableStateOf("100") } // API limiti (maks 100)
    var forceOverwrite by remember { mutableStateOf(false) } // Yeniden çevirme kutucuğu

    // Log listesinin otomatik kayması için
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Log listesine yeni bir satır eklendiğinde en alta kaydır
    LaunchedEffect(logLines.size) {
        if (logLines.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(logLines.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin: Veri Yönetim Paneli") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // --- BÖLÜM 1: VERİ DOLDURMA (SEEDING) ---
            Text(
                "1. Veritabanını Doldur (Spoonacular -> Firestore)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = searchTerms,
                onValueChange = { searchTerms = it },
                label = { Text("Arama Terimleri (virgülle ayrılmış)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            OutlinedTextField(
                value = count,
                onValueChange = { count = it },
                label = { Text("Çekilecek Tarif Sayısı (Maks 100)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Button(
                onClick = { viewModel.startMigration(searchTerms, count) },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "ÇALIŞIYOR..." else "Veritabanını Doldurmaya Başla")
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // --- BÖLÜM 2: TOPLU ÇEVİRİ ---
            Text(
                "2. Veritabanını Çevir (Firestore -> ML Kit -> Firestore)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // "Zorla Yeniden Çevir" Checkbox'ı
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = forceOverwrite,
                    onCheckedChange = { forceOverwrite = it },
                    enabled = !isLoading
                )
                Text(
                    text = "Daha önce çevrilmiş (veya yanlış doldurulmuş) alanları zorla yeniden çevir.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Çeviri Butonu
            Button(
                onClick = { viewModel.startBatchTranslation(forceOverwrite) },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(if (isLoading) "ÇALIŞIYOR..." else "Tüm Veritabanını Türkçe'ye Çevir")
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.padding(top = 8.dp).fillMaxWidth())
            }

            // --- LOG ALANI ---
            Text("İşlem Logları:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp))
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f) // Kalan tüm alanı kapla
                    .background(Color.Black)
                    .padding(8.dp)
            ) {
                items(logLines) { line ->
                    Text(
                        text = line,
                        color = if (line.startsWith("HATA") || line.startsWith("KRİTİK")) Color.Red else if (line.startsWith("ATLANDI")) Color.Yellow else Color.Green,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}