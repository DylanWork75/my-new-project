package com.example.workapp.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class AppAutomationAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        if (instance === this) instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    suspend fun tap(x: Int, y: Int): String = dispatchInAppGesture { bounds ->
        require(bounds.contains(x, y)) { "Tap coordinates must be inside the WorkApp window." }
        GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(Path().apply {
                moveTo(x.toFloat(), y.toFloat())
            }, 0, TAP_DURATION_MS))
            .build()
    }

    suspend fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, durationMs: Int): String =
        dispatchInAppGesture { bounds ->
            require(durationMs in MIN_GESTURE_DURATION_MS..MAX_GESTURE_DURATION_MS) {
                "Swipe duration must be between $MIN_GESTURE_DURATION_MS and $MAX_GESTURE_DURATION_MS ms."
            }
            require(bounds.contains(startX, startY) && bounds.contains(endX, endY)) {
                "Swipe coordinates must stay inside the WorkApp window."
            }
            val path = Path().apply {
                moveTo(startX.toFloat(), startY.toFloat())
                lineTo(endX.toFloat(), endY.toFloat())
            }
            GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs.toLong()))
                .build()
        }

    suspend fun pinch(centerX: Int, centerY: Int, scaleFactor: Float, durationMs: Int): String =
        dispatchInAppGesture { bounds ->
            require(scaleFactor in MIN_PINCH_SCALE..MAX_PINCH_SCALE) {
                "Pinch scaleFactor must be between $MIN_PINCH_SCALE and $MAX_PINCH_SCALE."
            }
            require(durationMs in MIN_GESTURE_DURATION_MS..MAX_GESTURE_DURATION_MS) {
                "Pinch duration must be between $MIN_GESTURE_DURATION_MS and $MAX_GESTURE_DURATION_MS ms."
            }
            val endRadius = (PINCH_START_RADIUS_PX * scaleFactor).toInt()
            val points = listOf(
                centerX - PINCH_START_RADIUS_PX to centerY,
                centerX + PINCH_START_RADIUS_PX to centerY,
                centerX - endRadius to centerY,
                centerX + endRadius to centerY
            )
            require(points.all { (x, y) -> bounds.contains(x, y) }) {
                "Pinch coordinates must stay inside the WorkApp window."
            }

            fun pinchPath(startX: Int, endX: Int) = Path().apply {
                moveTo(startX.toFloat(), centerY.toFloat())
                lineTo(endX.toFloat(), centerY.toFloat())
            }

            GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(
                    pinchPath(centerX - PINCH_START_RADIUS_PX, centerX - endRadius), 0, durationMs.toLong()
                ))
                .addStroke(GestureDescription.StrokeDescription(
                    pinchPath(centerX + PINCH_START_RADIUS_PX, centerX + endRadius), 0, durationMs.toLong()
                ))
                .build()
        }

    private suspend fun dispatchInAppGesture(
        gestureFactory: (Rect) -> GestureDescription
    ): String = withContext(Dispatchers.Main.immediate) {
        val root = checkNotNull(rootInActiveWindow) {
            "The WorkApp window is not available for automation."
        }
        check(root.packageName?.toString() == packageName) {
            "Automation is allowed only while WorkApp is the active app."
        }
        val bounds = Rect().also { root.getBoundsInScreen(it) }
        val gesture = gestureFactory(bounds)

        suspendCancellableCoroutine { continuation ->
            val accepted = dispatchGesture(
                gesture,
                object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        if (continuation.isActive) continuation.resume("Gesture completed.")
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        if (continuation.isActive) continuation.resume("Gesture was cancelled.")
                    }
                },
                null
            )
            if (!accepted && continuation.isActive) continuation.resume("Android rejected the gesture.")
        }
    }

    companion object {
        private const val TAP_DURATION_MS = 80L
        private const val MIN_GESTURE_DURATION_MS = 80
        private const val MAX_GESTURE_DURATION_MS = 2_000
        private const val PINCH_START_RADIUS_PX = 90
        private const val MIN_PINCH_SCALE = 0.35f
        private const val MAX_PINCH_SCALE = 3.0f

        @Volatile
        private var instance: AppAutomationAccessibilityService? = null

        fun isEnabled(): Boolean = instance != null

        suspend fun getActiveAppBounds(): String = withContext(Dispatchers.Main.immediate) {
            val service = checkNotNull(instance) {
                "Enable WorkApp screen automation in Android Accessibility settings first."
            }
            val root = checkNotNull(service.rootInActiveWindow) {
                "The WorkApp window is not available for automation."
            }
            check(root.packageName?.toString() == service.packageName) {
                "Automation is allowed only while WorkApp is the active app."
            }
            val bounds = Rect().also(root::getBoundsInScreen)
            "WorkApp window bounds in absolute screen pixels: left=${bounds.left}, top=${bounds.top}, " +
                "right=${bounds.right}, bottom=${bounds.bottom}, width=${bounds.width()}, height=${bounds.height()}."
        }

        fun openAccessibilitySettings(context: Context) {
            context.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        suspend fun tapScreen(x: Int, y: Int): String =
            checkNotNull(instance) { "Enable WorkApp screen automation in Android Accessibility settings first." }
                .tap(x, y)

        suspend fun swipeScreen(
            startX: Int, startY: Int, endX: Int, endY: Int, durationMs: Int
        ): String = checkNotNull(instance) {
            "Enable WorkApp screen automation in Android Accessibility settings first."
        }.swipe(startX, startY, endX, endY, durationMs)

        suspend fun pinchScreen(centerX: Int, centerY: Int, scaleFactor: Float, durationMs: Int): String =
            checkNotNull(instance) { "Enable WorkApp screen automation in Android Accessibility settings first." }
                .pinch(centerX, centerY, scaleFactor, durationMs)
    }
}
