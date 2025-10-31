package com.alica.evdekisef.ui.navigation // Sizin paket adınız

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alica.evdekisef.ui.admin.AdminScreen // Admin ekranı hala burada
import com.alica.evdekisef.ui.detail.DetailScreen
import com.alica.evdekisef.ui.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val ADMIN = "admin"
    // ID artık Int değil, String (Document ID) olacak
    const val DETAIL = "detail/{recipeId}"

    fun detailRoute(recipeId: String) = "detail/$recipeId"
}

@Composable
fun AppNavigation(
    onLogout: () -> Unit // YENİ: Logout fonksiyonunu aldık
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME // Başlangıç ekranı artık HOME
    ) {

        // Admin Ekranı Rotası (Hala erişilebilir)
        composable(Routes.ADMIN) {
            AdminScreen()
        }

        // Ana Ekran Rotası
        composable(Routes.HOME) {
            HomeScreen(
                onLogout = onLogout, // Logout'u UI'a iletiyoruz
                onRecipeClick = { recipeId ->
                    // ID'yi (String) yolluyoruz
                    navController.navigate(Routes.detailRoute(recipeId))
                },
                onNavigateToAdmin = {
                    // (Gizli admin ekranına gitmek için bir yol)
                    navController.navigate(Routes.ADMIN)
                }
            )
        }

        // Detay Ekranı Rotası
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("recipeId") {
                type = NavType.StringType // DEĞİŞTİ: Artık String
            })
        ) {
            DetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}