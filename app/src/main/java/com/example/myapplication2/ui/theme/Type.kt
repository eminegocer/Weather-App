package com.example.myapplication2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

/**
 * Yazı tipi ölçeği. Tema üç dosyadan oluşur: Color, Theme, Type.
 *
 * Material 3'ün hazır ölçeğinden başlayıp yalnızca başlıkları biraz kalınlaştırıyoruz.
 * Bileşenler boyutu elle yazmaz; `MaterialTheme.typography.titleMedium` gibi
 * **rol** isimleriyle okur. Böylece tipografi tek yerden değişir.
 */
private val base = Typography()

internal val HavaTypography = base.copy(
    displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Bold)
)
