package com.alica.evdekisef.ui.detail // Sizin paket adınız

import android.text.Html
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.alica.evdekisef.data.model.RecipeDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit // Geri dönme fonksiyonu
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? DetailUiState.Success)?.trTitle ?: "Tarif Detayları"
                    Text(title, maxLines = 1) // Tek satıra sığdır
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri Dön")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailUiState.Success -> {
                    RecipeDetailContent(
                        recipe = state.recipe,
                        trTitle = state.trTitle,
                        trSummary = state.trSummary,
                        trInstructions = state.trInstructions,
                        trIngredients = state.trIngredients
                    )
                }
                is DetailUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

// (HTML'i temizlemek için yardımcı fonksiyon)
@Composable
fun HtmlText(html: String) {
    val context = LocalContext.current
    val styledText = remember(html) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
    }
    Text(text = styledText.toString())
}


@Composable
fun RecipeDetailContent(
    recipe: RecipeDetail, // Orijinal resim, süre, kişilik için
    trTitle: String,
    trSummary: String,
    trInstructions: String,
    trIngredients: List<String>
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // Tüm ekranı kaydırılabilir yap
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(recipe.image)
                .crossfade(true)
                .build(),
            contentDescription = recipe.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Başlık
            Text(
                text = trTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2'li Bilgi Kutusu (Süre ve Kişi Sayısı)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoChip(
                    icon = Icons.Default.DateRange,
                    text = "${recipe.readyInMinutes} Dakika"
                )
                InfoChip(
                    icon = Icons.Default.Info,
                    text = "${recipe.servings} Kişilik"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            // Malzemeler
            Text(
                text = "Malzemeler",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            trIngredients.forEach { ingredientText ->
                Row(
                    modifier = Modifier.padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = ingredientText) // Çevrilmiş metni bas
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            // Hazırlanışı (Çevrilmiş Metin)
            Text(
                text = "Hazırlanışı",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Text(text = trInstructions.ifEmpty { "Hazırlanış talimatı bulunamadı." })

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            // Özet (Orijinal İngilizce)
            Text(
                text = "Özet",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Text(text = trSummary.ifEmpty { "Özet bulunamadı." })
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}