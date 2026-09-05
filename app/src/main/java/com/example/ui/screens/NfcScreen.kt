package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NfcCardModel
import com.example.ui.MainViewModel
import com.example.ui.theme.LocalFlipperColors

@Composable
fun NfcScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalFlipperColors.current
    val lastCard by viewModel.lastScannedCard.collectAsState()
    val isReaderActive by viewModel.isReaderActive.collectAsState()
    val isEmulating by viewModel.nfcManager.isEmulating.collectAsState()
    val activeEmulatingCard by viewModel.nfcManager.activeEmulatingCard.collectAsState()
    val savedCards by viewModel.nfcManager.savedCards.collectAsState()
    val apduLogs by viewModel.apduLogs.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Module Status Banner
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
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "NFC",
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NFC 13.56 МГц (ISO 14443 / HCE)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }

                        val nfcStatusText = when {
                            !viewModel.isNfcAvailable -> "НЕ ПОДДЕРЖИВАЕТСЯ"
                            !viewModel.isNfcEnabled -> "ВЫКЛЮЧЕН В СИСТЕМЕ"
                            else -> "ГОТОВ"
                        }
                        val statusColor = when {
                            !viewModel.isNfcAvailable -> colors.terminalRed
                            !viewModel.isNfcEnabled -> colors.terminalAmber
                            else -> colors.terminalGreen
                        }

                        Text(
                            text = "[$nfcStatusText]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Чтение и эмуляция карт 13.56 МГц (Mifare, NTAG, ISO-DEP). Поднесите карту к задней крышке телефона.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.text.copy(alpha = 0.7f)
                    )
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
                    onClick = {
                        viewModel.setReaderActive(!isReaderActive)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReaderActive) colors.terminalGreen else colors.primary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nfc_scan_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isReaderActive) Icons.Default.Sensors else Icons.Default.Refresh,
                        contentDescription = null,
                        tint = colors.background,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isReaderActive) "СКАНИРОВАНИЕ..." else "СКАНИРОВАТЬ",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.background
                    )
                }

                OutlinedButton(
                    onClick = {
                        // Load a sample card dump for instant testing
                        val sample = savedCards.firstOrNull() ?: NfcCardModel(
                            id = "sample_test",
                            name = "Demo Mifare 1K Card",
                            uidHex = "8B:22:91:0F",
                            techList = listOf("NfcA", "MifareClassic"),
                            standard = "ISO 14443-3A",
                            atqaHex = "00:04",
                            sakHex = "08",
                            payloadHex = "Dump: 8B22910F 00 08 04",
                            parsedNdef = "Badge ID: #90412"
                        )
                        viewModel.loadSimulatedTag(sample)
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nfc_sample_dump_btn")
                ) {
                    Text(
                        text = "ТЕСТОВЫЙ ДАМП",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.primary
                    )
                }
            }
        }

        // Active Card Inspector
        item {
            val card = lastCard
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.primary, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                if (card == null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "[КАРТА НЕ ВЫБРАНА]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Поднесите физическую карту к NFC чипу или нажмите 'ТЕСТОВЫЙ ДАМП'.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.text.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = card.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Text(
                                text = card.standard,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = colors.accent
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "UID: ",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text.copy(alpha = 0.7f)
                            )
                            Text(
                                text = card.uidHex,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.terminalGreen
                            )
                        }

                        if (card.atqaHex.isNotEmpty() || card.sakHex.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ATQA: ${card.atqaHex} | SAK: ${card.sakHex}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = colors.text.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Технологии: ${card.techList.joinToString(", ")}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.text.copy(alpha = 0.6f)
                        )

                        if (card.parsedNdef.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.background, RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = "NDEF:\n${card.parsedNdef}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = colors.accent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Emulation toggle for this card
                        Button(
                            onClick = { viewModel.toggleCardEmulation(card) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEmulating && activeEmulatingCard?.id == card.id)
                                    colors.terminalRed else colors.accent
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("emulate_card_btn")
                        ) {
                            Icon(
                                imageVector = if (isEmulating && activeEmulatingCard?.id == card.id)
                                    Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = colors.background,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEmulating && activeEmulatingCard?.id == card.id)
                                    "ОСТАНОВИТЬ ЭМУЛЯЦИЮ" else "ЭМУЛИРОВАТЬ ЭТУ КАРТУ (HCE)",
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

        // HCE Live APDU Monitor (if emulating)
        item {
            AnimatedVisibility(visible = isEmulating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.terminalGreen, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "● HCE ЭМУЛЯЦИЯ АКТИВНА",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.terminalGreen
                            )
                            Text(
                                text = "AID: F0010203040506",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = colors.accent
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Телефон отвечает на внешние APDU считывателей (терминалов, турникетов, других телефонов).",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.text.copy(alpha = 0.7f)
                        )

                        if (apduLogs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Последний обмен:",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            val latest = apduLogs.first()
                            Text(
                                text = "REQ: ${latest.commandHex}\nRESP: ${latest.responseHex} (${latest.status})",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = colors.text.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Saved Cards List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "СОХРАНЕННЫЕ ДАМПЫ (${savedCards.size})",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
        }

        // Saved Cards Items
        items(savedCards) { itemCard ->
            val isCurrentEmulating = isEmulating && activeEmulatingCard?.id == itemCard.id

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surface)
                    .border(
                        1.dp,
                        if (isCurrentEmulating) colors.terminalGreen else colors.border,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { viewModel.loadSimulatedTag(itemCard) }
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = itemCard.name,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentEmulating) colors.terminalGreen else colors.text
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "UID: ${itemCard.uidHex} [${itemCard.standard}]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.text.copy(alpha = 0.6f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.toggleCardEmulation(itemCard) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentEmulating) colors.terminalRed else colors.primary
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (isCurrentEmulating) "СТОП" else "HCE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = colors.background,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete card",
                            tint = colors.text.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { viewModel.nfcManager.deleteCard(itemCard.id) }
                        )
                    }
                }
            }
        }
    }
}
