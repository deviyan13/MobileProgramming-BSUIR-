package com.example.calculator.data.repository

import com.example.calculator.domain.model.AppTheme
import com.example.calculator.domain.model.HistoryRecord
import com.example.calculator.domain.repository.CalculatorRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CalculatorRepositoryImpl : CalculatorRepository {
    private val db = FirebaseFirestore.getInstance()

    // 1. Сохранение истории
    override suspend fun saveHistory(record: HistoryRecord) {
        try {
            val data = mapOf(
                "expression" to record.expression,
                "result" to record.result,
                "timestamp" to record.timestamp
            )
            db.collection("history").add(data).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 2. Получение истории в реальном времени
    override suspend fun getHistory(): Flow<List<HistoryRecord>> = callbackFlow {
        val subscription = db.collection("history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val records = snapshot.documents.mapNotNull { doc ->
                        HistoryRecord(
                            expression = doc.getString("expression") ?: "",
                            result = doc.getString("result") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                    trySend(records)
                }
            }
        awaitClose { subscription.remove() }
    }

    // 3. Получение стартового цвета
    // ПРОВЕРЬ: В Firestore должен быть путь themes/active или поменяй ID на существующий
    override suspend fun getRemoteThemeColor(): String {
        return try {
            // Пытаемся взять документ 'active' из коллекции 'themes'
            // Если ты хранишь текущую тему в другом месте, поменяй ID "active"
            val document = db.collection("themes").document("active").get().await()
            document.getString("color") ?: "#FFCCC2DC"
        } catch (e: Exception) {
            "#FFCCC2DC" // Дефолтный оранжевый, чтобы кнопки не исчезли
        }
    }

    // 4. Получение списка всех тем из коллекции "themes"
    override fun getAvailableThemes(): Flow<List<AppTheme>> = callbackFlow {
        val subscription = db.collection("themes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val themes = snapshot.documents.mapNotNull { doc ->
                        // Пропускаем служебный документ 'active', если он есть,
                        // чтобы он не дублировался в списке кружочков
                        if (doc.id == "active") return@mapNotNull null

                        AppTheme(
                            name = doc.getString("name") ?: "Unknown",
                            color = doc.getString("color") ?: "#FFFFFF"
                        )
                    }
                    trySend(themes)
                }
            }
        awaitClose { subscription.remove() }
    }
}