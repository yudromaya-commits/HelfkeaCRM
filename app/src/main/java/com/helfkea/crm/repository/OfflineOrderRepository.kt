package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.data.local.dao.OrderDao
import com.helfkea.crm.data.local.mapper.OrderMapper
import com.helfkea.crm.data.network.NetworkMonitor
import com.helfkea.crm.data.sync.SyncStatus
import com.helfkea.crm.model.OrderCreateRequest
import com.helfkea.crm.model.OrderCreateResponse
import com.helfkea.crm.model.OrderResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response
import java.util.*

class OfflineOrderRepository(
    private val orderDao: OrderDao,
    private val taskApi: TaskApi,
    private val networkMonitor: NetworkMonitor
) {
    
    companion object {
        private const val TAG = "OfflineOrderRepo"
    }
    
    // Получение данных
    fun getAllOrders(): Flow<List<OrderResponse>> {
        return orderDao.getAllOrders().map { entities ->
            OrderMapper.toOrderResponseList(entities)
        }
    }
    
    fun getOrdersByContragentId(contragentId: String): Flow<List<OrderResponse>> {
        return orderDao.getOrdersByContragentId(contragentId).map { entities ->
            OrderMapper.toOrderResponseList(entities)
        }
    }
    
    suspend fun getOrderById(id: String): OrderResponse? {
        val entity = orderDao.getOrderById(id)
        return entity?.let { OrderMapper.toOrderResponse(it) }
    }
    
    suspend fun getOrderByServerId(serverId: String): OrderResponse? {
        val entity = orderDao.getOrderByServerId(serverId)
        return entity?.let { OrderMapper.toOrderResponse(it) }
    }
    
    // Создание заказа
    suspend fun createOrder(request: OrderCreateRequest): OrderCreateResponse {
        return if (networkMonitor.isConnected()) {
            createOrderOnline(request)
        } else {
            createOrderOffline(request)
        }
    }
    
    private suspend fun createOrderOnline(request: OrderCreateRequest): OrderCreateResponse {
        return try {
            Log.d(TAG, "Создаем заказ онлайн для контрагента: ${request.contragentName}")
            
            val response = taskApi.service.createOrderV2(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.orderId != null) {
                    // Заказ успешно создан на сервере
                    Log.d(TAG, "Заказ создан на сервере, ID: ${body.orderId}")
                    
                    // Сохраняем в локальную БД как синхронизированный
                    val orderEntity = OrderMapper.toEntity(request, SyncStatus.SYNCED).copy(
                        serverId = body.orderId,
                        lastSyncTime = Date()
                    )
                    
                    orderDao.insertOrder(orderEntity)
                    
                    OrderCreateResponse(
                        success = true,
                        orderId = orderEntity.id,  // Возвращаем локальный ID
                        error = null
                    )
                } else {
                    // Сервер вернул ошибку
                    Log.w(TAG, "Сервер вернул ошибку: ${body?.error}")
                    createOrderOffline(request).copy(
                        success = false,
                        error = body?.error ?: "Ошибка сервера"
                    )
                }
            } else {
                // Ошибка HTTP
                Log.e(TAG, "Ошибка HTTP: ${response.code()}")
                createOrderOffline(request).copy(
                    success = false,
                    error = "Ошибка сервера: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сети: ${e.message}", e)
            createOrderOffline(request).copy(
                success = false,
                error = "Ошибка сети: ${e.message}"
            )
        }
    }
    
    private suspend fun createOrderOffline(request: OrderCreateRequest): OrderCreateResponse {
        Log.d(TAG, "Создаем заказ офлайн для контрагента: ${request.contragentName}")
        
        val orderEntity = OrderMapper.toEntity(request, SyncStatus.PENDING)
        orderDao.insertOrder(orderEntity)
        
        return OrderCreateResponse(
            success = true,
            orderId = orderEntity.id,  // Возвращаем локальный ID
            error = null
        )
    }
    
    // Синхронизация
    suspend fun syncPendingOrders(): Boolean {
        return try {
            Log.d(TAG, "Начинаем синхронизацию заказов...")
            
            // Получаем заказы, ожидающие синхронизации
            val pendingEntities = orderDao.getOrdersBySyncStatuses(
                listOf(SyncStatus.PENDING, SyncStatus.FAILED)
            )
            
            if (pendingEntities.isEmpty()) {
                Log.d(TAG, "Нет заказов для синхронизации")
                return true
            }
            
            Log.d(TAG, "Найдено ${pendingEntities.size} заказов для синхронизации")
            
            var successCount = 0
            var errorCount = 0
            
            for (entity in pendingEntities) {
                try {
                    // Создаем запрос на основе локальных данных
                    val request = OrderMapper.toOrderCreateRequest(entity)
                    
                    val response = taskApi.service.createOrderV2(request)
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody?.success == true && responseBody.orderId != null) {
                            // Успешно синхронизировано
                            orderDao.updateServerId(
                                localId = entity.id,
                                serverId = responseBody.orderId,
                                orderNumber = null, // response не содержит номера
                                syncStatus = SyncStatus.SYNCED,
                                lastSyncTime = Date()
                            )
                            successCount++
                            Log.d(TAG, "Заказ синхронизирован: ${entity.contragentName} -> ${responseBody.orderId}")
                        } else {
                            // Ошибка сервера
                            orderDao.updateOrderSyncStatus(
                                id = entity.id,
                                syncStatus = SyncStatus.FAILED,
                                lastSyncTime = Date(),
                                syncError = "Ошибка синхронизации: сервер вернул success=false"
                            )
                            errorCount++
                            Log.w(TAG, "Ошибка синхронизации заказа: ${entity.contragentName}")
                        }
                    } else {
                        // Ошибка HTTP
                        orderDao.updateOrderSyncStatus(
                            id = entity.id,
                            syncStatus = SyncStatus.FAILED,
                            lastSyncTime = Date(),
                            syncError = "Ошибка HTTP: ${response.code()}"
                        )
                        errorCount++
                        Log.e(TAG, "HTTP ошибка при синхронизации заказа: ${response.code()}")
                    }
                } catch (e: Exception) {
                    // Ошибка сети
                    orderDao.updateOrderSyncStatus(
                        id = entity.id,
                        syncStatus = SyncStatus.FAILED,
                        lastSyncTime = Date(),
                        syncError = "Ошибка сети: ${e.message}"
                    )
                    errorCount++
                    Log.e(TAG, "Сетевая ошибка при синхронизации заказа: ${e.message}", e)
                }
            }
            
            Log.d(TAG, "Синхронизация заказов завершена: успешно $successCount, ошибок $errorCount")
            errorCount == 0
            
        } catch (e: Exception) {
            Log.e(TAG, "Критическая ошибка при синхронизации заказов: ${e.message}", e)
            false
        }
    }
    
    // Загрузка данных с сервера
    suspend fun loadFromServer(): Boolean {
        return try {
            Log.d(TAG, "Загружаем заказы с сервера...")
            
            if (!networkMonitor.isConnected()) {
                Log.d(TAG, "Нет интернета, пропускаем загрузку заказов")
                return false
            }
            
            // TODO: Нужен метод для получения списка заказов с сервера
            // Пока возвращаем true, так как заказы создаются локально и синхронизируются
            Log.d(TAG, "Загрузка заказов с сервера пока не реализована")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки заказов с сервера: ${e.message}", e)
            false
        }
    }
    
    // Статистика синхронизации
    suspend fun getPendingCount(): Int = orderDao.getPendingCount()
    suspend fun getFailedCount(): Int = orderDao.getFailedCount()
    suspend fun getSyncedCount(): Int = orderDao.getSyncedCount()
}