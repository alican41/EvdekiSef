package com.alica.evdekisef.di

import javax.inject.Qualifier

// 1. Etiket: Malzeme haritası için
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IngredientMap

// 2. Etiket: Ek haritası için
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SuffixMap

// di/Qualifiers.kt
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnitMap