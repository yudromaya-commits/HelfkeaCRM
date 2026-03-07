package com.helfkea.crm.data.local.dao

import androidx.room.*
import com.helfkea.crm.data.local.entity.InteractionEntity
import com.helfkea.crm.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionDao {
    
    // CRUD операции
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: InteractionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(interactions: List<InteractionEntity>)
    
    @Update
    suspend fun updateInteraction(interaction: InteractionEntity)
    
    @Delete
    suspend fun deleteInteraction(interaction: InteractionEntity)
    
    @Query("DELETE FROM interactions")
    suspend fun deleteAll()
    
    // Запросы
    @Query("SELECT * FROM interactions ORDER BY date DESC")
    fun getAllInteractions(): Flow<List<InteractionEntity>>
    
    @Query("SELECT * FROM interactions WHERE id = :id")
    suspend fun getInteractionById(id: String): InteractionEntity?
    
    @Query("SELECT * FROM interactions WHERE serverId = :serverId")
    suspend fun getInteractionByServerId(serverId: String): InteractionEntity?
    
    @Query("SELECT * FROM interactions WHERE contragentId = :contragentId ORDER BY date DESC")
    fun getInteractionsByContragentId(contragentId: String): Flow<List<InteractionEntity>>
    
    @Query("SELECT * FROM interactions WHERE syncStatus = :syncStatus")
    suspend fun getInteractionsBySyncStatus(syncStatus: SyncStatus): List<InteractionEntity>
    
    @Query("SELECT * FROM interactions WHERE syncStatus IN (:syncStatuses)")
    suspend fun getInteractionsBySyncStatuses(syncStatuses: List<SyncStatus>): List<InteractionEntity>
    
    // Статистика синхронизации
    @Query("SELECT COUNT(*) FROM interactions WHERE syncStatus = 'PENDING'")
    suspend fun getPendingCount(): Int
    
    @Query("SELECT COUNT(*) FROM interactions WHERE syncStatus = 'FAILED'")
    suspend fun getFailedCount(): Int
    
    @Query("SELECT COUNT(*) FROM interactions WHERE syncStatus = 'SYNCED'")
    suspend fun getSyncedCount(): Int
    
    // Обновление статуса синхронизации
    @Query("UPDATE interactions SET syncStatus = :syncStatus, lastSyncTime = :lastSyncTime, syncError = :syncError WHERE id = :id")
    suspend fun updateInteractionSyncStatus(id: String, syncStatus: SyncStatus, lastSyncTime: Long?, syncError: String?)
    
    suspend fun updateInteractionSyncStatus(id: String, syncStatus: SyncStatus, lastSyncTime: java.util.Date?, syncError: String?) {
        updateInteractionSyncStatus(id, syncStatus, lastSyncTime?.time, syncError)
    }
    
    // Обновление serverId после синхронизации
    @Query("UPDATE interactions SET serverId = :serverId, syncStatus = :syncStatus, lastSyncTime = :lastSyncTime WHERE id = :localId")
    suspend fun updateServerId(localId: String, serverId: String, syncStatus: SyncStatus, lastSyncTime: Long?)
    
    suspend fun updateServerId(localId: String, serverId: String, syncStatus: SyncStatus, lastSyncTime: java.util.Date?) {
        updateServerId(localId, serverId, syncStatus, lastSyncTime?.time)
    }
    
    // Поиск
    @Query("SELECT * FROM interactions WHERE comment LIKE '%' || :query || '%' OR contragentName LIKE '%' || :query || '%' ORDER BY date DESC")
    suspend fun searchInteractions(query: String): List<InteractionEntity>
}