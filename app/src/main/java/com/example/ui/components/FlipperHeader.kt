package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FirmwarePack
import com.example.ui.MascotMood
import com.example.ui.MascotState
import com.example.ui.theme.LocalFlipperColors

@Composable
fun FlipperHeader(
    firmware: FirmwarePack,
    mascotState: MascotState,
    isNfcActive: Boolean,
    isBleActive: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalFlipperColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Top status indicators line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.primary)
                        .alpha(blinkAlpha)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = firmware.terminalPrompt.trim(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isNfcActive) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "NFC active",
                        tint = colors.terminalGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
                if (isBleActive) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = "BLE active",
                        tint = colors.accent,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SdCard,
                        contentDescription = "SD Status",
                        tint = colors.text.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "SD:OK",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = colors.text.copy(alpha = 0.7f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = colors.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "97%",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Mascot banner in retro Flipper style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background, RoundedCornerShape(6.dp))
                .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ASCII mascot avatar
            val mascotFace = when (firmware.mascotStyle) {
                "cyber_cat" -> when (mascotState.mood) {
                    MascotMood.HAPPY -> "(=^･ω･^=)b"
                    MascotMood.SCANNING -> "(=✧ω✧=)?"
                    MascotMood.TRANSMIT -> "(=ↀωↀ=)⚡"
                    MascotMood.SLEEP -> "(= -ω- =)zzZ"
                    MascotMood.IDLE -> "(=^･ｪ･^=)"
                }
                "skull" -> when (mascotState.mood) {
                    MascotMood.HAPPY -> "[ ☠ ★ ]"
                    MascotMood.SCANNING -> "[ ☠ ?? ]"
                    MascotMood.TRANSMIT -> "[ ☠ >> ]"
                    MascotMood.SLEEP -> "[ ☠ .. ]"
                    MascotMood.IDLE -> "[ ☠ ^_^ ]"
                }
                "robot" -> when (mascotState.mood) {
                    MascotMood.HAPPY -> "d[o_0]b"
                    MascotMood.SCANNING -> "?[O_o]?"
                    MascotMood.TRANSMIT -> ">[O_O]⚡"
                    MascotMood.SLEEP -> "-[ -_- ]-"
                    MascotMood.IDLE -> "[o_o]"
                }
                else -> when (mascotState.mood) {
                    MascotMood.HAPPY -> "(~‾▿‾)~ <3"
                    MascotMood.SCANNING -> "(~◎_◎~)?"
                    MascotMood.TRANSMIT -> "(~>_<~)⚡"
                    MascotMood.SLEEP -> "(~-_-)~ zzZ"
                    MascotMood.IDLE -> "(~^з^)~"
                }
            }

            Box(
                modifier = Modifier
                    .background(colors.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = mascotFace,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = colors.primary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FLIPPER DROID",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Text(
                        text = "LVL ${mascotState.level} | XP ${mascotState.xp}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = colors.accent
                    )
                }
                Text(
                    text = mascotState.message,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.text.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }
    }
}
