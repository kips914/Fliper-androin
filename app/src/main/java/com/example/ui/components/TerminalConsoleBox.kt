package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalFlipperColors

@Composable
fun TerminalConsoleBox(
    logs: List<String>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalFlipperColors.current
    var isExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty() && isExpanded) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
    ) {
        // Toggle bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ">_ TERMINAL LOGS (${logs.size})",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = colors.primary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isExpanded) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear logs",
                        tint = colors.text.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clickable { onClearLogs() }
                            .padding(end = 8.dp)
                            .testTag("clear_logs_btn")
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = "Expand console",
                    tint = colors.primary
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 140.dp)
                    .background(colors.background)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "[Терминал чист. События будут появляться здесь...]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = colors.text.copy(alpha = 0.4f),
                        modifier = Modifier.padding(4.dp)
                    )
                } else {
                    LazyColumn(state = listState) {
                        items(logs) { logLine ->
                            val color = when {
                                logLine.contains("[ERR") -> colors.terminalRed
                                logLine.contains("[APDU") -> colors.accent
                                logLine.contains("OK") || logLine.contains("SUCCESS") -> colors.terminalGreen
                                logLine.contains("[TX") -> colors.terminalAmber
                                else -> colors.text.copy(alpha = 0.8f)
                            }
                            Text(
                                text = logLine,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = color,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
