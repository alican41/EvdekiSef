package com.alica.evdekisef.di // Sizin paket adınız

import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TranslationModule {

    @Provides
    @Singleton
    fun provideTranslatorOptions(): TranslatorOptions {
        // !!! HATA BURADAYDI - ŞİMDİ DÜZELTİLDİ !!!
        // Kaynak dili İngilizce, hedef dili Türkçe olarak ayarlıyoruz.
        return TranslatorOptions.Builder()
            .setSourceLanguage("en") // ESKİSİ: "tr"
            .setTargetLanguage("tr") // ESKİSİ: "en"
            .build()
    }

    @Provides
    @Singleton
    fun provideTranslator(options: TranslatorOptions): Translator {
        val translator = Translation.getClient(options)

        // Uygulama başlarken YENİ (EN->TR) modelin indirilmesini tetikliyoruz.
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                println("ML Kit EN->TR modeli başarıyla indirildi veya zaten mevcuttu.")
            }
            .addOnFailureListener { exception ->
                println("ML Kit model indirme hatası: $exception")
            }

        return translator
    }
}