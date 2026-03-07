package com.helfkea.crm.data.local.mapper

import com.helfkea.crm.data.local.entity.TaskEntity
import com.helfkea.crm.model.Task
import com.helfkea.crm.model.TaskAttachment
import com.helfkea.crm.model.TaskComment
import java.text.SimpleDateFormat
import java.util.*

object TaskMapper {
    
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    private val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    fun toEntity(task: Task, syncStatus: com.helfkea.crm.data.sync.SyncStatus): TaskEntity {
        return TaskEntity(
            id = task.id ?: UUID.randomUUID().toString(),
            serverId = task.id,
            name = task.name,
            description = task.description,
            status = task.status,
            producer = task.producer,
            executionDate = task.executionDate,
            important = task.important,
            executor = task.executor,
            contragentId = task.contragentId,
            contragentName = task.contragentName,
            createdAt = parseDate(task.date),
            updatedAt = parseDate(task.date),
            syncStatus = syncStatus,
            lastSyncTime = if (syncStatus == com.helfkea.crm.data.sync.SyncStatus.SYNCED) Date() else null,
            syncError = null
        )
    }
    
    fun toDomain(entity: TaskEntity): Task {
        return Task(
            id = entity.serverId ?: entity.id,
            date = dateFormat.format(entity.createdAt),
            description = entity.description,
            status = entity.status,
            producer = entity.producer,
            executionDate = entity.executionDate,
            name = entity.name,
            important = entity.important,
            executor = entity.executor,
            contragentId = entity.contragentId,
            contragentName = entity.contragentName,
            contragent = if (entity.contragentId != null && entity.contragentName != null) {
                com.helfkea.crm.model.ContragentInTask(
                    id = entity.contragentId,
                    name = entity.contragentName
                )
            } else null,
            attachments = emptyList(),
            comment = emptyList()
        )
    }
    
    fun toDomainList(entities: List<TaskEntity>): List<Task> {
        return entities.map { toDomain(it) }
    }
    
    fun toEntityList(tasks: List<Task>, syncStatus: com.helfkea.crm.data.sync.SyncStatus): List<TaskEntity> {
        return tasks.map { toEntity(it, syncStatus) }
    }
    
    private fun parseDate(dateString: String): Date {
        return try {
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            try {
                simpleDateFormat.parse(dateString) ?: Date()
            } catch (e2: Exception) {
                Date()
            }
        }
    }
    
    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }
}