package com.example.data.db

import androidx.room.*
import com.example.data.model.CustomerEntity
import com.example.data.model.EventEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ShelfZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): EventEntity?

    @Query("SELECT * FROM events WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getEventsSince(sinceTimestamp: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE status = :status ORDER BY timestamp DESC")
    fun getEventsByStatus(status: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getEventsByCustomer(customerId: String): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: String)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()

    @Query("SELECT COUNT(*) FROM events")
    fun getEventCount(): Flow<Int>
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY lastSeenTimestamp DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE status = 'حاضر در کادر'")
    fun getActiveCustomers(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR ocrKeywords LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)
}

@Dao
interface ShelfDao {
    @Query("SELECT * FROM shelf_zones ORDER BY name ASC")
    fun getAllShelves(): Flow<List<ShelfZoneEntity>>

    @Query("SELECT * FROM shelf_zones WHERE id = :id LIMIT 1")
    suspend fun getShelfById(id: String): ShelfZoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateShelf(shelf: ShelfZoneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shelves: List<ShelfZoneEntity>)

    @Update
    suspend fun updateShelf(shelf: ShelfZoneEntity)
}
