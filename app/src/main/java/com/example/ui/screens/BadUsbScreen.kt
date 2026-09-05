package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Usb
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.LocalFlipperColors

@Composable
fun BadUsbScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalFlipperColors.current
    val editorContent by viewModel.scriptEditorContent.collectAsState()
    val selectedScript by viewModel.selectedScript.collectAsState()
    val isExecuting by viewModel.isDuckyExecuting.collectAsState()
    val simulatedScreenText by viewModel.simulatedScreenText.collectAsState()
    val templates = viewModel.duckyManager.defaultTemplates
    val syntaxErrors = viewModel.duckyManager.validateSyntax(editorContent)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Module Header Banner
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
                                imageVector = Icons.Default.Usb,
                                contentDescription = "BadUSB",
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BADUSB (DUCKYSCRIPT СИНТЕЗАТОР)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }

                        Text(
                            text = "[ВИРТУАЛЬНЫЙ HID]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.terminalGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Генерация скриптов DuckyScript для эмуляции клавиатуры (HID). Запускайте виртуальную симуляцию или экспортируйте payload.dd для USB OTG.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.text.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Templates Row
        item {
            Column {
                Text(
                    text = "ШАБЛОНЫ СКРИПТОВ:",
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
                    for (tmpl in templates) {
                        val isSelected = tmpl.id == selectedScript.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) colors.primary.copy(alpha = 0.25f) else colors.surface)
                                .border(1.dp, if (isSelected) colors.primary else colors.border, RoundedCornerShape(4.dp))
                                .clickable { viewModel.selectDuckyTemplate(tmpl) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tmpl.title,
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

        // Syntax Toolbar
        item {
            Column {
                Text(
                    text = "БЫСТРЫЕ КОМАНДЫ DUCKYSCRIPT:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val chips = listOf("DELAY 500", "STRING ", "ENTER", "GUI r", "CTRL ALT t", "REM ", "TAB", "ESC")
                    for (chip in chips) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.background)
                                .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                                .clickable { viewModel.insertDuckySnippet(chip) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ $chip",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = colors.primary
                            )
                        }
                    }
                }
            }
        }

        // DuckyScript Code Editor
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.primary, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "РЕДАКТОР СКРИПТА (payload.dd)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Text(
                            text = "${editorContent.lines().size} строк",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.text.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = editorContent,
                        onValueChange = { viewModel.updateScriptContent(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .testTag("ducky_script_editor"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    )

                    if (syntaxErrors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ошибки синтаксиса:\n" + syntaxErrors.joinToString("\n"),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.terminalRed
                        )
                    }
                }
            }
        }

        // Action Buttons: Run Simulation & Share / Export
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (isExecuting) viewModel.stopDuckySimulation() else viewModel.runDuckySimulation()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isExecuting) colors.terminalRed else colors.primary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("run_ducky_sim_btn")
                ) {
                    Icon(
                        imageVector = if (isExecuting) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = colors.background,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isExecuting) "ОСТАНОВИТЬ" else "СИМУЛЯЦИЯ ВВОДА",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.background
                    )
                }

                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, editorContent)
                            putExtra(Intent.EXTRA_TITLE, "payload.dd")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Отправить скрипт BadUSB")
                        context.startActivity(shareIntent)
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_ducky_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ЭКСПОРТ (USB/PC)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.primary
                    )
                }
            }
        }

        // Simulated Virtual PC Screen Monitor
        item {
            AnimatedVisibility(visible = isExecuting || simulatedScreenText.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.background)
                        .border(1.dp, colors.terminalGreen, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ВИРТУАЛЬНЫЙ МОНИТОР ПК (ВЫВОД НАЖАТИЙ)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.terminalGreen
                            )
                            if (isExecuting) {
                                Text(
                                    text = "● НАБОР...",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = colors.terminalGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = simulatedScreenText.ifEmpty { "[Ожидание нажатий клавиш...]" },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = colors.text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp, max = 150.dp)
                        )
                    }
                }
            }
        }

        // Legal & Hardware Notice
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "ВАЖНО: Инструмент предназначен только для тестирования собственных ПК и учебных стендов. Прямая эмуляция аппаратного USB-контроллера на смартфонах требует поддержки ядра USB Gadget HID (Linux/Kali NetHunter).",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.text.copy(alpha = 0.65f)
                )
            }
        }
    }
}
