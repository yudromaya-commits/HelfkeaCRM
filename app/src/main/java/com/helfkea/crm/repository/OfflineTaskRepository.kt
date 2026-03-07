package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.data.local.dao.TaskDao
import com.helfkea.crm.data.local.mapper.TaskMapper
import com.helfkea.crm.data.network.NetworkMonitor
import com.helfkea.crm.data.sync.SyncStatus
import com.helfkea.crm.model.CreateTaskRequest
import com.helfkea.crm.model.CreateTaskResponse
import com.helfkea.crm.model.Task
import com.helfkea.crm.model.UpdateTaskRequest
import com.helfkea.crm.model.AddCommentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class OfflineTaskRepository(
    private val taskDao: TaskDao,
    private val taskApi: TaskApi,
    private val networkMonitor: NetworkMonitor
) {
    
    companion object {
        private const val TAG = "OfflineTaskRepository"
    }
    
    /**
     * Получить все задачи как Flow (автоматическое обновление UI)
     */
    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
            .map { entities -> TaskMapper.toDomainList(entities) }
    }
    
    /**
     * Получить задачи для текущего пользователя (мои задачи)
     */
    fun getMyTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
            .map { entities -> 
                TaskMapper.toDomainList(entities.filter { it.executor == "Program" })
            }
    }
    
    /**
     * Получить задачи, назначенные мне
     */
    fun getTasksAssignedToMe(): Flow<List<Task>> {
        return taskDao.getAllTasks()
            .map { entities ->
                TaskMapper.toDomainList(entities.filter { it.producer == "Program" })
            }
    }
    
    /**
     * Создать задачу локально (офлайн)
     */
    suspend fun createTaskLocal(
        name: String,
        description: String,
        executionDate: String,
        important: Boolean = false,
        executor: String? = null,
        contragentId: String? = null,
        contragentName: String? = null
    ): Task {
        Log.d(TAG, "Создание локальной задачи: $name")
        
        val taskEntity = com.helfkea.crm.data.local.entity.TaskEntity.createLocal(
            name = name,
            description = description,
            executionDate = executionDate,
            important = important,
            executor = executor,
            contragentId = contragentId,
            contragentName = contragentName
        )
        
        taskDao.insertTask(taskEntity)
        
        return TaskMapper.toDomain(taskEntity)
    }
    
    /**
     * Создать задачу с отправкой на сервер (онлайн/офлайн)
     */
    suspend fun createTask(createTaskRequest: CreateTaskRequest): CreateTaskResponse {
        return if (networkMonitor.isConnected()) {
            // Онлайн режим - отправляем сразу
            createTaskOnline(createTaskRequest)
        } else {
            // Офлайн режим - сохраняем локально
            createTaskOffline(createTaskRequest)
        }
    }
    
    /**
     * Создать задачу в онлайн режиме
     */
    private suspend fun createTaskOnline(createTaskRequest: CreateTaskRequest): CreateTaskResponse {
        return try {
            Log.d(TAG, "Отправка задачи на сервер: ${createTaskRequest.name}")
            
            val response = taskApi.service.createTask(createTaskRequest)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.id != null) {
                    // Задача успешно создана на сервере
                    Log.d(TAG, "Задача создана на сервере, ID: ${body.id}")
                    
                    // Сохраняем в локальную БД как синхронизированную
                    val taskEntity = com.helfkea.crm.data.local.entity.TaskEntity.createLocal(
                        name = createTaskRequest.name,
                        description = createTaskRequest.description,
                        status = createTaskRequest.status ?: "Назначена",
                        producer = "Программа", // По умолчанию
                        executionDate = createTaskRequest.executionDate ?: "",
                        important = createTaskRequest.important ?: false,
                        executor = createTaskRequest.executorId,
                        contragentId = createTaskRequest.contragentId,
                        contragentName = null
                    ).copy(
                        serverId = body.id,
                        syncStatus = com.helfkea.crm.data.sync.SyncStatus.SYNCED,
                        lastSyncTime = Date()
                    )
                    
                    taskDao.insertTask(taskEntity)
                }
                body ?: CreateTaskResponse(
                    success = false,
                    error = "Пустой ответ от сервера"
                )
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Ошибка сервера: ${response.code()} - $errorBody")
                
                // Сохраняем как локальную с ошибкой
                createTaskOffline(createTaskRequest).copy(
                    success = false,
                    error = "Ошибка сервера: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сети: ${e.message}")
            
            // Сохраняем как локальную с ошибкой
            createTaskOffline(createTaskRequest).copy(
                success = false,
                error = "Ошибка сети: ${e.message}"
            )
        }
    }
    
    /**
     * Создать задачу в офлайн режиме
     */
    private suspend fun createTaskOffline(createTaskRequest: CreateTaskRequest): CreateTaskResponse {
        Log.d(TAG, "Сохранение задачи локально (офлайн): ${createTaskRequest.name}")
        
        val taskEntity = com.helfkea.crm.data.local.entity.TaskEntity.createLocal(
            name = createTaskRequest.name,
            description = createTaskRequest.description,
            status = createTaskRequest.status ?: "Назначена",
            producer = "Программа", // По умолчанию
            executionDate = createTaskRequest.executionDate ?: "",
            important = createTaskRequest.important ?: false,
            executor = createTaskRequest.executorId,
            contragentId = createTaskRequest.contragentId,
            contragentName = null
        )
        
        taskDao.insertTask(taskEntity)
        
        return CreateTaskResponse(
            success = true,
            id = taskEntity.id,  // Возвращаем локальный ID
            error = null
        )
    }
    
    /**
     * Обновить задачу
     */
    suspend fun updateTask(request: UpdateTaskRequest): Result<CreateTaskResponse> {
        return if (networkMonitor.isConnected()) {
            updateTaskOnline(request)
        } else {
            updateTaskOffline(request)
        }
    }
    
    private suspend fun updateTaskOnline(request: UpdateTaskRequest): Result<CreateTaskResponse> {
        return try {
            val response = taskApi.service.updateTask(request)
            if (response.isSuccessful) {
                // Обновляем локальную запись
                taskDao.getTaskByServerId(request.taskId)?.let { entity ->
                    val updatedEntity = entity.copy(
                        name = request.name ?: entity.name,
                        description = request.description ?: entity.description,
                        status = request.status ?: entity.status,
                        executionDate = request.executionDate ?: entity.executionDate,
                        important = request.important ?: entity.important,
                        executor = request.executor ?: entity.executor,
                        updatedAt = Date(),
                        syncStatus = com.helfkea.crm.data.sync.SyncStatus.SYNCED,
                        lastSyncTime = Date()
                    )
                    taskDao.updateTask(updatedEntity)
                }
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка обновления задачи: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при обновлении задачи: ${e.message}")
            Result.failure(e)
        }
    }
    
    private suspend fun updateTaskOffline(request: UpdateTaskRequest): Result<CreateTaskResponse> {
        return try {
            // Находим задачу по serverId или localId
            val entity = taskDao.getTaskByServerId(request.taskId) ?: taskDao.getTaskById(request.taskId)
            
            if (entity != null) {
                val updatedEntity = entity.copy(
                    name = request.name ?: entity.name,
                    description = request.description ?: entity.description,
                    status = request.status ?: entity.status,
                    executionDate = request.executionDate ?: entity.executionDate,
                    important = request.important ?: entity.important,
                    executor = request.executor ?: entity.executor,
                    updatedAt = Date(),
                    syncStatus = com.helfkea.crm.data.sync.SyncStatus.PENDING  // Помечаем как требующую синхронизации
                )
                taskDao.updateTask(updatedEntity)
                
                Result.success(CreateTaskResponse(
                    success = true,
                    id = entity.serverId ?: entity.id,
                    error = null
                ))
            } else {
                Result.failure(Exception("Задача не найдена"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Добавить комментарий
     */
    suspend fun addComment(request: AddCommentRequest): Result<CreateTaskResponse> {
        // В текущей реализации комментарии не сохраняются локально
        // Можно расширить функциональность позже
        return try {
            val response = taskApi.service.addComment(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка добавления комментария: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при добавлении комментария: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Получить количество ожидающих синхронизации задач
     */
    suspend fun getPendingCount(): Int {
        return taskDao.getPendingCount()
    }
    
    /**
     * Получить количество задач с ошибкой синхронизации
     */
    suspend fun getFailedCount(): Int {
        return taskDao.getFailedCount()
    }
    
    /**
     * Загрузить задачи с сервера и сохранить локально
     */
    suspend fun loadTasksFromServer(): Boolean {
        if (!networkMonitor.isConnected()) {
            Log.d(TAG, "Нет подключения к интернету, пропускаем загрузку")
            return false
        }
        
        return try {
            Log.d(TAG, "Загрузка задач с сервера...")
            
            // Загружаем задачи с сервера
            val serverTasks = taskApi.service.getTasksMe() + taskApi.service.getTasksMy()
            Log.d(TAG, "Загружено задач с сервера: ${serverTasks.size}")
            
            if (serverTasks.isNotEmpty()) {
                // Преобразуем в Entity и сохраняем
                val entities = TaskMapper.toEntityList(serverTasks, com.helfkea.crm.data.sync.SyncStatus.SYNCED)
                taskDao.insertTasks(entities)
                Log.d(TAG, "Сохранено задач в локальную БД: ${entities.size}")
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при загрузке задач с сервера: ${e.message}")
            false
        }
    }
    
    /**
     * Синхронизировать локальные изменения с сервером
     */
    suspend fun syncPendingTasks(): SyncResult {
        if (!networkMonitor.isConnected()) {
            return SyncResult.NoInternet
        }
        
        return try {
            Log.d(TAG, "Синхронизация ожидающих задач...")
            
            val pendingTasks = taskDao.getTasksBySyncStatus(com.helfkea.crm.data.sync.SyncStatus.PENDING)
            Log.d(TAG, "Найдено ожидающих задач: ${pendingTasks.size}")
            
            if (pendingTasks.isEmpty()) {
                return SyncResult.Success(0, 0, 0)
            }
            
            var sentCount = 0
            var failedCount = 0
            
            // Отправляем каждую задачу по отдельности
            for (entity in pendingTasks) {
                try {
                    val task = TaskMapper.toDomain(entity)
                    
                    val createRequest = CreateTaskRequest(
                        name = task.name,
                        description = task.description,
                        status = task.status,
                        executorId = task.executor,
                        executionDate = task.executionDate,
                        important = task.important,
                        contragentId = task.contragentId
                    )
                    
                    val response = taskApi.service.createTask(createRequest)
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody?.success == true && responseBody.id != null) {
                            // Обновляем запись после успешной синхронизации
                            taskDao.updateTaskAfterSync(
                                localId = entity.id,
                                serverId = responseBody.id,
                                status = com.helfkea.crm.data.sync.SyncStatus.SYNCED,
                                syncTime = Date()
                            )
                            sentCount++
                            Log.d(TAG, "Задача синхронизирована: ${entity.name} -> ${responseBody.id}")
                        } else {
                            // Помечаем как ошибку
                            taskDao.updateTaskSyncStatus(
                                entity.id,
                                com.helfkea.crm.data.sync.SyncStatus.FAILED,
                                "Ошибка синхронизации: сервер вернул success=false"
                            )
                            failedCount++
                            Log.e(TAG, "Ошибка синхронизации задачи: ${entity.name}, ответ: $responseBody")
                        }
                    } else {
                        // Помечаем как ошибку
                        taskDao.updateTaskSyncStatus(
                            entity.id,
                            com.helfkea.crm.data.sync.SyncStatus.FAILED,
                            "Ошибка синхронизации: ${response.code()}"
                        )
                        failedCount++
                        Log.e(TAG, "Ошибка синхронизации задачи: ${entity.name}, код: ${response.code()}")
                    }
                } catch (e: Exception) {
                    taskDao.updateTaskSyncStatus(
                        entity.id,
                        com.helfkea.crm.data.sync.SyncStatus.FAILED,
                        "Исключение: ${e.message}"
                    )
                    failedCount++
                    Log.e(TAG, "Исключение при синхронизации задачи: ${e.message}")
                }
            }
            
            SyncResult.Success(sentCount, 0, failedCount)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error")
        }
    }
}

sealed class SyncResult {
    object NoInternet : SyncResult()
    data class Success(
        val sentCount: Int,
        val receivedCount: Int,
        val failedCount: Int
    ) : SyncResult()
    data class Error(val message: String) : SyncResult()
}