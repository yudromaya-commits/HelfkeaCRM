package com.helfkea.crm

import android.app.Application
import android.util.Log
import androidx.work.WorkManager
import com.helfkea.crm.data.local.database.AppDatabase
import com.helfkea.crm.data.network.NetworkMonitor
import com.helfkea.crm.data.sync.UniversalSyncManager
import com.helfkea.crm.repository.OfflineContragentRepository
import com.helfkea.crm.repository.OfflineIndividualRepository
import com.helfkea.crm.repository.OfflineInteractionRepository
import com.helfkea.crm.repository.OfflineOrderRepository
import com.helfkea.crm.repository.OfflineProductRepository
import com.helfkea.crm.repository.OfflineTaskRepository

class HelfkeaCRMApp : Application() {
    
    companion object {
        private const val TAG = "HelfkeaCRMApp"
        
        // Глобальные инстансы (упрощенная версия без DI)
        lateinit var universalSyncManager: UniversalSyncManager
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Приложение запущено, инициализируем систему синхронизации...")
        
        // Инициализируем компоненты синхронизации
        initSyncSystem()
        
        Log.d(TAG, "Система синхронизации инициализирована")
    }
    
    private fun initSyncSystem() {
        try {
            // Создаем компоненты
            val database = AppDatabase.getInstance(this)
            val networkMonitor = NetworkMonitor(this)
            val workManager = WorkManager.getInstance(this)
            
            // Создаем репозитории
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
            
            // Создаем UniversalSyncManager
            universalSyncManager = UniversalSyncManager(
                context = this,
                taskRepository = taskRepository,
                contragentRepository = contragentRepository,
                individualRepository = individualRepository,
                interactionRepository = interactionRepository,
                orderRepository = orderRepository,
                productRepository = productRepository,
                workManager = workManager
            )
            
            Log.d(TAG, "UniversalSyncManager создан успешно")
            
            // Загружаем начальные данные с сервера (если есть интернет)
            if (networkMonitor.isConnected()) {
                Log.d(TAG, "Есть интернет, загружаем начальные данные...")
                // Запускаем в фоне
                workManager.enqueue(com.helfkea.crm.workers.UniversalSyncWorker.createOneTimeWorkRequest())
            } else {
                Log.d(TAG, "Нет интернета, работаем в офлайн-режиме")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка инициализации системы синхронизации: ${e.message}", e)
        }
    }
    
    override fun onTerminate() {
        Log.d(TAG, "Приложение завершает работу...")
        universalSyncManager.stop()
        super.onTerminate()
    }
}