package com.example.ui.screens

import android.Manifest
import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ai.*
import com.example.camera.CameraManager
import com.example.ui.ShopGuardianUiState
import com.example.ui.ShopGuardianViewModel
import com.example.ui.components.CameraBoundingBoxesOverlay
import com.example.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SurveillanceScreen(
    viewModel: ShopGuardianViewModel,
    uiState: ShopGuardianUiState,
    onNavigateToEvents: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val telemetry by viewModel.telemetry.collectAsState()
    val humans by viewModel.activeHumans.collectAsState()
    val hands by viewModel.activeHands.collectAsState()
    val products by viewModel.activeProducts.collectAsState()
    val shelvesSlot by viewModel.simulationEngine.shelves.collectAsState()
    val events by viewModel.allEvents.collectAsState()

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    var cameraManager by remember { mutableStateOf<CameraManager?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraManager?.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
            .testTag("surveillance_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        .background(ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Shop Guardian AI",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "سامانه هوشمند پایش و تحلیل قفسه",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Mode Selector Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SlateSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.testTag("mode_selector_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (uiState.isSimulatingMode) "شبیه‌ساز سناریو" else "دوربین زنده",
                        color = if (uiState.isSimulatingMode) CyberCyan else GuardGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = { viewModel.toggleSimulationMode() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isSimulatingMode) Icons.Default.SmartToy else Icons.Default.CameraAlt,
                            contentDescription = "تغییر حالت",
                            tint = if (uiState.isSimulatingMode) CyberCyan else GuardGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Camera / Simulation Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SlateSurface)
                .border(1.5.dp, SlateBorder, RoundedCornerShape(18.dp))
                .testTag("surveillance_viewport")
        ) {
            if (!uiState.isSimulatingMode && cameraPermissionState.status.isGranted) {
                // Real CameraX Preview
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            previewViewRef = this
                            val manager = CameraManager(ctx, lifecycleOwner)
                            cameraManager = manager
                            manager.startCamera(this) { err ->
                                // fallback to simulation if camera error occurs
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Simulated Retail Viewport Background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A))
                )
            }

            // Real-time HUD and Bounding Box Overlay
            CameraBoundingBoxesOverlay(
                telemetry = telemetry,
                humans = humans,
                hands = hands,
                products = products,
                shelves = shelvesSlot
            )

            // Viewport Hardware Action Controls (Top Overlay)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Torch toggle
                if (!uiState.isSimulatingMode) {
                    IconButton(
                        onClick = {
                            val active = cameraManager?.toggleTorch() ?: false
                            isTorchOn = active
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(SlateDark.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "فلش",
                            tint = if (isTorchOn) AlertAmber else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Switch camera
                    IconButton(
                        onClick = {
                            previewViewRef?.let { cameraManager?.switchLens(it) }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(SlateDark.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "تغییر دوربین",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Live Telemetry KPI Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                modifier = Modifier.weight(1f),
                title = "مشتریان حاضر",
                value = "${telemetry.customerCount}",
                icon = Icons.Default.People,
                color = CyberCyan
            )
            KpiCard(
                modifier = Modifier.weight(1f),
                title = "رویدادهای امروز",
                value = "${events.size}",
                icon = Icons.Default.NotificationsActive,
                color = AlertAmber
            )
            KpiCard(
                modifier = Modifier.weight(1f),
                title = "اقلام تحت نظر",
                value = "${products.size}",
                icon = Icons.Default.ShoppingBag,
                color = GuardGreen
            )
            KpiCard(
                modifier = Modifier.weight(1f),
                title = "حافظه اشغالی",
                value = "${uiState.storageUsageMb} MB",
                icon = Icons.Default.Storage,
                color = HandPurple
            )
        }

        // Scenario Simulation Triggers & Actions
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
            shape = RoundedCornerShape(16.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اجرای سناریوی آزمون هوش مصنوعی:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(onClick = onNavigateToEvents) {
                        Text("مشاهده پرونده‌ها (${events.size})", color = CyberCyan, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.triggerScenario(SimulationScenario.PICKUP_AND_LEAVE) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("scenario_pickup_leave_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AlarmRed.copy(alpha = 0.25f))
                    ) {
                        Text("برداشتن و خروج", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.triggerScenario(SimulationScenario.PICKUP_AND_RESTORE) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("scenario_restore_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GuardGreen.copy(alpha = 0.25f))
                    ) {
                        Text("برداشتن و بازگرداندن", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.triggerScenario(SimulationScenario.DISPLACE_ON_SHELF) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("scenario_displace_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertAmber.copy(alpha = 0.25f))
                    ) {
                        Text("جابه‌جایی کالا", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SlateSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Text(text = value, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = title, color = TextSecondary, fontSize = 9.5.sp, textAlign = TextAlign.Center)
        }
    }
}
