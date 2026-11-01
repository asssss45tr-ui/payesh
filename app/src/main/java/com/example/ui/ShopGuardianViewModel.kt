package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.*
import com.example.data.db.ShopDatabase
import com.example.data.model.CustomerEntity
import com.example.data.model.EventEntity
import com.example.data.model.ShelfZoneEntity
import com.example.data.repository.ShopGuardianRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

data class ShopGuardianUiState(
    val selectedTab: Int = 0, // 0: Live, 1: Events, 2: Shelf Calibration, 3: Archive, 4: Settings
    val isSimulatingMode: Boolean = true,
    val isTorchOn: Boolean = false,
    val selectedEvent: EventEntity? = null,
    val eventFilter: String = "همه",
    val searchQuery: String = "",
    val isVerificationDialogOpen: Boolean = false,
    val verificationTargetEvent: EventEntity? = null,
    val storageUsageMb: Float = 4.2f,
    val alertMessage: String? = null
)

class ShopGuardianViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ShopDatabase.getInstance(application)
    val repository = ShopGuardianRepository(
        eventDao = db.eventDao(),
        customerDao = db.customerDao(),
        productDao = db.productDao(),
        shelfDao = db.shelfDao()
    )

    val productRecognizer = ProductRecognizer(application)

    private val _uiState = MutableStateFlow(ShopGuardianUiState())
    val uiState: StateFlow<ShopGuardianUiState> = _uiState.asStateFlow()

    val allEvents: StateFlow<List<EventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allShelves: StateFlow<List<ShelfZoneEntity>> = repository.allShelves
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulation engine
    val simulationEngine = RetailSimulationEngine(
        context = application,
        scope = viewModelScope,
        productRecognizer = productRecognizer,
        onEventCompleted = { customerId, productName, brand, shelfName, interactionType, confidence, color, report, status, before, start, pickup, after, final, crop ->
            saveNewEvent(
                customerId, productName, brand, shelfName, interactionType, confidence, color, report, status, before, start, pickup, after, final, crop
            )
        }
    )

    val telemetry: StateFlow<VisionTelemetry> = simulationEngine.telemetry
    val activeHumans: StateFlow<List<DetectedHuman>> = simulationEngine.activeHumans
    val activeHands: StateFlow<List<DetectedHand>> = simulationEngine.activeHands
    val activeProducts: StateFlow<List<DetectedProduct>> = simulationEngine.activeProducts

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedInitialDataIfEmpty()
            calculateStorageSize()
        }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun selectEvent(event: EventEntity?) {
        _uiState.update { it.copy(selectedEvent = event) }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(eventFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleSimulationMode() {
        _uiState.update { it.copy(isSimulatingMode = !it.isSimulatingMode) }
    }

    fun toggleTorchState(newState: Boolean) {
        _uiState.update { it.copy(isTorchOn = newState) }
    }

    fun openVerificationDialog(event: EventEntity) {
        _uiState.update {
            it.copy(
                isVerificationDialogOpen = true,
                verificationTargetEvent = event
            )
        }
    }

    fun closeVerificationDialog() {
        _uiState.update {
            it.copy(
                isVerificationDialogOpen = false,
                verificationTargetEvent = null
            )
        }
    }

    fun triggerScenario(scenario: SimulationScenario) {
        simulationEngine.triggerScenario(scenario)
    }

    private fun saveNewEvent(
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
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val eventId = "EVT-${(1000..9999).random()}"
            val newEvent = EventEntity(
                id = eventId,
                timestamp = System.currentTimeMillis(),
                customerId = customerId,
                productName = productName,
                brandName = brand,
                shelfName = shelfName,
                interactionType = interactionType,
                confidenceScore = confidence,
                status = status,
                beforeFramePath = beforePath,
                startFramePath = startPath,
                pickupFramePath = pickupPath,
                afterFramePath = afterPath,
                finalFramePath = finalPath,
                croppedProductPath = croppedPath,
                aiReportText = reportText,
                dominantColor = dominantColor
            )
            repository.insertEvent(newEvent)

            // Update customer record
            repository.insertOrUpdateCustomer(
                CustomerEntity(
                    id = customerId,
                    firstSeenTimestamp = System.currentTimeMillis() - 10000,
                    lastSeenTimestamp = System.currentTimeMillis(),
                    dwellSeconds = 12,
                    shelfVisited = shelfName,
                    interactionCount = 1,
                    status = "خارج شده"
                )
            )

            calculateStorageSize()
        }
    }

    fun confirmVerification(
        eventId: String,
        correctedName: String,
        correctedType: String,
        status: String,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.verifyAndCorrectEvent(eventId, correctedName, correctedType, status, notes)
            closeVerificationDialog()
            // If the verified event was currently open in detail, update selectedEvent
            val updated = repository.getEventById(eventId)
            _uiState.update { it.copy(selectedEvent = updated) }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(eventId)
            if (_uiState.value.selectedEvent?.id == eventId) {
                _uiState.update { it.copy(selectedEvent = null) }
            }
            calculateStorageSize()
        }
    }

    fun clearAllArchive() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
            _uiState.update { it.copy(selectedEvent = null) }
            calculateStorageSize()
        }
    }

    private fun calculateStorageSize() {
        try {
            val root = File(getApplication<Application>().filesDir, "ShopGuardian")
            if (root.exists()) {
                val bytes = root.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
                val mb = (bytes / (1024f * 1024f)).coerceAtLeast(0.1f)
                _uiState.update { it.copy(storageUsageMb = String.format(Locale.US, "%.1f", mb).toFloat()) }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
