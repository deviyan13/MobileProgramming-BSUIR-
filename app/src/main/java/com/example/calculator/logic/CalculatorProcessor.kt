package com.example.calculator.logic

import java.math.BigDecimal
import java.math.RoundingMode
import java.math.MathContext

class CalculatorProcessor {
    private val mc = MathContext(16, RoundingMode.HALF_UP)

    // Выполняет вычисление, возвращает BigDecimal или null (ошибка)
    fun calculate(first: BigDecimal, second: BigDecimal, operator: String): BigDecimal? {
        return try {
            when (operator) {
                "+" -> first.add(second, mc)
                "-" -> first.subtract(second, mc)
                "*" -> first.multiply(second, mc)
                "/" -> {
                    if (second.compareTo(BigDecimal.ZERO) == 0) {
                        return null // деление на ноль
                    } else {
                        first.divide(second, 8, RoundingMode.HALF_UP)
                    }
                }
                else -> second
            }
        } catch (e: Exception) {
            null
        }
    }

    // Преобразует BigDecimal в строку для отображения (с экспонентой при необходимости)
    fun formatResult(result: BigDecimal): String {
        val absValue = result.abs()

        return if (absValue >= BigDecimal("1000000000000000") ||
            (absValue < BigDecimal("0.000001") && absValue != BigDecimal.ZERO)) {

            "%.9E".format(result).replace("E+", "e")
        } else {
            result.stripTrailingZeros().toPlainString()
        }
    }

    fun toggleSign(value: BigDecimal): BigDecimal = value.negate()

    fun applyPercentage(value: BigDecimal): BigDecimal =
        value.divide(BigDecimal("100"), mc)
}