package com.helfkea.crm.data.local.dao

import androidx.room.*
import com.helfkea.crm.data.local.entity.TaskEntity
import com.helfkea.crm.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks ORDER BY execution_date DESC, created_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE sync_status = :status ORDER BY created_at ASC")
    suspend fun getTasksBySyncStatus(status: SyncStatus): List<TaskEntity>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE server_id = :serverId")
    suspend fun getTaskByServerId(serverId: String): TaskEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Query("UPDATE tasks SET server_id = :serverId, sync_status = :status, last_sync_time = :syncTime, sync_error = NULL WHERE id = :localId")
    suspend fun updateTaskAfterSync(localId: String, serverId: String, status: SyncStatus, syncTime: Date)
    
    @Query("UPDATE tasks SET sync_status = :status, sync_error = :error WHERE id = :id")
    suspend fun updateTaskSyncStatus(id: String, status: SyncStatus, error: String? = null)
    
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)
    
    @Query("SELECT MAX(last_sync_time) FROM tasks WHERE sync_status = 'SYNCED'")
    suspend fun getLastSyncTime(): Date?
    
    @Query("SELECT COUNT(*) FROM tasks WHERE sync_status = 'PENDING'")
    suspend fun getPendingCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE sync_status = 'FAILED'")
    suspend fun getFailedCount(): Int
    
    @Query("SELECT * FROM tasks WHERE contragent_id = :contragentId ORDER BY execution_date DESC")
    fun getTasksByContragentId(contragentId: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY execution_date ASC")
    fun getTasksByStatus(status: String): Flow<List<TaskEntity>>
}