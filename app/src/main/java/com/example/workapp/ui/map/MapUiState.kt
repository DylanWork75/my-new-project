package com.example.workapp.ui.map

import com.example.workapp.data.model.City
import com.example.workapp.data.model.CityCategory

data class LatLngTarget(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float = 10f,
    val eventId: Long = System.currentTimeMillis()
)

data class MapUiState(
    val cities: List<City> = emptyList(),
    val filteredCities: List<City> = emptyList(),
    val selectedCity: City? = null,
    val selectedCategoryFilter: CityCategory? = null,
    val selectedCountryFilter: String? = null, // "US", "MX", or null for all
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val cameraMoveEvent: LatLngTarget? = null
)
