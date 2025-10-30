package com.alica.evdekisef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.alica.evdekisef.ui.home.HomeScreen
import com.alica.evdekisef.ui.navigation.AppNavigation
import com.alica.evdekisef.ui.theme.EvdekiSefTheme // Kendi temanız
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hilt'in bu Activity'e bağımlılık sağlaması için şart
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EvdekiSefTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

