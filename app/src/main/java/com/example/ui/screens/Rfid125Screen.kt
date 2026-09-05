package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.LocalFlipperColors

@Composable
fun Rfid125Screen(modifier: Modifier = Modifier) {
    val colors = LocalFlipperColors.current
    var em4100Hex by remember { mutableStateOf("0102030405") }
    var calculatedMatrix by remember { mutableStateOf(generateEm4100Matrix("0102030405")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Warning Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.terminalRed.copy(alpha = 0.15f))
                    .border(1.5.dp, colors.terminalRed, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = colors.terminalRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "МОДУЛЬ НЕДОСТУПЕН: LF RFID 125 кГц",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.terminalRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "В современных смартфонах физически ОТСУТСТВУЕТ антенна 125 кГц (LF). Телефонный NFC-чип спроектирован исключительно для частоты 13.56 МГц (HF).\n\n" +
                                "Для работы с домофонными брелоками (EM-Marin EM4100, HID ProxCard II, Indala, T5577) требуется внешнее устройство (Flipper Zero или USB LF-считыватель).",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.text.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Hardware Comparison Card
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
                        text = "ФИЗИКА: ПОЧЕМУ ТЕЛЕФОН НЕ МОЖЕТ 125 кГц?",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "• Индуктивность катушки:\n" +
                                "  Для 125 кГц требуется катушка индуктивностью ~1-5 мГн (сотни витков тонкого медного провода и ферритовый сердечник).\n" +
                                "  Для 13.56 МГц в телефоне используется плоская спиральная дорожка на плате с индуктивностью всего 1-2 мкГн.\n\n" +
                                "• Аналоговый тракт:\n" +
                                "  Контроллер NFC телефона не поддерживает модуляцию ASK/FSK на низкой частоте 125 кГц.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = colors.text.copy(alpha = 0.75f)
                    )
                }
            }
        }

        // Educational Protocol Sandbox: EM4100 Matrix Calculator
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.primary, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ЛАБОРАТОРИЯ EM4100 (64-BIT FRAME)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Text(
                            text = "[ЭМУЛЯЦИЯ СТРУКТУРЫ]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = colors.accent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Введите 5 байт (10 hex символов) идентификатора брелока для расчета проверочной матрицы четности EM-Marin:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = colors.text.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = em4100Hex,
                            onValueChange = {
                                if (it.length <= 10) {
                                    em4100Hex = it.uppercase()
                                    calculatedMatrix = generateEm4100Matrix(it)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.text,
                                unfocusedTextColor = colors.text
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("em4100_hex_input")
                        )

                        Button(
                            onClick = {
                                calculatedMatrix = generateEm4100Matrix(em4100Hex)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "РАСЧЕТ",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = colors.background,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "МАТРИЦА КАДРА EM4100:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.background, RoundedCornerShape(4.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = calculatedMatrix,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.terminalGreen
                        )
                    }
                }
            }
        }
    }
}

private fun generateEm4100Matrix(hex: String): String {
    val clean = hex.replace(" ", "").padEnd(10, '0').take(10)
    val sb = StringBuilder()
    sb.append("Preamble : 1 1 1 1 1 1 1 1 1\n")
    sb.append("----------------------------\n")
    sb.append("NIBBLE   : D0 D1 D2 D3 | PARITY\n")

    val colParity = IntArray(4) { 0 }

    for (i in 0 until 10) {
        val digit = Character.digit(clean[i], 16).coerceAtLeast(0)
        val b3 = (digit shr 3) and 1
        val b2 = (digit shr 2) and 1
        val b1 = (digit shr 1) and 1
        val b0 = digit and 1
        val rowParity = (b3 xor b2 xor b1 xor b0)

        colParity[0] = colParity[0] xor b3
        colParity[1] = colParity[1] xor b2
        colParity[2] = colParity[2] xor b1
        colParity[3] = colParity[3] xor b0

        val label = "Data[$i] (${clean[i]})".padEnd(8, ' ')
        sb.append("$label: $b3  $b2  $b1  $b0  |   $rowParity\n")
    }
    sb.append("----------------------------\n")
    sb.append("COL PARITY: ${colParity[0]}  ${colParity[1]}  ${colParity[2]}  ${colParity[3]}  | STOP: 0\n")
    sb.append("Total Frame Size: 64 bits (Manchester encoded)")
    return sb.toString()
}
