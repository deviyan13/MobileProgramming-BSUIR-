package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.logic.CalculatorProcessor
import com.example.calculator.ui.theme.CalcAction
import com.example.calculator.ui.theme.CalcDarkText
import com.example.calculator.ui.theme.CalcGray
import com.example.calculator.ui.theme.CalculatorTheme

@Composable
fun CalculatorScreen() {
    var displayText by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var pendingOperator by remember { mutableStateOf("") }
    val processor = remember { CalculatorProcessor() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Светлая тема
            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.Bottom // Табло прижато к кнопкам
    ) {
        // Табло вывода
        Text(
            text = displayText,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
            color = CalcDarkText,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            textAlign = TextAlign.End,
            maxLines = 1
        )

        // Кнопки (сетка)
        val buttons = listOf(
            listOf("AC", "+/-", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { symbol ->
                    val isAction = symbol in listOf("÷", "×", "-", "+", "=")
                    val isSpecial = symbol in listOf("AC", "+/-", "%")

                    CalcButton(
                        symbol = symbol,
                        modifier = Modifier.weight(if (symbol == "0") 2f else 1f),
                        containerColor = when {
                            isAction -> CalcAction // Твой новый легкий тон
                            isSpecial -> Color.LightGray.copy(alpha = 0.3f)
                            else -> CalcGray
                        },
                        onClick = {
                            // Здесь будет вызов нашей логики (обработаем в следующем шаге)
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
        modifier = modifier.height(80.dp), // Фиксированная высота для "квадратности"
        shape = RoundedCornerShape(20.dp), // Мягкие углы
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.headlineMedium,
            color = CalcDarkText
        )
    }
}