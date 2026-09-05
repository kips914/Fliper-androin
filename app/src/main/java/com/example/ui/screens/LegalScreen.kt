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
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Shield
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
fun LegalScreen(modifier: Modifier = Modifier) {
    val colors = LocalFlipperColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Legal Banner
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
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ПРАВОВОЕ ПОЛОЖЕНИЕ И ЭТИКА",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ВНИМАНИЕ: Flipper Droid предназначен исключительно для образовательных целей, аудита защищенности и тестирования СОБСТВЕННОГО оборудования.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.terminalAmber
                    )
                }
            }
        }

        // Section 1: Personal devices rule
        item {
            LegalNoticeCard(
                title = "1. ТОЛЬКО СОБСТВЕННЫЕ УСТРОЙСТВА",
                content = "Любое тестирование (чтение и эмуляция NFC меток, генерация скриптов BadUSB, сканирование радиоэфира) разрешено проводить строго на принадлежащих вам устройствах, тестовых стендах или с письменного согласия владельца инфраструктуры."
            )
        }

        // Section 2: Criminal liability warning
        item {
            LegalNoticeCard(
                title = "2. НЕДОПУСТИМОСТЬ НЕПРАВОМЕРНОГО ДОСТУПА",
                content = "Попытка перехвата ключей чужих домофонов, шлагбаумов, систем контроля доступа (СКУД) организаций, несанкционированное подключение к чужим компьютерам или копирование платежных данных преследуется по закону (включая ст. 272, 273, 274 УК РФ и аналогичные нормы международного права о компьютерных преступлениях)."
            )
        }

        // Section 3: Radio compliance
        item {
            LegalNoticeCard(
                title = "3. БЕЗОПАСНОСТЬ РАДИОЭФИРА",
                content = "Flipper Droid не нарушает нормы частотного регулирования (ГКРЧ / FCC). Модуль Sub-GHz работает в режиме виртуальной лаборатории: сигнал анализируется и синтезируется только программно в виде математической модели таймингов, без генерации реального электромагнитного радиоизлучения."
            )
        }

        // Section 4: Responsible disclosure
        item {
            LegalNoticeCard(
                title = "4. БЕЛЫЙ ХАКИНГ И ОБУЧЕНИЕ",
                content = "Цель приложения — помочь разработчикам, инженерам встроенных систем и специалистам по информационной безопасности изучить принципы работы протоколов Mifare, NTAG, DuckyScript, Bluetooth Advertising и модуляций OOK/ASK в безопасной виртуализированной среде."
            )
        }
    }
}

@Composable
private fun LegalNoticeCard(
    title: String,
    content: String
) {
    val colors = LocalFlipperColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(6.dp))
            .padding(12.dp)
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
            Text(
                text = content,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = colors.text.copy(alpha = 0.85f),
                lineHeight = 15.sp
            )
        }
    }
}
