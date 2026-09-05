package com.example.model

data class NfcCardModel(
    val id: String,
    val name: String,
    val uidHex: String,
    val techList: List<String>,
    val standard: String,
    val atqaHex: String = "",
    val sakHex: String = "",
    val payloadHex: String = "",
    val parsedNdef: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
