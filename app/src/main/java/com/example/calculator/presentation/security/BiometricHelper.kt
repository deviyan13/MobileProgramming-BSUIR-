package com.example.calculator.presentation.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat

class BiometricHelper(private val activity: FragmentActivity) {

    fun showBiometricPrompt(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricManager = BiometricManager.from(activity)

        // Проверяем доступность: сначала STRONG, если нет - WEAK
        val canAuth = biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            val errorMsg = when (canAuth) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Нет сканера на устройстве"
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Датчик занят или недоступен"
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Отпечаток не зарегистрирован в настройках"
                else -> "Ошибка биометрии: $canAuth"
            }
            onError(errorMsg)
            return
        }

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Авторизация")
            .setSubtitle("Приложите палец")
            // На Android 10 важно: либо NegativeButton, либо DEVICE_CREDENTIAL
            .setNegativeButtonText("Отмена")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}