package com.example.ai

import android.content.Context
import android.graphics.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class SimulationScenario {
    PICKUP_AND_LEAVE, // برداشتن کالا و خروج
    PICKUP_AND_RESTORE, // برداشتن و بازگرداندن به قفسه
    DISPLACE_ON_SHELF, // جابه‌جایی روی قفسه
    INSPECT_ONLY // صرفاً نگاه کردن و لمس ملایم
}

class RetailSimulationEngine(
    private val context: Context,
    private val scope: CoroutineScope,
    private val productRecognizer: ProductRecognizer,
    private val onEventCompleted: (
        customerId: String,
        productName: String,
        brand: String,
        shelfName: String,
        interactionType: String,
        confidence: Float,
        dominantColor: String,
        reportText: String,
        status: String,
        beforePath: String?,
        startPath: String?,
        pickupPath: String?,
        afterPath: String?,
        finalPath: String?,
        croppedPath: String?
    ) -> Unit
) {
    private val _telemetry = MutableStateFlow(VisionTelemetry())
    val telemetry: StateFlow<VisionTelemetry> = _telemetry.asStateFlow()

    private val _activeHumans = MutableStateFlow<List<DetectedHuman>>(emptyList())
    val activeHumans: StateFlow<List<DetectedHuman>> = _activeHumans.asStateFlow()

    private val _activeHands = MutableStateFlow<List<DetectedHand>>(emptyList())
    val activeHands: StateFlow<List<DetectedHand>> = _activeHands.asStateFlow()

    private val _activeProducts = MutableStateFlow<List<DetectedProduct>>(emptyList())
    val activeProducts: StateFlow<List<DetectedProduct>> = _activeProducts.asStateFlow()

    private val _shelves = MutableStateFlow<List<ShelfSlot>>(defaultShelves())
    val shelves: StateFlow<List<ShelfSlot>> = _shelves.asStateFlow()

    private var currentJob: Job? = null
    private var isEngineRunning = false

    init {
        resetToDefaultState()
    }

    private fun defaultShelves(): List<ShelfSlot> {
        return listOf(
            ShelfSlot("S1", "قفسه ۱ (نوشیدنی)", RectFNorm(0.08f, 0.45f, 0.48f, 0.88f), true, false),
            ShelfSlot("S2", "قفسه ۲ (تنقلات)", RectFNorm(0.52f, 0.45f, 0.92f, 0.88f), true, false)
        )
    }

    fun resetToDefaultState() {
        _activeHumans.value = emptyList()
        _activeHands.value = emptyList()
        _activeProducts.value = listOf(
            DetectedProduct(
                id = "P1",
                name = "کوکاکولا ۳۳۰ میلی‌لیتر",
                brand = "Coca-Cola",
                bounds = RectFNorm(0.18f, 0.58f, 0.32f, 0.78f),
                initialBounds = RectFNorm(0.18f, 0.58f, 0.32f, 0.78f),
                dominantColor = "قرمز",
                confidence = 0.94f
            ),
            DetectedProduct(
                id = "P2",
                name = "چیپس مزمز طلایی",
                brand = "MazMaz",
                bounds = RectFNorm(0.64f, 0.55f, 0.82f, 0.80f),
                initialBounds = RectFNorm(0.64f, 0.55f, 0.82f, 0.80f),
                dominantColor = "زرد / طلایی",
                confidence = 0.91f
            )
        )
        _telemetry.value = VisionTelemetry(
            fps = 30.0f,
            latencyMs = 28,
            customerCount = 0,
            productsTracked = 2,
            status = MonitoringStatus.IDLE,
            countdownRemaining = 0,
            statusMessage = "محیط فروشگاه در حالت آرام (آماده تشخیص)"
        )
    }

    /**
     * Runs an intelligent end-to-end retail surveillance scenario
     */
    fun triggerScenario(scenario: SimulationScenario) {
        currentJob?.cancel()
        currentJob = scope.launch(Dispatchers.Default) {
            val customerId = "مشتری #${(10..99).random()}"
            val startTs = System.currentTimeMillis()
            var stepBitmap: Bitmap?

            // 1. Customer arrives
            _telemetry.value = _telemetry.value.copy(
                status = MonitoringStatus.MONITORING_ACTIVE,
                customerCount = 1,
                statusMessage = "$customerId وارد فروشگاه شد (شروع پایش)"
            )
            _activeHumans.value = listOf(
                DetectedHuman(
                    id = customerId,
                    bounds = RectFNorm(0.05f, 0.20f, 0.40f, 0.90f),
                    dwellTimeSeconds = 1,
                    isNearShelf = true,
                    shelfName = "قفسه ۱ (نوشیدنی)",
                    confidence = 0.96f
                )
            )

            // Capture Frame 1: Before Event
            val beforeBitmap = createSyntheticFrame(
                humanBounds = RectFNorm(0.05f, 0.20f, 0.40f, 0.90f),
                handBounds = null,
                productBounds = RectFNorm(0.18f, 0.58f, 0.32f, 0.78f),
                productColor = Color.RED,
                shelfEmpty = false,
                label = "فریم ۱: وضعیت قبل از تعامل"
            )
            val beforePath = saveFrame(beforeBitmap, "before", "frame1")
            delay(1200)

            // 2. Customer approaches shelf & reaches hand
            _activeHumans.value = listOf(
                DetectedHuman(
                    id = customerId,
                    bounds = RectFNorm(0.12f, 0.20f, 0.45f, 0.90f),
                    dwellTimeSeconds = 3,
                    isNearShelf = true,
                    shelfName = "قفسه ۱ (نوشیدنی)",
                    confidence = 0.97f
                )
            )
            _activeHands.value = listOf(
                DetectedHand(
                    id = "H1",
                    bounds = RectFNorm(0.20f, 0.52f, 0.30f, 0.62f),
                    isReachingProduct = true,
                    targetProductId = "P1",
                    confidence = 0.91f
                )
            )
            _telemetry.value = _telemetry.value.copy(
                status = MonitoringStatus.INTERACTION_DETECTED,
                statusMessage = "دست $customerId به سمت کالا در قفسه ۱ حرکت کرد"
            )

            // Capture Frame 2: Start of Interaction
            val startBitmap = createSyntheticFrame(
                humanBounds = RectFNorm(0.12f, 0.20f, 0.45f, 0.90f),
                handBounds = RectFNorm(0.20f, 0.52f, 0.30f, 0.62f),
                productBounds = RectFNorm(0.18f, 0.58f, 0.32f, 0.78f),
                productColor = Color.RED,
                shelfEmpty = false,
                label = "فریم ۲: شروع تعامل و نزدیک شدن دست"
            )
            val startPath = saveFrame(startBitmap, "start", "frame2")
            delay(1300)

            // 3. Pickup / Movement moment
            val isRestoring = scenario == SimulationScenario.PICKUP_AND_RESTORE
            val isDisplacing = scenario == SimulationScenario.DISPLACE_ON_SHELF
            val isLeaving = scenario == SimulationScenario.PICKUP_AND_LEAVE

            // Crop product bitmap for recognition
            val croppedProductBitmap = createSyntheticProductCrop(Color.RED, "Coca-Cola")
            val croppedPath = saveFrame(croppedProductBitmap, "products", "crop_coca")

            _activeHands.value = listOf(
                DetectedHand(
                    id = "H1",
                    bounds = RectFNorm(0.25f, 0.48f, 0.36f, 0.60f),
                    isReachingProduct = true,
                    targetProductId = "P1",
                    confidence = 0.93f
                )
            )
            _activeProducts.value = listOf(
                DetectedProduct(
                    id = "P1",
                    name = "کوکاکولا ۳۳۰ میلی‌لیتر",
                    brand = "Coca-Cola",
                    bounds = RectFNorm(0.25f, 0.52f, 0.37f, 0.72f),
                    initialBounds = RectFNorm(0.18f, 0.58f, 0.32f, 0.78f),
                    dominantColor = "قرمز",
                    isMoved = true,
                    confidence = 0.95f,
                    localCroppedPath = croppedPath
                ),
                _activeProducts.value[1]
            )

            // Capture Frame 3: During picking
            val pickupBitmap = createSyntheticFrame(
                humanBounds = RectFNorm(0.15f, 0.20f, 0.48f, 0.90f),
                handBounds = RectFNorm(0.25f, 0.48f, 0.36f, 0.60f),
                productBounds = RectFNorm(0.25f, 0.52f, 0.37f, 0.72f),
                productColor = Color.RED,
                shelfEmpty = true,
                label = "فریم ۳: لحظه برداشتن و جابه‌جایی کالا"
            )
            val pickupPath = saveFrame(pickupBitmap, "pickup", "frame3")
            delay(1400)

            // 4. After pickup / decision
            val afterBitmap = if (isRestoring) {
                _telemetry.value = _telemetry.value.copy(statusMessage = "مشتری کالا را مجدداً داخل قفسه ۱ قرار داد")
                createSyntheticFrame(
                    humanBounds = RectFNorm(0.15f, 0.20f, 0.48f, 0.90f),
                    handBounds = RectFNorm(0.20f, 0.55f, 0.30f, 0.65f),
                    productBounds = RectFNorm(0.18f, 0.58f, 0.32f, 0.78f),
                    productColor = Color.RED,
                    shelfEmpty = false,
                    label = "فریم ۴: بازگرداندن کالا به قفسه"
                )
            } else {
                _telemetry.value = _telemetry.value.copy(statusMessage = "محل کالا در قفسه خالی دیده شد (کالا در دست مشتری)")
                createSyntheticFrame(
                    humanBounds = RectFNorm(0.20f, 0.20f, 0.55f, 0.90f),
                    handBounds = RectFNorm(0.28f, 0.45f, 0.38f, 0.58f),
                    productBounds = RectFNorm(0.28f, 0.50f, 0.40f, 0.70f),
                    productColor = Color.RED,
                    shelfEmpty = true,
                    label = "فریم ۴: کالا در دست مشتری، جایگاه قفسه خالی"
                )
            }
            val afterPath = saveFrame(afterBitmap, "after", "frame4")
            delay(1200)

            // 5. Customer exits or finishes -> Countdown starts
            _activeHands.value = emptyList()
            if (isLeaving) {
                _activeHumans.value = emptyList() // left frame
                _activeProducts.value = listOf(_activeProducts.value[1]) // P1 left view
            } else if (isRestoring) {
                _activeProducts.value = listOf(
                    DetectedProduct(
                        id = "P1",
                        name = "کوکاکولا ۳۳۰ میلی‌لیتر",
                        brand = "Coca-Cola",
                        bounds = RectFNorm(0.18f, 0.58f, 0.32f, 0.78f),
                        initialBounds = RectFNorm(0.18f, 0.58f, 0.32f, 0.78f),
                        dominantColor = "قرمز",
                        isRestored = true,
                        confidence = 0.95f,
                        localCroppedPath = croppedPath
                    ),
                    _activeProducts.value[1]
                )
            }

            // Countdown to stop
            _telemetry.value = _telemetry.value.copy(
                status = MonitoringStatus.COUNTDOWN_STOPPING,
                customerCount = if (isLeaving) 0 else 1
            )
            for (sec in 3 downTo 1) {
                _telemetry.value = _telemetry.value.copy(
                    countdownRemaining = sec,
                    statusMessage = "شمارش معکوس پایان رویداد: $sec ثانیه"
                )
                delay(1000)
            }

            // Final Frame
            val finalBitmap = createSyntheticFrame(
                humanBounds = if (isLeaving) null else RectFNorm(0.30f, 0.20f, 0.65f, 0.90f),
                handBounds = null,
                productBounds = if (isLeaving) null else RectFNorm(0.18f, 0.58f, 0.32f, 0.78f),
                productColor = Color.RED,
                shelfEmpty = isLeaving,
                label = "فریم ۵: وضعیت نهایی پس از پایان تعامل"
            )
            val finalPath = saveFrame(finalBitmap, "final", "frame5")
            val endTs = System.currentTimeMillis()

            // Interaction outcome classification
            val (interactionType, statusOutcome) = when {
                isLeaving -> Pair("برداشتن کالا", "نیازمند بررسی")
                isRestoring -> Pair("گذاشتن مجدد", "تأیید شده")
                isDisplacing -> Pair("جابه‌جایی کالا", "تأیید شده")
                else -> Pair("تماس بدون تغییر", "تأیید شده")
            }

            val report = ReportGenerator.generateEventReport(
                customerId = customerId,
                productName = "کوکاکولا ۳۳۰ میلی‌لیتر",
                shelfName = "قفسه ۱ (نوشیدنی)",
                interactionType = interactionType,
                startTimestamp = startTs,
                endTimestamp = endTs,
                confidence = 0.94f,
                dominantColor = "قرمز",
                isDisplaced = isDisplacing || isLeaving,
                isSlotEmpty = isLeaving,
                isRestored = isRestoring,
                isOutOfView = isLeaving
            )

            // Dispatch event to Room database
            onEventCompleted(
                customerId,
                "کوکاکولا ۳۳۰ میلی‌لیتر",
                "Coca-Cola",
                "قفسه ۱ (نوشیدنی)",
                interactionType,
                0.94f,
                "قرمز",
                report,
                statusOutcome,
                beforePath,
                startPath,
                pickupPath,
                afterPath,
                finalPath,
                croppedPath
            )

            resetToDefaultState()
        }
    }

    private fun createSyntheticFrame(
        humanBounds: RectFNorm?,
        handBounds: RectFNorm?,
        productBounds: RectFNorm?,
        productColor: Int,
        shelfEmpty: Boolean,
        label: String
    ): Bitmap {
        val width = 640
        val height = 480
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Dark background store aisle
        val bgPaint = Paint().apply { color = Color.rgb(20, 28, 45) }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Floor perspective lines
        val linePaint = Paint().apply {
            color = Color.rgb(35, 48, 75)
            strokeWidth = 3f
        }
        canvas.drawLine(0f, height * 0.45f, width.toFloat(), height * 0.45f, linePaint)
        canvas.drawLine(width * 0.2f, height * 0.45f, 0f, height.toFloat(), linePaint)
        canvas.drawLine(width * 0.8f, height * 0.45f, width.toFloat(), height.toFloat(), linePaint)

        // Draw Shelf 1 (Left)
        val shelfPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            style = Paint.Style.FILL
        }
        val shelfBorder = Paint().apply {
            color = Color.rgb(51, 65, 85)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val s1Rect = RectF(width * 0.08f, height * 0.45f, width * 0.48f, height * 0.88f)
        canvas.drawRoundRect(s1Rect, 8f, 8f, shelfPaint)
        canvas.drawRoundRect(s1Rect, 8f, 8f, shelfBorder)

        // Draw Shelf 2 (Right)
        val s2Rect = RectF(width * 0.52f, height * 0.45f, width * 0.92f, height * 0.88f)
        canvas.drawRoundRect(s2Rect, 8f, 8f, shelfPaint)
        canvas.drawRoundRect(s2Rect, 8f, 8f, shelfBorder)

        // Draw shelf labels
        val textPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 22f
            isAntiAlias = true
        }
        canvas.drawText("قفسه ۱: نوشیدنی", s1Rect.left + 15f, s1Rect.top + 35f, textPaint)
        canvas.drawText("قفسه ۲: تنقلات", s2Rect.left + 15f, s2Rect.top + 35f, textPaint)

        // If product present on shelf 1 or moved
        if (productBounds != null) {
            val pPaint = Paint().apply {
                color = productColor
                style = Paint.Style.FILL
            }
            val pRect = RectF(
                productBounds.left * width,
                productBounds.top * height,
                productBounds.right * width,
                productBounds.bottom * height
            )
            canvas.drawRoundRect(pRect, 10f, 10f, pPaint)
            // Product can highlight
            val pBorder = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(pRect, 10f, 10f, pBorder)
            val pText = Paint().apply {
                color = Color.WHITE
                textSize = 18f
                isFakeBoldText = true
            }
            canvas.drawText("Coke", pRect.left + 10f, pRect.centerY(), pText)
        } else if (shelfEmpty) {
            // Draw dotted empty slot outline
            val emptyPaint = Paint().apply {
                color = Color.rgb(239, 68, 68)
                style = Paint.Style.STROKE
                strokeWidth = 3f
                pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
            }
            canvas.drawRect(width * 0.18f, height * 0.58f, width * 0.32f, height * 0.78f, emptyPaint)
            val emptyText = Paint().apply {
                color = Color.rgb(239, 68, 68)
                textSize = 18f
            }
            canvas.drawText("[محل خالی]", width * 0.18f + 5f, height * 0.68f, emptyText)
        }

        // Draw product on Shelf 2
        val chipsPaint = Paint().apply { color = Color.rgb(234, 179, 8) }
        val cRect = RectF(width * 0.64f, height * 0.55f, width * 0.82f, height * 0.80f)
        canvas.drawRoundRect(cRect, 10f, 10f, chipsPaint)

        // Draw Customer (silhouette / bounding figure)
        if (humanBounds != null) {
            val hPaint = Paint().apply {
                color = Color.argb(160, 37, 99, 235)
                style = Paint.Style.FILL
            }
            val hRect = RectF(
                humanBounds.left * width,
                humanBounds.top * height,
                humanBounds.right * width,
                humanBounds.bottom * height
            )
            canvas.drawRoundRect(hRect, 16f, 16f, hPaint)
            val hBorder = Paint().apply {
                color = Color.rgb(0, 229, 255)
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRoundRect(hRect, 16f, 16f, hBorder)
        }

        // Draw Hand box
        if (handBounds != null) {
            val handPaint = Paint().apply {
                color = Color.rgb(168, 85, 247)
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            val handRect = RectF(
                handBounds.left * width,
                handBounds.top * height,
                handBounds.right * width,
                handBounds.bottom * height
            )
            canvas.drawRect(handRect, handPaint)
        }

        // Top banner watermark
        val bannerPaint = Paint().apply { color = Color.argb(180, 0, 0, 0) }
        canvas.drawRect(0f, 0f, width.toFloat(), 50f, bannerPaint)
        val bannerText = Paint().apply {
            color = Color.rgb(0, 229, 255)
            textSize = 20f
            isFakeBoldText = true
        }
        canvas.drawText("Shop Guardian AI - $label", 20f, 32f, bannerText)

        return bmp
    }

    private fun createSyntheticProductCrop(color: Int, text: String): Bitmap {
        val bmp = Bitmap.createBitmap(240, 320, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val p = Paint().apply { this.color = color }
        canvas.drawRoundRect(10f, 10f, 230f, 310f, 24f, 24f, p)
        val tPaint = Paint().apply {
            this.color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, 120f, 160f, tPaint)
        return bmp
    }

    private fun saveFrame(bitmap: Bitmap?, subfolder: String, prefix: String): String? {
        return productRecognizer.saveBitmap(bitmap, subfolder, prefix)
    }
}
