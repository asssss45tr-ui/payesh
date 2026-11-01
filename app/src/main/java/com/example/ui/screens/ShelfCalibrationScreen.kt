package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShelfZoneEntity
import com.example.ui.ShopGuardianViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ShelfCalibrationScreen(
    viewModel: ShopGuardianViewModel
) {
    val shelves by viewModel.allShelves.collectAsState()
    val scope = rememberCoroutineScope()
    var selectedShelf by remember { mutableStateOf<ShelfZoneEntity?>(null) }
    var calibrationMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
            .padding(16.dp)
            .testTag("shelf_calibration_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "کالیبراسیون و مقایسه قفسه‌ها",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ثبت تصویر مرجع و پایش تغییرات چیدمان و موجودی تصویری",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Success / Calibration toast
        AnimatedVisibility(visible = calibrationMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(12.dp),
                color = GuardGreen.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GuardGreen)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GuardGreen)
                    Text(text = calibrationMessage ?: "", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // Shelf list cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(shelves) { shelf ->
                ShelfZoneCard(
                    shelf = shelf,
                    onCalibrateReference = {
                        scope.launch {
                            val updated = shelf.copy(
                                isChanged = false,
                                lastUpdated = System.currentTimeMillis()
                            )
                            viewModel.repository.updateShelf(updated)
                            calibrationMessage = "تصویر مرجع ${shelf.name} با موفقیت به‌روزرسانی و ثبت شد"
                        }
                    },
                    onToggleDifferenceTest = {
                        scope.launch {
                            val updated = shelf.copy(
                                isChanged = !shelf.isChanged,
                                currentProductCount = if (shelf.isChanged) shelf.expectedProductCount else (shelf.expectedProductCount - 1),
                                lastUpdated = System.currentTimeMillis()
                            )
                            viewModel.repository.updateShelf(updated)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ShelfZoneCard(
    shelf: ShelfZoneEntity,
    onCalibrateReference: () -> Unit,
    onToggleDifferenceTest: () -> Unit
) {
    val statusColor = if (shelf.isChanged) AlarmRed else GuardGreen

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shelf_card_${shelf.id}"),
        shape = RoundedCornerShape(16.dp),
        color = SlateSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (shelf.isChanged) AlarmRed else SlateBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(text = shelf.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "شناسه قفسه: ${shelf.id}", color = TextMuted, fontSize = 11.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, statusColor)
                ) {
                    Text(
                        text = if (shelf.isChanged) "تغییر چیدمان / کسری کالا" else "چیدمان پایدار و منطبق",
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = SlateBorder)

            // Layout & Count Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "موجودی اولیه مرجع", color = TextSecondary, fontSize = 11.sp)
                    Text(text = "${shelf.expectedProductCount} قلم کالا", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text(text = "موجودی شمارش‌شده فعلی", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = "${shelf.currentProductCount} قلم کالا",
                        color = if (shelf.isChanged) AlarmRed else GuardGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(text = "مختصات پایش", color = TextSecondary, fontSize = 11.sp)
                    Text(text = "[${(shelf.xMin * 100).toInt()}% تا ${(shelf.xMax * 100).toInt()}%]", color = CyberCyan, fontSize = 12.sp)
                }
            }

            // Calibration actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onToggleDifferenceTest,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                ) {
                    Text(
                        text = if (shelf.isChanged) "بازنشانی تغییرات" else "آزمون تغییر چیدمان",
                        color = TextPrimary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onCalibrateReference,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("ثبت تصویر مرجع", color = SlateDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
