package com.example.model

data class FirmwarePack(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val primaryColorHex: String,
    val backgroundColorHex: String,
    val surfaceColorHex: String,
    val accentColorHex: String,
    val textColorHex: String,
    val enabledModules: List<String>, // "nfc", "rfid", "badusb", "subghz", "ble", "limits", "legal"
    val subGhzProtocols: List<String>,
    val mascotStyle: String = "dolphin", // "dolphin", "cyber_cat", "skull", "robot"
    val terminalPrompt: String = "flipper@droid:~$ "
)
