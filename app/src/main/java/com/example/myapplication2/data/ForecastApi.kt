package com.example.myapplication2.data

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Hava tahmini servisinin menüsü.
 *
 * [CityApi] ile aynı mantık, farklı sunucu: arama `geocoding-api.open-meteo.com`,
 * tahmin ise `api.open-meteo.com` adresinden gelir. Bu yüzden [ApiClient] içinde
 * iki ayrı Retrofit kurulumu vardır.
 *
 * Örnek istek:
 * `GET https://api.open-meteo.com/v1/forecast?latitude=39.92&longitude=32.85&current=...&timezone=auto`
 */
interface ForecastApi {

    /**
     * Tek bir konumun anlık durumunu, saatlik ve günlük tahminini getirir.
     *
     * @param current Anlık olarak istenen değişkenler, virgülle ayrılır.
     * @param hourly Saatlik istenen değişkenler.
     * @param daily Günlük istenen değişkenler.
     * @param forecastDays Kaç günlük tahmin istendiği.
     * @param timezone "auto" dersek sunucu zamanları şehrin yerel saatine göre gönderir.
     */
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("forecast_days") forecastDays: Int = FORECAST_DAYS,
        @Query("timezone") timezone: String = "auto"
    ): ForecastResponse

    /**
     * Tek bir konumun yalnızca anlık durumunu ister. Liste özetinde saatlik ve
     * günlük bloklara ihtiyaç yoktur.
     */
    @GET("v1/forecast")
    suspend fun getCurrent(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_SUMMARY_FIELDS,
        @Query("timezone") timezone: String = "auto"
    ): ForecastResponse

    /**
     * Birden çok konumun anlık durumunu tek istekte ister.
     *
     * Enlem ve boylam virgülle ayrılır ve aynı sırada olmalıdır. Open-Meteo bu
     * çağrıya JSON dizisi döner; dizi sırası koordinat sırasıyla aynıdır.
     */
    @GET("v1/forecast")
    suspend fun getCurrentForLocations(
        @Query(value = "latitude", encoded = true) latitude: String,
        @Query(value = "longitude", encoded = true) longitude: String,
        @Query("current") current: String = CURRENT_SUMMARY_FIELDS,
        @Query("timezone") timezone: String = "auto"
    ): List<ForecastResponse>

    companion object {
        const val CURRENT_FIELDS =
            "temperature_2m,weather_code,apparent_temperature,relative_humidity_2m,wind_speed_10m"
        const val CURRENT_SUMMARY_FIELDS = "temperature_2m,weather_code"
        const val HOURLY_FIELDS = "temperature_2m,weather_code,precipitation_probability"
        const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max"
        const val FORECAST_DAYS = 7
    }
}
