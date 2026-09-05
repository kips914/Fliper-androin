package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.model.FirmwarePack

data class FlipperColors(
    val primary: Color,
    val background: Color,
    val surface: Color,
    val accent: Color,
    val text: Color,
    val terminalGreen: Color = Color(0xFF39FF14),
    val terminalAmber: Color = Color(0xFFFFB000),
    val terminalRed: Color = Color(0xFFFF453A),
    val cardBackground: Color = Color(0xFF161922),
    val border: Color = Color(0xFF2A2E3D)
)

val LocalFlipperColors = staticCompositionLocalOf {
    FlipperColors(
        primary = Color(0xFFFF8200),
        background = Color(0xFF0F1117),
        surface = Color(0xFF1B1E28),
        accent = Color(0xFFFFA233),
        text = Color(0xFFFFFFFF)
    )
}

fun parseColorSafe(hex: String, fallback: Color): Color {
    return try {
        val clean = hex.trim().removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> (0xFF000000 or clean.toLong(16)).toInt()
            8 -> clean.toLong(16).toInt()
            else -> return fallback
        }
        Color(colorInt)
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun FlipperDroidTheme(
    firmwarePack: FirmwarePack,
    content: @Composable () -> Unit
) {
    val primaryColor = parseColorSafe(firmwarePack.primaryColorHex, Color(0xFFFF8200))
    val bgColor = parseColorSafe(firmwarePack.backgroundColorHex, Color(0xFF0F1117))
    val surfaceColor = parseColorSafe(firmwarePack.surfaceColorHex, Color(0xFF1B1E28))
    val accentColor = parseColorSafe(firmwarePack.accentColorHex, Color(0xFFFFA233))
    val textColor = parseColorSafe(firmwarePack.textColorHex, Color(0xFFFFFFFF))

    val flipperColors = FlipperColors(
        primary = primaryColor,
        background = bgColor,
        surface = surfaceColor,
        accent = accentColor,
        text = textColor,
        cardBackground = surfaceColor.copy(alpha = 0.95f),
        border = primaryColor.copy(alpha = 0.35f)
    )

    val colorScheme: ColorScheme = darkColorScheme(
        primary = primaryColor,
        onPrimary = Color.Black,
        primaryContainer = primaryColor.copy(alpha = 0.2f),
        onPrimaryContainer = primaryColor,
        background = bgColor,
        onBackground = textColor,
        surface = surfaceColor,
        onSurface = textColor,
        secondary = accentColor,
        onSecondary = Color.Black
    )

    CompositionLocalProvider(LocalFlipperColors provides flipperColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
