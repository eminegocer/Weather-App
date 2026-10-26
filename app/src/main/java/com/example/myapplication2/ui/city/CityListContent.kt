package com.example.myapplication2.ui.city

import com.example.myapplication2.model.CityListItem

/**
 * Şehir listesinin başarı içeriği.
 *
 * [turkeyOverview] doğruysa bu liste arama yapılmadan gösterilen alfabetik
 * Türkiye şehirleri ve her birinin anlık hava özetidir.
 */
data class CityListContent(
    val items: List<CityListItem>,
    val turkeyOverview: Boolean
)
