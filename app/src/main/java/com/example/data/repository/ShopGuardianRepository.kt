package com.example.data.repository

import com.example.data.db.CustomerDao
import com.example.data.db.EventDao
import com.example.data.db.ProductDao
import com.example.data.db.ShelfDao
import com.example.data.model.CustomerEntity
import com.example.data.model.EventEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ShelfZoneEntity
import kotlinx.coroutines.flow.Flow

class ShopGuardianRepository(
    private val eventDao: EventDao,
    private val customerDao: CustomerDao,
    private val productDao: ProductDao,
    private val shelfDao: ShelfDao
) {
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    val eventCount: Flow<Int> = eventDao.getEventCount()
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val allShelves: Flow<List<ShelfZoneEntity>> = shelfDao.getAllShelves()
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    suspend fun getEventById(id: String): EventEntity? = eventDao.getEventById(id)

    suspend fun insertEvent(event: EventEntity) {
        eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: EventEntity) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(id: String) {
        eventDao.deleteEventById(id)
    }

    suspend fun insertOrUpdateCustomer(customer: CustomerEntity) {
        customerDao.insertOrUpdateCustomer(customer)
    }

    suspend fun updateShelf(shelf: ShelfZoneEntity) {
        shelfDao.updateShelf(shelf)
    }

    suspend fun verifyAndCorrectEvent(
        eventId: String,
        correctedProductName: String,
        correctedInteractionType: String,
        status: String,
        userNotes: String
    ) {
        val event = eventDao.getEventById(eventId) ?: return
        val updated = event.copy(
            productName = correctedProductName,
            interactionType = correctedInteractionType,
            status = status,
            userNotes = userNotes,
            verifiedByUser = true
        )
        eventDao.updateEvent(updated)
    }

    suspend fun seedInitialDataIfEmpty() {
        // Seed default shelves
        shelfDao.insertAll(
            listOf(
                ShelfZoneEntity(
                    id = "S1",
                    name = "قفسه ۱: نوشیدنی‌ها و مایعات",
                    xMin = 0.08f,
                    yMin = 0.45f,
                    xMax = 0.48f,
                    yMax = 0.88f,
                    expectedProductCount = 6,
                    currentProductCount = 6
                ),
                ShelfZoneEntity(
                    id = "S2",
                    name = "قفسه ۲: چیپس و تنقلات",
                    xMin = 0.52f,
                    yMin = 0.45f,
                    xMax = 0.92f,
                    yMax = 0.88f,
                    expectedProductCount = 8,
                    currentProductCount = 8
                ),
                ShelfZoneEntity(
                    id = "S3",
                    name = "قفسه ۳: بهداشتی و آرایشی",
                    xMin = 0.20f,
                    yMin = 0.15f,
                    xMax = 0.80f,
                    yMax = 0.40f,
                    expectedProductCount = 5,
                    currentProductCount = 5
                )
            )
        )

        // Seed initial reference catalog
        productDao.insertAll(
            listOf(
                ProductEntity(
                    id = "PRD-1",
                    name = "کوکاکولا ۳۳۰ میلی‌لیتر",
                    brand = "Coca-Cola",
                    category = "نوشیدنی",
                    expectedShelf = "قفسه ۱: نوشیدنی‌ها و مایعات",
                    color = "قرمز",
                    ocrKeywords = "coca cola classic red can",
                    confidence = 0.96f
                ),
                ProductEntity(
                    id = "PRD-2",
                    name = "چیپس سیب‌زمینی چیپلت مزمز",
                    brand = "MazMaz",
                    category = "تنقلات",
                    expectedShelf = "قفسه ۲: چیپس و تنقلات",
                    color = "زرد / طلایی",
                    ocrKeywords = "mazmaz chiplet potato gold",
                    confidence = 0.91f
                ),
                ProductEntity(
                    id = "PRD-3",
                    name = "شکلات صبحانه نوتلا ۳۵۰ گرم",
                    brand = "Nutella",
                    category = "صبحانه",
                    expectedShelf = "قفسه ۲: چیپس و تنقلات",
                    color = "قهوه‌ای / قرمز",
                    ocrKeywords = "nutella ferrero hazelnut",
                    confidence = 0.94f
                )
            )
        )
    }

    suspend fun clearAllData() {
        eventDao.deleteAllEvents()
        customerDao.deleteAllCustomers()
    }
}
