package com.helfkea.crm.data.local.dao

import androidx.room.*
import com.helfkea.crm.data.local.entity.OrderEntity
import com.helfkea.crm.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    
    // CRUD операции
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<OrderEntity>)
    
    @Update
    suspend fun updateOrder(order: OrderEntity)
    
    @Delete
    suspend fun deleteOrder(order: OrderEntity)
    
    @Query("DELETE FROM orders")
    suspend fun deleteAll()
    
    // Запросы
    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>
    
    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): OrderEntity?
    
    @Query("SELECT * FROM orders WHERE serverId = :serverId")
    suspend fun getOrderByServerId(serverId: String): OrderEntity?
    
    @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber")
    suspend fun getOrderByOrderNumber(orderNumber: String): OrderEntity?
    
    @Query("SELECT * FROM orders WHERE contragentId = :contragentId ORDER BY date DESC")
    fun getOrdersByContragentId(contragentId: String): Flow<List<OrderEntity>>
    
    @Query("SELECT * FROM orders WHERE syncStatus = :syncStatus")
    suspend fun getOrdersBySyncStatus(syncStatus: SyncStatus): List<OrderEntity>
    
    @Query("SELECT * FROM orders WHERE syncStatus IN (:syncStatuses)")
    suspend fun getOrdersBySyncStatuses(syncStatuses: List<SyncStatus>): List<OrderEntity>
    
    // Статистика синхронизации
    @Query("SELECT COUNT(*) FROM orders WHERE syncStatus = 'PENDING'")
    suspend fun getPendingCount(): Int
    
    @Query("SELECT COUNT(*) FROM orders WHERE syncStatus = 'FAILED'")
    suspend fun getFailedCount(): Int
    
    @Query("SELECT COUNT(*) FROM orders WHERE syncStatus = 'SYNCED'")
    suspend fun getSyncedCount(): Int
    
    // Обновление статуса синхронизации
    @Query("UPDATE orders SET syncStatus = :syncStatus, lastSyncTime = :lastSyncTime, syncError = :syncError WHERE id = :id")
    suspend fun updateOrderSyncStatus(id: String, syncStatus: SyncStatus, lastSyncTime: Long?, syncError: String?)
    
    suspend fun updateOrderSyncStatus(id: String, syncStatus: SyncStatus, lastSyncTime: java.util.Date?, syncError: String?) {
        updateOrderSyncStatus(id, syncStatus, lastSyncTime?.time, syncError)
    }
    
    // Обновление serverId после синхронизации
    @Query("UPDATE orders SET serverId = :serverId, orderNumber = :orderNumber, syncStatus = :syncStatus, lastSyncTime = :lastSyncTime WHERE id = :localId")
    suspend fun updateServerId(localId: String, serverId: String, orderNumber: String?, syncStatus: SyncStatus, lastSyncTime: Long?)
    
    suspend fun updateServerId(localId: String, serverId: String, orderNumber: String?, syncStatus: SyncStatus, lastSyncTime: java.util.Date?) {
        updateServerId(localId, serverId, orderNumber, syncStatus, lastSyncTime?.time)
    }
    
    // Поиск
    @Query("SELECT * FROM orders WHERE contragentName LIKE '%' || :query || '%' OR comment LIKE '%' || :query || '%' OR orderNumber LIKE '%' || :query || '%' ORDER BY date DESC")
    suspend fun searchOrders(query: String): List<OrderEntity>
}