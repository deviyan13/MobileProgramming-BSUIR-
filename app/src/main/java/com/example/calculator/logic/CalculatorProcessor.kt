package com.example.calculator.logic

import android.annotation.SuppressLint
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

    @SuppressLint("DefaultLocale")
    fun formatResult(result: BigDecimal): String {
        if (result.compareTo(BigDecimal.ZERO) == 0) return "0"

        val absValue = result.abs()
        val smallThreshold = BigDecimal("1e-6")
        val largeThreshold = BigDecimal("1e12")

        return if (absValue >= largeThreshold || (absValue <= smallThreshold && absValue > BigDecimal.ZERO)) {

            val scientific = String.format("%.6E", result)
            scientific.replace("E+", "e").replace("E-", "e-")
        } else {
            result.stripTrailingZeros().toPlainString()
        }
    }

    fun toggleSign(value: BigDecimal): BigDecimal = value.negate()

    fun applyPercentage(value: BigDecimal): BigDecimal =
        value.divide(BigDecimal("100"), mc)
}