package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.EventEntity
import com.example.ui.ShopGuardianViewModel
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: EventEntity,
    viewModel: ShopGuardianViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var activeFrameTab by remember { mutableStateOf(0) } // 0..4 for the 5 frames
    val webSearchResult = remember(event.productName, event.dominantColor) {
        viewModel.productRecognizer.searchProductInfo(event.dominantColor, event.productName)
    }

    val frames = listOf(
        Pair("قبل از رویداد", event.beforeFramePath),
        Pair("شروع تعامل", event.startFramePath),
        Pair("هنگام برداشتن", event.pickupFramePath),
        Pair("بعد از تعامل", event.afterFramePath),
        Pair("پایان رویداد", event.finalFramePath)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
            .testTag("event_detail_screen")
    ) {
        // Top App Bar
        Surface(
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, SlateBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("event_detail_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "شواهد تصویری و پرونده ${event.id}",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, event.aiReportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری گزارش نظارتی"))
                    }
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "اشتراک‌گذاری", tint = CyberCyan)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Five Chronological Evidence Frames Gallery (Section 8)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = SlateSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Text(
                                text = "مجموعه فریم‌های پنج‌گانه رویداد",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${activeFrameTab + 1} از ۵",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Main Frame Viewer
                    val currentPath = frames[activeFrameTab].second
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateDark)
                            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentPath != null && File(currentPath).exists()) {
                            AsyncImage(
                                model = File(currentPath),
                                contentDescription = frames[activeFrameTab].first,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.ImageNotSupported, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                                Text(frames[activeFrameTab].first, color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        // Bottom label badge inside viewer
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = SlateDark.copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, CyberCyan)
                        ) {
                            Text(
                                text = frames[activeFrameTab].first,
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // 5-Frame Navigation Thumbnails
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        frames.forEachIndexed { index, (label, path) ->
                            val isSelected = activeFrameTab == index
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { activeFrameTab = index }
                                    .border(
                                        width = if (isSelected) 2.dp else 0.8.dp,
                                        color = if (isSelected) CyberCyan else SlateBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                color = if (isSelected) CyberCyan.copy(alpha = 0.15f) else SlateDark
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (path != null && File(path).exists()) {
                                        AsyncImage(
                                            model = File(path),
                                            contentDescription = label,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            color = if (isSelected) CyberCyan else TextSecondary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Product Identification & Web Search Results (Section 6 & 7)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = SlateSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.TravelExplore, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(18.dp))
                            Text(
                                text = "شناسایی هوشمند کالا و جستجوی اینترنتی",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GuardGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, GuardGreen)
                        ) {
                            Text(
                                text = "${webSearchResult.matchPercentage}% تطبیق",
                                color = GuardGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cropped Product Image
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SlateDark)
                                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (event.croppedProductPath != null && File(event.croppedProductPath).exists()) {
                                AsyncImage(
                                    model = File(event.croppedProductPath),
                                    contentDescription = "برش کالا",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = TextSecondary)
                            }
                        }

                        // Product Extracted Attributes
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = webSearchResult.productName,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "برند: ${webSearchResult.brand} • مدل: ${webSearchResult.model}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "رنگ غالب: ${event.dominantColor}",
                                color = CyberCyan,
                                fontSize = 11.sp
                            )
                            Text(
                                text = webSearchResult.description,
                                color = TextMuted,
                                fontSize = 10.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // 3. AI Analytical Persian Report (Section 12)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = SlateSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Text(
                                text = "گزارش تحلیلی و شواهد هوش مصنوعی",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SlateDark,
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, SlateBorder)
                    ) {
                        Text(
                            text = event.aiReportText,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // 4. Verification and Manual Override Action (Section 15)
            Button(
                onClick = { viewModel.openVerificationDialog(event) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("event_detail_verify_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = SlateDark)
                    Text(
                        text = if (event.verifiedByUser) "ویرایش مجدد تأییدیه کارشناس" else "تأیید یا اصلاح اطلاعات رویداد",
                        color = SlateDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
