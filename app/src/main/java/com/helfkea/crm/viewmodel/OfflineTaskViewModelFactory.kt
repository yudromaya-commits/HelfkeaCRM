package com.helfkea.crm.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.helfkea.crm.HelfkeaCRMApp
import com.helfkea.crm.data.local.database.AppDatabase
import com.helfkea.crm.data.network.NetworkMonitor
import com.helfkea.crm.data.sync.UniversalSyncManager
import com.helfkea.crm.repository.OfflineTaskRepository

class OfflineTaskViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(OfflineTaskViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        
        // Используем глобальный UniversalSyncManager из Application
        val syncManager = HelfkeaCRMApp.universalSyncManager
        
        // Создаем репозиторий
        val database = AppDatabase.getInstance(context)
        val networkMonitor = NetworkMonitor(context)
        val taskRepository = OfflineTaskRepository(
            taskDao = database.taskDao(),
            taskApi = com.helfkea.crm.api.TaskApi,
            networkMonitor = networkMonitor
        )
        
        return OfflineTaskViewModel(taskRepository, syncManager) as T
    }
    
    companion object {
        private var instance: OfflineTaskViewModelFactory? = null
        
        fun getInstance(context: Context): OfflineTaskViewModelFactory {
            return instance ?: synchronized(this) {
                instance ?: OfflineTaskViewModelFactory(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}