package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventEntity
import com.example.ui.ShopGuardianViewModel
import com.example.ui.theme.*

@Composable
fun ArchiveScreen(
    viewModel: ShopGuardianViewModel,
    onEventSelected: (EventEntity) -> Unit
) {
    val context = LocalContext.current
    val events by viewModel.allEvents.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Quick natural query suggestions
    val suggestions = listOf(
        "کالای قرمز",
        "جابه‌جا شده",
        "قفسه ۱",
        "قفسه ۲",
        "بازگردانده شده",
        "نیازمند بررسی"
    )

    val matchedEvents = remember(events, searchQuery) {
        if (searchQuery.isBlank()) {
            events
        } else {
            val q = searchQuery.trim().lowercase()
            events.filter { event ->
                event.productName.lowercase().contains(q) ||
                        event.dominantColor.lowercase().contains(q) ||
                        event.interactionType.lowercase().contains(q) ||
                        event.shelfName.lowercase().contains(q) ||
                        event.status.lowercase().contains(q) ||
                        event.customerId.lowercase().contains(q) ||
                        event.aiReportText.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
            .padding(16.dp)
            .testTag("archive_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "آرشیو و جستجوی هوشمند پرونده‌ها",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "پایگاه داده محلی شواهد، فریم‌ها و ویدیوهای نظارتی",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Storage Usage Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(HandPurple.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, tint = HandPurple)
                    }
                    Column {
                        Text(text = "ساختار ذخیره‌سازی محلی (ShopGuardian/)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "حجم اشغالی: ${uiState.storageUsageMb} مگابایت • ${events.size} رویداد ثبت‌شده", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                IconButton(onClick = { showDeleteConfirmDialog = true }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "پاکسازی", tint = AlarmRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Natural Language Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("جستجوی طبیعی (مثلاً: کالای قرمز، قفسه ۱، جابه‌جا شده...)", color = TextSecondary, fontSize = 12.sp) },
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
                .testTag("archive_natural_search_input"),
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

        Spacer(modifier = Modifier.height(8.dp))

        // Query Suggestions Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(suggestions) { tag ->
                SuggestionChip(
                    onClick = { searchQuery = tag },
                    label = { Text(tag, fontSize = 11.sp, color = if (searchQuery == tag) CyberCyan else TextSecondary) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SlateSurface),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = if (searchQuery == tag) CyberCyan else SlateBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Export All Reports Button
        if (matchedEvents.isNotEmpty()) {
            OutlinedButton(
                onClick = {
                    val fullReport = StringBuilder()
                    fullReport.append("گزارش جامع رویدادهای فروشگاه - Shop Guardian AI\n\n")
                    matchedEvents.forEach { ev ->
                        fullReport.append("--------------------------------------\n")
                        fullReport.append(ev.aiReportText)
                        fullReport.append("\n\n")
                    }
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, fullReport.toString())
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(intent, "ارسال خروجی گزارش‌ها"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                    Text("خروجی گرفتن از تمام پرونده‌های انتخابی (${matchedEvents.size})", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Matched Events List
        if (matchedEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("موردی با جستجوی «$searchQuery» در آرشیو پیدا نشد", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(matchedEvents, key = { it.id }) { ev ->
                    EventCard(
                        event = ev,
                        onClick = { onEventSelected(ev) },
                        onVerify = { viewModel.openVerificationDialog(ev) }
                    )
                }
            }
        }
    }

    // Confirmation Dialog for Data Purge
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("پاکسازی کامل آرشیو و شواهد", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("آیا مطمئن هستید که می‌خواهید تمام رویدادها، تصاویر پنج‌گانه و فریم‌های ضبط‌شده را پاک کنید؟ این عملیات غیرقابل بازگشت است.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllArchive()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlarmRed)
                ) {
                    Text("بله، پاکسازی شود", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("انصراف", color = TextSecondary)
                }
            },
            containerColor = SlateSurface
        )
    }
}
