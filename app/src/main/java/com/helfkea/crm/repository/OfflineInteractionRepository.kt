package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.data.local.dao.InteractionDao
import com.helfkea.crm.data.local.mapper.InteractionMapper
import com.helfkea.crm.data.network.NetworkMonitor
import com.helfkea.crm.data.sync.SyncStatus
import com.helfkea.crm.model.CreateInteractionRequest
import com.helfkea.crm.model.CreateInteractionResponse
import com.helfkea.crm.model.Interaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response
import java.util.*

class OfflineInteractionRepository(
    private val interactionDao: InteractionDao,
    private val taskApi: TaskApi,
    private val networkMonitor: NetworkMonitor
) {
    
    companion object {
        private const val TAG = "OfflineInteractionRepo"
    }
    
    // Получение данных
    fun getAllInteractions(): Flow<List<Interaction>> {
        return interactionDao.getAllInteractions().map { entities ->
            InteractionMapper.toDomainList(entities)
        }
    }
    
    fun getInteractionsByContragentId(contragentId: String): Flow<List<Interaction>> {
        return interactionDao.getInteractionsByContragentId(contragentId).map { entities ->
            InteractionMapper.toDomainList(entities)
        }
    }
    
    suspend fun getInteractionById(id: String): Interaction? {
        val entity = interactionDao.getInteractionById(id)
        return entity?.let { InteractionMapper.toDomain(it) }
    }
    
    suspend fun getInteractionByServerId(serverId: String): Interaction? {
        val entity = interactionDao.getInteractionByServerId(serverId)
        return entity?.let { InteractionMapper.toDomain(it) }
    }
    
    // Создание взаимодействия
    suspend fun createInteraction(request: CreateInteractionRequest, contragentName: String? = null, contactType: String? = null, manager: String? = null): CreateInteractionResponse {
        return if (networkMonitor.isConnected()) {
            createInteractionOnline(request, contragentName, contactType, manager)
        } else {
            createInteractionOffline(request, contragentName, contactType, manager)
        }
    }
    
    private suspend fun createInteractionOnline(request: CreateInteractionRequest, contragentName: String? = null, contactType: String? = null, manager: String? = null): CreateInteractionResponse {
        return try {
            Log.d(TAG, "Создаем взаимодействие онлайн для контрагента: ${request.contragentId}")
            
            val response = taskApi.service.createInteraction(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.result == true) {
                    // Взаимодействие успешно создано на сервере
                    Log.d(TAG, "Взаимодействие создано на сервере")
                    
                    // Сохраняем в локальную БД как синхронизированное
                    // Note: Сервер не возвращает ID, поэтому используем локальный
                    val interactionEntity = InteractionMapper.fromCreateRequest(
                        request = request,
                        contragentName = contragentName,
                        contactType = contactType,
                        manager = manager
                    ).copy(
                        syncStatus = SyncStatus.SYNCED,
                        lastSyncTime = Date()
                    )
                    
                    interactionDao.insertInteraction(interactionEntity)
                    
                    CreateInteractionResponse(
                        error = null,
                        result = true
                    )
                } else {
                    // Сервер вернул ошибку
                    Log.w(TAG, "Сервер вернул ошибку: ${body?.error}")
                    createInteractionOffline(request, contragentName, contactType, manager).copy(
                        error = body?.error ?: "Ошибка сервера",
                        result = false
                    )
                }
            } else {
                // Ошибка HTTP
                Log.e(TAG, "Ошибка HTTP: ${response.code()}")
                createInteractionOffline(request, contragentName, contactType, manager).copy(
                    error = "Ошибка сервера: ${response.code()}",
                    result = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сети: ${e.message}", e)
            createInteractionOffline(request, contragentName, contactType, manager).copy(
                error = "Ошибка сети: ${e.message}",
                result = false
            )
        }
    }
    
    private suspend fun createInteractionOffline(request: CreateInteractionRequest, contragentName: String? = null, contactType: String? = null, manager: String? = null): CreateInteractionResponse {
        Log.d(TAG, "Создаем взаимодействие офлайн для контрагента: ${request.contragentId}")
        
        val interactionEntity = InteractionMapper.fromCreateRequest(
            request = request,
            contragentName = contragentName,
            contactType = contactType,
            manager = manager
        )
        
        interactionDao.insertInteraction(interactionEntity)
        
        return CreateInteractionResponse(
            error = null,
            result = true
        )
    }
    
    // Синхронизация
    suspend fun syncPendingInteractions(): Boolean {
        return try {
            Log.d(TAG, "Начинаем синхронизацию взаимодействий...")
            
            // Получаем взаимодействия, ожидающие синхронизации
            val pendingEntities = interactionDao.getInteractionsBySyncStatuses(
                listOf(SyncStatus.PENDING, SyncStatus.FAILED)
            )
            
            if (pendingEntities.isEmpty()) {
                Log.d(TAG, "Нет взаимодействий для синхронизации")
                return true
            }
            
            Log.d(TAG, "Найдено ${pendingEntities.size} взаимодействий для синхронизации")
            
            var successCount = 0
            var errorCount = 0
            
            for (entity in pendingEntities) {
                try {
                    // Создаем запрос на основе локальных данных
                    val request = CreateInteractionRequest(
                        contragentId = entity.contragentId,
                        typeContactId = entity.typeContactId,
                        secure = entity.secure,
                        comment = entity.comment
                    )
                    
                    val response = taskApi.service.createInteraction(request)
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody?.result == true) {
                            // Успешно синхронизировано
                            // Note: Сервер не возвращает ID для взаимодействий
                            interactionDao.updateInteractionSyncStatus(
                                id = entity.id,
                                syncStatus = SyncStatus.SYNCED,
                                lastSyncTime = Date(),
                                syncError = null
                            )
                            successCount++
                            Log.d(TAG, "Взаимодействие синхронизировано: ${entity.comment.take(50)}...")
                        } else {
                            // Ошибка сервера
                            interactionDao.updateInteractionSyncStatus(
                                id = entity.id,
                                syncStatus = SyncStatus.FAILED,
                                lastSyncTime = Date(),
                                syncError = "Ошибка синхронизации: сервер вернул result=false"
                            )
                            errorCount++
                            Log.w(TAG, "Ошибка синхронизации взаимодействия")
                        }
                    } else {
                        // Ошибка HTTP
                        interactionDao.updateInteractionSyncStatus(
                            id = entity.id,
                            syncStatus = SyncStatus.FAILED,
                            lastSyncTime = Date(),
                            syncError = "Ошибка HTTP: ${response.code()}"
                        )
                        errorCount++
                        Log.e(TAG, "HTTP ошибка при синхронизации взаимодействия: ${response.code()}")
                    }
                } catch (e: Exception) {
                    // Ошибка сети
                    interactionDao.updateInteractionSyncStatus(
                        id = entity.id,
                        syncStatus = SyncStatus.FAILED,
                        lastSyncTime = Date(),
                        syncError = "Ошибка сети: ${e.message}"
                    )
                    errorCount++
                    Log.e(TAG, "Сетевая ошибка при синхронизации взаимодействия: ${e.message}", e)
                }
            }
            
            Log.d(TAG, "Синхронизация взаимодействий завершена: успешно $successCount, ошибок $errorCount")
            errorCount == 0
            
        } catch (e: Exception) {
            Log.e(TAG, "Критическая ошибка при синхронизации взаимодействий: ${e.message}", e)
            false
        }
    }
    
    // Загрузка данных с сервера (для взаимодействий загрузка через контрагентов)
    suspend fun loadFromServer(contragentId: String, interactions: List<Interaction>, contragentName: String? = null): Boolean {
        return try {
            Log.d(TAG, "Загружаем взаимодействия для контрагента: $contragentId")
            
            // Конвертируем в Entity с статусом SYNCED
            val entities = InteractionMapper.toEntityList(interactions, contragentId, contragentName, SyncStatus.SYNCED)
            
            // Удаляем старые взаимодействия для этого контрагента
            // Note: В реальном приложении нужно более сложное обновление
            val existing = interactionDao.getInteractionsByContragentId(contragentId)
            // TODO: Реализовать умное обновление
            
            // Добавляем новые
            interactionDao.insertAll(entities)
            
            Log.d(TAG, "Взаимодействия успешно загружены и сохранены: ${entities.size} шт.")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки взаимодействий: ${e.message}", e)
            false
        }
    }
    
    // Статистика синхронизации
    suspend fun getPendingCount(): Int = interactionDao.getPendingCount()
    suspend fun getFailedCount(): Int = interactionDao.getFailedCount()
    suspend fun getSyncedCount(): Int = interactionDao.getSyncedCount()
}