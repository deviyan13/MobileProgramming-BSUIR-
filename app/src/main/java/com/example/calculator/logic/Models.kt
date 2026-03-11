package com.example.calculator.logic

data class HistoryRecord(
    val expression: String = "",
    val result: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class AppThemeConfig(
    val primaryColor: String = "#E0E7FF",
    val isDark: Boolean = false
)