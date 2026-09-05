package com.example.model

data class BleDeviceModel(
    val address: String,
    val name: String?,
    val rssi: Int,
    val serviceUuids: List<String> = emptyList(),
    val lastSeen: Long = System.currentTimeMillis(),
    val isSaved: Boolean = false
)
