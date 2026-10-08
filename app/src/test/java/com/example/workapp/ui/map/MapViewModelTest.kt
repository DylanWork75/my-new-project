package com.example.workapp.ui.map

import com.example.workapp.data.model.CityCategory
import com.example.workapp.data.repository.CityRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: CityRepositoryImpl
    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = CityRepositoryImpl()
        viewModel = MapViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadCities_initialState_containsUSAndMexicoCities() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Cities should not be empty", state.cities.isNotEmpty())
        assertTrue("Filtered cities should not be empty", state.filteredCities.isNotEmpty())

        val hasUS = state.cities.any { it.country == "US" }
        val hasMX = state.cities.any { it.country == "MX" }
        assertTrue("Dataset should contain US cities", hasUS)
        assertTrue("Dataset should contain Mexico cities", hasMX)
    }

    @Test
    fun selectCity_emitsSelectedCityStateAndCameraEvent() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val targetCity = viewModel.uiState.value.cities.first { it.country == "MX" }
        viewModel.selectCity(targetCity)

        val state = viewModel.uiState.value
        assertEquals("Selected city should match emitted city", targetCity, state.selectedCity)
        assertNotNull("Camera move event should be emitted on city selection", state.cameraMoveEvent)
        assertEquals(targetCity.latitude, state.cameraMoveEvent!!.latitude, 0.001)
        assertEquals(targetCity.longitude, state.cameraMoveEvent!!.longitude, 0.001)
    }

    @Test
    fun selectCityById_emitsCorrectSelectedCity() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val targetCity = viewModel.uiState.value.cities.first { it.id == "us-nyc" }
        viewModel.selectCityById("us-nyc")

        val state = viewModel.uiState.value
        assertEquals("Selected city should match NYC", targetCity, state.selectedCity)
    }

    @Test
    fun setCountryFilter_filtersCitiesByCountryCode() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // Filter US
        viewModel.setCountryFilter("US")
        val usState = viewModel.uiState.value
        assertTrue("Filtered cities should all be US", usState.filteredCities.all { it.country == "US" })

        // Filter MX
        viewModel.setCountryFilter("MX")
        val mxState = viewModel.uiState.value
        assertTrue("Filtered cities should all be MX", mxState.filteredCities.all { it.country == "MX" })

        // Clear country filter
        viewModel.setCountryFilter(null)
        val allState = viewModel.uiState.value
        assertEquals("Clearing country filter restores all cities", allState.cities.size, allState.filteredCities.size)
    }

    @Test
    fun setCategoryFilter_filtersCitiesByCategory() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setCategoryFilter(CityCategory.COASTAL)
        val state = viewModel.uiState.value
        assertTrue(
            "Filtered cities should match COASTAL category",
            state.filteredCities.all { it.category == CityCategory.COASTAL }
        )
    }

    @Test
    fun updateSearchQuery_filtersCitiesByNameOrState() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateSearchQuery("Miami")
        val state = viewModel.uiState.value
        assertTrue("Filtered cities should include Miami", state.filteredCities.any { it.name == "Miami" })
        assertTrue("Filtered cities count should be smaller than total", state.filteredCities.size < state.cities.size)
    }

    @Test
    fun clearSelection_resetsSelectedCityToNull() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val city = viewModel.uiState.value.cities.first()
        viewModel.selectCity(city)
        assertNotNull(viewModel.uiState.value.selectedCity)

        viewModel.clearSelection()
        assertNull("Selected city should be null after clearSelection", viewModel.uiState.value.selectedCity)
    }

    @Test
    fun resetCameraToNorthAmerica_emitsNorthAmericaCameraTarget() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.resetCameraToNorthAmerica()
        val event = viewModel.uiState.value.cameraMoveEvent
        assertNotNull("Camera move event should exist", event)
        assertEquals(32.0, event!!.latitude, 0.1)
        assertEquals(-100.0, event.longitude, 0.1)
        assertEquals(4.0f, event.zoom, 0.1f)
    }
}
