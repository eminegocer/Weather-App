package com.example.myapplication2.ui.city

import android.animation.ValueAnimator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * Küçük Türkiye silüeti ve üzerinde tek bir şehir noktası.
 *
 * Şekil de nokta da aynı enlem/boylam penceresine oturtulur. Böylece
 * Ankara ortaya, İzmir batıya, Van doğuya düşer. Çizgi, sadeleştirilmiş
 * gerçek ülke sınırıdır: Trakya ayrı durur, Sinop, Hatay ve doğu burnu yerindedir.
 *
 * Nokta Türkiye penceresinin dışındaysa (yurt dışı arama sonucu) yalnızca
 * çizgi kalır. Yanlış bir yere işaret koymayız.
 *
 * Kara boyanmaz; yalnızca kıyı çizgisi vardır. Şehir, deprem uygulamalarındaki
 * gibi genişleyip sönen bir dalgayla gösterilir. Telefon animasyonları kapalıysa
 * dalga durur, nokta yerinde kalır.
 *
 * [latitude] enlem, [longitude] boylamdır.
 */
@Composable
fun TurkeyLocator(
    latitude: Double,
    longitude: Double,
    cityName: String,
    modifier: Modifier = Modifier
) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val shore = if (dark) Color(0xFFD5E2E8) else Color(0xFF3E5C6B)
    val pin = if (dark) Color(0xFFFF6B6B) else Color(0xFFE23B3B)
    // 0'dan 1'e gidip başa saran saat. İki dalga yarım tur arayla çıkar.
    val motionEnabled = remember { ValueAnimator.areAnimatorsEnabled() }
    val transition = rememberInfiniteTransition(label = "locator")
    val animated by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "locatorWave"
    )
    val phase = if (motionEnabled) animated else 0f
    Canvas(
        modifier = modifier
            .width(102.dp)
            .height(60.dp)
            .semantics { contentDescription = "$cityName, Türkiye haritasındaki konumu" }
    ) {
        // Anadolu ve Trakya ayrı halkalardır. Aradaki boşluk Marmara ve boğazlardır.
        for (ring in TURKEY_RINGS) {
            val outline = Path().apply {
                val start = project(ring.first().first, ring.first().second)
                moveTo(start.x, start.y)
                for (index in 1 until ring.size) {
                    val point = project(ring[index].first, ring[index].second)
                    lineTo(point.x, point.y)
                }
                close()
            }
            drawPath(
                path = outline,
                color = shore,
                style = Stroke(
                    width = 1.25.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
        if (latitude in LAT_MIN..LAT_MAX && longitude in LON_MIN..LON_MAX) {
            drawPulse(project(latitude, longitude), phase, pin, motionEnabled)
        }
    }
}

/** Merkezdeki nokta yanıp söner; etrafındaki halkalar dışarı doğru silinerek büyür. */
private fun DrawScope.drawPulse(at: Offset, phase: Float, color: Color, moving: Boolean) {
    if (moving) {
        drawWave(at, phase, color)
        drawWave(at, (phase + 0.5f) % 1f, color)
    }
    val blink = if (moving) {
        val wave = (sin(phase.toDouble() * 2.0 * PI).toFloat() + 1f) / 2f
        0.4f + 0.6f * wave
    } else {
        1f
    }
    drawCircle(color = Color.White, radius = 3.6.dp.toPx(), center = at)
    drawCircle(color = color.copy(alpha = blink), radius = 2.5.dp.toPx(), center = at)
}

private fun DrawScope.drawWave(at: Offset, phase: Float, color: Color) {
    val spread = 2.5.dp.toPx() + 11.dp.toPx() * phase
    val fade = (1f - phase).coerceIn(0f, 1f)
    drawCircle(color = color.copy(alpha = 0.20f * fade), radius = spread, center = at)
    drawCircle(
        color = color.copy(alpha = 0.75f * fade),
        radius = spread,
        center = at,
        style = Stroke(width = 1.4.dp.toPx())
    )
}

/**
 * Enlemi yukarı, boylamı sağa koyar. Canvas'ta y aşağı büyüdüğü için
 * kuzey (büyük enlem) küçük y olur.
 */
private fun DrawScope.project(latitude: Double, longitude: Double): Offset {
    // Boylam derecesi bu enlemde daha kısadır. İkisini aynı ölçekle çizersek ülke şişman görünür.
    val widthUnits = (LON_MAX - LON_MIN) * LON_SCALE
    val heightUnits = LAT_MAX - LAT_MIN
    val pad = 0.07f
    val scale = min(
        size.width * (1f - pad * 2f) / widthUnits.toFloat(),
        size.height * (1f - pad * 2f) / heightUnits.toFloat()
    )
    val drawnWidth = widthUnits.toFloat() * scale
    val drawnHeight = heightUnits.toFloat() * scale
    val originX = (size.width - drawnWidth) / 2f
    val originY = (size.height - drawnHeight) / 2f
    val x = originX + ((longitude - LON_MIN) * LON_SCALE).toFloat() * scale
    val y = originY + (LAT_MAX - latitude).toFloat() * scale
    return Offset(x, y)
}

// Pencere, 81 ilin merkezini içine alacak kadar geniştir.
// 39° kuzeyde bir boylam derecesi, bir enlem derecesinin yaklaşık 0.78'i kadardır.
private const val LON_SCALE = 0.7771
private const val LON_MIN = 25.6
private const val LON_MAX = 45.3
private const val LAT_MIN = 35.6
private const val LAT_MAX = 42.4

/**
 * Türkiye'nin dış hattı. Her çift: enlem, boylam.
 * İlk halka Anadolu, ikincisi Trakya'dır; aradaki boşluk Marmara ve boğazlardır.
 * Hat, gerçek ülke sınırının küçük karta sadeleştirilmiş halidir.
 */
private val TURKEY_RINGS: List<List<Pair<Double, Double>>> = listOf(
    listOf(
        35.93 to 36.18,
        36.23 to 36.39,
        36.25 to 36.69,
        36.48 to 36.54,
        36.83 to 36.67,
        36.63 to 37.47,
        36.92 to 38.24,
        36.67 to 39.21,
        37.13 to 40.77,
        37.08 to 41.52,
        37.32 to 42.21,
        37.11 to 42.35,
        37.14 to 42.57,
        37.38 to 42.79,
        37.19 to 43.83,
        37.32 to 44.13,
        36.96 to 44.31,
        37.14 to 44.78,
        37.73 to 44.62,
        37.89 to 44.22,
        38.34 to 44.51,
        38.39 to 44.30,
        39.37 to 44.03,
        39.41 to 44.42,
        39.78 to 44.61,
        39.65 to 44.82,
        40.03 to 44.35,
        40.11 to 43.66,
        40.48 to 43.54,
        40.73 to 43.75,
        41.18 to 43.45,
        41.17 to 43.23,
        41.60 to 42.68,
        41.44 to 42.52,
        41.52 to 41.55,
        40.91 to 40.18,
        41.11 to 39.42,
        40.91 to 38.36,
        41.14 to 37.68,
        41.03 to 37.51,
        41.38 to 36.66,
        41.25 to 36.39,
        41.68 to 36.07,
        41.71 to 35.28,
        42.06 to 35.18,
        42.09 to 35.02,
        41.94 to 34.72,
        42.02 to 33.34,
        41.72 to 32.28,
        41.58 to 31.85,
        41.10 to 31.25,
        41.22 to 29.17,
        41.01 to 29.01,
        40.80 to 29.26,
        40.73 to 29.94,
        40.67 to 29.50,
        40.68 to 29.28,
        40.60 to 28.95,
        40.55 to 28.79,
        40.44 to 29.15,
        40.36 to 29.05,
        40.35 to 27.96,
        40.48 to 28.03,
        40.53 to 27.75,
        40.37 to 27.88,
        40.30 to 27.53,
        40.48 to 27.29,
        40.39 to 26.72,
        39.99 to 26.18,
        39.47 to 26.07,
        39.56 to 26.95,
        39.28 to 26.61,
        39.09 to 26.88,
        38.95 to 26.80,
        38.88 to 27.07,
        38.65 to 26.72,
        38.43 to 26.94,
        38.45 to 27.17,
        38.43 to 26.71,
        38.31 to 26.67,
        38.68 to 26.42,
        38.43 to 26.52,
        38.27 to 26.23,
        37.97 to 27.25,
        37.73 to 27.24,
        37.66 to 27.00,
        37.58 to 27.23,
        37.35 to 27.19,
        37.42 to 27.42,
        37.25 to 27.64,
        37.08 to 27.47,
        37.13 to 27.25,
        36.96 to 27.26,
        37.05 to 28.33,
        36.79 to 28.03,
        36.66 to 27.41,
        36.81 to 28.13,
        36.56 to 28.03,
        36.88 to 28.46,
        36.59 to 28.85,
        36.76 to 28.93,
        36.68 to 29.09,
        36.39 to 29.10,
        36.13 to 29.68,
        36.31 to 30.19,
        36.21 to 30.40,
        36.84 to 30.61,
        36.86 to 31.01,
        36.54 to 32.03,
        36.02 to 32.80,
        36.23 to 33.96,
        36.81 to 34.65,
        36.54 to 35.34,
        36.93 to 36.01,
        36.64 to 36.21,
        36.30 to 35.78,
        35.92 to 35.92,
        35.81 to 36.15,
        35.92 to 36.18
    ),
    listOf(
        40.10 to 26.31,
        40.97 to 27.51,
        41.08 to 28.23,
        40.95 to 28.83,
        40.98 to 29.00,
        41.23 to 29.12,
        41.52 to 28.22,
        41.98 to 28.03,
        41.91 to 27.56,
        42.10 to 27.24,
        41.98 to 26.62,
        41.74 to 26.33,
        41.61 to 26.60,
        41.35 to 26.63,
        41.25 to 26.32,
        40.95 to 26.36,
        40.73 to 26.04,
        40.59 to 26.13,
        40.59 to 26.84,
        40.32 to 26.22,
        40.05 to 26.17,
        40.09 to 26.31
    )
)
