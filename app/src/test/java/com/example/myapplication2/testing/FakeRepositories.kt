package com.example.myapplication2.testing

import com.example.myapplication2.data.CityRepository
import com.example.myapplication2.data.CurrentWeatherSource
import com.example.myapplication2.data.TurkeyOverviewSource
import com.example.myapplication2.data.WeatherRepository
import com.example.myapplication2.model.City
import com.example.myapplication2.model.CityListItem
import com.example.myapplication2.model.CurrentWeather
import com.example.myapplication2.model.Forecast
import java.time.LocalDateTime

/**
 * Testlerde gerçek repository yerine kullanılan sahteler.
 *
 * Repository'yi arayüz olarak tanımlamamızın sebebi tam olarak budur: ViewModel
 * testleri internete çıkmadan, istenen her sonucu (başarı / boş / hata)
 * kolayca üretebilir.
 */

/**
 * @param result Her çağrıda dönecek liste.
 * @param error Doluysa [search] bu hatayı fırlatır.
 */
class FakeCityRepository(
    var result: List<City> = emptyList(),
    var error: Throwable? = null
) : CityRepository {

    /** Son aranan metin; ViewModel'in doğru sorguyu ilettiğini doğrulamak için. */
    var lastQuery: String? = null
        private set

    /** Kaç kez arama yapıldığı; "Tekrar dene" davranışını doğrulamak için. */
    var callCount: Int = 0
        private set

    override suspend fun search(query: String): List<City> {
        lastQuery = query
        callCount++
        error?.let { throw it }
        return result
    }
}

/**
 * Türkiye özetinin sahtesi. Varsayılanı tek bir şehir döndürür; test istediği
 * listeyi veya hatayı yazar.
 */
class FakeTurkeyOverview(
    var result: List<CityListItem> = listOf(CityListItem(sampleCity())),
    var error: Throwable? = null
) : TurkeyOverviewSource {

    var callCount: Int = 0
        private set

    override suspend fun load(): List<CityListItem> {
        callCount++
        error?.let { throw it }
        return result
    }
}

/**
 * Yurt dışı arama sonuçlarının anlık havası. Türkiye şehirleri bu kaynağa gitmez.
 */
class FakeCurrentWeather(
    var result: List<CityListItem> = emptyList(),
    var error: Throwable? = null
) : CurrentWeatherSource {

    var callCount: Int = 0
        private set

    override suspend fun load(cities: List<City>): List<CityListItem> {
        callCount++
        error?.let { throw it }
        return result
    }
}

/** @param error Doluysa [getForecast] bu hatayı fırlatır. */
class FakeWeatherRepository(
    var result: Forecast = sampleForecast(),
    var error: Throwable? = null
) : WeatherRepository {

    var callCount: Int = 0
        private set

    override suspend fun getForecast(latitude: Double, longitude: Double): Forecast {
        callCount++
        error?.let { throw it }
        return result
    }
}

/** Testlerde tekrar tekrar yazmamak için hazır bir şehir. */
fun sampleCity(id: Long = 323786L, name: String = "Ankara") = City(
    id = id,
    name = name,
    latitude = 39.91987,
    longitude = 32.85427,
    countryCode = "TR",
    country = "Türkiye",
    admin1 = "Ankara"
)

/** Testlerde tekrar tekrar yazmamak için hazır bir tahmin. */
fun sampleForecast() = Forecast(
    current = CurrentWeather(
        temperatureC = 21.0,
        weatherCode = 0,
        observedAt = LocalDateTime.parse("2026-09-24T11:30")
    ),
    hourly = emptyList(),
    daily = emptyList()
)
