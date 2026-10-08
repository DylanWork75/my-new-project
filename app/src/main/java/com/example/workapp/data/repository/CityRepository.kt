package com.example.workapp.data.repository

import com.example.workapp.data.model.City
import com.example.workapp.data.model.CityCategory
import kotlinx.coroutines.flow.Flow

interface CityRepository {
    fun getAllCities(): Flow<List<City>>
    fun getCityById(id: String): Flow<City?>
    fun getCitiesByCountry(countryCode: String): Flow<List<City>>
    fun getCitiesByCategory(category: CityCategory): Flow<List<City>>
    fun searchCities(query: String): Flow<List<City>>
}
