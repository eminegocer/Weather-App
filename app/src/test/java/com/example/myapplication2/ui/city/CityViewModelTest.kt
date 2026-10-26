package com.example.myapplication2.ui.city

import com.example.myapplication2.model.CityListItem
import com.example.myapplication2.testing.FakeCityRepository
import com.example.myapplication2.testing.FakeCurrentWeather
import com.example.myapplication2.testing.FakeTurkeyOverview
import com.example.myapplication2.testing.MainDispatcherRule
import com.example.myapplication2.testing.sampleCity
import com.example.myapplication2.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Şehir listesinin açılış özetini ve arama durumlarını doğrular.
 *
 * Arama kutusu boşken Türkiye özeti yüklenir. Her harf geocoding araması
 * başlatır; kutu temizlenince özet geri gelir.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCityRepository()
    private val overview = FakeTurkeyOverview()
    private val currentWeather = FakeCurrentWeather()

    private fun createViewModel() = CityViewModel(repository, overview, currentWeather)

    @Test
    fun `baslangicta Loading gosterilir`() = runTest {
        val viewModel = createViewModel()

        assertEquals(LoadState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `acilista turkiye listesi yuklenir ve arama kutusu bostur`() = runTest {
        overview.result = listOf(CityListItem(sampleCity(name = "Adana"), temperatureC = 19.0, weatherCode = 1))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("", viewModel.query.value)
        assertEquals(1, overview.callCount)
        assertEquals(0, repository.callCount)
        val state = viewModel.uiState.value as LoadState.Success
        assertTrue(state.data.turkeyOverview)
        assertEquals("Adana", state.data.items.single().city.name)
        assertEquals(19.0, state.data.items.single().temperatureC!!, 0.01)
    }

    @Test
    fun `bos arama sonucu Empty durumuna cevrilir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        repository.result = emptyList()

        viewModel.onQueryChange("xyz")
        viewModel.search()
        advanceUntilIdle()

        assertEquals(LoadState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `baglanti hatasi anlasilir mesaja cevrilir`() = runTest {
        overview.error = IOException("no network")

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Error bekleniyordu, gelen: $state", state is LoadState.Error)
        assertEquals("İnternet bağlantını kontrol et.", (state as LoadState.Error).message)
    }

    @Test
    fun `sunucu hatasi farkli mesaj gosterir`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        repository.error = HttpException(
            Response.error<Unit>(500, "".toResponseBody("application/json".toMediaType()))
        )

        viewModel.onQueryChange("Rize")
        viewModel.search()
        advanceUntilIdle()

        assertEquals(
            "Sunucu şu an cevap vermiyor.",
            (viewModel.uiState.value as LoadState.Error).message
        )
    }

    @Test
    fun `her harf yazildiginda o metin aranir`() = runTest {
        repository.result = listOf(sampleCity(name = "Rize"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("R")
        advanceUntilIdle()
        viewModel.onQueryChange("Ri")
        advanceUntilIdle()

        assertEquals("Ri", viewModel.query.value)
        assertEquals("Ri", repository.lastQuery)
        assertEquals(2, repository.callCount)
        assertEquals(0, currentWeather.callCount)
        val state = viewModel.uiState.value as LoadState.Success
        assertEquals(false, state.data.turkeyOverview)
    }

    @Test
    fun `turkiye disindaki sehir icin derece istenir`() = runTest {
        val zadar = sampleCity(id = 9, name = "Zadar").copy(
            countryCode = "HR",
            country = "Hırvatistan",
            latitude = 44.1194,
            longitude = 15.2314
        )
        repository.result = listOf(zadar)
        currentWeather.result = listOf(
            CityListItem(city = zadar, temperatureC = 16.4, weatherCode = 2)
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("ist")
        advanceUntilIdle()

        assertEquals(1, currentWeather.callCount)
        val item = (viewModel.uiState.value as LoadState.Success).data.items.single()
        assertEquals(16.4, item.temperatureC!!, 0.01)
        assertEquals(2, item.weatherCode)
    }

    @Test
    fun `search guncel metinle istek atar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        repository.result = listOf(sampleCity(name = "Rize"))
        viewModel.onQueryChange("Rize")
        viewModel.search()
        advanceUntilIdle()

        assertEquals("Rize", repository.lastQuery)
        val state = viewModel.uiState.value as LoadState.Success
        assertEquals(false, state.data.turkeyOverview)
    }

    @Test
    fun `arama kutusu temizlenince turkiye listesine donulur`() = runTest {
        val adana = CityListItem(sampleCity(id = 1, name = "Adana"), temperatureC = 20.0, weatherCode = 0)
        overview.result = listOf(adana)
        val viewModel = createViewModel()
        advanceUntilIdle()

        repository.result = listOf(sampleCity(id = 9, name = "Rize"))
        viewModel.onQueryChange("Rize")
        viewModel.search()
        advanceUntilIdle()

        viewModel.onQueryChange("")

        val restored = viewModel.uiState.value as LoadState.Success
        assertEquals("Adana", restored.data.items.single().city.name)
        assertTrue(restored.data.turkeyOverview)
        assertEquals(1, overview.callCount)
    }

    @Test
    fun `tekrar dene yeni bir istek baslatir ve basarili olabilir`() = runTest {
        overview.error = IOException("no network")
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is LoadState.Error)

        overview.error = null
        overview.result = listOf(CityListItem(sampleCity(name = "Adana"), temperatureC = 18.0, weatherCode = 0))
        viewModel.search()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoadState.Success)
        assertEquals(2, overview.callCount)
    }
}
