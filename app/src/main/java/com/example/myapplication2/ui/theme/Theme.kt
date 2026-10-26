package com.example.myapplication2.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

/**
 * Açık tema. Sis mavisi zemin, beyaz kartlar, lagün mavisi vurgu.
 */
private val LightColors = lightColorScheme(
    primary = Sky40,
    onPrimary = Neutral99,
    primaryContainer = Sky90,
    onPrimaryContainer = Sky10,
    secondary = Amber40,
    onSecondary = Neutral99,
    secondaryContainer = Amber90,
    onSecondaryContainer = Sky10,
    background = Neutral95,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    surfaceContainer = Neutral95,
    surfaceContainerHigh = NeutralVariant90,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Red40,
    errorContainer = Red90
)

/** Koyu tema. Aynı roller, gece gökyüzüne yakın tonlarla. */
private val DarkColors = darkColorScheme(
    primary = Sky80,
    onPrimary = Sky20,
    primaryContainer = Sky30,
    onPrimaryContainer = Sky90,
    secondary = Amber80,
    onSecondary = Sky10,
    secondaryContainer = Amber40,
    onSecondaryContainer = Amber90,
    background = Ink,
    onBackground = Neutral90,
    surface = Ink,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    surfaceContainer = InkRaised,
    surfaceContainerHigh = InkHigh,
    outline = NeutralVariant30,
    outlineVariant = NeutralVariant30,
    error = Red80,
    errorContainer = Red40
)

/**
 * Ekranın üstten alta açılan zemini. Kartlar bunun üzerinde durur.
 */
@Composable
internal fun havaScreenBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.background
    )
)

/**
 * Uygulamanın teması. Bütün ekranlar bunun içine sarmalanır.
 *
 * @param darkTheme Varsayılan olarak telefonun sistem ayarını izler.
 */
@Composable
fun HavaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = HavaTypography,
        content = content
    )
}
