package com.helfkea.crm.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.helfkea.crm.repository.OfflineTaskRepository
import com.helfkea.crm.workers.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SyncManager(
    private val context: Context,
    private val taskRepository: OfflineTaskRepository,
    private val workManager: WorkManager
) {
    
    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_INTERVAL_MS = 30000L // 30 секунд для проверки сети
    }
    
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()
    
    private val _failedCount = MutableStateFlow(0)
    val failedCount: StateFlow<Int> = _failedCount.asStateFlow()
    
    private var networkCheckJob: Job? = null
    
    init {
        Log.d(TAG, "SyncManager инициализирован")
        startNetworkMonitoring()
        schedulePeriodicSync()
    }
    
    /**
     * Запустить синхронизацию вручную
     */
    fun sync() {
        scope.launch {
            _syncState.value = SyncState.Syncing
            
            // 1. Загружаем обновления с сервера
            taskRepository.loadTasksFromServer()
            
            // 2. Синхронизируем локальные изменения
            when (val result = taskRepository.syncPendingTasks()) {
                is com.helfkea.crm.repository.SyncResult.Success -> {
                    _syncState.value = SyncState.Success(
                        sentCount = result.sentCount,
                        receivedCount = result.receivedCount,
                        failedCount = result.failedCount
                    )
                    updateCounts()
                }
                is com.helfkea.crm.repository.SyncResult.NoInternet -> {
                    _syncState.value = SyncState.Error("Нет подключения к интернету")
                }
                is com.helfkea.crm.repository.SyncResult.Error -> {
                    _syncState.value = SyncState.Error(result.message)
                }
            }
            
            // Сбрасываем состояние через 3 секунды
            delay(3000)
            _syncState.value = SyncState.Idle
        }
    }
    
    /**
     * Запланировать периодическую синхронизацию
     */
    private fun schedulePeriodicSync() {
        val workRequest = SyncWorker.createPeriodicWorkRequest()
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        Log.d(TAG, "Периодическая синхронизация запланирована")
    }
    
    /**
     * Запустить одноразовую синхронизацию в фоне
     */
    fun triggerBackgroundSync() {
        val workRequest = SyncWorker.createOneTimeWorkRequest()
        workManager.enqueue(workRequest)
        Log.d(TAG, "Одноразовая фоновая синхронизация запущена")
    }
    
    /**
     * Начать мониторинг сети для автоматической синхронизации
     */
    private fun startNetworkMonitoring() {
        networkCheckJob?.cancel()
        networkCheckJob = scope.launch {
            while (true) {
                updateCounts()
                delay(SYNC_INTERVAL_MS)
            }
        }
    }
    
    /**
     * Обновить счетчики ожидающих и ошибочных задач
     */
    private suspend fun updateCounts() {
        val pending = taskRepository.getPendingCount()
        val failed = taskRepository.getFailedCount()
        
        if (_pendingCount.value != pending) {
            _pendingCount.value = pending
            Log.d(TAG, "Обновлен счетчик ожидающих задач: $pending")
        }
        
        if (_failedCount.value != failed) {
            _failedCount.value = failed
            Log.d(TAG, "Обновлен счетчик ошибочных задач: $failed")
        }
    }
    
    /**
     * Получить текущий статус синхронизации для отображения
     */
    fun getSyncStatusText(): String {
        return when (val state = _syncState.value) {
            is SyncState.Syncing -> "Синхронизация..."
            is SyncState.Success -> {
                if (state.failedCount > 0) {
                    "Синхронизировано: ${state.sentCount} отправлено, ${state.failedCount} ошибок"
                } else if (state.sentCount > 0) {
                    "Синхронизировано: ${state.sentCount} отправлено"
                } else {
                    "Всё синхронизировано"
                }
            }
            is SyncState.Error -> "Ошибка: ${state.message}"
            else -> ""
        }
    }
    
    /**
     * Получить цвет статуса для индикации
     */
    fun getSyncStatusColor(): SyncStatusColor {
        return when {
            _syncState.value is SyncState.Syncing -> SyncStatusColor.SYNCING
            _failedCount.value > 0 -> SyncStatusColor.ERROR
            _pendingCount.value > 0 -> SyncStatusColor.PENDING
            else -> SyncStatusColor.SYNCED
        }
    }
    
    /**
     * Остановить мониторинг
     */
    fun stop() {
        networkCheckJob?.cancel()
        networkCheckJob = null
        Log.d(TAG, "SyncManager остановлен")
    }
}

/**
 * Состояния синхронизации
 */
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(
        val sentCount: Int,
        val receivedCount: Int,
        val failedCount: Int
    ) : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * Цвета для индикации статуса синхронизации
 */
enum class SyncStatusColor {
    SYNCED,     // Зеленый - всё синхронизировано
    PENDING,    // Желтый - есть ожидающие задачи
    ERROR,      // Красный - есть ошибки
    SYNCING     // Синий - идёт синхронизация
}