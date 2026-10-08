package com.example.workapp.utils

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.example.workapp.automation.AppAutomationAccessibilityService

@LLMDescription("Tools for reading and controlling the WorkApp screen using absolute pixel coordinates.")
class MobileTestTools : ToolSet {

    @Tool(customName = "get_app_screen_bounds")
    @LLMDescription("Return the active WorkApp window bounds and dimensions in absolute screen pixels. Call before choosing coordinates.")
    suspend fun getAppScreenBounds(): String =
        AppAutomationAccessibilityService.getActiveAppBounds()

    @Tool(customName = "pinch_screen")
    @LLMDescription("Perform a two-finger pinch centered at absolute screen pixel coordinates. A scale factor above 1 zooms in; below 1 zooms out.")
    suspend fun pinchScreen(
        @LLMDescription("Pinch center X in screen pixels from the left edge.") centerX: Int,
        @LLMDescription("Pinch center Y in screen pixels from the top edge.") centerY: Int,
        @LLMDescription("Finger spacing multiplier between 0.35 and 3.0. Values above 1 zoom in.") scaleFactor: Float,
        @LLMDescription("Gesture duration in milliseconds, between 80 and 2000.") durationMs: Int
    ): String = AppAutomationAccessibilityService.pinchScreen(centerX, centerY, scaleFactor, durationMs)

    @Tool(customName = "swipe_screen")
    @LLMDescription("Swipe between two absolute screen pixel coordinates inside the active WorkApp window.")
    suspend fun swipeScreen(
        @LLMDescription("Swipe start X in screen pixels.") startX: Int,
        @LLMDescription("Swipe start Y in screen pixels.") startY: Int,
        @LLMDescription("Swipe end X in screen pixels.") endX: Int,
        @LLMDescription("Swipe end Y in screen pixels.") endY: Int,
        @LLMDescription("Gesture duration in milliseconds, between 80 and 2000.") durationMs: Int
    ): String = AppAutomationAccessibilityService.swipeScreen(startX, startY, endX, endY, durationMs)

    @Tool(customName = "click_coordinates")
    @LLMDescription("Tap one absolute screen pixel coordinate inside the active WorkApp window.")
    suspend fun clickCoordinates(
        @LLMDescription("Tap X in screen pixels from the left edge.") x: Int,
        @LLMDescription("Tap Y in screen pixels from the top edge.") y: Int
    ): String = AppAutomationAccessibilityService.tapScreen(x, y)
}
