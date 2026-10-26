package com.example.myapplication2.ui.forecast

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication2.R
import com.example.myapplication2.model.CurrentWeather
import com.example.myapplication2.model.DailyForecast
import com.example.myapplication2.model.Forecast
import com.example.myapplication2.model.HourlyForecast
import com.example.myapplication2.model.WeatherCondition
import com.example.myapplication2.ui.LoadState
import com.example.myapplication2.ui.component.ErrorView
import com.example.myapplication2.ui.theme.HavaTheme
import com.example.myapplication2.ui.theme.havaScreenBrush
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Tahmin ekranının bağlayıcı katmanı; ViewModel'i edinir ve state'i aktarır.
 * (Bkz. [com.example.myapplication2.ui.city.CityRoute] — aynı desen.)
 */
@Composable
fun ForecastRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ForecastViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForecastScreen(
        cityName = viewModel.cityName,
        subtitle = viewModel.subtitle,
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}

/**
 * Bir şehrin hava tahmini.
 *
 * Üç bölüm: anlık durum başlığı, saatlik yatay liste, günlük dikey liste.
 * Dört durumun tamamı burada da karşılanır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    cityName: String,
    subtitle: String,
    uiState: LoadState<Forecast>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(havaScreenBrush()),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(cityName.ifEmpty { stringResource(R.string.detail_title) }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                // Tek bir kart beklendiği için burada Shimmer yerine basit gösterge yeterli.
                LoadState.Loading -> CircularProgressIndicator()
                LoadState.Empty -> ErrorView(message = "Bu şehir için tahmin bulunamadı.")
                is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onRetry)
                is LoadState.Success -> ForecastContent(subtitle = subtitle, forecast = uiState.data)
            }
        }
    }
}

@Composable
private fun ForecastContent(subtitle: String, forecast: Forecast, modifier: Modifier = Modifier) {
    // Ekran ilk açıldığında içerik 0'dan 1'e çıkar: belirir ve biraz yukarı kayar.
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(durationMillis = 480, easing = FastOutSlowInEasing))
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = enter.value
                translationY = (1f - enter.value) * 28.dp.toPx()
            }
            // İçerik ekrana sığmayabilir; dikey kaydırma ekliyoruz.
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CurrentWeatherHeader(
            subtitle = subtitle,
            current = forecast.current,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        SectionTitle(stringResource(R.string.detail_hourly))
        HourlyRow(hours = forecast.hourly)
        SectionTitle(stringResource(R.string.detail_daily))
        // Günlük çubuk, o günü haftanın en soğuk ve en sıcak değerine göre yerleştirir.
        val weekMin = forecast.daily.minOfOrNull { it.minTemperatureC } ?: 0.0
        val weekMax = forecast.daily.maxOfOrNull { it.maxTemperatureC } ?: 0.0
        Card(
            modifier = Modifier.padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            forecast.daily.forEachIndexed { index, day ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DailyRow(
                    day = day,
                    isToday = index == 0,
                    weekMin = weekMin,
                    weekMax = weekMax
                )
            }
        }
    }
}

/** Akan hava sahnesi, sıcaklık ve hissedilen/nem/rüzgâr. */
@Composable
private fun CurrentWeatherHeader(subtitle: String, current: CurrentWeather, modifier: Modifier = Modifier) {
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    val hasMetrics = current.apparentTemperatureC != null ||
        current.humidityPercent != null ||
        current.windSpeedKmh != null
    val onScene = Color.White
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(268.dp)
            .clip(RoundedCornerShape(28.dp))
    ) {
        // Arkada hava animasyonu (güneş, yağmur, kar...). Öndeki yazılar bunun üstüne biner.
        WeatherScene(condition = condition, modifier = Modifier.fillMaxSize())
        // Alttaki karartma, beyaz yazının açık gökyüzünde de okunmasını sağlar.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.4f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.38f)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onScene.copy(alpha = 0.85f)
                )
            }
            Text(
                text = degrees(current.temperatureC),
                style = MaterialTheme.typography.displaySmall,
                color = onScene
            )
            Text(
                text = "${condition.emoji}  ${condition.label}",
                style = MaterialTheme.typography.titleMedium,
                color = onScene
            )
            if (hasMetrics) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    current.apparentTemperatureC?.let {
                        Metric(stringResource(R.string.detail_feels_like), degrees(it), onScene)
                    }
                    current.humidityPercent?.let {
                        Metric(stringResource(R.string.detail_humidity), "%$it", onScene)
                    }
                    current.windSpeedKmh?.let {
                        Metric(stringResource(R.string.detail_wind), "${it.roundToInt()} km/sa", onScene)
                    }
                }
            }
        }
    }
}

/**
 * Önümüzdeki 24 saat, yatay kaydırılabilir kartlar hâlinde.
 *
 * Servis günün başından itibaren veri gönderir; geçmiş saatleri gizlemek için
 * şu anki saatten öncekiler elenir.
 */
@Composable
private fun HourlyRow(hours: List<HourlyForecast>, modifier: Modifier = Modifier) {
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val upcoming = hours.filter { !it.time.isBefore(now) }.take(HOURLY_COUNT)
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items = upcoming, key = { _, hour -> hour.time.toString() }) { index, hour ->
            val highlighted = index == 0
            Card(
                modifier = Modifier.width(80.dp),
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (highlighted) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = hour.time.format(HOUR_FORMAT), style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = WeatherCondition.fromWeatherCode(hour.weatherCode).emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(text = degrees(hour.temperatureC), style = MaterialTheme.typography.titleMedium)
                    Text(
                        // Boş string yerine boşluk: kartların yüksekliği eşit kalsın.
                        text = percent(hour.precipitationProbability) ?: " ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/** Günlük tahmin satırı: gün adı · yağış · emoji · aralık · en düşük / en yüksek. */
@Composable
private fun DailyRow(
    day: DailyForecast,
    isToday: Boolean,
    weekMin: Double,
    weekMax: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isToday) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                } else {
                    Color.Transparent
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) stringResource(R.string.today) else day.date.format(DAY_FORMAT).replaceFirstChar(Char::titlecase),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = percent(day.precipitationProbability).orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = WeatherCondition.fromWeatherCode(day.weatherCode).emoji,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        TemperatureSpan(
            min = day.minTemperatureC,
            max = day.maxTemperatureC,
            weekMin = weekMin,
            weekMax = weekMax,
            modifier = Modifier
                .padding(end = 8.dp)
                .width(56.dp)
        )
        Text(
            text = degrees(day.minTemperatureC),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = degrees(day.maxTemperatureC),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}

/** Haftanın en düşük ve en yüksek değerine göre o günün sıcaklık aralığı. */
@Composable
private fun TemperatureSpan(
    min: Double,
    max: Double,
    weekMin: Double,
    weekMax: Double,
    modifier: Modifier = Modifier
) {
    val span = (weekMax - weekMin).let { if (it < 0.5) 1.0 else it }
    val start = ((min - weekMin) / span).toFloat().coerceIn(0f, 1f)
    val end = ((max - weekMin) / span).toFloat().coerceIn(start, 1f)
    val fraction = (end - start).coerceAtLeast(0.18f).coerceAtMost(1f)
    val clampedStart = start.coerceIn(0f, 1f - fraction)
    BoxWithConstraints(
        modifier = modifier
            .height(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .offset(x = maxWidth * clampedStart)
                .width(maxWidth * fraction)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun Metric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

/** 18.4 → "18°". Kullanıcıya ondalık göstermeye gerek yok. */
private fun degrees(celsius: Double) = "${celsius.roundToInt()}°"

/** Yağış olasılığı yalnızca sıfırdan büyükse anlamlıdır. */
private fun percent(value: Int?) = value?.takeIf { it > 0 }?.let { "%$it" }

private const val HOURLY_COUNT = 24
private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("tr"))

@Preview(showBackground = true)
@Composable
private fun ForecastScreenPreview() {
    HavaTheme {
        ForecastScreen(
            cityName = "Ankara",
            subtitle = "Ankara, Türkiye",
            uiState = LoadState.Success(
                Forecast(
                    current = CurrentWeather(
                        temperatureC = 21.0,
                        weatherCode = 0,
                        observedAt = LocalDateTime.of(2026, 1, 15, 14, 0),
                        apparentTemperatureC = 20.0,
                        humidityPercent = 45,
                        windSpeedKmh = 12.0
                    ),
                    hourly = List(8) {
                        HourlyForecast(
                            time = LocalDateTime.now().plusHours(it.toLong()),
                            temperatureC = 18.0 + it,
                            weatherCode = 2,
                            precipitationProbability = it * 5
                        )
                    },
                    daily = List(7) {
                        DailyForecast(
                            date = LocalDate.of(2026, 1, 15).plusDays(it.toLong()),
                            minTemperatureC = 9.0 + it,
                            maxTemperatureC = 19.0 + it,
                            weatherCode = 61,
                            precipitationProbability = 40
                        )
                    }
                )
            ),
            onBack = {},
            onRetry = {}
        )
    }
}
