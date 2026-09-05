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
fun SpectrumCanvas(
    points: List<Float>,
    centerFreqMhz: Double,
    isTransmitting: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalFlipperColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(colors.background, RoundedCornerShape(6.dp))
            .border(1.dp, colors.border, RoundedCornerShape(6.dp))
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // dBm grid: -20 to -100 dBm
            val minDb = -105f
            val maxDb = -20f
            val range = maxDb - minDb

            val gridColor = colors.primary.copy(alpha = 0.12f)
            val yLines = 4
            for (i in 0..yLines) {
                val y = h * (i / yLines.toFloat())
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }

            if (points.size < 2) return@Canvas

            val path = Path()
            val stepX = w / (points.size - 1).toFloat()

            for ((idx, dbVal) in points.withIndex()) {
                val clamped = dbVal.coerceIn(minDb, maxDb)
                val normalized = (clamped - minDb) / range // 0 to 1
                val y = h - (normalized * h)
                val x = idx * stepX
                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            val strokeColor = if (isTransmitting) colors.terminalGreen else colors.accent
            // Glow
            drawPath(
                path = path,
                color = strokeColor.copy(alpha = 0.35f),
                style = Stroke(width = 5f)
            )
            // Trace
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = 2f)
            )

            // Center marker line
            val midX = w / 2f
            drawLine(
                color = colors.primary.copy(alpha = 0.7f),
                start = Offset(midX, 0f),
                end = Offset(midX, h),
                strokeWidth = 1.5f
            )
        }

        Text(
            text = "FC: ${centerFreqMhz} MHz",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = colors.primary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(2.dp)
        )
        Text(
            text = "-20 dBm",
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            color = colors.text.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(2.dp)
        )
        Text(
            text = "-100 dBm (Noise Floor)",
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            color = colors.text.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(2.dp)
        )
        Text(
            text = if (isTransmitting) "[TX ACTIVE]" else "[RX PASSIVE]",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = if (isTransmitting) colors.terminalGreen else colors.text.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
        )
    }
}
