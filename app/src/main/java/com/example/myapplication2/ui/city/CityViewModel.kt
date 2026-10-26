package com.example.myapplication2.ui.city

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication2.data.CityRepository
import com.example.myapplication2.data.CurrentWeatherSource
import com.example.myapplication2.data.OpenMeteoCityRepository
import com.example.myapplication2.data.OpenMeteoCurrentWeather
import com.example.myapplication2.data.OpenMeteoTurkeyOverview
import com.example.myapplication2.data.TurkeyOverviewSource
import com.example.myapplication2.model.CityListItem
import com.example.myapplication2.ui.LoadState
import com.google.gson.JsonSyntaxException
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Şehir listesi ekranının beyni.
 *
 * Arama kutusu boşken 81 il, Türkçe alfabetik sırada ve anlık havasıyla durur.
 * Kullanıcı her harf yazdığında o metin aranır; Enter'a basmak gerekmez.
 * Kutu temizlenince Türkiye listesi, yeniden indirilmeden geri gelir.
 *
 * @param repository Varsayılanı gerçek şehir arama servisi.
 * @param turkeyOverview Varsayılanı 81 ilin anlık hava özeti.
 *   Tüm parametrelerin varsayılanı olduğu için ekranlarda `viewModel()` parametresiz kurucuyu kullanır.
 */
class CityViewModel(
    private val repository: CityRepository = OpenMeteoCityRepository(),
    private val turkeyOverview: TurkeyOverviewSource = OpenMeteoTurkeyOverview(),
    private val currentWeather: CurrentWeatherSource = OpenMeteoCurrentWeather()
) : ViewModel() {

    // Arama kutusundaki metin. Dışarıya yalnızca okunur hâli açılır.
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // Ekranın dört durumundan biri: yükleniyor, dolu, boş, hata.
    private val _uiState = MutableStateFlow<LoadState<CityListContent>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<CityListContent>> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    // Her yeni istek bu sayıyı artırır. Eski isteğin cevabı gelirse ekrana yazılmaz.
    private var generation = 0
    // Türkiye listesi bir kez inince burada durur. Kutu temizlenince yeniden indirmeyiz.
    private var cachedOverview: CityListContent? = null
    // true ise ekranda arama sonucu vardır; kutu silinince Türkiye listesine dönülür.
    private var showingSearch = false

    init {
        // Uygulama açılınca kutu boştur ve 81 ilin havası istenir. Otomatik "İstanbul" araması yok.
        loadTurkeyOverview()
    }

    /**
     * Arama kutusunun her değişiminde çağrılır.
     * Metin doluysa o harfle hemen aranır. Metin silinirse Türkiye listesine dönülür.
     */
    fun onQueryChange(newQuery: String) {
        val previous = _query.value
        _query.value = newQuery
        val text = newQuery.trim()
        if (text.isEmpty()) {
            // Kutu boşaldı: bekleyen aramayı iptal et, illeri geri getir.
            if (previous.isNotBlank() && showingSearch) {
                showCachedOverviewOrLoad()
            }
            return
        }
        // Sonuç zaten ekrandaysa her harfte iskelete dönmeyiz; liste yeni cevapla yerinde değişir.
        val keepCurrentList = showingSearch && _uiState.value is LoadState.Success
        searchCities(text, showLoading = !keepCurrentList)
    }

    /**
     * Klavyedeki Ara tuşu ve hata ekranındaki "Tekrar dene" buraya bağlıdır.
     * Harf yazmak zaten arar; bu fonksiyon aynı aramayı yeniler.
     * Kutu boşsa Türkiye listesi yenilenir.
     */
    fun search() {
        val text = _query.value.trim()
        if (text.isEmpty()) {
            loadTurkeyOverview()
        } else {
            searchCities(text)
        }
    }

    private fun showCachedOverviewOrLoad() {
        val cached = cachedOverview
        if (cached != null) {
            loadJob?.cancel()
            generation += 1
            showingSearch = false
            _uiState.value = LoadState.Success(cached)
        } else {
            loadTurkeyOverview()
        }
    }

    private fun loadTurkeyOverview() {
        showingSearch = false
        launchRequest(showLoading = true) {
            val content = CityListContent(
                items = turkeyOverview.load(),
                turkeyOverview = true
            )
            cachedOverview = content
            LoadState.Success(content)
        }
    }

    private fun searchCities(text: String, showLoading: Boolean = true) {
        showingSearch = true
        launchRequest(showLoading) {
            val cities = repository.search(text)
            if (cities.isEmpty()) {
                LoadState.Empty
            } else {
                // Türkiye'dekiler haritada durur. Dışarıdakilerin derecesi ayrıca istenir.
                val abroad = cities.filterNot { it.inTurkey }
                val weatherById = if (abroad.isEmpty()) {
                    emptyMap()
                } else {
                    currentWeather.load(abroad).associateBy { it.city.id }
                }
                LoadState.Success(
                    CityListContent(
                        items = cities.map { city ->
                            if (city.inTurkey) {
                                CityListItem(city = city)
                            } else {
                                weatherById[city.id] ?: CityListItem(city = city)
                            }
                        },
                        turkeyOverview = false
                    )
                )
            }
        }
    }

    private fun launchRequest(
        showLoading: Boolean = true,
        block: suspend () -> LoadState<CityListContent>
    ) {
        // Önceki istek yolda kaldıysa iptal et. Yoksa eski cevap yenisinin üstüne yazılabilir.
        loadJob?.cancel()
        val requestId = ++generation
        loadJob = viewModelScope.launch {
            if (showLoading) _uiState.value = LoadState.Loading
            val result = try {
                block()
            } catch (e: IOException) {
                Log.e(TAG, "İstek başarısız: bağlantı hatası", e)
                LoadState.Error("İnternet bağlantını kontrol et.")
            } catch (e: HttpException) {
                Log.e(TAG, "İstek başarısız: HTTP ${e.code()}", e)
                LoadState.Error("Sunucu şu an cevap vermiyor.")
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "İstek başarısız: yanıt çözümlenemedi", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            } catch (e: IllegalStateException) {
                Log.e(TAG, "İstek başarısız: eksik veri", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            }
            // Bu cevap beklerken daha yeni bir istek başladıysa sonucu çöpe at.
            if (requestId != generation) return@launch
            _uiState.value = result
        }
    }

    private companion object {
        const val TAG = "CityViewModel"
    }
}
