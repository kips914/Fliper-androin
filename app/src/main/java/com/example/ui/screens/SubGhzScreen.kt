package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.OscilloscopeCanvas
import com.example.ui.components.SpectrumCanvas
import com.example.ui.theme.LocalFlipperColors

@Composable
fun SubGhzScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalFlipperColors.current
    val firmware by viewModel.currentFirmware.collectAsState()
    val frequency by viewModel.subGhzFrequency.collectAsState()
    val protocol by viewModel.subGhzProtocol.collectAsState()
    val modulation by viewModel.subGhzModulation.collectAsState()
    val hexKey by viewModel.subGhzHexKey.collectAsState()
    val signal by viewModel.currentSignal.collectAsState()
    val spectrumPoints by viewModel.spectrumPoints.collectAsState()
    val isTransmitting by viewModel.isTransmitting.collectAsState()
    val rawInput by viewModel.rawDecoderInput.collectAsState()
    val rawOutput by viewModel.rawDecoderOutput.collectAsState()

    var customFreqText by remember(frequency) { mutableStateOf(frequency.toString()) }
    var customProtoText by remember(protocol) { mutableStateOf(protocol) }
    var keyText by remember(hexKey) { mutableStateOf(hexKey) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Warning & Virtual Lab Banner
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = "Sub-GHz",
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ЛАБОРАТОРИЯ SUB-GHZ (RF SANDBOX)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }

                        Text(
                            text = "[ВИРТУАЛЬНЫЙ RF]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "В смартфонах нет чипа CC1101 и радиоантенны Sub-GHz. Лаборатория синтезирует формы сигналов, анализирует тайминги и симулирует протоколы для обучения.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.text.copy(alpha = 0.75f)
                    )
                }
            }
        }

        // Frequency Selector
        item {
            Column {
                Text(
                    text = "НЕСУЩАЯ ЧАСТОТА (МГц):",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(433.92, 868.35, 315.00, 915.00)
                    for (f in presets) {
                        val isSelected = frequency == f
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) colors.primary.copy(alpha = 0.25f) else colors.surface)
                                .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(4.dp))
                                .clickable {
                                    viewModel.updateSubGhzParams(freq = f)
                                    customFreqText = f.toString()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$f MHz",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.primary else colors.text.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Protocol Selector (Dynamic from Firmware Pack)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ПРОТОКОЛ (ИЗ ПРОШИВКИ '${firmware.name}'):",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (p in firmware.subGhzProtocols) {
                        val isSelected = protocol == p
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) colors.accent.copy(alpha = 0.25f) else colors.surface)
                                .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(4.dp))
                                .clickable {
                                    viewModel.updateSubGhzParams(proto = p)
                                    customProtoText = p
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = p,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.accent else colors.text.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Custom Inputs (Freq, Protocol, Key, Modulation)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customFreqText,
                            onValueChange = {
                                customFreqText = it
                                it.toDoubleOrNull()?.let { f -> viewModel.updateSubGhzParams(freq = f) }
                            },
                            label = { Text("Частота MHz", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.text,
                                unfocusedTextColor = colors.text
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("subghz_freq_input")
                        )

                        OutlinedTextField(
                            value = keyText,
                            onValueChange = {
                                keyText = it.uppercase()
                                viewModel.updateSubGhzParams(key = it)
                            },
                            label = { Text("HEX Ключ / Payload", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.text,
                                unfocusedTextColor = colors.text
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("subghz_key_input")
                        )
                    }

                    // Modulation chips
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "МОДУЛЯЦИЯ:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.text.copy(alpha = 0.7f)
                        )
                        val mods = listOf("AM650", "AM270", "FM238", "FM476")
                        for (m in mods) {
                            val isSel = modulation == m
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isSel) colors.primary else colors.background)
                                    .border(1.dp, colors.border, RoundedCornerShape(3.dp))
                                    .clickable { viewModel.updateSubGhzParams(mod = m) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = m,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) colors.background else colors.text.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.synthesizeSubGhzSignal() },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("synthesize_rf_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = colors.background,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "СИНТЕЗИРОВАТЬ",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.background
                    )
                }

                Button(
                    onClick = { viewModel.startSimulatedTransmission() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTransmitting) colors.terminalAmber else colors.accent
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("emulate_tx_btn")
                ) {
                    Icon(
                        imageVector = if (isTransmitting) Icons.Default.Sensors else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = colors.background,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTransmitting) "ПЕРЕДАЧА..." else "ЭМУЛИРОВАТЬ TX",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.background
                    )
                }
            }
        }

        // Oscilloscope Pulse Waveform Display
        item {
            val sig = signal
            Column {
                Text(
                    text = "ОСЦИЛЛОГРАММА ИМПУЛЬСОВ PWM/OOK:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OscilloscopeCanvas(
                    pulses = sig?.pulses ?: emptyList(),
                    highUs = sig?.highMicroseconds ?: 320,
                    lowUs = sig?.lowMicroseconds ?: 640
                )
            }
        }

        // Spectrum Waterfall Display
        item {
            Column {
                Text(
                    text = "СПЕКТРАЛЬНЫЙ АНАЛИЗАТОР (RF POWER):",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(4.dp))
                SpectrumCanvas(
                    points = spectrumPoints,
                    centerFreqMhz = frequency,
                    isTransmitting = isTransmitting
                )
            }
        }

        // Signal Structure Description
        item {
            signal?.let { sig ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "СТРУКТУРА ПАКЕТА: ${sig.protocol} (${sig.frequencyMhz} МГц)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sig.description,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.text.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Payload: 0x${sig.hexKey} | Длина: ${sig.bitLength} бит | Длительность кадра: ~${sig.pulses.size * 320} μs",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.terminalGreen
                        )
                    }
                }
            }
        }

        // RAW Pulse Decoder
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "ДЕКОДЕР ИМПУЛЬСОВ RAW:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Вставьте тайминги микросекунд (положительные - High, отрицательные - Low):",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = colors.text.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = rawInput,
                        onValueChange = { viewModel.decodeRawInput(it) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "БИТЫ: ${rawOutput.first}\nHEX : ${rawOutput.second}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.terminalGreen
                    )
                }
            }
        }
    }
}
