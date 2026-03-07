package com.helfkea.crm.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import androidx.work.workDataOf
import com.helfkea.crm.data.local.database.AppDatabase
import com.helfkea.crm.data.network.NetworkMonitor
import com.helfkea.crm.repository.OfflineTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_TAG = "sync_worker"
        const val WORK_NAME_PERIODIC = "periodic_sync"
        const val WORK_NAME_ONETIME = "onetime_sync"
        
        /**
         * Создать WorkRequest для периодической синхронизации
         */
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SyncWorker>(
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
            return OneTimeWorkRequestBuilder<SyncWorker>()
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
                Log.d(TAG, "Запуск фоновой синхронизации...")
                
                // Создаем репозиторий внутри Worker
                val database = AppDatabase.getInstance(context)
                val networkMonitor = NetworkMonitor(context)
                val taskRepository = OfflineTaskRepository(
                    taskDao = database.taskDao(),
                    taskApi = com.helfkea.crm.api.TaskApi,
                    networkMonitor = networkMonitor
                )
                
                // 1. Загружаем обновления с сервера
                val loadSuccess = taskRepository.loadTasksFromServer()
                if (!loadSuccess) {
                    Log.w(TAG, "Не удалось загрузить задачи с сервера")
                }
                
                // 2. Синхронизируем локальные изменения
                when (val syncResult = taskRepository.syncPendingTasks()) {
                    is com.helfkea.crm.repository.SyncResult.Success -> {
                        Log.d(TAG, "Синхронизация завершена: отправлено=${syncResult.sentCount}, ошибок=${syncResult.failedCount}")
                        
                        if (syncResult.failedCount > 0) {
                            // Некоторые задачи не синхронизировались
                            Result.success(
                                Data.Builder()
                                    .putString("result", "partial")
                                    .putInt("sent", syncResult.sentCount)
                                    .putInt("failed", syncResult.failedCount)
                                    .build()
                            )
                        } else if (syncResult.sentCount > 0) {
                            // Все успешно
                            Result.success(
                                Data.Builder()
                                    .putString("result", "success")
                                    .putInt("sent", syncResult.sentCount)
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
                    }
                    
                    is com.helfkea.crm.repository.SyncResult.NoInternet -> {
                        Log.d(TAG, "Нет подключения к интернету")
                        Result.retry()  // Повторим позже
                    }
                    
                    is com.helfkea.crm.repository.SyncResult.Error -> {
                        Log.e(TAG, "Ошибка синхронизации: ${syncResult.message}")
                        Result.failure(
                            Data.Builder()
                                .putString("error", syncResult.message)
                                .build()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение в SyncWorker: ${e.message}", e)
                Result.failure(
                    Data.Builder()
                        .putString("error", e.message ?: "Unknown error")
                        .build()
                )
            }
        }
    }
}