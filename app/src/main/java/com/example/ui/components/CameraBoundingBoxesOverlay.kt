package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.*
import com.example.ui.theme.*

@Composable
fun CameraBoundingBoxesOverlay(
    modifier: Modifier = Modifier,
    telemetry: VisionTelemetry,
    humans: List<DetectedHuman>,
    hands: List<DetectedHand>,
    products: List<DetectedProduct>,
    shelves: List<ShelfSlot>
) {
    // Pulse animation for recording REC indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(modifier = modifier.fillMaxSize().testTag("camera_bounding_boxes_overlay")) {
        // Draw real-time bounding boxes via Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Draw Shelf Zones (Dashed Amber/Yellow)
            val shelfDash = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            for (shelf in shelves) {
                val left = shelf.bounds.left * canvasWidth
                val top = shelf.bounds.top * canvasHeight
                val width = shelf.bounds.width * canvasWidth
                val height = shelf.bounds.height * canvasHeight

                drawRect(
                    color = AlertAmber.copy(alpha = 0.4f),
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = 3.dp.toPx(), pathEffect = shelfDash)
                )
            }

            // 2. Draw Humans (Cyber Cyan with corner brackets)
            for (human in humans) {
                val left = human.bounds.left * canvasWidth
                val top = human.bounds.top * canvasHeight
                val width = human.bounds.width * canvasWidth
                val height = human.bounds.height * canvasHeight

                // Fill tint
                drawRect(
                    color = CyberCyan.copy(alpha = 0.08f),
                    topLeft = Offset(left, top),
                    size = Size(width, height)
                )
                // Border box
                drawRect(
                    color = CyberCyan,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // 3. Draw Hands (Purple with targeting crosshair)
            for (hand in hands) {
                val left = hand.bounds.left * canvasWidth
                val top = hand.bounds.top * canvasHeight
                val width = hand.bounds.width * canvasWidth
                val height = hand.bounds.height * canvasHeight

                drawRect(
                    color = HandPurple,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = 2.5.dp.toPx())
                )
                // Center crosshair
                val cx = left + width / 2f
                val cy = top + height / 2f
                drawLine(
                    color = HandPurple,
                    start = Offset(cx - 12f, cy),
                    end = Offset(cx + 12f, cy),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = HandPurple,
                    start = Offset(cx, cy - 12f),
                    end = Offset(cx, cy + 12f),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 4. Draw Products (Guard Green or Alert Amber)
            for (prod in products) {
                val left = prod.bounds.left * canvasWidth
                val top = prod.bounds.top * canvasHeight
                val width = prod.bounds.width * canvasWidth
                val height = prod.bounds.height * canvasHeight

                val boxColor = if (prod.isMoved) AlarmRed else GuardGreen

                drawRect(
                    color = boxColor,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }

        // Top HUD Bar: AI Performance, FPS & Recording status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: FPS & Latency
            Row(
                modifier = Modifier
                    .background(SlateDark.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                    .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${telemetry.fps.toInt()} FPS",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${telemetry.latencyMs}ms",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Right: REC indicator & Countdown
            if (telemetry.status == MonitoringStatus.MONITORING_ACTIVE ||
                telemetry.status == MonitoringStatus.INTERACTION_DETECTED ||
                telemetry.status == MonitoringStatus.COUNTDOWN_STOPPING
            ) {
                Row(
                    modifier = Modifier
                        .background(AlarmRed.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .border(1.dp, AlarmRed, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Recording",
                        tint = AlarmRed.copy(alpha = alphaAnim),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (telemetry.countdownRemaining > 0) {
                            "توقف در ${telemetry.countdownRemaining} ثانیه"
                        } else {
                            "REC ضبط هوشمند"
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .background(SlateDark.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                        .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(GuardGreen, CircleShape)
                    )
                    Text(
                        text = "پایش فعال",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Center Action Notification Banner
        if (telemetry.statusMessage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .background(SlateDark.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = telemetry.statusMessage,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
