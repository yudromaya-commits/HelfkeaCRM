package com.helfkea.crm.data.local.dao

import androidx.room.*
import com.helfkea.crm.data.local.entity.ContragentEntity
import com.helfkea.crm.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ContragentDao {
    
    @Query("SELECT * FROM contragents ORDER BY name ASC")
    fun getAllContragents(): Flow<List<ContragentEntity>>
    
    @Query("SELECT * FROM contragents WHERE sync_status = :status ORDER BY created_at ASC")
    suspend fun getContragentsBySyncStatus(status: SyncStatus): List<ContragentEntity>
    
    @Query("SELECT * FROM contragents WHERE id = :id")
    suspend fun getContragentById(id: String): ContragentEntity?
    
    @Query("SELECT * FROM contragents WHERE server_id = :serverId")
    suspend fun getContragentByServerId(serverId: String): ContragentEntity?
    
    @Query("SELECT * FROM contragents WHERE name LIKE '%' || :query || '%' OR full_name LIKE '%' || :query || '%' OR inn LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchContragents(query: String): Flow<List<ContragentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContragent(contragent: ContragentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContragents(contragents: List<ContragentEntity>)
    
    @Update
    suspend fun updateContragent(contragent: ContragentEntity)
    
    @Query("UPDATE contragents SET server_id = :serverId, sync_status = :status, last_sync_time = :syncTime, sync_error = NULL WHERE id = :localId")
    suspend fun updateContragentAfterSync(localId: String, serverId: String, status: SyncStatus, syncTime: Date)
    
    @Query("UPDATE contragents SET sync_status = :status, sync_error = :error WHERE id = :id")
    suspend fun updateContragentSyncStatus(id: String, status: SyncStatus, error: String? = null)
    
    @Query("DELETE FROM contragents WHERE id = :id")
    suspend fun deleteContragent(id: String)
    
    @Query("SELECT MAX(last_sync_time) FROM contragents WHERE sync_status = 'SYNCED'")
    suspend fun getLastSyncTime(): Date?
    
    @Query("SELECT COUNT(*) FROM contragents WHERE sync_status = 'PENDING'")
    suspend fun getPendingCount(): Int
    
    @Query("SELECT COUNT(*) FROM contragents WHERE sync_status = 'FAILED'")
    suspend fun getFailedCount(): Int
    
    @Query("SELECT * FROM contragents WHERE status = :status ORDER BY name ASC")
    fun getContragentsByStatus(status: String): Flow<List<ContragentEntity>>
    
    @Query("SELECT * FROM contragents WHERE category = :category ORDER BY name ASC")
    fun getContragentsByCategory(category: String): Flow<List<ContragentEntity>>
    
    @Query("SELECT * FROM contragents WHERE manager = :manager ORDER BY name ASC")
    fun getContragentsByManager(manager: String): Flow<List<ContragentEntity>>
}