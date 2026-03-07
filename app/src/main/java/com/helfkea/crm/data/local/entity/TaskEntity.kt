package com.helfkea.crm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.helfkea.crm.data.sync.SyncStatus
import java.util.Date

/**
 * Entity для хранения задач в локальной БД
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "server_id")
    val serverId: String?,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "status")
    val status: String,
    
    @ColumnInfo(name = "producer")
    val producer: String,
    
    @ColumnInfo(name = "execution_date")
    val executionDate: String,
    
    @ColumnInfo(name = "important")
    val important: Boolean,
    
    @ColumnInfo(name = "executor")
    val executor: String?,
    
    @ColumnInfo(name = "contragent_id")
    val contragentId: String?,
    
    @ColumnInfo(name = "contragent_name")
    val contragentName: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus,
    
    @ColumnInfo(name = "last_sync_time")
    val lastSyncTime: Date?,
    
    @ColumnInfo(name = "sync_error")
    val syncError: String?
) {
    companion object {
        fun createLocal(
            name: String,
            description: String,
            status: String = "Назначена",
            producer: String = "Программа",
            executionDate: String,
            important: Boolean = false,
            executor: String? = null,
            contragentId: String? = null,
            contragentName: String? = null
        ): TaskEntity {
            val now = Date()
            return TaskEntity(
                id = java.util.UUID.randomUUID().toString(),
                serverId = null,
                name = name,
                description = description,
                status = status,
                producer = producer,
                executionDate = executionDate,
                important = important,
                executor = executor,
                contragentId = contragentId,
                contragentName = contragentName,
                createdAt = now,
                updatedAt = now,
                syncStatus = SyncStatus.PENDING,
                lastSyncTime = null,
                syncError = null
            )
        }
    }
}