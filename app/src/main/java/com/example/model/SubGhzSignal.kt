package com.example.model

data class SubGhzSignal(
    val frequencyMhz: Double,
    val protocol: String,
    val modulation: String, // "AM650", "AM270", "FM238", "FM476"
    val bitLength: Int,
    val hexKey: String,
    val repeats: Int,
    val highMicroseconds: Int,
    val lowMicroseconds: Int,
    val pulses: List<Boolean>,
    val description: String = ""
)
