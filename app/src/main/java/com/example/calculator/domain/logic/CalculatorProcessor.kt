package com.example.calculator.domain.logic

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
        // лишние нули в конце перед проверкой на длину
        val stripped = result.stripTrailingZeros()
        val absValue = stripped.abs()

        // Если число слишком большое или маленькое — используем экспоненту
        return if (stripped.scale() > 8 || absValue >= BigDecimal("1000000000000") || (absValue < BigDecimal("0.000001") && absValue > BigDecimal.ZERO)) {
            val scientific = String.format("%.6E", result)
            scientific.replace("E+", "e").replace("E-", "e-")
        } else {
            stripped.toPlainString()
        }
    }

    fun toggleSign(value: BigDecimal): BigDecimal = value.negate()

    fun applyPercentage(value: BigDecimal): BigDecimal =
        value.divide(BigDecimal("100"), mc)
}