package com.example.ai

import android.graphics.Bitmap

data class RectFNorm(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
    val centerX: Float get() = left + width / 2f
    val centerY: Float get() = top + height / 2f

    fun intersects(other: RectFNorm): Boolean {
        return left < other.right && right > other.left && top < other.bottom && bottom > other.top
    }
}

data class DetectedHuman(
    val id: String,
    val bounds: RectFNorm,
    val dwellTimeSeconds: Int = 0,
    val isNearShelf: Boolean = false,
    val shelfName: String = "",
    val confidence: Float = 0.92f
)

data class DetectedHand(
    val id: String,
    val bounds: RectFNorm,
    val isReachingProduct: Boolean = false,
    val targetProductId: String? = null,
    val confidence: Float = 0.88f
)

data class DetectedProduct(
    val id: String,
    val name: String,
    val brand: String = "",
    val bounds: RectFNorm,
    val initialBounds: RectFNorm,
    val dominantColor: String = "آبی",
    val isMoved: Boolean = false,
    val isRemoved: Boolean = false,
    val isRestored: Boolean = false,
    val isOutOfView: Boolean = false,
    val confidence: Float = 0.89f,
    val croppedBitmap: Bitmap? = null,
    val localCroppedPath: String? = null
)

data class ShelfSlot(
    val id: String,
    val name: String,
    val bounds: RectFNorm,
    val hasProduct: Boolean = true,
    val isDisplaced: Boolean = false
)

enum class MonitoringStatus {
    IDLE,
    MONITORING_ACTIVE,
    INTERACTION_DETECTED,
    COUNTDOWN_STOPPING
}

data class VisionTelemetry(
    val fps: Float = 28.5f,
    val latencyMs: Long = 34,
    val customerCount: Int = 0,
    val productsTracked: Int = 0,
    val status: MonitoringStatus = MonitoringStatus.IDLE,
    val countdownRemaining: Int = 0,
    val statusMessage: String = "سیستم آماده پایش"
)

data class KeyEvidenceFrames(
    val beforeFrame: Bitmap? = null,
    val startFrame: Bitmap? = null,
    val pickupFrame: Bitmap? = null,
    val afterFrame: Bitmap? = null,
    val finalFrame: Bitmap? = null,
    val beforeFramePath: String? = null,
    val startFramePath: String? = null,
    val pickupFramePath: String? = null,
    val afterFramePath: String? = null,
    val finalFramePath: String? = null
)
