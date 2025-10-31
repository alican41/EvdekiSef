package com.alica.evdekisef // Sizin paket adınız

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.alica.evdekisef.data.settings.SettingsRepository // Ayarları almak için
import com.alica.evdekisef.ui.auth.AuthScreen
import com.alica.evdekisef.ui.navigation.AppNavigation
import com.alica.evdekisef.ui.theme.EvdekiSefTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var settingsRepo: SettingsRepository // Logout'ta dili silmek için

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EvdekiSefTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Auth durumunu dinle
                    val currentUser by remember(auth) {
                        mutableStateOf(auth.currentUser)
                    }
                    var isLoggedIn by remember { mutableStateOf(currentUser != null) }

                    if (isLoggedIn) {
                        AppNavigation(
                            onLogout = {
                                auth.signOut()
                                settingsRepo.clearLanguageOnLogout()
                                isLoggedIn = false // State'i güncelle (AuthScreen'e düşer)
                            }
                        )
                    } else {
                        AuthScreen(
                            onLoginSuccess = {
                                isLoggedIn = true // State'i güncelle (AppNavigation'a geçer)
                            }
                        )
                    }
                }
            }
        }
    }
}