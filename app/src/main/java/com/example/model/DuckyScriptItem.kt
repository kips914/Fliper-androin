package com.example.model

data class DuckyScriptItem(
    val id: String,
    val title: String,
    val description: String,
    val targetOS: String, // "Windows", "Linux", "macOS", "Cross-platform"
    val scriptContent: String
)
