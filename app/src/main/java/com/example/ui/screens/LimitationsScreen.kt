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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalFlipperColors

@Composable
fun LimitationsScreen(modifier: Modifier = Modifier) {
    val colors = LocalFlipperColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Notice Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.5.dp, colors.primary, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "АРХИТЕКТУРА И АППАРАТНЫЕ ЛИМИТЫ",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Flipper Droid — это программный эмулятор и учебная лаборатория.\n" +
                                "Приложение использует реальные радиочипы смартфона там, где это физически возможно (NFC 13.56 МГц, Bluetooth LE 2.4 ГГц), и программную эмуляцию (виртуализацию) для протоколов, требующих специализированного железа.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.text.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Hardware Comparison Matrix
        item {
            Text(
                text = "СРАВНЕНИЕ ЖЕЛЕЗА: СМАРТФОН vs FLIPPER ZERO",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent
            )
        }

        item {
            HardwareLimitCard(
                title = "1. SUB-GHZ (300 — 928 МГц: 433, 868 МГц)",
                phoneStatus = "НЕТ ЖЕЛЕЗА (ВИРТУАЛЬНЫЙ СИНТЕЗ)",
                flipperStatus = "ЧИП TI CC1101 + АНТЕННА",
                explanation = "Модемы сотовой связи (Qualcomm/MediaTek) и Wi-Fi строго изолированы закрытыми прошивками Baseband и не имеют аналоговых контуров для передачи на 433 МГц (ворота, шлагбаумы, погодные датчики). В приложении реализована 'Лаборатория' с синтезом таймингов и осциллограммой.",
                isAvailable = false
            )
        }

        item {
            HardwareLimitCard(
                title = "2. RFID 125 кГц (LF: EM-MARIN, HID PROX)",
                phoneStatus = "НЕДОСТУПНО АППАРАТНО",
                flipperStatus = "ФЕРРИТОВАЯ КАТУШКА 125 кГц",
                explanation = "Антенна смартфона рассчитана исключительно на 13.56 МГц (высокочастотный NFC). Низкочастотный резонансный контур 125 кГц требует объемной индуктивной катушки с сотнями витков, которая отсутствует в смартфонах.",
                isAvailable = false
            )
        }

        item {
            HardwareLimitCard(
                title = "3. NFC 13.56 МГц (HF: MIFARE, NTAG, ISO-DEP)",
                phoneStatus = "РАБОТАЕТ ЧЕРЕЗ ANDROID API",
                flipperStatus = "ST25R3916 HF NFC CHIP",
                explanation = "Используется реальный контроллер NFC смартфона: чтение тегов, дамп NDEF/UID, а также Host Card Emulation (HCE) для эмуляции смарт-карт через системную службу Android.",
                isAvailable = true
            )
        }

        item {
            HardwareLimitCard(
                title = "4. BLUETOOTH LE (2.4 ГГц)",
                phoneStatus = "РАБОТАЕТ (BLE SCANNER API)",
                flipperStatus = "STM32WB55 (BLE 5.4)",
                explanation = "Смартфон полноценно сканирует эфир 2.4 ГГц, анализирует рекламные пакеты (Advertising Frames), определяет RSSI и сохраняет MAC-адреса маяков.",
                isAvailable = true
            )
        }

        item {
            HardwareLimitCard(
                title = "5. BADUSB (USB HID КЛАВИАТУРА)",
                phoneStatus = "ГЕНЕРАТОР + СИМУЛЯТОР + ЭКСПОРТ",
                flipperStatus = "STM32 АППАРАТНЫЙ USB HID",
                explanation = "Приложение предоставляет генератор DuckyScript, проверку синтаксиса, пошаговую экранную симуляцию нажатий клавиш и экспорт в payload.dd для загрузки на USB-контроллеры (Pico, Rubber Ducky) или USB OTG.",
                isAvailable = true
            )
        }

        item {
            HardwareLimitCard(
                title = "6. ИНФРАКРАСНЫЙ ПОРТ (IR 38 кГц) И GPIO",
                phoneStatus = "ОТСУТСТВУЕТ В БОЛЬШИНСТВЕ ТЕЛЕФОНОВ",
                flipperStatus = "IR СВЕТОДИОД + ПРИЕМНИК + 18 ПИНОВ GPIO",
                explanation = "В современных телефонах нет ИК-передатчиков и физических выводов GPIO (1-Wire, UART, SPI).",
                isAvailable = false
            )
        }
    }
}

@Composable
private fun HardwareLimitCard(
    title: String,
    phoneStatus: String,
    flipperStatus: String,
    explanation: String,
    isAvailable: Boolean
) {
    val colors = LocalFlipperColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface)
            .border(
                1.dp,
                if (isAvailable) colors.terminalGreen.copy(alpha = 0.5f) else colors.border,
                RoundedCornerShape(6.dp)
            )
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Телефон: $phoneStatus",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = if (isAvailable) colors.terminalGreen else colors.terminalAmber
                )
            }
            Text(
                text = "Flipper: $flipperStatus",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = colors.text.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = explanation,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = colors.text.copy(alpha = 0.8f)
            )
        }
    }
}
