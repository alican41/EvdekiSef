package com.alica.evdekisef.ui.detail // Sizin paket adınız

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.alica.evdekisef.data.model.FirestoreRecipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val langCode = viewModel.langCode // Dili al

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? DetailUiState.Success)?.recipe.let {
                        if (langCode == "TR") it?.title_tr else it?.title_en
                    } ?: "Detaylar"
                    Text(title, maxLines = 1)
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
                    // Dili ve tarifi UI'a yolla
                    RecipeDetailContent(
                        recipe = state.recipe,
                        langCode = langCode
                    )
                }
                is DetailUiState.Error -> {
                    // (Hata durumu aynı)
                }
            }
        }
    }
}


@Composable
fun RecipeDetailContent(recipe: FirestoreRecipe, langCode: String) {
    val scrollState = rememberScrollState()

    // Dile göre verileri seç
    val title = (if (langCode == "TR" && recipe.title_tr.isNotBlank()) recipe.title_tr else recipe.title_en)
    val summary = (if (langCode == "TR" && recipe.summary_tr.isNotBlank()) recipe.summary_tr else recipe.summary_en)
    val instructions = (if (langCode == "TR" && recipe.instructions_tr.isNotBlank()) recipe.instructions_tr else recipe.instructions_en)
    val ingredients = (if (langCode == "TR" && recipe.full_ingredients_tr.isNotEmpty()) recipe.full_ingredients_tr else recipe.full_ingredients_en)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(recipe.imageUrl) // Firestore'dan gelen link
                .crossfade(true)
                .build(),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoChip(icon = Icons.Default.DateRange, text = "${recipe.readyInMinutes} Dakika")
                InfoChip(icon = Icons.Default.Info, text = "${recipe.servings} Kişilik")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            // Malzemeler
            Text(text = "Malzemeler", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            ingredients.forEach { ingredient ->
                Row(modifier = Modifier.padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = ingredient)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            // Hazırlanışı
            Text(text = "Hazırlanışı", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            Text(text = instructions.ifEmpty { "Talimatlar bulunamadı." })

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            // Özet
            Text(text = "Özet", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            Text(text = summary.ifEmpty { "Özet bulunamadı." })
        }
    }
}

// (InfoChip Composable'ı aynı, değişmedi)
@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}