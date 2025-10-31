package com.alica.evdekisef.di

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Bu harita, bizim "Türkçe -> İngilizce" sözlüğümüz olacak
    // Hilt ile bu haritayı ViewModel'e enjekte edeceğiz.
    @Provides
    @Singleton
    @IngredientMap
    fun provideIngredientMap(): Map<String, String> {
        return mapOf(
            // --- Popüler Malzemeler ---
            "Tavuk" to "Chicken",
            "Sığır Eti" to "Beef",
            "Somon" to "Salmon",
            "Kuzu" to "Lamb",
            "Yumurta" to "Eggs",
            "Mantar" to "Mushrooms",
            "Soğan" to "Onion",
            "Sarımsak" to "Garlic",
            "Domates" to "Tomato",
            "Patates" to "Potato",
            "Havuç" to "Carrot",
            "Brokoli" to "Broccoli",
            "Pirinç" to "Rice",
            "Makarna" to "Pasta",

            // --- Diğerleri (Zamanla eklenebilir) ---
            "Pastırma" to "Bacon",
            "Avokado" to "Avocado",
            "Ton Balığı" to "Tuna",
            "Limon" to "Lemon",
            "Biber" to "Peppers",
            "Peynir" to "Cheese"

            // Bu listeyi API'nin desteklediği tüm malzemelerle (list.php?i=list)
            // zenginleştirebiliriz.
        )
    }

    @Provides
    @Singleton
    @SuffixMap
    fun provideTurkishSuffixMap(): Map<String, String> {
        // Bu harita, seçilen malzemeye hangi "ek"in geleceğini belirler.
        return mapOf(
            "Tavuk" to "Tavuklu",
            "Sığır Eti" to "Sığırlı",
            "Somon" to "Somonlu",
            "Kuzu" to "Kuzulu",
            "Yumurta" to "Yumurtalı",
            "Mantar" to "Mantarlı",
            "Soğan" to "Soğanlı",
            "Sarımsak" to "Sarımsaklı",
            "Domates" to "Domatesli",
            "Patates" to "Patatesli",
            "Havuç" to "Havuçlu",
            "Brokoli" to "Brokolili",
            "Pirinç" to "Pirinçli",
            "Makarna" to "Makarnalı",
            "Pastırma" to "Pastırmalı",
            "Avokado" to "Avokadolu",
            "Ton Balığı" to "Ton Balıklı",
            "Limon" to "Limonlu",
            "Biber" to "Biberli",
            "Peynir" to "Peynirli"
        )
    }

    @Provides
    @Singleton
    @UnitMap // Etiketi ekle
    fun provideUnitMap(): Map<String, String> {
        // Spoonacular'da en sık kullanılan birimler
        return mapOf(
            // Kısaltmalar
            "g" to "gr",
            "kg" to "kg",
            "ml" to "ml",
            "l" to "L",
            "lb" to "lb (pound)",
            "lbs" to "lbs (pound)",
            "oz" to "oz (ons)",
            "cup" to "su bardağı",
            "cups" to "su bardağı",
            "tbsp" to "yemek kaşığı",
            "tablespoon" to "yemek kaşığı",
            "tablespoons" to "yemek kaşığı",
            "tsp" to "çay kaşığı",
            "teaspoon" to "çay kaşığı",
            "teaspoons" to "çay kaşığı",

            // Kelimeler
            "pinch" to "tutam",
            "dash" to "tutam",
            "slice" to "dilim",
            "slices" to "dilim",
            "clove" to "diş" ,
            "cloves" to "diş",
            "serving" to "porsiyon",
            "servings" to "porsiyon",
            "piece" to "adet",
            "pieces" to "adet"
            // (Bu liste zamanla zenginleştirilebilir)
        )
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        // Dil ayarını bu dosyada saklayacağız
        return context.getSharedPreferences("evdeki_sef_prefs", Context.MODE_PRIVATE)
    }


}