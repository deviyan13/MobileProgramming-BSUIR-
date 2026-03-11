package com.example.calculator.presentation.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.data.repository.CalculatorRepositoryImpl
import com.example.calculator.domain.logic.CalculatorProcessor
import com.example.calculator.domain.model.AppTheme
import com.example.calculator.domain.model.HistoryRecord
import com.example.calculator.domain.repository.CalculatorRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class CalculatorViewModel : ViewModel() {
    private val repository: CalculatorRepository = CalculatorRepositoryImpl()

    private val _remoteThemeColor = MutableStateFlow("#FFFFFF")
    val remoteThemeColor: StateFlow<String> = _remoteThemeColor.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryRecord>>(emptyList())
    val history: StateFlow<List<HistoryRecord>> = _history.asStateFlow()

    private val _availableThemes = MutableStateFlow<List<AppTheme>>(emptyList())
    val availableThemes = _availableThemes.asStateFlow()

    init {
        fetchThemeFromCloud()
        observeHistory()

        viewModelScope.launch {
            repository.getAvailableThemes().collect { _availableThemes.value = it }
        }
    }
    private fun fetchThemeFromCloud() {
        viewModelScope.launch {
            _remoteThemeColor.value = repository.getRemoteThemeColor()
        }
    }

    fun selectTheme(selectedColor: String) {
        // 1. Мгновенно меняем UI локально
        _remoteThemeColor.value = selectedColor
    }



    private fun observeHistory() {
        viewModelScope.launch {
            repository.getHistory().collect { list ->
                _history.value = list
            }
        }
    }

    private fun onEqualsPressed(expression: String, result: String) {
        viewModelScope.launch {
            repository.saveHistory(HistoryRecord(expression, result))
        }
    }
    var displayText by mutableStateOf("0")
        private set

    var operand1 by mutableStateOf<BigDecimal?>(null)
        private set

    var pendingOperator by mutableStateOf("")
        private set

    var isNewOp by mutableStateOf(true)
        private set

    var showScanner by mutableStateOf(false)

    private val processor = CalculatorProcessor()

    // Вспомогательные функции
    private fun getCurrentNumber(): BigDecimal? {
        return if (displayText == "Error") null else displayText.toBigDecimalOrNull()
    }

    private fun updateDisplayFromNumber(number: BigDecimal?) {
        displayText = if (number == null) "Error" else processor.formatResult(number)
    }

    // Логика обработки нажатий (перенесена из твоего onClick без изменений)
    fun onAction(symbol: String) {
        when (symbol) {
            "AC" -> {
                displayText = "0"
                operand1 = null
                pendingOperator = ""
                isNewOp = true
            }
            "+/-" -> {
                // Логика смены знака
                getCurrentNumber()?.let {
                    val result = processor.toggleSign(it)
                    updateDisplayFromNumber(result)
                    // Если мы только что ввели число и поменяли знак,
                    // это не должно сбрасывать возможность печатать дальше
                    if (isNewOp) operand1 = result
                }
            }
            "%" -> {
                // Логика процентов
                getCurrentNumber()?.let {
                    val result = processor.applyPercentage(it)
                    updateDisplayFromNumber(result)
                    isNewOp = true // Результат процента обычно завершает ввод числа
                }
            }
            "÷", "×", "-", "+" -> {
                val currentNumber = getCurrentNumber()
                if (currentNumber != null) {
                    if (operand1 != null && !isNewOp) {
                        val result = processor.calculate(
                            operand1!!, currentNumber,
                            if (pendingOperator == "×") "*" else if (pendingOperator == "÷") "/" else pendingOperator
                        )
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
                    val expression = "${operand1} $pendingOperator $currentNumber"
                    val result = processor.calculate(
                        operand1!!, currentNumber,
                        if (pendingOperator == "×") "*" else if (pendingOperator == "÷") "/" else pendingOperator
                    )
                    updateDisplayFromNumber(result)

                    // --- STEP 3: Сохранение в Firebase ---
                    onEqualsPressed(expression, displayText)

                    operand1 = null
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
            else -> { // Цифры 0-9
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

    // Добавь этот вспомогательный метод для Step 3 (если еще не добавил)
    private fun saveHistoryToCloud(expression: String, result: String) {
        viewModelScope.launch {
            try {
                repository.saveHistory(com.example.calculator.domain.model.HistoryRecord(expression, result))
            } catch (e: Exception) {
                // Логируем ошибку, если Firebase недоступен
            }
        }
    }

    fun onNumberScanned(number: String) {
        displayText = number
        isNewOp = true
        showScanner = false
    }

    fun toggleScanner(show: Boolean) {
        showScanner = show
    }
}