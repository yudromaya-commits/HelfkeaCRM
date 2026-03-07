package com.helfkea.crm.data.local.dao

import androidx.room.*
import com.helfkea.crm.data.local.entity.IndividualEntity
import com.helfkea.crm.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface IndividualDao {
    
    // CRUD операции
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIndividual(individual: IndividualEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(individuals: List<IndividualEntity>)
    
    @Update
    suspend fun updateIndividual(individual: IndividualEntity)
    
    @Delete
    suspend fun deleteIndividual(individual: IndividualEntity)
    
    @Query("DELETE FROM individuals")
    suspend fun deleteAll()
    
    // Запросы
    @Query("SELECT * FROM individuals ORDER BY name")
    fun getAllIndividuals(): Flow<List<IndividualEntity>>
    
    @Query("SELECT * FROM individuals WHERE id = :id")
    suspend fun getIndividualById(id: String): IndividualEntity?
    
    @Query("SELECT * FROM individuals WHERE serverId = :serverId")
    suspend fun getIndividualByServerId(serverId: String): IndividualEntity?
    
    @Query("SELECT * FROM individuals WHERE syncStatus = :syncStatus")
    suspend fun getIndividualsBySyncStatus(syncStatus: SyncStatus): List<IndividualEntity>
    
    @Query("SELECT * FROM individuals WHERE syncStatus IN (:syncStatuses)")
    suspend fun getIndividualsBySyncStatuses(syncStatuses: List<SyncStatus>): List<IndividualEntity>
    
    // Статистика синхронизации
    @Query("SELECT COUNT(*) FROM individuals WHERE syncStatus = 'PENDING'")
    suspend fun getPendingCount(): Int
    
    @Query("SELECT COUNT(*) FROM individuals WHERE syncStatus = 'FAILED'")
    suspend fun getFailedCount(): Int
    
    @Query("SELECT COUNT(*) FROM individuals WHERE syncStatus = 'SYNCED'")
    suspend fun getSyncedCount(): Int
    
    // Обновление статуса синхронизации
    @Query("UPDATE individuals SET syncStatus = :syncStatus, lastSyncTime = :lastSyncTime, syncError = :syncError WHERE id = :id")
    suspend fun updateIndividualSyncStatus(id: String, syncStatus: SyncStatus, lastSyncTime: Long?, syncError: String?)
    
    suspend fun updateIndividualSyncStatus(id: String, syncStatus: SyncStatus, lastSyncTime: java.util.Date?, syncError: String?) {
        updateIndividualSyncStatus(id, syncStatus, lastSyncTime?.time, syncError)
    }
    
    // Обновление serverId после синхронизации
    @Query("UPDATE individuals SET serverId = :serverId, syncStatus = :syncStatus, lastSyncTime = :lastSyncTime WHERE id = :localId")
    suspend fun updateServerId(localId: String, serverId: String, syncStatus: SyncStatus, lastSyncTime: Long?)
    
    suspend fun updateServerId(localId: String, serverId: String, syncStatus: SyncStatus, lastSyncTime: java.util.Date?) {
        updateServerId(localId, serverId, syncStatus, lastSyncTime?.time)
    }
    
    // Поиск
    @Query("SELECT * FROM individuals WHERE name LIKE '%' || :query || '%' OR fullName LIKE '%' || :query || '%' OR inn LIKE '%' || :query || '%' ORDER BY name")
    suspend fun searchIndividuals(query: String): List<IndividualEntity>
}