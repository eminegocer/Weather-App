package com.example.myapplication2.ui.city

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication2.R
import com.example.myapplication2.model.City
import com.example.myapplication2.model.CityListItem
import com.example.myapplication2.ui.LoadState
import com.example.myapplication2.ui.component.EmptyView
import com.example.myapplication2.ui.component.ErrorView
import com.example.myapplication2.ui.component.ShimmerList
import com.example.myapplication2.ui.theme.HavaTheme
import com.example.myapplication2.ui.theme.havaScreenBrush

/**
 * Şehir arama ekranının "bağlayıcı" katmanı.
 *
 * Tek işi ViewModel'i edinip state'i toplamak ve [CityScreen]'e aktarmaktır.
 * Bu ayrım sayesinde [CityScreen] ViewModel tanımaz: @Preview'da ve testte
 * elle state verilerek çalıştırılabilir.
 *
 * `collectAsStateWithLifecycle`: StateFlow'u Compose state'ine
 * çevirir ve uygulama arka plandayken dinlemeyi durdurur.
 */
@Composable
fun CityRoute(onCityClick: (City) -> Unit, modifier: Modifier = Modifier, viewModel: CityViewModel = viewModel()) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CityScreen(
        query = query,
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onCityClick = onCityClick,
        modifier = modifier
    )
}

/**
 * Şehir arama ekranı: `when` ile her duruma bir görünüm.
 *
 * State tutmaz, yalnızca aldığı değerleri çizer ve olayları yukarı bildirir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityScreen(
    query: String,
    uiState: LoadState<CityListContent>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCityClick: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(havaScreenBrush()),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Dört durumun tamamı burada karşılanır. `sealed` olduğu için
                // bir dalı unutursak Kotlin derleme hatası verir.
                when (uiState) {
                    LoadState.Loading -> ShimmerList()
                    is LoadState.Success -> CityList(content = uiState.data, onCityClick = onCityClick)
                    LoadState.Empty -> EmptyView(
                        title = "Şehir bulunamadı",
                        message = "Yazımı kontrol et ya da başka bir şehir dene."
                    )
                    is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onSearch)
                }
            }
        }
    }
}

/**
 * Arama kutusu.
 *
 * State tutmaz: değeri yukarıdan alır, her değişikliği yukarıya bildirir.
 * Her harf [onQueryChange] ile aranır. Klavyedeki Ara tuşu aynı aramayı bir kez daha ister.
 */
@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit, modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        label = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            // Temizle düğmesi yalnızca kutu doluyken görünür.
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_clear))
                }
            }
        },
        singleLine = true,
        // Enter, yazılan metni bir kez daha arar. Liste zaten her harfte güncellenir.
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch()
            }
        )
    )
}

/**
 * Kaydırılabilir şehir listesi.
 *
 * `LazyColumn` tembeldir: yüzlerce şehir olsa da yalnızca ekranda görünen satırlar
 * çizilir, kaybolanlar geri dönüştürülür.
 *
 * `key = { it.city.id }`: listedeki öğeler değiştiğinde Compose hangi satırın hangisi
 * olduğunu bilir; gereksiz yeniden çizim yapmaz.
 */
@Composable
private fun CityList(content: CityListContent, onCityClick: (City) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (content.turkeyOverview) {
            item(key = "turkey-header") {
                Text(
                    text = stringResource(R.string.turkey_overview_title),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        items(items = content.items, key = { it.city.id }) { item ->
            CityCard(
                city = item.city,
                temperatureC = item.temperatureC,
                weatherCode = item.weatherCode,
                onClick = { onCityClick(item.city) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenPreview() {
    HavaTheme {
        CityScreen(
            query = "",
            uiState = LoadState.Success(
                CityListContent(
                    items = listOf(
                        CityListItem(
                            city = City(1, "Adana", 37.0, 35.3213, "TR", "Türkiye", null),
                            temperatureC = 29.0,
                            weatherCode = 0
                        ),
                        CityListItem(
                            city = City(6, "Ankara", 39.9334, 32.8597, "TR", "Türkiye", null),
                            temperatureC = 18.0,
                            weatherCode = 61
                        )
                    ),
                    turkeyOverview = true
                )
            ),
            onQueryChange = {},
            onSearch = {},
            onCityClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenEmptyPreview() {
    HavaTheme {
        CityScreen(
            query = "xyz",
            uiState = LoadState.Empty,
            onQueryChange = {},
            onSearch = {},
            onCityClick = {}
        )
    }
}
