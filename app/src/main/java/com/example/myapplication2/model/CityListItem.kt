package com.example.myapplication2.model

/**
 * Şehir listesindeki tek satır.
 *
 * [temperatureC] ve [weatherCode] Türkiye özetinde ve yurt dışı arama sonucunda doludur.
 * Türkiye'de bulunan bir arama sonucu haritada gösterilir; derece kartta yazılmaz.
 */
data class CityListItem(
    val city: City,
    val temperatureC: Double? = null,
    val weatherCode: Int? = null
)
