package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.EventEntity
import com.example.ui.ShopGuardianUiState
import com.example.ui.ShopGuardianViewModel
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: ShopGuardianViewModel,
    uiState: ShopGuardianUiState,
    onEventSelected: (EventEntity) -> Unit
) {
    val events by viewModel.allEvents.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("همه") }

    val filterOptions = listOf(
        "همه",
        "امروز",
        "برداشته شده",
        "بازگردانده شده",
        "جابه‌جایی کالا",
        "نیازمند بررسی",
        "تأیید شده"
    )

    // Filter logic
    val filteredEvents = remember(events, searchQuery, selectedFilter) {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        events.filter { event ->
            val matchesSearch = searchQuery.isEmpty() ||
                    event.productName.contains(searchQuery, ignoreCase = true) ||
                    event.customerId.contains(searchQuery, ignoreCase = true) ||
                    event.id.contains(searchQuery, ignoreCase = true) ||
                    event.shelfName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "امروز" -> (now - event.timestamp) < oneDayMillis
                "برداشته شده" -> event.interactionType == "برداشتن کالا"
                "بازگردانده شده" -> event.interactionType == "گذاشتن مجدد"
                "جابه‌جایی کالا" -> event.interactionType == "جابه‌جایی کالا"
                "نیازمند بررسی" -> event.status == "نیازمند بررسی"
                "تأیید شده" -> event.status == "تأیید شده"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
            .padding(16.dp)
            .testTag("events_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مرکز مدیریت رویدادها",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ElectricBlue.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "${filteredEvents.size} مورد",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("جستجو بر اساس نام کالا، شناسه مشتری یا رویداد...", color = TextSecondary, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "پاک کردن", tint = TextSecondary)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("event_search_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = SlateBorder,
                focusedContainerColor = SlateSurface,
                unfocusedContainerColor = SlateSurface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                        selectedLabelColor = CyberCyan,
                        containerColor = SlateSurface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == filter,
                        borderColor = SlateBorder,
                        selectedBorderColor = CyberCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Events List or Empty State
        if (filteredEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "هیچ رویدادی با این مشخصات یافت نشد",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "از بخش پایش، سناریوهای آزمون را اجرا نمایید",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredEvents, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventSelected(event) },
                        onVerify = { viewModel.openVerificationDialog(event) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: EventEntity,
    onClick: () -> Unit,
    onVerify: () -> Unit
) {
    val timeFormatted = remember(event.timestamp) {
        val sdf = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale("fa", "IR"))
        sdf.format(Date(event.timestamp))
    }

    val statusColor = when (event.status) {
        "تأیید شده" -> GuardGreen
        "نیازمند بررسی" -> AlertAmber
        "رد شده" -> AlarmRed
        else -> ElectricBlue
    }

    val typeColor = when (event.interactionType) {
        "برداشتن کالا" -> AlarmRed
        "گذاشتن مجدد" -> GuardGreen
        "جابه‌جایی کالا" -> AlertAmber
        else -> CyberCyan
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        color = SlateSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail image (startFrame or cropped product)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateDark)
                    .border(1.dp, SlateBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val imagePath = event.pickupFramePath ?: event.startFramePath ?: event.croppedProductPath
                if (imagePath != null && File(imagePath).exists()) {
                    AsyncImage(
                        model = File(imagePath),
                        contentDescription = "تصویر رویداد",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Info Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Top line: Customer & Event ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.customerId,
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.id,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                // Product Name
                Text(
                    text = event.productName,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Shelf & Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = event.shelfName,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = timeFormatted,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Badges row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Interaction type badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = typeColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, typeColor)
                        ) {
                            Text(
                                text = event.interactionType,
                                color = typeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, statusColor)
                        ) {
                            Text(
                                text = event.status,
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Confidence score
                    Text(
                        text = "${(event.confidenceScore * 100).toInt()}% دقت",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
