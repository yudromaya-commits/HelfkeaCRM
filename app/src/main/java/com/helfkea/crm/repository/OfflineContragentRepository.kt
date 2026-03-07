package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.data.local.dao.ContragentDao
import com.helfkea.crm.data.local.mapper.ContragentMapper
import com.helfkea.crm.data.network.NetworkMonitor
import com.helfkea.crm.data.sync.SyncStatus
import com.helfkea.crm.model.Contragent
import com.helfkea.crm.model.CreateContragentRequest
import com.helfkea.crm.model.CreateContragentResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
class OfflineContragentRepository(
    private val contragentDao: ContragentDao,
    private val taskApi: TaskApi,
    private val networkMonitor: NetworkMonitor
) {
    
    companion object {
        private const val TAG = "OfflineContragentRepo"
    }
    
    /**
     * Получить всех контрагентов как Flow
     */
    fun getAllContragents(): Flow<List<Contragent>> {
        return contragentDao.getAllContragents()
            .map { entities -> ContragentMapper.toDomainList(entities) }
    }
    
    /**
     * Поиск контрагентов
     */
    fun searchContragents(query: String): Flow<List<Contragent>> {
        return contragentDao.searchContragents(query)
            .map { entities -> ContragentMapper.toDomainList(entities) }
    }
    
    /**
     * Получить контрагентов по статусу
     */
    fun getContragentsByStatus(status: String): Flow<List<Contragent>> {
        return contragentDao.getContragentsByStatus(status)
            .map { entities -> ContragentMapper.toDomainList(entities) }
    }
    
    /**
     * Создать контрагента локально (офлайн)
     */
    suspend fun createContragentLocal(
        name: String,
        fullName: String? = null,
        inn: String? = null,
        kpp: String? = null,
        ogrn: String? = null,
        legalAddress: String? = null,
        actualAddress: String? = null,
        phone: String? = null,
        email: String? = null,
        website: String? = null,
        manager: String? = null,
        status: String? = "Активен",
        category: String? = null,
        notes: String? = null
    ): Contragent {
        Log.d(TAG, "Создание локального контрагента: $name")
        
        val contragentEntity = com.helfkea.crm.data.local.entity.ContragentEntity.createLocal(
            name = name,
            fullName = fullName,
            inn = inn,
            kpp = kpp,
            ogrn = ogrn,
            legalAddress = legalAddress,
            actualAddress = actualAddress,
            phone = phone,
            email = email,
            website = website,
            manager = manager,
            status = status,
            category = category,
            notes = notes
        )
        
        contragentDao.insertContragent(contragentEntity)
        
        return ContragentMapper.toDomain(contragentEntity)
    }
    
    /**
     * Создать контрагента с отправкой на сервер (онлайн/офлайн)
     */
    suspend fun createContragent(request: CreateContragentRequest): CreateContragentResponse {
        return if (networkMonitor.isConnected()) {
            // Онлайн режим - отправляем сразу
            createContragentOnline(request)
        } else {
            // Офлайн режим - сохраняем локально
            createContragentOffline(request)
        }
    }
    
    /**
     * Создать контрагента в онлайн режиме
     */
    private suspend fun createContragentOnline(request: CreateContragentRequest): CreateContragentResponse {
        return try {
            Log.d(TAG, "Отправка контрагента на сервер: ${request.name}")
            
            val response = taskApi.service.createContragent(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.result == true && body.id != null) {
                    // Контрагент успешно создан на сервере
                    Log.d(TAG, "Контрагент создан на сервере, ID: ${body.id}")
                    
                    // Сохраняем в локальную БД как синхронизированного
                    val contragentEntity = com.helfkea.crm.data.local.entity.ContragentEntity.createLocal(
                        name = request.name,
                        fullName = null,
                        inn = request.inn.takeIf { it.isNotBlank() },
                        kpp = null,
                        ogrn = null,
                        legalAddress = request.adress.takeIf { it.isNotBlank() },
                        actualAddress = request.adress.takeIf { it.isNotBlank() },
                        phone = request.number.takeIf { it.isNotBlank() },
                        email = null,
                        website = null,
                        manager = null,
                        status = if (request.client) "Клиент" else "Активен",
                        category = if (request.tradingOrganization) "Торгующая организация" else null,
                        notes = request.description.takeIf { it.isNotBlank() }
                    ).copy(
                        serverId = body.id,
                        syncStatus = SyncStatus.SYNCED,
                        lastSyncTime = Date()
                    )
                    
                    contragentDao.insertContragent(contragentEntity)
                }
                body ?: CreateContragentResponse(
                    error = "Пустой ответ от сервера",
                    result = false
                )
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Ошибка сервера: ${response.code()} - $errorBody")
                
                // Сохраняем как локального с ошибкой
                createContragentOffline(request).copy(
                    error = "Ошибка сервера: ${response.code()}",
                    result = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сети: ${e.message}")
            
            // Сохраняем как локального с ошибкой
            createContragentOffline(request).copy(
                result = false,
                error = "Ошибка сети: ${e.message}"
            )
        }
    }
    
    /**
     * Создать контрагента в офлайн режиме
     */
    private suspend fun createContragentOffline(request: CreateContragentRequest): CreateContragentResponse {
        Log.d(TAG, "Сохранение контрагента локально (офлайн): ${request.name}")
        
        val contragentEntity = com.helfkea.crm.data.local.entity.ContragentEntity.createLocal(
            name = request.name,
            fullName = null,
            inn = request.inn.takeIf { it.isNotBlank() },
            kpp = null,
            ogrn = null,
            legalAddress = request.adress.takeIf { it.isNotBlank() },
            actualAddress = request.adress.takeIf { it.isNotBlank() },
            phone = request.number.takeIf { it.isNotBlank() },
            email = null,
            website = null,
            manager = null,
            status = if (request.client) "Клиент" else "Активен",
            category = if (request.tradingOrganization) "Торгующая организация" else null,
            notes = request.description.takeIf { it.isNotBlank() }
        )
        
        contragentDao.insertContragent(contragentEntity)
        
        return CreateContragentResponse(
            error = "",
            result = true,
            id = contragentEntity.id  // Возвращаем локальный ID
        )
    }
    
    /**
     * Загрузить контрагентов с сервера и сохранить локально
     */
    suspend fun loadContragentsFromServer(): Boolean {
        if (!networkMonitor.isConnected()) {
            Log.d(TAG, "Нет подключения к интернету, пропускаем загрузку")
            return false
        }
        
        return try {
            Log.d(TAG, "Загрузка контрагентов с сервера...")
            
            // Загружаем контрагентов с сервера
            val serverContragents = taskApi.service.getContragents()
            Log.d(TAG, "Загружено контрагентов с сервера: ${serverContragents.size}")
            
            if (serverContragents.isNotEmpty()) {
                // Преобразуем в Entity и сохраняем
                val entities = ContragentMapper.toEntityList(serverContragents, SyncStatus.SYNCED)
                contragentDao.insertContragents(entities)
                Log.d(TAG, "Сохранено контрагентов в локальную БД: ${entities.size}")
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при загрузке контрагентов с сервера: ${e.message}")
            false
        }
    }
    
    /**
     * Синхронизировать локальные изменения с сервером
     */
    suspend fun syncPendingContragents(): SyncResult {
        if (!networkMonitor.isConnected()) {
            return SyncResult.NoInternet
        }
        
        return try {
            Log.d(TAG, "Синхронизация ожидающих контрагентов...")
            
            val pendingContragents = contragentDao.getContragentsBySyncStatus(SyncStatus.PENDING)
            Log.d(TAG, "Найдено ожидающих контрагентов: ${pendingContragents.size}")
            
            if (pendingContragents.isEmpty()) {
                return SyncResult.Success(0, 0, 0)
            }
            
            var sentCount = 0
            var failedCount = 0
            
            // Отправляем каждого контрагента по отдельности
            for (entity in pendingContragents) {
                try {
                    val contragent = ContragentMapper.toDomain(entity)
                    
                    val createRequest = CreateContragentRequest(
                        name = contragent.name,
                        type = "Юр.лицо", // По умолчанию
                        inn = contragent.inn ?: "",
                        number = contragent.phones.firstOrNull() ?: "",
                        adress = contragent.mainAddress,
                        description = contragent.pinnedComment ?: "",
                        doctor = false,
                        clinic = false,
                        lead = false,
                        client = true, // По умолчанию клиент
                        pasient = false,
                        tradingOrganization = contragent.segment.contains("торг", ignoreCase = true)
                    )
                    
                    val response = taskApi.service.createContragent(createRequest)
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody?.result == true && responseBody.id != null) {
                            // Обновляем запись после успешной синхронизации
                            contragentDao.updateContragentAfterSync(
                                localId = entity.id,
                                serverId = responseBody.id,
                                status = SyncStatus.SYNCED,
                                syncTime = Date()
                            )
                            sentCount++
                            Log.d(TAG, "Контрагент синхронизирован: ${entity.name} -> ${responseBody.id}")
                        } else {
                            // Помечаем как ошибку
                            contragentDao.updateContragentSyncStatus(
                                entity.id,
                                SyncStatus.FAILED,
                                "Ошибка синхронизации: сервер вернул result=false"
                            )
                            failedCount++
                            Log.e(TAG, "Ошибка синхронизации контрагента: ${entity.name}, ответ: $responseBody")
                        }
                    } else {
                        // Помечаем как ошибку
                        contragentDao.updateContragentSyncStatus(
                            entity.id,
                            SyncStatus.FAILED,
                            "Ошибка синхронизации: ${response.code()}"
                        )
                        failedCount++
                        Log.e(TAG, "Ошибка синхронизации контрагента: ${entity.name}, код: ${response.code()}")
                    }
                } catch (e: Exception) {
                    contragentDao.updateContragentSyncStatus(
                        entity.id,
                        SyncStatus.FAILED,
                        "Исключение: ${e.message}"
                    )
                    failedCount++
                    Log.e(TAG, "Исключение при синхронизации контрагента: ${e.message}")
                }
            }
            
            SyncResult.Success(sentCount, 0, failedCount)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Получить количество ожидающих синхронизации контрагентов
     */
    suspend fun getPendingCount(): Int {
        return contragentDao.getPendingCount()
    }
    
    /**
     * Получить количество контрагентов с ошибкой синхронизации
     */
    suspend fun getFailedCount(): Int {
        return contragentDao.getFailedCount()
    }
    
    /**
     * Получить контрагента по ID
     */
    suspend fun getContragentById(id: String): Contragent? {
        return contragentDao.getContragentById(id)?.let { ContragentMapper.toDomain(it) }
    }
    
    /**
     * Получить контрагента по server ID
     */
    suspend fun getContragentByServerId(serverId: String): Contragent? {
        return contragentDao.getContragentByServerId(serverId)?.let { ContragentMapper.toDomain(it) }
    }
}