package com.alica.evdekisef.ui.home // Sizin paket adınız

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.alica.evdekisef.data.model.FirestoreRecipe
import com.alica.evdekisef.ui.home.components.IngredientSelectionSheet // GERİ GELDİ
import kotlinx.coroutines.launch
import com.alica.evdekisef.data.model.RecipeMatchInfo
import com.alica.evdekisef.ui.core.HomeStrings // YENİ İMPORT
import com.alica.evdekisef.ui.home.components.IngredientSelectionSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val langCode = viewModel.langCode
    val strings = viewModel.strings // Dile göre metinler
    val selectedCount by viewModel.selectedIngredients.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(strings.title) }, // Dile göre
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Çıkış Yap")
                    }
                },
                actions = {
                    Button(
                        onClick = { isSheetOpen = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = strings.ingredients, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("${strings.ingredients} (${selectedCount.size})") // Dile göre
                    }
                    IconButton(onClick = onNavigateToAdmin) {
                        Icon(Icons.Default.Settings, contentDescription = "Admin")
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }
                // --- YENİ DURUM ---
                is HomeUiState.Empty -> {
                    Text(
                        text = strings.prompt, // Dile göre
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
                is HomeUiState.Success -> {
                    RecipeList(
                        matchedRecipes = state.matchedRecipes,
                        langCode = langCode,
                        strings = strings, // Metinleri yolla
                        onRecipeClick = onRecipeClick
                    )
                }
                is HomeUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = sheetState
        ) {
            IngredientSelectionSheet(
                onSearchClicked = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) { isSheetOpen = false }
                    }
                }
            )
        }
    }
}

@Composable
fun RecipeList(
    matchedRecipes: List<RecipeMatchInfo>,
    langCode: String,
    strings: HomeStrings, // YENİ
    onRecipeClick: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        items(matchedRecipes) { matchInfo ->
            RecipeItemCard(
                matchInfo = matchInfo,
                langCode = langCode,
                strings = strings, // YENİ
                onRecipeClick = onRecipeClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeItemCard(
    matchInfo: RecipeMatchInfo,
    langCode: String,
    strings: HomeStrings, // YENİ
    onRecipeClick: (String) -> Unit
) {
    val recipe = matchInfo.recipe
    val title = (if (langCode == "TR" && recipe.title_tr.isNotBlank()) recipe.title_tr else recipe.title_en)

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onRecipeClick(recipe.id.toString()) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(recipe.imageUrl).crossfade(true).build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title.ifEmpty { recipe.title_en },
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // "Eksik Malzeme" metni artık dil desteğiyle geliyor
                val missingCount = matchInfo.missingCount
                Text(
                    text = if (missingCount == 0) {
                        strings.allIngredientsAvailable
                    } else {
                        strings.missingCount(missingCount)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (missingCount == 0) Color(0xFF008000) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}