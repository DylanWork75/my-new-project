package com.example.workapp.ui.map

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import com.atiurin.ultron.core.compose.createUltronComposeRule
import com.atiurin.ultron.core.uiautomator.uiobject2.UltronUiObject2.Companion.by
import com.atiurin.ultron.extensions.assertIsDisplayed
import com.atiurin.ultron.extensions.click
import com.atiurin.ultron.extensions.inputText
import com.example.workapp.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CityMapInteractionTest {

    @get:Rule
    val composeRule = createUltronComposeRule<MainActivity>()

    @Test
    fun mapCanZoomAndPan() {
        hasTestTag("GoogleMapView").assertIsDisplayed()

        // UI Automator gestures go through Ultron to interact with the native map surface.
        val mapSurface = by(By.desc("Interactive city map")).withTimeout(15_000L)
        mapSurface.isDisplayed()
        mapSurface.pinchClose(0.45f)
        mapSurface.swipeLeft(0.45f)
        mapSurface.swipeUp(0.45f)

        hasTestTag("GoogleMapView").assertIsDisplayed()
        hasTestTag("ResetCameraButton").assertIsDisplayed()
    }

    @Test
    fun canSelectMultipleCityNamesFromTheMapCityList() {
        hasTestTag("OpenCityListButton").click()
        hasTestTag("CityListItem_us-nyc").click()
        hasTestTag("SelectedCityDetailCard").assertIsDisplayed()
        hasText("New York, United States").assertIsDisplayed()

        hasTestTag("CloseCityDetailButton").click()
        hasTestTag("OpenCityListButton").click()
        hasTestTag("SearchTextField").click()
        hasTestTag("SearchTextField").inputText("Chicago")
        hasTestTag("CityListItem_us-chi").click()
        hasTestTag("SelectedCityDetailCard").assertIsDisplayed()
        hasText("Chicago, Illinois").assertIsDisplayed()
    }
}
