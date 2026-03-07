package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.data.local.dao.IndividualDao
import com.helfkea.crm.data.local.mapper.IndividualMapper
import com.helfkea.crm.data.network.NetworkMonitor
import com.helfkea.crm.data.sync.SyncStatus
import com.helfkea.crm.model.Individual
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class OfflineIndividualRepository(
    private val individualDao: IndividualDao,
    private val taskApi: TaskApi,
    private val networkMonitor: NetworkMonitor
) {
    
    companion object {
        private const val TAG = "OfflineIndividualRepo"
    }
    
    // Получение данных
    fun getAllIndividuals(): Flow<List<Individual>> {
        return individualDao.getAllIndividuals().map { entities ->
            IndividualMapper.toDomainList(entities)
        }
    }
    
    suspend fun getIndividualById(id: String): Individual? {
        val entity = individualDao.getIndividualById(id)
        return entity?.let { IndividualMapper.toDomain(it) }
    }
    
    suspend fun getIndividualByServerId(serverId: String): Individual? {
        val entity = individualDao.getIndividualByServerId(serverId)
        return entity?.let { IndividualMapper.toDomain(it) }
    }
    
    // Синхронизация - только загрузка с сервера (физлица только для чтения)
    suspend fun syncPendingIndividuals(): Boolean {
        // Для физлиц только загрузка с сервера, нет создания/обновления
        return loadFromServer()
    }
    
    // Загрузка данных с сервера
    suspend fun loadFromServer(): Boolean {
        return try {
            Log.d(TAG, "Загружаем физлица с сервера...")
            
            if (!networkMonitor.isConnected()) {
                Log.d(TAG, "Нет интернета, пропускаем загрузку физлиц")
                return false
            }
            
            val serverIndividuals = taskApi.service.getIndividuals()
            Log.d(TAG, "Получено ${serverIndividuals.size} физлиц с сервера")
            
            // Конвертируем в Entity с статусом SYNCED
            val entities = IndividualMapper.toEntityList(serverIndividuals, SyncStatus.SYNCED)
            
            // Сохраняем в БД
            individualDao.deleteAll()  // Очищаем старые данные
            individualDao.insertAll(entities)
            
            Log.d(TAG, "Физлица успешно загружены и сохранены")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки физлиц с сервера: ${e.message}", e)
            false
        }
    }
    
    // Статистика синхронизации
    suspend fun getPendingCount(): Int = individualDao.getPendingCount()
    suspend fun getFailedCount(): Int = individualDao.getFailedCount()
    suspend fun getSyncedCount(): Int = individualDao.getSyncedCount()
    
    // Вспомогательные методы
    private fun parseDate(dateString: String?): Date? {
        if (dateString.isNullOrBlank()) return null
        
        return try {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun formatDate(date: Date?): String {
        return date?.let {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(it)
        } ?: ""
    }
}