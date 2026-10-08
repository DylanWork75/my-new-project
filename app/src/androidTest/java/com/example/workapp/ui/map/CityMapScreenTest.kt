package com.example.workapp.ui.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.workapp.data.model.City
import com.example.workapp.data.model.CityCategory
import com.example.workapp.ui.theme.WorkAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CityMapScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleCity = City(
        id = "us-nyc",
        name = "New York City",
        country = "US",
        countryName = "United States",
        stateOrRegion = "New York",
        latitude = 40.7128,
        longitude = -74.0060,
        description = "Sample NYC Description",
        category = CityCategory.METROPOLIS,
        iconIndicator = "location_city",
        population = 8335897,
        rating = 4.8,
        popularAttractions = listOf("Central Park", "Statue of Liberty")
    )

    @Test
    fun selectedCityDetailOverlay_displaysCityInformationWhenCitySelected() {
        var dismissed = false
        var centeredCity: City? = null

        composeTestRule.setContent {
            WorkAppTheme {
                SelectedCityDetailOverlay(
                    city = sampleCity,
                    onDismiss = { dismissed = true },
                    onCenterCamera = { centeredCity = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("SelectedCityDetailCard").assertIsDisplayed()
        composeTestRule.onNodeWithText("New York City").assertIsDisplayed()
        composeTestRule.onNodeWithText("New York, United States").assertIsDisplayed()
        composeTestRule.onNodeWithText("Metropolis").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sample NYC Description").assertIsDisplayed()

        // Click center button
        composeTestRule.onNodeWithTag("CenterCameraOnCityButton").performClick()
        assertEquals(sampleCity, centeredCity)

        // Click close button
        composeTestRule.onNodeWithTag("CloseCityDetailButton").performClick()
        assert(dismissed)
    }

    @Test
    fun searchAndFilterBar_rendersCountryAndCategoryChips() {
        var selectedCountry: String? = null

        composeTestRule.setContent {
            WorkAppTheme {
                SearchAndFilterBar(
                    searchQuery = "",
                    selectedCountry = null,
                    selectedCategory = null,
                    onSearchQueryChange = {},
                    onCountryFilterChange = { selectedCountry = it },
                    onCategoryFilterChange = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("SearchTextField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("FilterChipAllCountries").assertIsDisplayed()
        composeTestRule.onNodeWithTag("FilterChipUS").assertIsDisplayed()
        composeTestRule.onNodeWithTag("FilterChipMX").assertIsDisplayed()

        // Click US filter chip
        composeTestRule.onNodeWithTag("FilterChipUS").performClick()
        assertEquals("US", selectedCountry)
    }
}
