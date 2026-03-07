package com.helfkea.crm.data.local.mapper

import com.helfkea.crm.data.local.entity.ContragentEntity
import com.helfkea.crm.model.ContactAddress
import com.helfkea.crm.model.Contragent
import com.helfkea.crm.model.Interaction
import com.helfkea.crm.model.RelatedContragent
import java.text.SimpleDateFormat
import java.util.*

object ContragentMapper {
    
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    private val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    fun toEntity(contragent: Contragent, syncStatus: com.helfkea.crm.data.sync.SyncStatus): ContragentEntity {
        // Извлекаем данные из контактов и адресов
        val phones = contragent.phones
        val emails = contragent.emails
        val addresses = contragent.contactsAndAddresses.filter { it.isAddressType }
        
        return ContragentEntity(
            id = contragent.id,
            serverId = contragent.id,
            name = contragent.name,
            fullName = contragent.name,
            inn = contragent.inn,
            kpp = null,
            ogrn = null,
            legalAddress = addresses.firstOrNull()?.displayText,
            actualAddress = addresses.firstOrNull()?.displayText,
            phone = phones.firstOrNull(),
            email = emails.firstOrNull(),
            website = null,
            manager = null,
            status = "Активен",
            category = contragent.segment,
            notes = contragent.pinnedComment,
            createdAt = parseDate(""),
            updatedAt = parseDate(""),
            syncStatus = syncStatus,
            lastSyncTime = if (syncStatus == com.helfkea.crm.data.sync.SyncStatus.SYNCED) Date() else null,
            syncError = null
        )
    }
    
    fun toDomain(entity: ContragentEntity): Contragent {
        // Создаем список контактных адресов из полей
        val contactAddresses = mutableListOf<ContactAddress>()
        
        entity.phone?.takeIf { it.isNotBlank() }?.let { phone ->
            contactAddresses.add(ContactAddress(type = "Телефон", representation = phone))
        }
        
        entity.email?.takeIf { it.isNotBlank() }?.let { email ->
            contactAddresses.add(ContactAddress(type = "Email", representation = email))
        }
        
        entity.legalAddress?.takeIf { it.isNotBlank() }?.let { address ->
            contactAddresses.add(ContactAddress(type = "Адрес", representation = address))
        }
        
        entity.actualAddress?.takeIf { it.isNotBlank() }?.let { address ->
            contactAddresses.add(ContactAddress(type = "Адрес", representation = address))
        }
        
        return Contragent(
            id = entity.serverId ?: entity.id,
            name = entity.name,
            types = listOf("Юр.лицо"),
            inn = entity.inn,
            contactsAndAddresses = contactAddresses,
            lastOrder = "",
            averageCheck = 0.0,
            ordersCount = 0,
            totalOrdersSum = 0.0,
            segment = entity.category ?: "",
            interactions = emptyList(),
            pinnedComment = entity.notes,
            relatedContragents = emptyList()
        )
    }
    
    fun toDomainList(entities: List<ContragentEntity>): List<Contragent> {
        return entities.map { toDomain(it) }
    }
    
    fun toEntityList(contragents: List<Contragent>, syncStatus: com.helfkea.crm.data.sync.SyncStatus): List<ContragentEntity> {
        return contragents.map { toEntity(it, syncStatus) }
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
    
    private fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }
}