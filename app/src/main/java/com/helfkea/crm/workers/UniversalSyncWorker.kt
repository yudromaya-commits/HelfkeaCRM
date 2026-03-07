package com.helfkea.crm.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.helfkea.crm.data.local.database.AppDatabase
import com.helfkea.crm.data.network.NetworkMonitor
import com.helfkea.crm.repository.OfflineContragentRepository
import com.helfkea.crm.repository.OfflineIndividualRepository
import com.helfkea.crm.repository.OfflineInteractionRepository
import com.helfkea.crm.repository.OfflineOrderRepository
import com.helfkea.crm.repository.OfflineProductRepository
import com.helfkea.crm.repository.OfflineTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class UniversalSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "UniversalSyncWorker"
        const val WORK_TAG = "universal_sync_worker"
        const val WORK_NAME_PERIODIC = "universal_periodic_sync"
        const val WORK_NAME_ONETIME = "universal_onetime_sync"
        
        /**
         * Создать WorkRequest для периодической синхронизации
         */
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<UniversalSyncWorker>(
                15, TimeUnit.MINUTES  // Синхронизация каждые 15 минут
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(WORK_TAG)
                .build()
        }
        
        /**
         * Создать WorkRequest для одноразовой синхронизации
         */
        fun createOneTimeWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<UniversalSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(WORK_TAG)
                .build()
        }
    }
    
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Запуск универсальной фоновой синхронизации...")
                
                // Создаем репозитории внутри Worker
                val database = AppDatabase.getInstance(context)
                val networkMonitor = NetworkMonitor(context)
                
                val taskRepository = OfflineTaskRepository(
                    taskDao = database.taskDao(),
                    taskApi = com.helfkea.crm.api.TaskApi,
                    networkMonitor = networkMonitor
                )
                
                val contragentRepository = OfflineContragentRepository(
                    contragentDao = database.contragentDao(),
                    taskApi = com.helfkea.crm.api.TaskApi,
                    networkMonitor = networkMonitor
                )
                
                val individualRepository = OfflineIndividualRepository(
                    individualDao = database.individualDao(),
                    taskApi = com.helfkea.crm.api.TaskApi,
                    networkMonitor = networkMonitor
                )
                
                val interactionRepository = OfflineInteractionRepository(
                    interactionDao = database.interactionDao(),
                    taskApi = com.helfkea.crm.api.TaskApi,
                    networkMonitor = networkMonitor
                )
                
                val orderRepository = OfflineOrderRepository(
                    orderDao = database.orderDao(),
                    taskApi = com.helfkea.crm.api.TaskApi,
                    networkMonitor = networkMonitor
                )
                
                val productRepository = OfflineProductRepository(
                    productDao = database.productDao(),
                    taskApi = com.helfkea.crm.api.TaskApi,
                    networkMonitor = networkMonitor
                )
                
                var totalSent = 0
                var totalFailed = 0
                var hasErrors = false
                
                // 1. Загружаем обновления с сервера
                Log.d(TAG, "Загрузка обновлений с сервера...")
                
                val tasksLoaded = taskRepository.loadTasksFromServer()
                if (!tasksLoaded) {
                    Log.w(TAG, "Не удалось загрузить задачи с сервера")
                }
                
                val contragentsLoaded = contragentRepository.loadContragentsFromServer()
                if (!contragentsLoaded) {
                    Log.w(TAG, "Не удалось загрузить контрагентов с сервера")
                }
                
                // 2. Синхронизируем локальные изменения
                Log.d(TAG, "Синхронизация локальных изменений...")
                
                // Задачи
                when (val taskResult = taskRepository.syncPendingTasks()) {
                    is com.helfkea.crm.repository.SyncResult.Success -> {
                        totalSent += taskResult.sentCount
                        totalFailed += taskResult.failedCount
                        if (taskResult.failedCount > 0) hasErrors = true
                        Log.d(TAG, "Задачи: отправлено=${taskResult.sentCount}, ошибок=${taskResult.failedCount}")
                    }
                    is com.helfkea.crm.repository.SyncResult.NoInternet -> {
                        Log.d(TAG, "Нет интернета для синхронизации задач")
                        return@withContext Result.retry()
                    }
                    is com.helfkea.crm.repository.SyncResult.Error -> {
                        Log.e(TAG, "Ошибка синхронизации задач: ${taskResult.message}")
                        hasErrors = true
                    }
                }
                
                // Контрагенты
                when (val contragentResult = contragentRepository.syncPendingContragents()) {
                    is com.helfkea.crm.repository.SyncResult.Success -> {
                        totalSent += contragentResult.sentCount
                        totalFailed += contragentResult.failedCount
                        if (contragentResult.failedCount > 0) hasErrors = true
                        Log.d(TAG, "Контрагенты: отправлено=${contragentResult.sentCount}, ошибок=${contragentResult.failedCount}")
                    }
                    is com.helfkea.crm.repository.SyncResult.NoInternet -> {
                        Log.d(TAG, "Нет интернета для синхронизации контрагентов")
                        return@withContext Result.retry()
                    }
                    is com.helfkea.crm.repository.SyncResult.Error -> {
                        Log.e(TAG, "Ошибка синхронизации контрагентов: ${contragentResult.message}")
                        hasErrors = true
                    }
                }
                
                // Физлица
                try {
                    val individualsLoaded = individualRepository.loadFromServer()
                    if (!individualsLoaded) {
                        Log.w(TAG, "Не удалось загрузить физлица с сервера")
                    }
                    
                    val individualsResult = individualRepository.syncPendingIndividuals()
                    if (individualsResult) {
                        Log.d(TAG, "Физлица успешно синхронизированы")
                    } else {
                        Log.e(TAG, "Ошибка синхронизации физлиц")
                        hasErrors = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Исключение при синхронизации физлиц: ${e.message}", e)
                    hasErrors = true
                }
                
                // Взаимодействия
                try {
                    val interactionsResult = interactionRepository.syncPendingInteractions()
                    if (interactionsResult) {
                        Log.d(TAG, "Взаимодействия успешно синхронизированы")
                    } else {
                        Log.e(TAG, "Ошибка синхронизации взаимодействий")
                        hasErrors = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Исключение при синхронизации взаимодействий: ${e.message}", e)
                    hasErrors = true
                }
                
                // Заказы
                try {
                    val ordersLoaded = orderRepository.loadFromServer()
                    if (!ordersLoaded) {
                        Log.w(TAG, "Не удалось загрузить заказы с сервера")
                    }
                    
                    val ordersResult = orderRepository.syncPendingOrders()
                    if (ordersResult) {
                        Log.d(TAG, "Заказы успешно синхронизированы")
                    } else {
                        Log.e(TAG, "Ошибка синхронизации заказов")
                        hasErrors = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Исключение при синхронизации заказов: ${e.message}", e)
                    hasErrors = true
                }
                
                // Продукты
                try {
                    val productsLoaded = productRepository.loadFromServer()
                    if (!productsLoaded) {
                        Log.w(TAG, "Не удалось загрузить продукты с сервера")
                    }
                    
                    val productsResult = productRepository.syncPendingProducts()
                    if (productsResult) {
                        Log.d(TAG, "Продукты успешно синхронизированы")
                    } else {
                        Log.e(TAG, "Ошибка синхронизации продуктов")
                        hasErrors = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Исключение при синхронизации продуктов: ${e.message}", e)
                    hasErrors = true
                }
                
                // 3. Формируем результат
                Log.d(TAG, "Синхронизация завершена: всего отправлено=$totalSent, ошибок=$totalFailed")
                
                if (hasErrors || totalFailed > 0) {
                    // Некоторые элементы не синхронизировались
                    Result.success(
                        Data.Builder()
                            .putString("result", "partial")
                            .putInt("sent", totalSent)
                            .putInt("failed", totalFailed)
                            .build()
                    )
                } else if (totalSent > 0) {
                    // Все успешно
                    Result.success(
                        Data.Builder()
                            .putString("result", "success")
                            .putInt("sent", totalSent)
                            .build()
                    )
                } else {
                    // Нет изменений для синхронизации
                    Result.success(
                        Data.Builder()
                            .putString("result", "no_changes")
                            .build()
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение в UniversalSyncWorker: ${e.message}", e)
                Result.failure(
                    Data.Builder()
                        .putString("error", e.message ?: "Unknown error")
                        .build()
                )
            }
        }
    }
}