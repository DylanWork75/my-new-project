package com.example.workapp.data.repository

import com.example.workapp.data.model.City
import com.example.workapp.data.model.CityCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CityRepositoryImpl(
    private val dataset: List<City> = CityDataset.CITIES
) : CityRepository {

    override fun getAllCities(): Flow<List<City>> {
        return flowOf(dataset)
    }

    override fun getCityById(id: String): Flow<City?> {
        val city = dataset.find { it.id.equals(id, ignoreCase = true) }
        return flowOf(city)
    }

    override fun getCitiesByCountry(countryCode: String): Flow<List<City>> {
        val filtered = dataset.filter { it.country.equals(countryCode, ignoreCase = true) }
        return flowOf(filtered)
    }

    override fun getCitiesByCategory(category: CityCategory): Flow<List<City>> {
        val filtered = dataset.filter { it.category == category }
        return flowOf(filtered)
    }

    override fun searchCities(query: String): Flow<List<City>> {
        if (query.isBlank()) {
            return flowOf(dataset)
        }
        val trimmed = query.trim()
        val filtered = dataset.filter { city ->
            city.name.contains(trimmed, ignoreCase = true) ||
                    city.stateOrRegion.contains(trimmed, ignoreCase = true) ||
                    city.countryName.contains(trimmed, ignoreCase = true) ||
                    city.description.contains(trimmed, ignoreCase = true) ||
                    city.popularAttractions.any { attraction -> attraction.contains(trimmed, ignoreCase = true) }
        }
        return flowOf(filtered)
    }
}
