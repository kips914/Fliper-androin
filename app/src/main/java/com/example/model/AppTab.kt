package com.example.model

enum class AppTab(val moduleId: String, val title: String, val shortLabel: String) {
    NFC("nfc", "NFC 13.56 MHz", "NFC"),
    RFID125("rfid", "RFID 125 kHz", "125kHz"),
    BAD_USB("badusb", "BadUSB (Ducky)", "BadUSB"),
    SUB_GHZ("subghz", "Sub-GHz Lab", "Sub-GHz"),
    BLE("ble", "Bluetooth LE", "BLE"),
    FIRMWARE("firmware", "Firmware & Packs", "Firmware"),
    LIMITATIONS("limits", "Ограничения Железа", "Лимиты"),
    LEGAL("legal", "Легальность & Инфо", "Инфо")
}
