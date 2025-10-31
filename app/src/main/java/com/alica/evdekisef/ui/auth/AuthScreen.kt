package com.alica.evdekisef.ui.auth // Sizin paket adınız

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alica.evdekisef.R // Arka plan resminiz için
import com.alica.evdekisef.ui.core.AppStrings
import com.alica.evdekisef.ui.core.AuthStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    // --- DİL SEÇİMİ STATE'İ ---
    var selectedLang by remember { mutableStateOf("TR") }
    val strings = remember(selectedLang) { AppStrings.getAuthStrings(selectedLang) }
    var showLangMenu by remember { mutableStateOf(false) }
    // -------------------------

    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. ARKA PLAN GÖRSELİ
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = "Arka Plan",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Koyu Filtre
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(0.8f), Color.Black.copy(0.6f))
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- DİL SEÇİMİ DROPDOWN ---
                Box(modifier = Modifier.padding(top = 16.dp)) {
                    OutlinedButton(
                        onClick = { showLangMenu = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text(selectedLang)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dil Seç")
                    }
                    DropdownMenu(
                        expanded = showLangMenu,
                        onDismissRequest = { showLangMenu = false }
                    ) {
                        AppStrings.languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    selectedLang = lang
                                    showLangMenu = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = strings.welcome, // Dile göre değişen başlık
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                )

                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ) {
                            Tab(
                                selected = pagerState.currentPage == 0,
                                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                                text = { Text(strings.login) } // Dile göre
                            )
                            Tab(
                                selected = pagerState.currentPage == 1,
                                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                                text = { Text(strings.register) } // Dile göre
                            )
                        }

                        if (uiState is AuthUiState.Error) {
                            Text(
                                text = strings.getError((uiState as AuthUiState.Error).message), // Dile göre
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (page) {
                                0 -> LoginPage(viewModel, strings, selectedLang)
                                1 -> RegisterPage(viewModel, strings, selectedLang)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. Giriş/Kayıt Composable'ları güncellendi (strings ve langCode alıyor)
@Composable
fun LoginPage(viewModel: AuthViewModel, strings: AuthStrings, langCode: String) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
        focusedLabelColor = Color.White, unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White
    )

    Column(
        modifier = Modifier.padding(32.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(strings.email) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), colors = colors)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(strings.password) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), colors = colors)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.login(email, password, langCode) }, // langCode'u yolla
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.loginButton)
        }
    }
}

@Composable
fun RegisterPage(viewModel: AuthViewModel, strings: AuthStrings, langCode: String) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
        focusedLabelColor = Color.White, unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White
    )

    Column(
        modifier = Modifier.padding(32.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(strings.email) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), colors = colors)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text(strings.passwordHint) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), colors = colors)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.register(email, password, langCode) }, // langCode'u yolla
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.registerButton)
        }
    }
}