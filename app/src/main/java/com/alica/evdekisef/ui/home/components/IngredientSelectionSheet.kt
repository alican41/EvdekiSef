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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alica.evdekisef.ui.home.HomeViewModel
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientSelectionSheet(
    viewModel: HomeViewModel = hiltViewModel(),
    onSearchClicked: () -> Unit
) {
    // !!! DEĞİŞİKLİK BURADA: Değişken adı netleştirildi !!!
    val allIngredientKeywords by viewModel.allIngredientKeywords.collectAsState()
    val selectedIngredients by viewModel.selectedIngredients.collectAsState()
    var customIngredientText by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {

        // --- BÖLÜM 1: ÖZEL MALZEME GİRİŞİ ---
        Text(
            text = "Malzeme Ekle:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customIngredientText,
                onValueChange = { customIngredientText = it },
                label = { Text("Malzeme yazın") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.addIngredient(customIngredientText)
                    customIngredientText = ""
                    keyboardController?.hide()
                })
            )
            IconButton(onClick = {
                viewModel.addIngredient(customIngredientText)
                customIngredientText = ""
                keyboardController?.hide()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Ekle")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BÖLÜM 2: SEPETİNİZ (FlowRow) ---
        Text(
            text = "Sepetiniz (${selectedIngredients.size}):",
            style = MaterialTheme.typography.titleSmall
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 4.dp
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

        // --- BÖLÜM 3: HIZLI EKLE (LazyRow) ---
        Text(
            text = "Hızlı Ekle (Normal Malzemeler):", // Başlığı netleştirdim
            style = MaterialTheme.typography.titleSmall
        )
        LazyRow(
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // !!! DEĞİŞİKLİK BURADA: 'allIngredientKeywords' (normal) kullanılıyor !!!
            val availableKeywords = allIngredientKeywords.filter { !selectedIngredients.contains(it) }
            items(availableKeywords) { keyword ->
                SuggestionChip(
                    onClick = { viewModel.addIngredient(keyword) },
                    label = { Text(keyword) }
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // --- ARAMA BUTONU ---
        Button(
            onClick = {
                viewModel.performSearch() // Filtrelemeyi tetikle
                onSearchClicked()         // Paneli kapat
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedIngredients.isNotEmpty() // Sepet boşsa arama yapmasın
        ) {
            Text("Sonuçları Göster (${selectedIngredients.size})")
        }
    }
}