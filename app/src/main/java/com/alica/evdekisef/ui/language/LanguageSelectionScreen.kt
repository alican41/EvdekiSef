package com.alica.evdekisef.ui.language // Sizin paket adınız

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit // "TR" veya "EN"
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Hoş Geldiniz / Welcome",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "Lütfen bir dil seçin / Please select a language",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onLanguageSelected("TR") },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Türkçe (Devam Et)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onLanguageSelected("EN") },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("English (Continue)")
            }
        }
    }
}