package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.logic.CalculatorProcessor
import java.math.BigDecimal

// Цвета (оставьте как у вас)
val CalcGray = Color(0xFFF3F3F3)
val CalcAction = Color(0xFFE0E7FF)
val CalcSpecial = Color(0xFFE5E5E5)
val CalcDarkText = Color(0xFF2D2D2D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = Color.White) {
                CalculatorScreen()
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var displayText by remember { mutableStateOf("0") }
    // operand1 теперь хранится как BigDecimal (точное значение)
    var operand1 by remember { mutableStateOf<BigDecimal?>(null) }
    var pendingOperator by remember { mutableStateOf("") }
    var isNewOp by remember { mutableStateOf(true) }

    val processor = remember { CalculatorProcessor() }

    fun getCurrentNumber(): BigDecimal? {
        return if (displayText == "Error") null else displayText.toBigDecimalOrNull()
    }

    fun updateDisplayFromNumber(number: BigDecimal?) {
        displayText = if (number == null) "Error" else processor.formatResult(number)
    }

    val fontSize = when {
        displayText.length > 10 -> 35.sp
        displayText.length > 8 -> 50.sp
        else -> 75.sp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = fontSize,
                    textAlign = TextAlign.End
                ),
                color = CalcDarkText,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val buttons = listOf(
            listOf("AC", "+/-", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { symbol ->
                    val isAction = symbol in listOf("÷", "×", "-", "+", "=")
                    val isSpecial = symbol in listOf("AC", "+/-", "%")

                    CalcButton(
                        symbol = symbol,
                        modifier = Modifier.weight(if (symbol == "0") 2.1f else 1f),
                        containerColor = when {
                            isAction -> CalcAction
                            isSpecial -> CalcSpecial
                            else -> CalcGray
                        },
                        onClick = {
                            when (symbol) {
                                "AC" -> {
                                    displayText = "0"
                                    operand1 = null
                                    pendingOperator = ""
                                    isNewOp = true
                                }
                                "+/-" -> {
                                    val current = getCurrentNumber()
                                    if (current != null) {
                                        val newValue = processor.toggleSign(current)
                                        updateDisplayFromNumber(newValue)
                                        isNewOp = false
                                    }
                                }
                                "%" -> {
                                    val current = getCurrentNumber()
                                    if (current != null) {
                                        val newValue = processor.applyPercentage(current)
                                        updateDisplayFromNumber(newValue)
                                        isNewOp = false
                                    }
                                }
                                "÷", "×", "-", "+" -> {
                                    val currentNumber = getCurrentNumber()
                                    if (currentNumber != null) {
                                        if (operand1 != null && !isNewOp) {
                                            val result = processor.calculate(operand1!!, currentNumber, pendingOperator)
                                            if (result != null) {
                                                updateDisplayFromNumber(result)
                                                operand1 = result      // сохраняем результат как первый операнд
                                            } else {
                                                displayText = "Error"
                                                operand1 = null
                                            }
                                        } else {
                                            operand1 = currentNumber   // просто запоминаем первый операнд
                                        }
                                    }
                                    // запоминаем оператор
                                    pendingOperator = when (symbol) {
                                        "×" -> "*"
                                        "÷" -> "/"
                                        else -> symbol
                                    }
                                    isNewOp = true
                                }
                                "=" -> {
                                    val currentNumber = getCurrentNumber()
                                    if (operand1 != null && currentNumber != null && pendingOperator.isNotEmpty()) {
                                        val result = processor.calculate(operand1!!, currentNumber, pendingOperator)
                                        if (result != null) {
                                            updateDisplayFromNumber(result)
                                            // результат в operand1 для цепочки операций
                                            operand1 = result
                                        } else {
                                            displayText = "Error"
                                            operand1 = null
                                        }
                                        pendingOperator = ""
                                        isNewOp = true
                                    }
                                }
                                "." -> {
                                    if (isNewOp) {
                                        displayText = "0."
                                        isNewOp = false
                                    } else if (!displayText.contains(".")) {
                                        displayText += "."
                                    }
                                }
                                else -> { // цифры
                                    if (isNewOp || displayText == "0" || displayText == "Error") {
                                        displayText = symbol
                                        isNewOp = false
                                    } else if (displayText.length < 12) {
                                        displayText += symbol
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    symbol: String,
    modifier: Modifier,
    containerColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(82.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = CalcDarkText
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp)
        )
    }
}