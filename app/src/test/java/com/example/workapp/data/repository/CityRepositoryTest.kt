package com.example.workapp.data.repository

import com.example.workapp.data.model.CityCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CityRepositoryTest {

    private lateinit var repository: CityRepository

    @Before
    fun setUp() {
        repository = CityRepositoryImpl()
    }

    @Test
    fun getAllCities_returnsNonEmptyListWithUSAndMexicoCities() = runTest {
        val cities = repository.getAllCities().first()
        assertTrue("Cities list should not be empty", cities.isNotEmpty())
        
        val usCities = cities.filter { it.country == "US" }
        val mxCities = cities.filter { it.country == "MX" }

        assertTrue("US cities should be present", usCities.isNotEmpty())
        assertTrue("Mexico cities should be present", mxCities.isNotEmpty())
    }

    @Test
    fun getCitiesByCountry_returnsCorrectCitiesForUS() = runTest {
        val usCities = repository.getCitiesByCountry("US").first()
        assertTrue(usCities.all { it.country == "US" })
        assertTrue(usCities.any { it.name == "New York City" })
    }

    @Test
    fun getCitiesByCountry_returnsCorrectCitiesForMexico() = runTest {
        val mxCities = repository.getCitiesByCountry("MX").first()
        assertTrue(mxCities.all { it.country == "MX" })
        assertTrue(mxCities.any { it.name == "Mexico City" })
    }

    @Test
    fun getCityById_returnsCorrectCity() = runTest {
        val city = repository.getCityById("us-nyc").first()
        assertNotNull(city)
        assertEquals("New York City", city?.name)
        assertEquals(40.7128, city?.latitude ?: 0.0, 0.001)
        assertEquals(-74.0060, city?.longitude ?: 0.0, 0.001)
    }

    @Test
    fun getCitiesByCategory_returnsMatchingCategoryCities() = runTest {
        val coastalCities = repository.getCitiesByCategory(CityCategory.COASTAL).first()
        assertTrue(coastalCities.all { it.category == CityCategory.COASTAL })
        assertTrue(coastalCities.any { it.name == "Miami" || it.name == "Puerto Vallarta" })
    }

    @Test
    fun searchCities_matchesNameOrAttraction() = runTest {
        val searchResults = repository.searchCities("Cancún").first()
        assertEquals(1, searchResults.size)
        assertEquals("Cancún", searchResults.first().name)

        val attractionResults = repository.searchCities("Frida Kahlo").first()
        assertTrue(attractionResults.any { it.name == "Mexico City" })
    }
}
