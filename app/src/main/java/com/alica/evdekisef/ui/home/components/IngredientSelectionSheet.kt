package com.alica.evdekisef.ui.home.components // Sizin paket adınız

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alica.evdekisef.ui.home.HomeViewModel
import androidx.compose.foundation.layout.FlowRow

/**
 * Alttan açılan malzeme seçim paneli.
 * Kendi ViewModel'ini hiltViewModel() ile alır (HomeScreen ile aynı instance).
 */
@Composable
fun IngredientSelectionSheet(
    viewModel: HomeViewModel = hiltViewModel(),
    onSearchClicked: () -> Unit // "Tarif Bul" butonuna basıldığında
) {
    // State'leri doğrudan ViewModel'den topluyoruz
    val popularIngredients by viewModel.popularIngredients.collectAsState()
    val selectedIngredients by viewModel.selectedIngredients.collectAsState()
    val customIngredientText by viewModel.customIngredientText.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    // Panel içeriği
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding() // (Alt sistem çubuğu için boşluk)
    ) {

        // --- BÖLÜM 1: ÖZEL MALZEME GİRİŞİ (TextField) ---
        Text(
            text = "Malzeme Ekle (örn: Pırasa):",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customIngredientText,
                onValueChange = viewModel::onCustomTextChanged,
                label = { Text("Malzeme yazın") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.addCustomIngredient()
                    keyboardController?.hide()
                })
            )
            IconButton(onClick = {
                viewModel.addCustomIngredient()
                keyboardController?.hide()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Ekle")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BÖLÜM 2: SEPETİNİZ (Seçilenler - FlowRow) ---
        Text(
            text = "Sepetiniz (${selectedIngredients.size}):",
            style = MaterialTheme.typography.titleSmall
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            if (selectedIngredients.isEmpty()) {
                Text("Sepetiniz boş", style = MaterialTheme.typography.bodySmall)
            }
            selectedIngredients.forEach { ingredient ->
                InputChip(
                    selected = true,
                    onClick = { viewModel.removeIngredient(ingredient) },
                    label = { Text(ingredient) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Kaldır",
                            modifier = Modifier.size(InputChipDefaults.IconSize)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BÖLÜM 3: HIZLI EKLE (Popüler - LazyRow) ---
        Text(
            text = "Hızlı Ekle:",
            style = MaterialTheme.typography.titleSmall
        )
        LazyRow(
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val availablePopular = popularIngredients.filter { !selectedIngredients.contains(it) }
            items(availablePopular) { ingredient ->
                SuggestionChip(
                    onClick = { viewModel.addPopularIngredient(ingredient) },
                    label = { Text(ingredient) }
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // --- ARAMA BUTONU ---
        Button(
            onClick = {
                viewModel.performSearch() // 1. Aramayı tetikle
                onSearchClicked()      // 2. Paneli kapat (HomeScreen'e sinyal gönder)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedIngredients.isNotEmpty()
        ) {
            Text("Tarifleri Göster (${selectedIngredients.size})")
        }
    }
}