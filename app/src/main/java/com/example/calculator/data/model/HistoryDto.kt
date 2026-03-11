package com.example.calculator.data.model

// Firebase требует пустой конструктор и переменные по умолчанию
data class HistoryDto(
    val expression: String = "",
    val result: String = "",
    val timestamp: Long = 0
)