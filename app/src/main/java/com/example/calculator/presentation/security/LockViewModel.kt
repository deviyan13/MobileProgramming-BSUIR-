package com.example.calculator.presentation.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.example.calculator.data.security.SecurityManager

// Добавь аннотацию, чтобы Compose видел изменения
class LockViewModel(private val securityManager: SecurityManager) : ViewModel(), DefaultLifecycleObserver {

    var isAuthorized by mutableStateOf(false)
    var isPinSet by mutableStateOf(securityManager.hasPin())
    var enteredPin by mutableStateOf("")
    var isError by mutableStateOf(false)

    // Это событие срабатывает, когда пользователь сворачивает ЛЮБОЕ окно приложения
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // Если PIN установлен, принудительно снимаем авторизацию
        if (isPinSet) {
            isAuthorized = false
            enteredPin = "" // Очищаем ввод для безопасности
        }
    }

    fun onPinInput(digit: String) {
        if (enteredPin.length < 4) {
            enteredPin += digit
            isError = false
        }
        if (enteredPin.length == 4) {
            if (!isPinSet) {
                securityManager.savePin(enteredPin)
                isPinSet = true
                isAuthorized = true
                enteredPin = ""
            } else {
                if (securityManager.verifyPin(enteredPin)) {
                    isAuthorized = true
                    isError = false
                    enteredPin = ""
                } else {
                    isError = true
                    enteredPin = ""
                }
            }
        }
    }

    // Метод для полного сброса (для критериев лабы)
    fun resetSecurity() {
        securityManager.clearPin()
        isPinSet = false
        isAuthorized = false
        enteredPin = ""
    }
}