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
        // Kaynak dil Türkçe, hedef dil İngilizce
        return TranslatorOptions.Builder()
            .setSourceLanguage("tr")
            .setTargetLanguage("en")
            .build()
    }

    @Provides
    @Singleton
    fun provideTranslator(options: TranslatorOptions): Translator {
        val translator = Translation.getClient(options)

        // Uygulama başlarken çeviri modelinin indirilmesini tetikliyoruz.
        // Bu, kullanıcının ilk aramasında gecikme yaşamasını engeller.
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                println("ML Kit TR-EN modeli başarıyla indirildi veya zaten mevcuttu.")
            }
            .addOnFailureListener { exception ->
                println("ML Kit model indirme hatası: $exception")
            }

        return translator
    }
}