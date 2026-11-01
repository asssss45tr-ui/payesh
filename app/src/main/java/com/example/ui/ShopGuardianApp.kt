package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.VerificationDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

@Composable
fun ShopGuardianApp(
    viewModel: ShopGuardianViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Force RTL layout direction for Persian interface
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ShopGuardianTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize().testTag("shop_guardian_scaffold"),
                containerColor = SlateDark,
                bottomBar = {
                    if (uiState.selectedEvent == null) {
                        NavigationBar(
                            containerColor = SlateSurface,
                            tonalElevation = 8.dp,
                            windowInsets = WindowInsets.navigationBars,
                            modifier = Modifier.testTag("main_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = uiState.selectedTab == 0,
                                onClick = { viewModel.setTab(0) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "پایش زنده"
                                    )
                                },
                                label = { Text("پایش زنده", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    indicatorColor = CyberCyan.copy(alpha = 0.15f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )

                            NavigationBarItem(
                                selected = uiState.selectedTab == 1,
                                onClick = { viewModel.setTab(1) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FormatListBulleted,
                                        contentDescription = "رویدادها"
                                    )
                                },
                                label = { Text("رویدادها", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    indicatorColor = CyberCyan.copy(alpha = 0.15f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )

                            NavigationBarItem(
                                selected = uiState.selectedTab == 2,
                                onClick = { viewModel.setTab(2) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = "قفسه‌ها"
                                    )
                                },
                                label = { Text("قفسه‌ها", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    indicatorColor = CyberCyan.copy(alpha = 0.15f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )

                            NavigationBarItem(
                                selected = uiState.selectedTab == 3,
                                onClick = { viewModel.setTab(3) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "آرشیو"
                                    )
                                },
                                label = { Text("آرشیو", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    indicatorColor = CyberCyan.copy(alpha = 0.15f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )

                            NavigationBarItem(
                                selected = uiState.selectedTab == 4,
                                onClick = { viewModel.setTab(4) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "تنظیمات"
                                    )
                                },
                                label = { Text("تنظیمات", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    indicatorColor = CyberCyan.copy(alpha = 0.15f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (uiState.selectedEvent != null) {
                        EventDetailScreen(
                            event = uiState.selectedEvent!!,
                            viewModel = viewModel,
                            onBack = { viewModel.selectEvent(null) }
                        )
                    } else {
                        when (uiState.selectedTab) {
                            0 -> SurveillanceScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onNavigateToEvents = { viewModel.setTab(1) }
                            )
                            1 -> EventsScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onEventSelected = { ev -> viewModel.selectEvent(ev) }
                            )
                            2 -> ShelfCalibrationScreen(viewModel = viewModel)
                            3 -> ArchiveScreen(
                                viewModel = viewModel,
                                onEventSelected = { ev -> viewModel.selectEvent(ev) }
                            )
                            4 -> SettingsScreen(
                                viewModel = viewModel,
                                uiState = uiState
                            )
                        }
                    }

                    // Verification & Correction Dialog
                    if (uiState.isVerificationDialogOpen && uiState.verificationTargetEvent != null) {
                        VerificationDialog(
                            event = uiState.verificationTargetEvent!!,
                            onDismiss = { viewModel.closeVerificationDialog() },
                            onConfirm = { name, type, status, notes ->
                                viewModel.confirmVerification(
                                    eventId = uiState.verificationTargetEvent!!.id,
                                    correctedName = name,
                                    correctedType = type,
                                    status = status,
                                    notes = notes
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
