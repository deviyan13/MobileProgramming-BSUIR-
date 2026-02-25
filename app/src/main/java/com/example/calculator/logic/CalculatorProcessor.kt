package com.example.calculator.logic

import java.math.BigDecimal
import java.math.RoundingMode
import java.math.MathContext

class CalculatorProcessor {
    private val mc = MathContext(16, RoundingMode.HALF_UP)

    fun calculate(first: Double, second: Double, operator: String): String {
        val b1 = BigDecimal(first.toString())
        val b2 = BigDecimal(second.toString())

        val result = try {
            when (operator) {
                "+" -> b1.add(b2, mc)
                "-" -> b1.subtract(b2, mc)
                "*" -> b1.multiply(b2, mc)
                "/" -> if (b2.toDouble() == 0.0) return "Error" else b1.divide(b2, 8, RoundingMode.HALF_UP)
                else -> b2
            }
        } catch (e: Exception) {
            return "Error"
        }
        return formatResult(result)
    }

    private fun formatResult(result: BigDecimal): String {
        val absValue = result.abs()

        return if (absValue >= BigDecimal("1000000000000000") ||
            (absValue < BigDecimal("0.000001") && absValue != BigDecimal.ZERO)) {

            "%.9E".format(result).replace("E+", "e")
        } else {
            result.stripTrailingZeros().toPlainString()
        }
    }

    fun toggleSign(displayText: String): String {
        if (displayText == "0" || displayText == "Error") return displayText

        return if (displayText.startsWith("-")) {
            displayText.removePrefix("-")
        } else {
            "-$displayText"
        }
    }

    fun applyPercentage(displayText: String): String {
        if (displayText == "Error") return displayText

        return try {
            val value = BigDecimal(displayText)
            val result = value.divide(BigDecimal("100"), mc)
            formatResult(result)
        } catch (e: Exception) {
            displayText
        }
    }
}