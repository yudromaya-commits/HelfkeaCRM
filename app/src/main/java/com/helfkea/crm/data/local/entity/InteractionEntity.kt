package com.helfkea.crm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.helfkea.crm.data.local.database.Converters
import java.util.*

@Entity(tableName = "interactions")
@TypeConverters(Converters::class)
data class InteractionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Серверный ID (заполняется после синхронизации)
    val serverId: String? = null,
    
    // Связь с контрагентом
    val contragentId: String,
    val contragentName: String? = null,
    
    // Данные взаимодействия
    val typeContactId: String,
    val contactType: String? = null, // Для отображения
    val secure: Boolean = false,
    val comment: String,
    val manager: String? = null,
    val date: Date = Date(),
    val pinned: Boolean = false,
    val result: String? = null,
    
    // Статус синхронизации
    val syncStatus: com.helfkea.crm.data.sync.SyncStatus = com.helfkea.crm.data.sync.SyncStatus.PENDING,
    val lastSyncTime: Date? = null,
    val syncError: String? = null,
    
    // Метаданные
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    companion object {
        fun createLocal(
            contragentId: String,
            contragentName: String? = null,
            typeContactId: String,
            contactType: String? = null,
            secure: Boolean = false,
            comment: String,
            manager: String? = null,
            date: Date = Date(),
            pinned: Boolean = false,
            result: String? = null
        ): InteractionEntity {
            return InteractionEntity(
                contragentId = contragentId,
                contragentName = contragentName,
                typeContactId = typeContactId,
                contactType = contactType,
                secure = secure,
                comment = comment,
                manager = manager,
                date = date,
                pinned = pinned,
                result = result
            )
        }
        
        fun fromServer(
            serverId: String,
            contragentId: String,
            contragentName: String? = null,
            typeContactId: String,
            contactType: String? = null,
            secure: Boolean = false,
            comment: String,
            manager: String? = null,
            date: Date = Date(),
            pinned: Boolean = false,
            result: String? = null
        ): InteractionEntity {
            return InteractionEntity(
                id = UUID.randomUUID().toString(),
                serverId = serverId,
                contragentId = contragentId,
                contragentName = contragentName,
                typeContactId = typeContactId,
                contactType = contactType,
                secure = secure,
                comment = comment,
                manager = manager,
                date = date,
                pinned = pinned,
                result = result,
                syncStatus = com.helfkea.crm.data.sync.SyncStatus.SYNCED,
                lastSyncTime = Date()
            )
        }
    }
}