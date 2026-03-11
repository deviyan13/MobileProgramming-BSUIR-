package com.example.calculator.domain.repository

import com.example.calculator.domain.model.AppTheme
import com.example.calculator.domain.model.HistoryRecord
import kotlinx.coroutines.flow.Flow

interface CalculatorRepository {
    suspend fun saveHistory(record: HistoryRecord)
    suspend fun getHistory(): Flow<List<HistoryRecord>>
    suspend fun getRemoteThemeColor(): String

    fun getAvailableThemes(): Flow<List<AppTheme>>
}