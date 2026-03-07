package com.helfkea.crm.data.local.mapper

import com.helfkea.crm.data.local.entity.IndividualEntity
import com.helfkea.crm.model.ContactAddress
import com.helfkea.crm.model.Individual
import com.helfkea.crm.model.Interaction
import com.helfkea.crm.model.RelatedContragent
import java.text.SimpleDateFormat
import java.util.*

object IndividualMapper {
    
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    private val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    fun toEntity(individual: Individual, syncStatus: com.helfkea.crm.data.sync.SyncStatus): IndividualEntity {
        // Извлекаем данные из контактов и адресов
        val phones = individual.phones
        val emails = individual.emails
        val addresses = individual.contactsAndAddresses.filter { it.isAddressType }
        
        return IndividualEntity(
            id = individual.id,
            serverId = individual.id,
            name = individual.name,
            fullName = individual.name,
            phone = phones.firstOrNull(),
            email = emails.firstOrNull(),
            address = addresses.firstOrNull()?.displayText,
            notes = individual.pinnedComment,
            lastOrder = individual.lastOrder,
            averageCheck = individual.averageCheck,
            ordersCount = individual.ordersCount,
            totalOrdersSum = individual.totalOrdersSum,
            segment = individual.segment,
            syncStatus = syncStatus,
            lastSyncTime = if (syncStatus == com.helfkea.crm.data.sync.SyncStatus.SYNCED) Date() else null
        )
    }
    
    fun toDomain(entity: IndividualEntity): Individual {
        // Создаем список контактных адресов из полей
        val contactAddresses = mutableListOf<ContactAddress>()
        
        entity.phone?.takeIf { it.isNotBlank() }?.let { phone ->
            contactAddresses.add(ContactAddress(type = "Телефон", representation = phone))
        }
        
        entity.email?.takeIf { it.isNotBlank() }?.let { email ->
            contactAddresses.add(ContactAddress(type = "Email", representation = email))
        }
        
        entity.address?.takeIf { it.isNotBlank() }?.let { address ->
            contactAddresses.add(ContactAddress(type = "Адрес", representation = address))
        }
        
        return Individual(
            id = entity.serverId ?: entity.id,
            name = entity.name,
            types = listOf("Физ.лицо"),
            contactsAndAddresses = contactAddresses,
            lastOrder = entity.lastOrder ?: "",
            averageCheck = entity.averageCheck,
            ordersCount = entity.ordersCount,
            totalOrdersSum = entity.totalOrdersSum,
            segment = entity.segment ?: "",
            interactions = emptyList(),
            pinnedComment = entity.notes,
            relatedContragents = emptyList()
        )
    }
    
    fun toDomainList(entities: List<IndividualEntity>): List<Individual> {
        return entities.map { toDomain(it) }
    }
    
    fun toEntityList(individuals: List<Individual>, syncStatus: com.helfkea.crm.data.sync.SyncStatus): List<IndividualEntity> {
        return individuals.map { toEntity(it, syncStatus) }
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