package com.example.calculator

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.presentation.calculator.CalculatorScreen
import com.example.calculator.presentation.calculator.CalculatorViewModel
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.calculator.data.security.SecurityManager
import com.example.calculator.presentation.security.BiometricHelper
import com.example.calculator.presentation.security.LockScreen
import com.example.calculator.presentation.security.LockViewModel

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}



class MainActivity : FragmentActivity() {
    private lateinit var securityManager: SecurityManager
    private lateinit var lockViewModel: LockViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        securityManager = SecurityManager(this)
        // Важно: создаем ViewModel так, чтобы она жила долго
        lockViewModel = LockViewModel(securityManager)

        // Подписываемся на жизненный цикл приложения
        ProcessLifecycleOwner.get().lifecycle.addObserver(lockViewModel)

        val biometricHelper = BiometricHelper(this)

        setContent {
            val calcViewModel: CalculatorViewModel = viewModel()

            // Основной контейнер
            Box(modifier = Modifier.fillMaxSize()) {
                // Калькулятор рисуется всегда, НО...
                CalculatorScreen(calcViewModel, lockViewModel)

                // ...если авторизация нужна и PIN установлен, поверх рисуем LockScreen
                if (lockViewModel.isPinSet && !lockViewModel.isAuthorized) {
                    LockScreen(lockViewModel) {
                        biometricHelper.showBiometricPrompt(
                            onSuccess = { lockViewModel.isAuthorized = true },
                            onError = { msg -> Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show() }
                        )
                    }
                }

                // Если PIN еще не создан (первый запуск)
                if (!lockViewModel.isPinSet) {
                    LockScreen(lockViewModel) {}
                }
            }
        }
    }
}