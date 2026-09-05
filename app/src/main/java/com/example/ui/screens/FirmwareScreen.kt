package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FirmwarePack
import com.example.ui.MainViewModel
import com.example.ui.theme.LocalFlipperColors
import com.example.ui.theme.parseColorSafe

@Composable
fun FirmwareScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalFlipperColors.current
    val currentFw by viewModel.currentFirmware.collectAsState()
    val availablePacks by viewModel.firmwareManager.availablePacks.collectAsState()

    var showJsonEditor by remember { mutableStateOf(false) }
    var jsonText by remember { mutableStateOf("") }
    var newProtocolInput by remember { mutableStateOf("") }

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    viewModel.importZipFirmware(inputStream) { result ->
                        result.onSuccess {
                            Toast.makeText(context, "Прошивка '${it.name}' установлена!", Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            Toast.makeText(context, "Ошибка ZIP: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Не удалось прочитать файл: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active Pack Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.5.dp, colors.primary, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Firmware",
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentFw.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }

                        Text(
                            text = "v${currentFw.version}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Автор: ${currentFw.author} | Маскот: [${currentFw.mascotStyle}]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = colors.text.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentFw.description,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.text.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    // Color swatches
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Палитра:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.text.copy(alpha = 0.7f)
                        )
                        ColorSwatch(currentFw.primaryColorHex, "Primary")
                        ColorSwatch(currentFw.backgroundColorHex, "BG")
                        ColorSwatch(currentFw.surfaceColorHex, "Surface")
                        ColorSwatch(currentFw.accentColorHex, "Accent")
                    }
                }
            }
        }

        // Preset Packs
        item {
            Column {
                Text(
                    text = "ДОСТУПНЫЕ ПРОШИВКИ (FIRMWARE PACKS):",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (pack in availablePacks) {
                        val isActive = pack.id == currentFw.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isActive) colors.primary.copy(alpha = 0.15f) else colors.surface)
                                .border(
                                    1.dp,
                                    if (isActive) colors.primary else colors.border,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.applyFirmwarePack(pack) }
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = pack.name,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) colors.primary else colors.text
                                        )
                                        if (isActive) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "[АКТИВНА]",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.terminalGreen
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${pack.description} (${pack.subGhzProtocols.size} протоколов)",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = colors.text.copy(alpha = 0.65f),
                                        maxLines = 1
                                    )
                                }

                                ColorSwatch(pack.primaryColorHex, "")
                            }
                        }
                    }
                }
            }
        }

        // Import Buttons (ZIP / JSON)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { zipPickerLauncher.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("import_zip_pack_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = colors.background,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ИМПОРТ ZIP",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.background
                    )
                }

                OutlinedButton(
                    onClick = {
                        showJsonEditor = !showJsonEditor
                        if (showJsonEditor) {
                            jsonText = generateSampleJson(currentFw)
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("import_json_pack_btn")
                ) {
                    Text(
                        text = if (showJsonEditor) "СКРЫТЬ JSON" else "JSON РЕДАКТОР",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.primary
                    )
                }
            }
        }

        // Expandable JSON Pack Editor
        item {
            AnimatedVisibility(visible = showJsonEditor) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.accent, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "JSON КОНФИГУРАЦИЯ ПРОШИВКИ (MOMENTUM FORMAT):",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = jsonText,
                            onValueChange = { jsonText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.text,
                                unfocusedTextColor = colors.text
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.importJsonFirmware(jsonText) { res ->
                                    res.onSuccess {
                                        Toast.makeText(context, "Пак '${it.name}' успешно сохранен!", Toast.LENGTH_SHORT).show()
                                    }.onFailure {
                                        Toast.makeText(context, "Ошибка JSON: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ПРИМЕНИТЬ JSON",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.background
                            )
                        }
                    }
                }
            }
        }

        // Live Module Switcher
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "ВКЛЮЧЕНИЕ / ВЫКЛЮЧЕНИЕ МОДУЛЕЙ В ПРОШИВКЕ:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val modules = listOf(
                        "nfc" to "NFC 13.56 MHz (ISO 14443 / HCE)",
                        "rfid" to "RFID 125 kHz (Hardware Limit Sandbox)",
                        "badusb" to "BadUSB (DuckyScript Generator)",
                        "subghz" to "Sub-GHz Laboratory (433 MHz RF)",
                        "ble" to "Bluetooth LE Scanner"
                    )

                    for ((modId, modTitle) in modules) {
                        val isEnabled = currentFw.enabledModules.contains(modId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = modTitle,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = if (isEnabled) colors.text else colors.text.copy(alpha = 0.4f)
                            )
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { viewModel.toggleModule(modId, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.primary,
                                    checkedTrackColor = colors.primary.copy(alpha = 0.4f),
                                    uncheckedThumbColor = colors.border,
                                    uncheckedTrackColor = colors.background
                                )
                            )
                        }
                    }
                }
            }
        }

        // Mascot Selector
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "ВЫБОР МАСКОТА И АНИМАЦИИ:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val mascots = listOf(
                        "dolphin" to "Flipper Dolphin (~^з^)~",
                        "cyber_cat" to "Momentum Cat (=^･ω･^=)",
                        "skull" to "Unleashed Skull [ ☠ ★ ]",
                        "robot" to "VT100 Robot d[o_0]b"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for ((id, title) in mascots) {
                            val isSel = currentFw.mascotStyle == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) colors.accent.copy(alpha = 0.25f) else colors.background)
                                    .border(1.dp, if (isSel) colors.accent else colors.border, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.firmwareManager.updateMascot(id) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = id.uppercase(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) colors.accent else colors.text.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sub-GHz Protocols Customizer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "НАЗВАНИЯ ПРОТОКОЛОВ SUB-GHZ В ПРОШИВКЕ:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentFw.subGhzProtocols.joinToString(", "),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = colors.text.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newProtocolInput,
                            onValueChange = { newProtocolInput = it },
                            placeholder = { Text("Новый протокол...", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.text,
                                unfocusedTextColor = colors.text
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                if (newProtocolInput.isNotBlank()) {
                                    val updated = currentFw.subGhzProtocols + newProtocolInput.trim()
                                    viewModel.updateFirmwareProtocols(updated)
                                    newProtocolInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "+ ДОБАВИТЬ",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = colors.background,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(hex: String, label: String) {
    val color = parseColorSafe(hex, Color.White)
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
    )
}

private fun generateSampleJson(pack: FirmwarePack): String {
    return """
{
  "id": "custom_${System.currentTimeMillis() % 1000}",
  "name": "Custom Cyber Pack",
  "version": "1.0.0",
  "author": "User",
  "description": "Мой кастомный пак Flipper Droid",
  "colors": {
    "primary": "#FF8200",
    "background": "#0D0E12",
    "surface": "#171A24",
    "accent": "#FFA533",
    "text": "#FFFFFF"
  },
  "enabledModules": ["nfc", "rfid", "badusb", "subghz", "ble"],
  "subGhzProtocols": ["KeeLoq 64bit", "Came 12bit", "Nice Flo", "Custom-AM"],
  "mascotStyle": "dolphin",
  "terminalPrompt": "flipper@custom:~$ "
}
""".trimIndent()
}
