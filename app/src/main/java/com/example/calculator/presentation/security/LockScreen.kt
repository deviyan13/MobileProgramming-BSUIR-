package com.example.calculator.presentation.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LockScreen(viewModel: LockViewModel, onBiometricClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (!viewModel.isPinSet) "Создайте PIN-код" else "Введите PIN-код",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Индикаторы ввода (точки)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                val isFilled = index < viewModel.enteredPin.length
                Box(
                    modifier = Modifier.size(20.dp)
                        .background(if (isFilled) Color.Black else Color.LightGray, CircleShape)
                        .border(1.dp, Color.Gray, CircleShape)
                )
            }
        }

        if (viewModel.isError) {
            Text("Неверный PIN", color = Color.Red, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.height(64.dp))

        // Клавиатура 3x4
        val buttons = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "Bio", "0", "Del")
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            buttons.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { char ->
                        IconButton(
                            onClick = {
                                when (char) {
                                    "Bio" -> onBiometricClick()
                                    "Del" -> if (viewModel.enteredPin.isNotEmpty())
                                        viewModel.enteredPin = viewModel.enteredPin.dropLast(1)
                                    else -> viewModel.onPinInput(char)
                                }
                            },
                            modifier = Modifier.size(80.dp).background(Color(0xFFF0F0F0), CircleShape)
                        ) {
                            if (char == "Bio") {
                                // Показываем иконку отпечатка только если PIN уже есть в системе
                                if (viewModel.isPinSet) {
                                    IconButton(
                                        onClick = { onBiometricClick() },
                                        modifier = Modifier.size(80.dp).background(Color(0xFFF0F0F0), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Fingerprint, "Bio", tint = Color.Black)
                                    }
                                } else {
                                    // Если PIN только создается, показываем пустую заглушку для симметрии
                                    Spacer(modifier = Modifier.size(80.dp))
                                }
                            }
                            else if (char == "Del") Icon(Icons.Default.Backspace, "Del")
                            else Text(char, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}