package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BleDeviceModel
import com.example.ui.MainViewModel
import com.example.ui.theme.LocalFlipperColors

@Composable
fun BleScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalFlipperColors.current
    val isScanning by viewModel.isBleScanning.collectAsState()
    val discoveredDevicesMap by viewModel.discoveredBleDevices.collectAsState()
    val savedDevices by viewModel.savedBleDevices.collectAsState()

    val discoveredList = discoveredDevicesMap.values.toList().sortedByDescending { it.rssi }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.startBleScan()
    }

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
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = "BLE",
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BLUETOOTH LE СКАНЕР (2.4 GHz)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }

                        val status = when {
                            !viewModel.bleManager.isBluetoothAvailable() -> "НЕТ BLE"
                            !viewModel.bleManager.isBluetoothEnabled() -> "ВЫКЛЮЧЕН"
                            isScanning -> "СКАНИРОВАНИЕ..."
                            else -> "ГОТОВ"
                        }
                        val statusColor = if (isScanning) colors.terminalGreen else colors.accent

                        Text(
                            text = "[$status]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Сканирование радиоэфира Bluetooth Low Energy. Обнаружение рекламных пакетов (Advertising), сбор MAC-адресов и уровня сигнала RSSI.",
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
                        if (isScanning) {
                            viewModel.stopBleScan()
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.BLUETOOTH_SCAN,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    )
                                )
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.BLUETOOTH,
                                        Manifest.permission.BLUETOOTH_ADMIN
                                    )
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScanning) colors.terminalRed else colors.primary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ble_scan_btn")
                ) {
                    Icon(
                        imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.Refresh,
                        contentDescription = null,
                        tint = colors.background,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScanning) "СТОП" else "СКАНИРОВАТЬ ЭФИР",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.background
                    )
                }
            }
        }

        // Discovered Devices Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ОБНАРУЖЕНО УСТРОЙСТВ (${discoveredList.size})",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
        }

        if (discoveredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isScanning) "[Поиск маяков в радиоэфире 2.4 ГГц...]" else "[Эфир чист. Нажмите 'СКАНИРОВАТЬ ЭФИР']",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.text.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(discoveredList) { device ->
                BleDeviceRow(
                    device = device,
                    onSaveToggle = {
                        if (device.isSaved) viewModel.removeBleDevice(device.address)
                        else viewModel.saveBleDevice(device)
                    },
                    onCopyMac = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("MAC", device.address))
                        Toast.makeText(context, "MAC скопирован: ${device.address}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Saved MAC Addresses Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "СОХРАНЕННЫЕ MAC-АДРЕСА (${savedDevices.size})",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
            }
        }

        if (savedDevices.isEmpty()) {
            item {
                Text(
                    text = "Список пуст. Нажмите на иконку закладки возле найденного устройства, чтобы сохранить его MAC.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.text.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        } else {
            items(savedDevices) { savedDev ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = savedDev.name ?: "[Неизвестное BLE]",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent
                            )
                            Text(
                                text = "MAC: ${savedDev.address}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = colors.terminalGreen
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy MAC",
                                tint = colors.text.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("MAC", savedDev.address))
                                        Toast.makeText(context, "MAC скопирован!", Toast.LENGTH_SHORT).show()
                                    }
                            )
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove MAC",
                                tint = colors.terminalRed,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { viewModel.removeBleDevice(savedDev.address) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BleDeviceRow(
    device: BleDeviceModel,
    onSaveToggle: () -> Unit,
    onCopyMac: () -> Unit
) {
    val colors = LocalFlipperColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface)
            .border(
                1.dp,
                if (device.isSaved) colors.primary else colors.border,
                RoundedCornerShape(6.dp)
            )
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
                        text = device.name ?: "[Unknown BLE Peripheral]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.address,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.terminalGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = colors.text.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onCopyMac() }
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                val rssiBars = when {
                    device.rssi > -60 -> "|||||"
                    device.rssi > -70 -> "||||"
                    device.rssi > -80 -> "|||"
                    device.rssi > -90 -> "||"
                    else -> "|"
                }
                Text(
                    text = "RSSI: ${device.rssi} dBm [$rssiBars]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.primary
                )
            }

            Icon(
                imageVector = if (device.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Save device",
                tint = if (device.isSaved) colors.primary else colors.text.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onSaveToggle() }
            )
        }
    }
}
