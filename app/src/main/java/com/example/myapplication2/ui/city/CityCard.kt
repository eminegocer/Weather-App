package com.example.myapplication2.ui.city

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication2.model.City
import com.example.myapplication2.model.WeatherCondition
import com.example.myapplication2.ui.forecast.weatherAccent
import com.example.myapplication2.ui.theme.HavaTheme
import kotlin.math.roundToInt

/**
 * Listedeki tek bir şehir satırı.
 *
 * Emoji ya da ülke kodu küçük, nötr bir dairededir. Kartın kenarı ince bir
 * çizgiyle ayrılır. Şehir Türkiye'deyse sağda harita, değilse derece durur.
 *
 * @param onClick Karta dokunulduğunda çağrılır. Bileşen ne olacağını bilmez,
 *   yalnızca haber verir.
 */
@Composable
fun CityCard(
    city: City,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    temperatureC: Double? = null,
    weatherCode: Int? = null
) {
    // Türkiye'de emoji ve etiket hava kodundan gelir. Yurt dışında sol rozet ülke kodu kalır.
    val condition = if (city.inTurkey) weatherCode?.let(WeatherCondition::fromWeatherCode) else null
    val shape = RoundedCornerShape(20.dp)
    // Yurt dışı kartında derecenin rengi. Daire nötr kalır; hava rengi şerit olarak dönmez.
    val accent = condition?.let(::weatherAccent) ?: MaterialTheme.colorScheme.secondary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = shape, clip = false)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji veya ülke kodu, karttan bir ton ayrı duran küçük dairede.
        Box(
            modifier = Modifier
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = condition?.emoji ?: (city.countryCode ?: "?"),
                style = if (condition != null) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.titleSmall
                },
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        // Orta: şehir adı ve "Açık" / "Yağmurlu" gibi kısa etiket. weight kalan genişliği alır.
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = city.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = condition?.label ?: city.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (city.inTurkey) {
            TurkeyLocator(
                latitude = city.latitude,
                longitude = city.longitude,
                cityName = city.name,
                modifier = Modifier.padding(start = 8.dp, end = 12.dp)
            )
        } else {
            // Yurt dışı: Türkiye haritası yok. Sağda yalnızca anlık derece.
            Text(
                text = temperatureC?.let { "${it.roundToInt()}°" } ?: "–",
                modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                style = MaterialTheme.typography.headlineSmall,
                color = accent,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CityCardPreview() {
    HavaTheme {
        CityCard(
            city = City(
                id = 323786,
                name = "Ankara",
                latitude = 39.91987,
                longitude = 32.85427,
                countryCode = "TR",
                country = "Türkiye",
                admin1 = "Ankara"
            ),
            onClick = {},
            weatherCode = 0
        )
    }
}
