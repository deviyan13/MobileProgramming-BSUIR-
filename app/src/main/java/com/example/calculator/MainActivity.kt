package com.example.calculator

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.calculator.logic.CalculatorProcessor
import com.example.calculator.presentation.scanner.ScannerScreen
import com.example.calculator.ui.theme.CalcAction
import com.example.calculator.ui.theme.CalcDarkText
import com.example.calculator.ui.theme.CalcGray
import com.example.calculator.ui.theme.CalcSpecial
import java.math.BigDecimal
import android.Manifest
import androidx.compose.material.icons.filled.CameraAlt
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        val SplashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            Surface(color = Color.White) {
                CalculatorScreen()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalculatorScreen() {
    // Состояния
    var displayText by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<BigDecimal?>(null) }
    var pendingOperator by remember { mutableStateOf("") }
    var isNewOp by remember { mutableStateOf(true) }

    val processor = remember { CalculatorProcessor() }

    var showScanner by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Состояние разрешения на камеру
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
        } else {
            Toast.makeText(context, "Нет доступа к камере", Toast.LENGTH_SHORT).show()
        }
    }

    fun openScanner() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            showScanner = true
        } else {
            cameraPermissionLauncher.launch(permission)
        }
    }

    fun onNumberScanned(number: String) {
        displayText = number
        isNewOp = true
    }

    // Вспомогательные функции для работы с числами
    fun getCurrentNumber(): BigDecimal? {
        return if (displayText == "Error") null else displayText.toBigDecimalOrNull()
    }

    fun updateDisplayFromNumber(number: BigDecimal?) {
        displayText = if (number == null) "Error" else processor.formatResult(number)
    }

    // Адаптивный размер шрифта
    val fontSize = when {
        displayText.length > 12 -> 30.sp
        displayText.length > 10 -> 40.sp
        displayText.length > 8 -> 55.sp
        else -> 75.sp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Поле вывода
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

        // Строка с кнопкой сканера (вынесена отдельно)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { openScanner() }) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Сканировать число",
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Контейнер для кнопок калькулятора
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val buttons = listOf(
                listOf("AC", "+/-", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            // Распределяем строки равномерно по высоте
            buttons.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // каждая строка занимает равную долю высоты
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { symbol ->
                        CalcButton(
                            symbol = symbol,
                            modifier = Modifier
                                .weight(if (symbol == "0") 2.1f else 1f)
                                .fillMaxHeight(), // кнопка растягивается по высоте строки
                            containerColor = when {
                                symbol in listOf("÷", "×", "-", "+", "=") -> CalcAction
                                symbol in listOf("AC", "+/-", "%") -> CalcSpecial
                                else -> CalcGray
                            },
                            // Внутри CalculatorScreen
                            onClick = {
                                when (symbol) {
                                    "AC" -> {
                                        displayText = "0"
                                        operand1 = null
                                        pendingOperator = ""
                                        isNewOp = true
                                    }
                                    "÷", "×", "-", "+" -> {
                                        val currentNumber = getCurrentNumber()
                                        if (currentNumber != null) {
                                            if (operand1 != null && !isNewOp) {
                                                // Промежуточное вычисление при нажатии на оператор
                                                val result = processor.calculate(operand1!!, currentNumber,
                                                    if (pendingOperator == "×") "*" else if (pendingOperator == "÷") "/" else pendingOperator)
                                                if (result != null) {
                                                    updateDisplayFromNumber(result)
                                                    operand1 = result
                                                }
                                            } else {
                                                operand1 = currentNumber
                                            }
                                        }
                                        pendingOperator = symbol
                                        isNewOp = true
                                    }
                                    "=" -> {
                                        val currentNumber = getCurrentNumber()
                                        if (operand1 != null && currentNumber != null && pendingOperator.isNotEmpty()) {
                                            val result = processor.calculate(operand1!!, currentNumber,
                                                if (pendingOperator == "×") "*" else if (pendingOperator == "÷") "/" else pendingOperator)
                                            updateDisplayFromNumber(result)
                                            operand1 = null // Сбрасываем для нового расчета
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
                                    else -> { // Цифры
                                        if (isNewOp) {
                                            displayText = symbol
                                            isNewOp = false
                                        } else {
                                            if (displayText == "0") displayText = symbol
                                            else if (displayText.length < 15) displayText += symbol
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

    if (showScanner) {
        ScannerScreen(
            onNumberDetected = { number ->
                onNumberScanned(number)
                showScanner = false
            },
            onClose = { showScanner = false }
        )
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
        modifier = modifier,
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