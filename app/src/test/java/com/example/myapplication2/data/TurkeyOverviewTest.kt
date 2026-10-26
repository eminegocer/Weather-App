package com.example.myapplication2.data

import com.example.myapplication2.model.City
import com.example.myapplication2.testing.createApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Türkiye özetinin sütunları şehir sırasıyla eşlediğini doğrular.
 */
class TurkeyOverviewTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ForecastApi

    private val ankara = city(6, "Ankara", 39.9334, 32.8597)
    private val izmir = city(35, "İzmir", 38.4237, 27.1428)

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = server.createApi()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `coklu yanit sehir sirasini bozmaz`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                [
                  {"latitude":39.9334,"longitude":32.8597,"current":{"time":"2026-10-25T12:00","temperature_2m":18.2,"weather_code":0}},
                  {"latitude":38.4237,"longitude":27.1428,"current":{"time":"2026-10-25T12:00","temperature_2m":22.4,"weather_code":61}}
                ]
                """.trimIndent()
            )
        )

        val items = overview(listOf(ankara, izmir)).load()

        assertEquals("Ankara", items[0].city.name)
        assertEquals(18.2, items[0].temperatureC!!, 0.01)
        assertEquals(0, items[0].weatherCode)
        assertEquals("İzmir", items[1].city.name)
        assertEquals(22.4, items[1].temperatureC!!, 0.01)
        assertEquals(61, items[1].weatherCode)

        val path = checkNotNull(server.takeRequest().path)
        assertTrue(path.startsWith("/v1/forecast"))
        assertTrue(path.contains("39.9334"))
        assertTrue(path.contains("38.4237"))
        assertTrue(path.contains("temperature_2m"))
    }

    @Test
    fun `tek sehir nesne yanitini okur`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"latitude":39.9334,"longitude":32.8597,"current":{"time":"2026-10-25T12:00","temperature_2m":11.0,"weather_code":3}}"""
            )
        )

        val items = overview(listOf(ankara)).load()

        assertEquals(1, items.size)
        assertEquals(11.0, items.single().temperatureC!!, 0.01)
        assertEquals(3, items.single().weatherCode)
    }

    @Test
    fun `eksik kayit hata firlatir`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """[{"latitude":39.9334,"longitude":32.8597,"current":{"time":"2026-10-25T12:00","temperature_2m":18.0,"weather_code":0}}]"""
            )
        )

        val error = runCatching { overview(listOf(ankara, izmir)).load() }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
    }

    private fun overview(cities: List<City>) = OpenMeteoTurkeyOverview(
        api = api,
        ioDispatcher = Dispatchers.Unconfined,
        cities = cities,
        chunkSize = 20
    )

    private fun city(plate: Int, name: String, latitude: Double, longitude: Double) = City(
        id = plate.toLong(),
        name = name,
        latitude = latitude,
        longitude = longitude,
        countryCode = "TR",
        country = "Türkiye",
        admin1 = null
    )
}
