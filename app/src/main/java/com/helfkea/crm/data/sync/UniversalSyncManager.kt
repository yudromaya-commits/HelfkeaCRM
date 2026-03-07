package com.helfkea.crm.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.helfkea.crm.repository.*
import com.helfkea.crm.workers.UniversalSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class UniversalSyncManager(
    private val context: Context,
    private val taskRepository: OfflineTaskRepository,
    private val contragentRepository: OfflineContragentRepository,
    private val individualRepository: OfflineIndividualRepository,
    private val interactionRepository: OfflineInteractionRepository,
    private val orderRepository: OfflineOrderRepository,
    private val productRepository: OfflineProductRepository,
    private val workManager: WorkManager
) {
    
    companion object {
        private const val TAG = "UniversalSyncManager"
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
        Log.d(TAG, "UniversalSyncManager инициализирован")
        startNetworkMonitoring()
        schedulePeriodicSync()
    }
    
    /**
     * Запустить полную синхронизацию всех данных
     */
    fun syncAll() {
        scope.launch {
            _syncState.value = SyncState.Syncing
            
            var totalSent = 0
            var totalReceived = 0
            var totalFailed = 0
            
            // 1. Загружаем обновления с сервера для всех типов
            val tasksLoaded = taskRepository.loadTasksFromServer()
            val contragentsLoaded = contragentRepository.loadContragentsFromServer()
            val individualsLoaded = individualRepository.loadFromServer()
            val ordersLoaded = orderRepository.loadFromServer()
            val productsLoaded = productRepository.loadFromServer()
            
            if (tasksLoaded) totalReceived++
            if (contragentsLoaded) totalReceived++
            if (individualsLoaded) totalReceived++
            if (ordersLoaded) totalReceived++
            if (productsLoaded) totalReceived++
            
            // 2. Синхронизируем локальные изменения для всех типов
            val taskSyncResult = taskRepository.syncPendingTasks()
            val contragentSyncResult = contragentRepository.syncPendingContragents()
            val individualSyncResult = individualRepository.syncPendingIndividuals()
            val interactionSyncResult = interactionRepository.syncPendingInteractions()
            val orderSyncResult = orderRepository.syncPendingOrders()
            val productSyncResult = productRepository.syncPendingProducts()
            
            // Суммируем результаты
            when (taskSyncResult) {
                is SyncResult.Success -> {
                    totalSent += taskSyncResult.sentCount
                    totalFailed += taskSyncResult.failedCount
                }
                else -> {
                    // Учитываем ошибки
                    totalFailed++
                }
            }
            
            when (contragentSyncResult) {
                is SyncResult.Success -> {
                    totalSent += contragentSyncResult.sentCount
                    totalFailed += contragentSyncResult.failedCount
                }
                else -> {
                    totalFailed++
                }
            }
            
            // Для individuals пока используем boolean результат
            if (individualSyncResult) {
                // Успешная синхронизация
                // TODO: Добавить подсчет отправленных/ошибочных для individuals
            } else {
                totalFailed++
            }
            
            // Для interactions используем boolean результат
            if (interactionSyncResult) {
                // Успешная синхронизация
                // TODO: Добавить подсчет отправленных/ошибочных для interactions
            } else {
                totalFailed++
            }
            
            // Для orders используем boolean результат
            if (orderSyncResult) {
                // Успешная синхронизация
                // TODO: Добавить подсчет отправленных/ошибочных для orders
            } else {
                totalFailed++
            }
            
            // Для products используем boolean результат
            if (productSyncResult) {
                // Успешная синхронизация
                // TODO: Добавить подсчет отправленных/ошибочных для products
            } else {
                totalFailed++
            }
            
            // 3. Обновляем состояние
            _syncState.value = SyncState.Success(
                sentCount = totalSent,
                receivedCount = totalReceived,
                failedCount = totalFailed
            )
            
            // Обновляем счетчики
            updateCounts()
            
            // Сбрасываем состояние через 3 секунды
            delay(3000)
            _syncState.value = SyncState.Idle
        }
    }
    
    /**
     * Запустить синхронизацию только задач
     */
    fun syncTasks() {
        scope.launch {
            _syncState.value = SyncState.Syncing
            
            // Загружаем и синхронизируем задачи
            taskRepository.loadTasksFromServer()
            when (val result = taskRepository.syncPendingTasks()) {
                is SyncResult.Success -> {
                    _syncState.value = SyncState.Success(
                        sentCount = result.sentCount,
                        receivedCount = 1,
                        failedCount = result.failedCount
                    )
                }
                is SyncResult.NoInternet -> {
                    _syncState.value = SyncState.Error("Нет подключения к интернету")
                }
                is SyncResult.Error -> {
                    _syncState.value = SyncState.Error(result.message)
                }
            }
            
            updateCounts()
            
            delay(3000)
            _syncState.value = SyncState.Idle
        }
    }
    
    /**
     * Запустить синхронизацию только контрагентов
     */
    fun syncContragents() {
        scope.launch {
            _syncState.value = SyncState.Syncing
            
            // Загружаем и синхронизируем контрагентов
            contragentRepository.loadContragentsFromServer()
            when (val result = contragentRepository.syncPendingContragents()) {
                is SyncResult.Success -> {
                    _syncState.value = SyncState.Success(
                        sentCount = result.sentCount,
                        receivedCount = 1,
                        failedCount = result.failedCount
                    )
                }
                is SyncResult.NoInternet -> {
                    _syncState.value = SyncState.Error("Нет подключения к интернету")
                }
                is SyncResult.Error -> {
                    _syncState.value = SyncState.Error(result.message)
                }
            }
            
            updateCounts()
            
            delay(3000)
            _syncState.value = SyncState.Idle
        }
    }
    
    /**
     * Запустить синхронизацию только физлиц
     */
    fun syncIndividuals() {
        scope.launch {
            _syncState.value = SyncState.Syncing
            
            // Загружаем и синхронизируем физлиц
            individualRepository.loadFromServer()
            val result = individualRepository.syncPendingIndividuals()
            
            if (result) {
                _syncState.value = SyncState.Success(
                    sentCount = 0, // TODO: Добавить подсчет для individuals
                    receivedCount = 1,
                    failedCount = 0
                )
            } else {
                _syncState.value = SyncState.Error("Ошибка синхронизации физлиц")
            }
            
            updateCounts()
            
            delay(3000)
            _syncState.value = SyncState.Idle
        }
    }
    
    /**
     * Запустить синхронизацию только взаимодействий
     */
    fun syncInteractions() {
        scope.launch {
            _syncState.value = SyncState.Syncing
            
            // Синхронизируем взаимодействия
            val result = interactionRepository.syncPendingInteractions()
            
            if (result) {
                _syncState.value = SyncState.Success(
                    sentCount = 0, // TODO: Добавить подсчет для interactions
                    receivedCount = 0,
                    failedCount = 0
                )
            } else {
                _syncState.value = SyncState.Error("Ошибка синхронизации взаимодействий")
            }
            
            updateCounts()
            
            delay(3000)
            _syncState.value = SyncState.Idle
        }
    }
    
    /**
     * Запустить синхронизацию только заказов
     */
    fun syncOrders() {
        scope.launch {
            _syncState.value = SyncState.Syncing
            
            // Загружаем и синхронизируем заказы
            orderRepository.loadFromServer()
            val result = orderRepository.syncPendingOrders()
            
            if (result) {
                _syncState.value = SyncState.Success(
                    sentCount = 0, // TODO: Добавить подсчет для orders
                    receivedCount = 1,
                    failedCount = 0
                )
            } else {
                _syncState.value = SyncState.Error("Ошибка синхронизации заказов")
            }
            
            updateCounts()
            
            delay(3000)
            _syncState.value = SyncState.Idle
        }
    }
    
    /**
     * Запустить синхронизацию только продуктов
     */
    fun syncProducts() {
        scope.launch {
            _syncState.value = SyncState.Syncing
            
            // Загружаем и синхронизируем продукты
            productRepository.loadFromServer()
            val result = productRepository.syncPendingProducts()
            
            if (result) {
                _syncState.value = SyncState.Success(
                    sentCount = 0, // TODO: Добавить подсчет для products
                    receivedCount = 1,
                    failedCount = 0
                )
            } else {
                _syncState.value = SyncState.Error("Ошибка синхронизации продуктов")
            }
            
            updateCounts()
            
            delay(3000)
            _syncState.value = SyncState.Idle
        }
    }
    
    /**
     * Запланировать периодическую синхронизацию
     */
    private fun schedulePeriodicSync() {
        val workRequest = UniversalSyncWorker.createPeriodicWorkRequest()
        workManager.enqueueUniquePeriodicWork(
            UniversalSyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        Log.d(TAG, "Периодическая синхронизация запланирована")
    }
    
    /**
     * Запустить одноразовую синхронизацию в фоне
     */
    fun triggerBackgroundSync() {
        val workRequest = UniversalSyncWorker.createOneTimeWorkRequest()
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
     * Обновить счетчики ожидающих и ошибочных элементов
     */
    private suspend fun updateCounts() {
        val taskPending = taskRepository.getPendingCount()
        val taskFailed = taskRepository.getFailedCount()
        val contragentPending = contragentRepository.getPendingCount()
        val contragentFailed = contragentRepository.getFailedCount()
        val individualPending = individualRepository.getPendingCount()
        val individualFailed = individualRepository.getFailedCount()
        val interactionPending = interactionRepository.getPendingCount()
        val interactionFailed = interactionRepository.getFailedCount()
        val orderPending = orderRepository.getPendingCount()
        val orderFailed = orderRepository.getFailedCount()
        val productPending = productRepository.getPendingCount()
        val productFailed = productRepository.getFailedCount()
        
        val totalPending = taskPending + contragentPending + individualPending + interactionPending + orderPending + productPending
        val totalFailed = taskFailed + contragentFailed + individualFailed + interactionFailed + orderFailed + productFailed
        
        if (_pendingCount.value != totalPending) {
            _pendingCount.value = totalPending
            Log.d(TAG, "Обновлен счетчик ожидающих: задачи=$taskPending, контрагенты=$contragentPending, физлица=$individualPending, взаимодействия=$interactionPending, заказы=$orderPending, продукты=$productPending")
        }
        
        if (_failedCount.value != totalFailed) {
            _failedCount.value = totalFailed
            Log.d(TAG, "Обновлен счетчик ошибочных: задачи=$taskFailed, контрагенты=$contragentFailed, физлица=$individualFailed, взаимодействия=$interactionFailed, заказы=$orderFailed, продукты=$productFailed")
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
     * Получить детализированную статистику
     */
    suspend fun getDetailedStats(): SyncStats {
        return SyncStats(
            tasksPending = taskRepository.getPendingCount(),
            tasksFailed = taskRepository.getFailedCount(),
            contragentsPending = contragentRepository.getPendingCount(),
            contragentsFailed = contragentRepository.getFailedCount(),
            individualsPending = individualRepository.getPendingCount(),
            individualsFailed = individualRepository.getFailedCount(),
            interactionsPending = interactionRepository.getPendingCount(),
            interactionsFailed = interactionRepository.getFailedCount(),
            ordersPending = orderRepository.getPendingCount(),
            ordersFailed = orderRepository.getFailedCount(),
            productsPending = productRepository.getPendingCount(),
            productsFailed = productRepository.getFailedCount()
        )
    }
    
    /**
     * Остановить мониторинг
     */
    fun stop() {
        networkCheckJob?.cancel()
        networkCheckJob = null
        Log.d(TAG, "UniversalSyncManager остановлен")
    }
}

/**
 * Детализированная статистика синхронизации
 */
data class SyncStats(
    val tasksPending: Int = 0,
    val tasksFailed: Int = 0,
    val contragentsPending: Int = 0,
    val contragentsFailed: Int = 0,
    val individualsPending: Int = 0,
    val individualsFailed: Int = 0,
    val interactionsPending: Int = 0,
    val interactionsFailed: Int = 0,
    val ordersPending: Int = 0,
    val ordersFailed: Int = 0,
    val productsPending: Int = 0,
    val productsFailed: Int = 0
) {
    val totalPending: Int get() = tasksPending + contragentsPending + individualsPending + interactionsPending + ordersPending + productsPending
    val totalFailed: Int get() = tasksFailed + contragentsFailed + individualsFailed + interactionsFailed + ordersFailed + productsFailed
    val hasPending: Boolean get() = totalPending > 0
    val hasFailed: Boolean get() = totalFailed > 0
}