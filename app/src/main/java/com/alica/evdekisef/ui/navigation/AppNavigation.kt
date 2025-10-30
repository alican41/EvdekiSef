package com.alica.evdekisef.ui.navigation // Sizin paket adınız

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alica.evdekisef.ui.detail.DetailScreen
import com.alica.evdekisef.ui.home.HomeScreen

// Rotalarımızı tanımlıyoruz
object Routes {
    const val HOME = "home"
    // {recipeId} diyerek buraya bir argüman geleceğini belirtiyoruz
    const val DETAIL = "detail/{recipeId}"

    // Argümanı kolayca doldurmak için yardımcı fonksiyon
    fun detailRoute(recipeId: Int) = "detail/$recipeId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME // Uygulama bu ekrandan başlasın
    ) {
        // Ana Ekran Rotası
        composable(Routes.HOME) {
            HomeScreen(
                // HomeScreen'den bir tıklama gelirse
                // recipeId'yi alıp detay rotasına yönlendir
                onRecipeClick = { recipeId ->
                    navController.navigate(Routes.detailRoute(recipeId))
                }
            )
        }

        // Detay Ekranı Rotası
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("recipeId") { // 'recipeId' argümanını tanımla
                type = NavType.IntType
            })
        ) {
            // (ViewModel, Hilt ve savedStateHandle sayesinde
            // 'recipeId'yi otomatik olarak alacak)
            DetailScreen(
                onNavigateBack = {
                    navController.popBackStack() // Geri tuşu
                }
            )
        }
    }
}