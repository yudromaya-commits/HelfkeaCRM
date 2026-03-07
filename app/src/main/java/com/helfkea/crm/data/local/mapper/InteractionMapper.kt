package com.helfkea.crm.data.local.mapper

import com.helfkea.crm.data.local.entity.InteractionEntity
import com.helfkea.crm.model.Interaction
import java.text.SimpleDateFormat
import java.util.*

object InteractionMapper {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    fun toEntity(interaction: Interaction, contragentId: String, contragentName: String? = null, syncStatus: com.helfkea.crm.data.sync.SyncStatus): InteractionEntity {
        return InteractionEntity(
            id = UUID.randomUUID().toString(),
            serverId = null, // Взаимодействия из сервера не имеют ID в модели
            contragentId = contragentId,
            contragentName = contragentName,
            typeContactId = "", // Нет в модели
            contactType = interaction.contactType,
            secure = false,
            comment = interaction.comment ?: "",
            manager = interaction.manager,
            date = parseDate(interaction.date),
            pinned = interaction.isPinned,
            result = interaction.result,
            syncStatus = syncStatus,
            lastSyncTime = if (syncStatus == com.helfkea.crm.data.sync.SyncStatus.SYNCED) Date() else null
        )
    }
    
    fun toDomain(entity: InteractionEntity): Interaction {
        return Interaction(
            date = formatDate(entity.date),
            contactType = entity.contactType,
            comment = entity.comment,
            manager = entity.manager,
            pinned = if (entity.pinned) "Да" else "Нет",
            result = entity.result
        )
    }
    
    fun toDomainList(entities: List<InteractionEntity>): List<Interaction> {
        return entities.map { toDomain(it) }
    }
    
    fun toEntityList(interactions: List<Interaction>, contragentId: String, contragentName: String? = null, syncStatus: com.helfkea.crm.data.sync.SyncStatus): List<InteractionEntity> {
        return interactions.map { toEntity(it, contragentId, contragentName, syncStatus) }
    }
    
    fun fromCreateRequest(
        request: com.helfkea.crm.model.CreateInteractionRequest,
        contragentName: String? = null,
        contactType: String? = null,
        manager: String? = null
    ): InteractionEntity {
        return InteractionEntity.createLocal(
            contragentId = request.contragentId,
            contragentName = contragentName,
            typeContactId = request.typeContactId,
            contactType = contactType,
            secure = request.secure,
            comment = request.comment,
            manager = manager,
            date = Date()
        )
    }
    
    private fun parseDate(dateString: String?): Date {
        if (dateString.isNullOrBlank()) return Date()
        
        return try {
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            try {
                displayDateFormat.parse(dateString) ?: Date()
            } catch (e2: Exception) {
                Date()
            }
        }
    }
    
    private fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }
}