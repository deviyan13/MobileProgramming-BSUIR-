package com.example.calculator.logic

class CalculatorProcessor {

    fun calculate(first: Double, second: Double, operator: String): Double {
        return when (operator) {
            "+" -> first + second
            "-" -> first - second
            "*" -> first * second
            "/" -> {
                if (second != 0.0) first / second
                else 0.0 // Здесь можно потом добавить обработку ошибки "деление на ноль"
            }
            else -> 0.0
        }
    }

    fun toggleSign(value: String): String {
        return if (value.startsWith("-")) value.drop(1) else "-$value"
    }

    fun applyPercentage(value: String): String {
        return (value.toDoubleOrNull()?.div(100))?.toString() ?: value
    }
}