package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val firstSeenTimestamp: Long,
    val lastSeenTimestamp: Long,
    val dwellSeconds: Int = 0,
    val shelfVisited: String = "قفسه نامشخص",
    val interactionCount: Int = 0,
    val status: String = "حاضر در کادر" // "حاضر در کادر", "خارج شده"
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val customerId: String,
    val productName: String,
    val brandName: String = "",
    val shelfName: String,
    val interactionType: String, // "برداشتن کالا", "جابه‌جایی کالا", "گذاشتن مجدد", "خروج از دید", "تماس بدون تغییر"
    val confidenceScore: Float,
    val status: String = "نیازمند بررسی", // "تأیید شده", "نامشخص", "نیازمند بررسی", "رد شده"
    val beforeFramePath: String? = null,
    val startFramePath: String? = null,
    val pickupFramePath: String? = null,
    val afterFramePath: String? = null,
    val finalFramePath: String? = null,
    val croppedProductPath: String? = null,
    val videoPath: String? = null,
    val aiReportText: String,
    val userNotes: String? = null,
    val dominantColor: String = "نامشخص",
    val verifiedByUser: Boolean = false
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val brand: String,
    val category: String,
    val expectedShelf: String,
    val color: String,
    val referenceImagePath: String? = null,
    val ocrKeywords: String = "",
    val confidence: Float = 0.85f,
    val webSearchMatchUrl: String? = null,
    val priceEstimate: String? = null
)

@Entity(tableName = "shelf_zones")
data class ShelfZoneEntity(
    @PrimaryKey val id: String,
    val name: String,
    val xMin: Float,
    val yMin: Float,
    val xMax: Float,
    val yMax: Float,
    val expectedProductCount: Int = 4,
    val currentProductCount: Int = 4,
    val referenceImagePath: String? = null,
    val isChanged: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
