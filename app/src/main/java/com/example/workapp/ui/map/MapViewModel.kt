package com.example.workapp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workapp.data.model.City
import com.example.workapp.data.model.CityCategory
import com.example.workapp.data.repository.CityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: CityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState(isLoading = true))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadCities()
    }

    fun loadCities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAllCities().collect { cities ->
                _uiState.update { state ->
                    val filtered = filterCities(
                        cities = cities,
                        category = state.selectedCategoryFilter,
                        country = state.selectedCountryFilter,
                        query = state.searchQuery
                    )
                    state.copy(
                        cities = cities,
                        filteredCities = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectCity(city: City?) {
        _uiState.update { state ->
            val newCameraEvent = city?.let {
                LatLngTarget(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    zoom = 9.5f
                )
            }
            state.copy(
                selectedCity = city,
                cameraMoveEvent = newCameraEvent ?: state.cameraMoveEvent
            )
        }
    }

    fun selectCityById(id: String?) {
        if (id == null) {
            selectCity(null)
            return
        }
        val city = _uiState.value.cities.find { it.id.equals(id, ignoreCase = true) }
        selectCity(city)
    }

    fun setCategoryFilter(category: CityCategory?) {
        _uiState.update { state ->
            val filtered = filterCities(
                cities = state.cities,
                category = category,
                country = state.selectedCountryFilter,
                query = state.searchQuery
            )
            val selectedStillValid = state.selectedCity?.let { selected ->
                filtered.any { it.id == selected.id }
            } == true

            state.copy(
                selectedCategoryFilter = category,
                filteredCities = filtered,
                selectedCity = if (selectedStillValid) state.selectedCity else null
            )
        }
    }

    fun setCountryFilter(countryCode: String?) {
        _uiState.update { state ->
            val filtered = filterCities(
                cities = state.cities,
                category = state.selectedCategoryFilter,
                country = countryCode,
                query = state.searchQuery
            )
            val selectedStillValid = state.selectedCity?.let { selected ->
                filtered.any { it.id == selected.id }
            } == true

            state.copy(
                selectedCountryFilter = countryCode,
                filteredCities = filtered,
                selectedCity = if (selectedStillValid) state.selectedCity else null
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = filterCities(
                cities = state.cities,
                category = state.selectedCategoryFilter,
                country = state.selectedCountryFilter,
                query = query
            )
            state.copy(
                searchQuery = query,
                filteredCities = filtered
            )
        }
    }

    fun clearSelection() {
        selectCity(null)
    }

    fun resetCameraToNorthAmerica() {
        _uiState.update { state ->
            state.copy(
                cameraMoveEvent = LatLngTarget(
                    latitude = 32.0,
                    longitude = -100.0,
                    zoom = 4.0f
                )
            )
        }
    }

    private fun filterCities(
        cities: List<City>,
        category: CityCategory?,
        country: String?,
        query: String
    ): List<City> {
        return cities.filter { city ->
            val matchesCategory = category == null || city.category == category
            val matchesCountry = country == null || city.country.equals(country, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    city.name.contains(query, ignoreCase = true) ||
                    city.stateOrRegion.contains(query, ignoreCase = true) ||
                    city.countryName.contains(query, ignoreCase = true)
            matchesCategory && matchesCountry && matchesQuery
        }
    }
}
