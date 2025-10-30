package com.alica.evdekisef.ui.home // Sizin paket adınız

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alica.evdekisef.data.model.RecipeSearchResult
import com.alica.evdekisef.ui.home.components.IngredientSelectionSheet // YENİ İÇERİĞİ IMPORT ET

// Diğer import'lar...
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onRecipeClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCount by viewModel.selectedIngredients.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // --- 1. KAYDIRMA DAVRANIŞINI (SCROLL BEHAVIOR) TANIMLA ---
    // Bu, TopAppBar'ın kaydırmayı "dinlemesini" sağlar.
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        // --- 2. SCAFFOLD'A nestedScroll MODIFIER'INI EKLE ---
        // TopAppBar'ın kaydırmadan haberdar olması için.
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            TopAppBar(
                title = { Text("Evdeki Şef") },
                actions = {
                    Button(
                        onClick = { isSheetOpen = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Malzemeler",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Malzemeler (${selectedCount.size})")
                    }
                },
                // --- 3. TOPAPPBAR'A DAVRANIŞI VE RENKLERİ EKLE ---
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    // Kaydırma olmadığında (en üstte)
                    containerColor = MaterialTheme.colorScheme.surface,
                    // Kaydırma başladığında TopAppBar'ın alacağı renk
                    // Bu renk hem light hem dark mode'da arka plandan farklı ve belirgindir.
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }
    ) { innerPadding -> // Adını 'padding' yerine 'innerPadding' olarak değiştirdik

        // --- SONUÇ ALANI ---
        Box(
            modifier = Modifier
                // 4. Scaffold'dan gelen iç boşluğu (innerPadding) buraya uygula
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                // ... (when bloğunun içi aynı, DEĞİŞMEDİ)
                is HomeViewModel.HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }
                is HomeViewModel.HomeUiState.Success -> {
                    // RecipeList'in artık padding almasına gerek yok,
                    // çünkü Box zaten padding'i aldı.
                    RecipeList(
                        recipes = state.recipes,
                        onRecipeClick = onRecipeClick)
                }
                is HomeViewModel.HomeUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                is HomeViewModel.HomeUiState.Empty -> {
                    Text(
                        text = "Tarifleri görmek için yukarıdaki 'Malzemeler' butonuna basın.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }

    // --- BOTTOM SHEET (DEĞİŞMEDİ) ---
    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = sheetState
        ) {
            IngredientSelectionSheet(
                onSearchClicked = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            isSheetOpen = false
                        }
                    }
                }
            )
        }
    }
}

// --- RecipeList'ten GEREKSİZ PADDING'İ KALDIR ---
@Composable
fun RecipeList(recipes: List<RecipeSearchResult>,
               onRecipeClick: (Int) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        // 'contentPadding'i güncelliyoruz, artık sadece alt boşluk yeterli.
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        items(recipes) { recipe ->
            RecipeItemCard(
                recipe = recipe,
                onRecipeClick = onRecipeClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeItemCard(recipe: RecipeSearchResult,
                   onRecipeClick: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            onRecipeClick(recipe.id)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.image)
                    .crossfade(true)
                    .build(),
                contentDescription = recipe.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                val missingCount = recipe.missedIngredientCount
                Text(
                    text = if (missingCount == 0) {
                        "Tüm malzemeler evde var!"
                    } else {
                        "$missingCount eksik malzeme"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (missingCount == 0) Color(0xFF008000) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}