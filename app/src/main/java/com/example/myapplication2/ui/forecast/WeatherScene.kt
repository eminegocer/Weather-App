package com.example.myapplication2.ui.forecast

import android.animation.ValueAnimator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.myapplication2.model.WeatherCondition
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Liste kartının sol şeridi. Koyu tonlar beyaz yazıyla okunur. */
internal fun weatherAccent(condition: WeatherCondition): Color = when (condition) {
    WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR -> Color(0xFFE08A12)
    WeatherCondition.PARTLY_CLOUDY -> Color(0xFF4E6A86)
    WeatherCondition.OVERCAST -> Color(0xFF3E5163)
    WeatherCondition.FOG -> Color(0xFF5E6C78)
    WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS -> Color(0xFF1F4E79)
    WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS -> Color(0xFF3E6F8C)
    WeatherCondition.THUNDERSTORM -> Color(0xFF2C2458)
    WeatherCondition.UNKNOWN -> Color(0xFF5C656C)
}

/**
 * Şehrin anlık havasına göre akan sahne.
 *
 * Güneş döner, bulut kayar, yağmur ve kar düşer, fırtınada şimşek çakar.
 * Sistem animasyonları kapalıysa sahne tek karede durur.
 */
@Composable
internal fun WeatherScene(condition: WeatherCondition, modifier: Modifier = Modifier) {
    // Gökyüzü renkleri ve damla listesi hava değişmedikçe yeniden üretilmez.
    val palette = remember(condition) { scenePalette(condition) }
    val particles = remember(condition) { particlesFor(condition) }
    // Telefonun animasyon ayarı kapalıysa sahne donmuş bir karede kalır.
    val motionEnabled = remember { ValueAnimator.areAnimatorsEnabled() }
    // 0'dan 1'e gidip başa saran saat. Güneşin dönüşü ve yağmurun düşüşü bu sayıdandır.
    val transition = rememberInfiniteTransition(label = "weather")
    val animated by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = palette.loopMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weatherTime"
    )
    val time = if (motionEnabled) animated else 0.2f

    Canvas(modifier = modifier.fillMaxSize()) {
        if (size.minDimension <= 0f) return@Canvas
        // Önce gökyüzü, sonra üstüne güneş / bulut / yağış. when hangi havanın ne çizeceğini seçer.
        drawRect(Brush.verticalGradient(listOf(palette.top, palette.bottom)))
        when (condition) {
            WeatherCondition.CLEAR -> drawSun(time, withRays = true)
            WeatherCondition.MAINLY_CLEAR -> {
                drawSun(time, withRays = true)
                drawClouds(time, alpha = 0.55f, count = 1)
            }
            WeatherCondition.PARTLY_CLOUDY -> {
                drawSun(time, withRays = true)
                drawClouds(time, alpha = 0.75f, count = 2)
            }
            WeatherCondition.OVERCAST -> drawClouds(time, alpha = 0.9f, count = 3)
            WeatherCondition.FOG -> {
                drawClouds(time, alpha = 0.35f, count = 2)
                drawFog(time)
            }
            WeatherCondition.DRIZZLE -> {
                drawClouds(time, alpha = 0.7f, count = 2)
                drawRain(time, particles, heavy = false)
            }
            WeatherCondition.RAIN -> {
                drawClouds(time, alpha = 0.82f, count = 3)
                drawRain(time, particles, heavy = true)
            }
            WeatherCondition.RAIN_SHOWERS -> {
                drawSun(time, withRays = false)
                drawClouds(time, alpha = 0.7f, count = 2)
                drawRain(time, particles, heavy = true)
            }
            WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS -> {
                drawClouds(time, alpha = 0.45f, count = 2)
                drawSnow(time, particles)
            }
            WeatherCondition.THUNDERSTORM -> {
                drawClouds(time, alpha = 0.92f, count = 3)
                drawRain(time, particles, heavy = true)
                drawLightning(time)
            }
            WeatherCondition.UNKNOWN -> drawSun(time, withRays = false)
        }
    }
}

private data class ScenePalette(val top: Color, val bottom: Color, val loopMs: Int)

private fun scenePalette(condition: WeatherCondition): ScenePalette = when (condition) {
    WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR ->
        ScenePalette(Color(0xFF3A8DDE), Color(0xFF8EC8F5), 8_000)
    WeatherCondition.PARTLY_CLOUDY ->
        ScenePalette(Color(0xFF5C8FB8), Color(0xFFB7D0E4), 9_000)
    WeatherCondition.OVERCAST ->
        ScenePalette(Color(0xFF66788A), Color(0xFF9AABB8), 12_000)
    WeatherCondition.FOG ->
        ScenePalette(Color(0xFF8B98A3), Color(0xFFC5CED6), 14_000)
    WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS ->
        ScenePalette(Color(0xFF35556F), Color(0xFF6E8DA6), 2_600)
    WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS ->
        ScenePalette(Color(0xFF7FA0BE), Color(0xFFE4EEF6), 7_000)
    WeatherCondition.THUNDERSTORM ->
        ScenePalette(Color(0xFF1C2433), Color(0xFF3A3358), 2_200)
    WeatherCondition.UNKNOWN ->
        ScenePalette(Color(0xFF6E8794), Color(0xFFB7C9D4), 8_000)
}

/**
 * Tek bir damla veya kar tanesi.
 * [x] ve [phase] 0 ile 1 arasındadır: x yatay yer, phase düşüşe nereden başladığı.
 * Liste bir kez kurulur; her karede yalnızca [time] ile yerleri kayar.
 */
private data class Particle(val x: Float, val speed: Float, val size: Float, val phase: Float)

private fun particlesFor(condition: WeatherCondition): List<Particle> {
    val count = when (condition) {
        WeatherCondition.DRIZZLE -> 16
        WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS -> 40
        WeatherCondition.SNOW -> 26
        WeatherCondition.SNOW_SHOWERS -> 18
        WeatherCondition.THUNDERSTORM -> 48
        else -> 0
    }
    // Sabit tohum: aynı hava her açılışta aynı damla dizilimini üretir, ekran zıplamaz.
    val random = Random(condition.ordinal + 17)
    return List(count) {
        Particle(
            x = random.nextFloat(),
            speed = 0.55f + random.nextFloat(),
            size = 0.35f + random.nextFloat(),
            phase = random.nextFloat()
        )
    }
}

/** Sağ üste güneş. [withRays] açıksa ışınlar [time] ile döner; parlaklık hafifçe nefes alır. */
private fun DrawScope.drawSun(time: Float, withRays: Boolean) {
    val center = Offset(size.width * 0.8f, size.height * 0.3f)
    val radius = 26.dp.toPx()
    val pulse = 1f + 0.05f * sin(time * 2.0 * PI).toFloat()
    drawCircle(Color(0xFFFFE08A).copy(alpha = 0.35f), radius * 1.8f * pulse, center)
    if (withRays) {
        val spin = time * 360f
        for (index in 0 until 10) {
            rotate(spin + index * 36f, center) {
                drawLine(
                    color = Color(0xFFFFE7A3),
                    start = Offset(center.x, center.y - radius - 8.dp.toPx()),
                    end = Offset(center.x, center.y - radius - 20.dp.toPx()),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
    drawCircle(Color(0xFFFFD56A), radius * pulse, center)
    drawCircle(Color(0xFFFFF6D2), radius * 0.62f, center)
}

/** Bulutlar soldan sağa kayar. Ekrandan çıkınca başa sarılır. */
private fun DrawScope.drawClouds(time: Float, alpha: Float, count: Int) {
    val travel = time * size.width
    repeat(count) { index ->
        val anchor = 0.12f + index * 0.3f
        val speed = 0.25f + index * 0.08f
        val x = ((anchor * size.width + travel * speed) % (size.width + 140f)) - 70f
        val y = size.height * (0.16f + index * 0.08f)
        cloud(Offset(x, y), 1f - index * 0.08f, Color.White.copy(alpha = alpha))
    }
}

/** Bir bulut, üst üste binmiş dört dairedir. Hazır bir bulut şekli yoktur. */
private fun DrawScope.cloud(origin: Offset, scale: Float, color: Color) {
    val unit = 18.dp.toPx() * scale
    drawCircle(color, unit * 1.15f, origin)
    drawCircle(color, unit * 0.82f, origin + Offset(-unit, unit * 0.28f))
    drawCircle(color, unit * 0.9f, origin + Offset(unit * 1.05f, unit * 0.35f))
    drawCircle(color, unit * 0.55f, origin + Offset(unit * 0.35f, -unit * 0.45f))
}

/** Damlalar yukarıdan aşağı iner. `% 1f` alta varınca damlayı tekrar üste alır. */
private fun DrawScope.drawRain(time: Float, particles: List<Particle>, heavy: Boolean) {
    val color = Color.White.copy(alpha = if (heavy) 0.72f else 0.5f)
    val width = if (heavy) 2.2.dp.toPx() else 1.4.dp.toPx()
    particles.forEach { drop ->
        val y = ((drop.phase + time * drop.speed) % 1f) * (size.height + 40f) - 20f
        val x = drop.x * size.width
        val length = 10.dp.toPx() + drop.size * 18.dp.toPx()
        drawLine(
            color = color,
            start = Offset(x, y),
            end = Offset(x - length * 0.18f, y + length),
            strokeWidth = width,
            cap = StrokeCap.Round
        )
    }
}

/** Kar yağmurdan yavaş düşer ve sinüs ile sağa sola savrulur. */
private fun DrawScope.drawSnow(time: Float, particles: List<Particle>) {
    particles.forEach { flake ->
        val y = ((flake.phase + time * flake.speed * 0.45f) % 1f) * (size.height + 24f) - 12f
        val sway = sin((time + flake.phase) * 2.0 * PI).toFloat() * 14.dp.toPx()
        val x = flake.x * size.width + sway
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = 2.dp.toPx() + flake.size * 3.5.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

/** Dört yatay sis bandı. Her biri kendi hızıyla sağa sola kayar. */
private fun DrawScope.drawFog(time: Float) {
    repeat(4) { index ->
        val shift = sin((time + index * 0.2f) * 2.0 * PI).toFloat() * 28.dp.toPx()
        val bandHeight = 26.dp.toPx()
        drawRoundRect(
            color = Color.White.copy(alpha = 0.16f + index * 0.04f),
            topLeft = Offset(-48.dp.toPx() + shift, size.height * (0.22f + index * 0.16f)),
            size = Size(size.width + 96.dp.toPx(), bandHeight),
            cornerRadius = CornerRadius(bandHeight, bandHeight)
        )
    }
}

/** Döngünün kısa bir anında ekran beyazlar ve kırık bir çizgi şimşek olur. Kalan sürede görünmez. */
private fun DrawScope.drawLightning(time: Float) {
    val window = (time * 3f) % 1f
    if (window > 0.08f) return
    val alpha = (1f - window / 0.08f) * 0.4f
    drawRect(Color.White.copy(alpha = alpha))
    val startX = size.width * 0.62f
    val path = Path().apply {
        moveTo(startX, size.height * 0.05f)
        lineTo(startX - 16.dp.toPx(), size.height * 0.28f)
        lineTo(startX + 6.dp.toPx(), size.height * 0.3f)
        lineTo(startX - 24.dp.toPx(), size.height * 0.58f)
    }
    drawPath(
        path = path,
        color = Color(0xFFFFF4C8),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}
