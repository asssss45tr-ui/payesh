package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.EventEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationDialog(
    event: EventEntity,
    onDismiss: () -> Unit,
    onConfirm: (
        correctedName: String,
        correctedType: String,
        status: String,
        notes: String
    ) -> Unit
) {
    var productName by remember { mutableStateOf(event.productName) }
    var interactionType by remember { mutableStateOf(event.interactionType) }
    var status by remember { mutableStateOf(event.status) }
    var userNotes by remember { mutableStateOf(event.userNotes ?: "") }

    val interactionOptions = listOf(
        "برداشتن کالا",
        "جابه‌جایی کالا",
        "گذاشتن مجدد",
        "خروج از دید",
        "تماس بدون تغییر"
    )

    val statusOptions = listOf(
        "تأیید شده",
        "نیازمند بررسی",
        "نامشخص",
        "رد شده"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
                .testTag("verification_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = SlateSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                    }
                    Text(
                        text = "تأیید و اصلاح هوشمند رویداد",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = SlateBorder)

                // Product Name Field
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("نام دقیق یا اصلاح‌شده کالا", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_product_name_input"),
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = CyberCyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateBorder
                    )
                )

                // Interaction Type Selector
                Text(
                    text = "نوع تعامل مشتری با کالا:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    interactionOptions.take(3).forEach { option ->
                        FilterChip(
                            selected = interactionType == option,
                            onClick = { interactionType = option },
                            label = { Text(option, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    interactionOptions.drop(3).forEach { option ->
                        FilterChip(
                            selected = interactionType == option,
                            onClick = { interactionType = option },
                            label = { Text(option, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }

                // Final Status Selector
                Text(
                    text = "وضعیت تأیید کارشناس:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statusOptions.forEach { opt ->
                        FilterChip(
                            selected = status == opt,
                            onClick = { status = opt },
                            label = { Text(opt, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (opt == "تأیید شده") GuardGreen.copy(alpha = 0.25f) else AlertAmber.copy(alpha = 0.25f),
                                selectedLabelColor = if (opt == "تأیید شده") GuardGreen else AlertAmber
                            )
                        )
                    }
                }

                // Notes Field
                OutlinedTextField(
                    value = userNotes,
                    onValueChange = { userNotes = it },
                    label = { Text("یادداشت و توضیحات تکمیلی (اختیاری)", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_notes_input"),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SlateBorder
                    )
                )

                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                    ) {
                        Text("انصراف", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            onConfirm(productName, interactionType, status, userNotes)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("confirm_verification_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                    ) {
                        Text("ثبت و تأیید", color = SlateDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
