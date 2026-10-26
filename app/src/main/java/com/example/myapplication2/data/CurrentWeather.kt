package com.example.myapplication2.data

import com.example.myapplication2.model.City
import com.example.myapplication2.model.CityListItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Arama sonucunda Türkiye dışında kalan şehirlerin anlık havası.
 * Türkiye illerinde kart harita gösterdiği için bu kaynak onlara bakmaz.
 */
fun interface CurrentWeatherSource {
    suspend fun load(cities: List<City>): List<CityListItem>
}

/**
 * Verilen konumların anlık sıcaklığını ve hava kodunu Open-Meteo'dan alır.
 * Arama en fazla on şehir döndürdüğü için tek istek yeter.
 */
class OpenMeteoCurrentWeather(
    private val api: ForecastApi = ApiClient.forecastApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CurrentWeatherSource {

    override suspend fun load(cities: List<City>): List<CityListItem> = withContext(ioDispatcher) {
        if (cities.isEmpty()) return@withContext emptyList()
        val forecasts = if (cities.size == 1) {
            val city = cities.first()
            listOf(api.getCurrent(latitude = city.latitude, longitude = city.longitude))
        } else {
            api.getCurrentForLocations(
                latitude = cities.joinToString(",") { it.latitude.toString() },
                longitude = cities.joinToString(",") { it.longitude.toString() }
            )
        }
        if (forecasts.size != cities.size) {
            throw IllegalStateException(
                "Expected ${cities.size} forecasts but received ${forecasts.size}"
            )
        }
        cities.zip(forecasts) { city, forecast ->
            CityListItem(
                city = city,
                temperatureC = forecast.current?.temperature,
                weatherCode = forecast.current?.weatherCode
            )
        }
    }
}
