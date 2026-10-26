package com.example.myapplication2.data

import com.example.myapplication2.model.City
import com.example.myapplication2.model.CityListItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Arama kutusu boşken gösterilen Türkiye hava listesinin kaynağı.
 */
fun interface TurkeyOverviewSource {
    /** İlleri Türkçe alfabetik sırada, anlık sıcaklık ve hava koduyla döndürür. */
    suspend fun load(): List<CityListItem>
}

/**
 * 81 ilin anlık havasını Open-Meteo'dan alır.
 *
 * İller parçalara bölünür; her parça tek HTTP isteğidir. Tek şehirlik parçada
 * servis dizi yerine nesne döndürdüğü için o çağrı ayrı metottadır.
 */
class OpenMeteoTurkeyOverview(
    private val api: ForecastApi = ApiClient.forecastApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val cities: List<City> = TurkeyCities.all,
    private val chunkSize: Int = DEFAULT_CHUNK_SIZE
) : TurkeyOverviewSource {

    override suspend fun load(): List<CityListItem> = withContext(ioDispatcher) {
        // 81 ili tek dev URL'ye sığdırmak yerine 27'şerli parçalara bölüp aynı anda istiyoruz.
        // awaitAll parça sırasını korur; Adana hâlâ listenin başında kalır.
        coroutineScope {
            cities.chunked(chunkSize)
                .map { chunk -> async { fetchChunk(chunk) } }
                .awaitAll()
                .flatten()
        }
    }

    private suspend fun fetchChunk(chunk: List<City>): List<CityListItem> {
        // Tek konumda servis JSON nesnesi döner, birden fazla konumda dizi. İkisini de listeye çeviriyoruz.
        val forecasts = if (chunk.size == 1) {
            val city = chunk.first()
            listOf(api.getCurrent(latitude = city.latitude, longitude = city.longitude))
        } else {
            api.getCurrentForLocations(
                latitude = chunk.joinToString(",") { it.latitude.toString() },
                longitude = chunk.joinToString(",") { it.longitude.toString() }
            )
        }
        // Eksik kayıt olursa sessizce kaydırmak yerine hata fırlatıyoruz; ViewModel bunu Error yapar.
        if (forecasts.size != chunk.size) {
            throw IllegalStateException(
                "Expected ${chunk.size} city forecasts but received ${forecasts.size}"
            )
        }
        // zip: 1. şehir ↔ 1. cevap. Sıcaklık veya kod gelmezse kart yine durur, derece boş kalır.
        return chunk.zip(forecasts) { city, forecast ->
            CityListItem(
                city = city,
                temperatureC = forecast.current?.temperature,
                weatherCode = forecast.current?.weatherCode
            )
        }
    }

    private companion object {
        const val DEFAULT_CHUNK_SIZE = 27
    }
}
