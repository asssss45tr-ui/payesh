package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ShopGuardianUiState
import com.example.ui.ShopGuardianViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: ShopGuardianViewModel,
    uiState: ShopGuardianUiState
) {
    val scrollState = rememberScrollState()

    var alertOnInteraction by remember { mutableStateOf(true) }
    var alertOnDisplacement by remember { mutableStateOf(true) }
    var alertOnShelfEmpty by remember { mutableStateOf(true) }
    var countdownDurationSec by remember { mutableStateOf(3f) }
    var aiSensitivity by remember { mutableStateOf(0.85f) }

    val modules = listOf(
        Pair("Camera Engine", "فعال و آماده اتصال"),
        Pair("Human Detection Engine", "تشخیص انسان با کادر اختصاصی"),
        Pair("Hand Detection Engine", "تشخیص دست و ردیابی حرکت به قفسه"),
        Pair("Object Detection Engine", "شناسایی کالا و کادرگذاری رنگی"),
        Pair("Object Tracking Engine", "ردیابی مکانی کالا در فریم‌های متوالی"),
        Pair("Customer Session Manager", "شناسه موقت و محاسبه مدت حضور"),
        Pair("Product Recognition Engine", "برش تصویر، رنگ غالب و تطبیق الگو"),
        Pair("Internet Search Adapter", "جستجوی اینترنتی مشخصات کالا"),
        Pair("Shelf Change Detection", "پایش چیدمان و خالی شدن قفسه"),
        Pair("Event Recorder", "شمارش معکوس و ضبط پیوسته"),
        Pair("Frame Capture Manager", "ثبت شواهد ۵ گانه قبل، حین و بعد"),
        Pair("Event Analysis Engine", "تحلیل رفتار چندمرحله‌ای مشتری"),
        Pair("Report Generator", "تولید خودکار گزارش تحلیلی فارسی"),
        Pair("Local Storage Engine", "ذخیره‌سازی Room و مدیریت فایل‌ها"),
        Pair("Archive Manager", "موتور جستجوی طبیعی و خروجی شواهد"),
        Pair("Notification Manager", "مدیریت هشدارها و اعلان‌های نظارتی"),
        Pair("Settings Manager", "کنترل پیکربندی و پارامترهای پایش")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Column {
            Text(
                text = "تنظیمات سامانه Shop Guardian AI",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "پیکربندی هوش مصنوعی، هشدارها، امنیت و وضعیت ماژول‌ها",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // 1. Alerts & Notifications Section (Section 18)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = AlertAmber)
                    Text("تنظیمات هشدار و اعلان‌های نظارتی", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = SlateBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("هشدار هنگام تماس دست با کالا", color = TextSecondary, fontSize = 12.5.sp)
                    Switch(
                        checked = alertOnInteraction,
                        onCheckedChange = { alertOnInteraction = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("هشدار جابه‌جایی کالا در قفسه", color = TextSecondary, fontSize = 12.5.sp)
                    Switch(
                        checked = alertOnDisplacement,
                        onCheckedChange = { alertOnDisplacement = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("هشدار خالی شدن جایگاه کالا", color = TextSecondary, fontSize = 12.5.sp)
                    Switch(
                        checked = alertOnShelfEmpty,
                        onCheckedChange = { alertOnShelfEmpty = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
                    )
                }
            }
        }

        // 2. AI Parameters & Timers
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = CyberCyan)
                    Text("پارامترهای پردازش و زمان‌سنجی", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = SlateBorder)

                // Countdown Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("زمان شمارش معکوس پایان رویداد پس از خروج مشتری:", color = TextSecondary, fontSize = 12.sp)
                        Text("${countdownDurationSec.toInt()} ثانیه", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = countdownDurationSec,
                        onValueChange = { countdownDurationSec = it },
                        valueRange = 2f..10f,
                        steps = 7,
                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                    )
                }

                // AI Confidence Sensitivity Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("حداقل آستانه اطمینان هوش مصنوعی:", color = TextSecondary, fontSize = 12.sp)
                        Text("${(aiSensitivity * 100).toInt()}%", color = GuardGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = aiSensitivity,
                        onValueChange = { aiSensitivity = it },
                        valueRange = 0.5f..0.95f,
                        colors = SliderDefaults.colors(thumbColor = GuardGreen, activeTrackColor = GuardGreen)
                    )
                }
            }
        }

        // 3. Privacy & Ethics Assurance (Section 20)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GuardGreen.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = GuardGreen)
                    Text("انطباق با اصول حریم خصوصی و امنیت (Privacy by Design)", color = GuardGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "• شناسه‌های اختصاص داده شده به مشتریان صرفاً موقت، عددی و محلی بوده و هیچ‌گونه تشخیص چهره، نام، بیومتریک یا هویت واقعی اشخاص انجام نمی‌شود.\n• کلیه پردازش‌های بینایی ماشین و ذخیره شواهد به صورت کاملاً آفلاین روی دستگاه انجام می‌گردد.\n• سیستم بر اصل بی‌طرفی طراحی شده و خروج کالا از زاویه دید دوربین را به منزله سرقت تلقی نمی‌کند.",
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // 4. The 17 Core Modules Architecture Status (Section 19)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SlateSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = CyberCyan)
                    Text("وضعیت معماری ۱۷ ماژول سامانه", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = SlateBorder)

                modules.forEachIndexed { idx, (name, desc) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("${idx + 1}.", color = TextMuted, fontSize = 11.sp)
                            Text(name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GuardGreen.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(0.6.dp, GuardGreen)
                        ) {
                            Text(desc, color = GuardGreen, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}
