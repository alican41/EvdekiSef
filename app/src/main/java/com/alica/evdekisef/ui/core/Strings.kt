package com.alica.evdekisef.ui.core // Sizin paket adınız

// Auth ekranı için çeviriler
data class AuthStrings(
    val welcome: String,
    val login: String,
    val register: String,
    val email: String,
    val password: String,
    val passwordHint: String,
    val loginButton: String,
    val registerButton: String,
    val getError: (String) -> String
)

// YENİ: Ana ekran için metinler
data class HomeStrings(
    val title: String,
    val prompt: String, // Boş ekran metni
    val ingredients: String, // Buton metni
    val noResults: String,
    val missingCount: (Int) -> String,
    val allIngredientsAvailable: String
)

// Global çeviriler
object AppStrings {
    val languages = listOf("TR", "EN")

    fun getAuthStrings(lang: String): AuthStrings {
        return when (lang) {
            "TR" -> AuthStrings(
                welcome = "Evdeki Şef'e Hoş Geldiniz",
                login = "Giriş Yap",
                register = "Kayıt Ol",
                email = "E-posta",
                password = "Şifre",
                passwordHint = "Şifre (En az 6 karakter)",
                loginButton = "Giriş Yap",
                registerButton = "Kayıt Ol",
                getError = { "Hata: $it" }
            )
            "EN" -> AuthStrings(
                welcome = "Welcome to Home Chef",
                login = "Login",
                register = "Register",
                email = "Email",
                password = "Password",
                passwordHint = "Password (min 6 characters)",
                loginButton = "Login",
                registerButton = "Register",
                getError = { "Error: $it" }
            )
            else -> getAuthStrings("TR") // Varsayılan TR
        }
    }

    fun getHomeStrings(lang: String): HomeStrings {
        return when (lang) {
            "TR" -> HomeStrings(
                title = "Evdeki Şef",
                prompt = "Tarif bulmak için lütfen malzeme seçin.",
                ingredients = "Malzemeler",
                noResults = "Seçilen malzemelere uygun tarif bulunamadı.",
                missingCount = { "$it eksik malzeme" },
                allIngredientsAvailable = "Tüm malzemeler evde var!"
            )
            "EN" -> HomeStrings(
                title = "Home Chef",
                prompt = "Please select ingredients to find recipes.",
                ingredients = "Ingredients",
                noResults = "No recipes found matching the selected ingredients.",
                missingCount = { "$it missing ingredients" },
                allIngredientsAvailable = "All ingredients are available!"
            )
            else -> getHomeStrings("TR")
        }
    }


}