package com.example.calculator.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.calculator.MainActivity
import com.example.calculator.R // Импорт твоих ресурсов
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Срабатывает, когда приходит пуш (если приложение открыто или в фоне для data-сообщений)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Firebase может прислать пуш двумя способами: notification (визуальный) или data (скрытый)
        val title = message.notification?.title ?: message.data["title"] ?: "Калькулятор"
        val body = message.notification?.body ?: message.data["body"] ?: "Новое уведомление!"

        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Отправляем токен в облако сразу при его генерации
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val deviceId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )

        val deviceData = mapOf(
            "token" to token,
            "lastUpdated" to com.google.firebase.Timestamp.now(),
            "platform" to "Android"
        )

        // Сохраняем в коллекцию "fcm_tokens"
        db.collection("fcm_tokens")
            .document(deviceId)
            .set(deviceData)
            .addOnSuccessListener {
                android.util.Log.d("FCM", "Токен успешно обновлен в Firestore")
            }
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "calculator_main_channel"

        // 1. Создаем канал уведомлений (Обязательно для Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Уведомления калькулятора", // То, что пользователь увидит в настройках телефона
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Канал для важных уведомлений от калькулятора"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Настраиваем интент, чтобы открывать MainActivity по клику
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE // IMMUTABLE обязателен для новых Android
        )

        // 3. Собираем само уведомление
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // Иконка (замени на свою, если ic_launcher не сработает, лучше использовать монохромную SVG)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true) // Уведомление исчезнет после клика
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // 4. Показываем (используем Random.nextInt(), чтобы новые пуши не заменяли старые)
        notificationManager.notify(Random.nextInt(), notificationBuilder.build())
    }
}