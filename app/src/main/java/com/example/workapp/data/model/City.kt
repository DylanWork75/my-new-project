package com.example.workapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class City(
    val id: String,
    val name: String,
    val country: String, // "US" or "MX"
    val countryName: String, // "United States" or "Mexico"
    val stateOrRegion: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val category: CityCategory,
    val iconIndicator: String, // Icon key or material icon identifier
    val population: Long,
    val rating: Double,
    val popularAttractions: List<String> = emptyList(),
    val imageUrl: String? = null,
    val timeZone: String = ""
)
