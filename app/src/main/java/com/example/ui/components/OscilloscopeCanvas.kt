package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalFlipperColors

@Composable
fun OscilloscopeCanvas(
    pulses: List<Boolean>,
    highUs: Int,
    lowUs: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalFlipperColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(colors.background, RoundedCornerShape(6.dp))
            .border(1.dp, colors.border, RoundedCornerShape(6.dp))
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val gridColor = colors.primary.copy(alpha = 0.12f)

            // Draw grid lines
            val stepY = h / 4f
            for (i in 1..3) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, stepY * i),
                    end = Offset(w, stepY * i),
                    strokeWidth = 1f
                )
            }
            val stepX = w / 8f
            for (i in 1..7) {
                drawLine(
                    color = gridColor,
                    start = Offset(stepX * i, 0f),
                    end = Offset(stepX * i, h),
                    strokeWidth = 1f
                )
            }

            if (pulses.isEmpty()) return@Canvas

            val displayPulses = pulses.take(64)
            val segmentWidth = w / displayPulses.size.toFloat()
            val highY = h * 0.25f
            val lowY = h * 0.75f

            val wavePath = Path()
            var currentY = if (displayPulses[0]) highY else lowY
            wavePath.moveTo(0f, currentY)

            for ((idx, isHigh) in displayPulses.withIndex()) {
                val nextY = if (isHigh) highY else lowY
                val x = idx * segmentWidth
                val nextX = (idx + 1) * segmentWidth

                // Vertical transition if state changed
                if (nextY != currentY) {
                    wavePath.lineTo(x, nextY)
                    currentY = nextY
                }
                wavePath.lineTo(nextX, currentY)
            }

            // Glow stroke
            drawPath(
                path = wavePath,
                color = colors.primary.copy(alpha = 0.3f),
                style = Stroke(width = 6f)
            )
            // Core signal stroke
            drawPath(
                path = wavePath,
                color = colors.primary,
                style = Stroke(width = 2.5f)
            )
        }

        // Overlay labels
        Text(
            text = "HIGH: ${highUs}μs",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = colors.primary,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
        )
        Text(
            text = "LOW: ${lowUs}μs",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = colors.text.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
        )
        Text(
            text = "DIGITAL OOK WAVEFORM",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = colors.accent,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        )
    }
}
