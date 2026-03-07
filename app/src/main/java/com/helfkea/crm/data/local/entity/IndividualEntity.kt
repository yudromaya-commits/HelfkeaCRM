package com.helfkea.crm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.helfkea.crm.data.local.database.Converters
import java.util.*

@Entity(tableName = "individuals")
@TypeConverters(Converters::class)
data class IndividualEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Серверный ID (заполняется после синхронизации)
    val serverId: String? = null,
    
    // Основные данные
    val name: String,
    val fullName: String? = null,
    val inn: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val birthDate: Date? = null,
    val passportSeries: String? = null,
    val passportNumber: String? = null,
    val passportIssuedBy: String? = null,
    val passportIssueDate: Date? = null,
    val notes: String? = null,
    
    // Статистика
    val lastOrder: String? = null,
    val averageCheck: Double = 0.0,
    val ordersCount: Int = 0,
    val totalOrdersSum: Double = 0.0,
    val segment: String? = null,
    
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
            name: String,
            fullName: String? = null,
            inn: String? = null,
            phone: String? = null,
            email: String? = null,
            address: String? = null,
            birthDate: Date? = null,
            passportSeries: String? = null,
            passportNumber: String? = null,
            passportIssuedBy: String? = null,
            passportIssueDate: Date? = null,
            notes: String? = null,
            lastOrder: String? = null,
            averageCheck: Double = 0.0,
            ordersCount: Int = 0,
            totalOrdersSum: Double = 0.0,
            segment: String? = null
        ): IndividualEntity {
            return IndividualEntity(
                name = name,
                fullName = fullName,
                inn = inn,
                phone = phone,
                email = email,
                address = address,
                birthDate = birthDate,
                passportSeries = passportSeries,
                passportNumber = passportNumber,
                passportIssuedBy = passportIssuedBy,
                passportIssueDate = passportIssueDate,
                notes = notes,
                lastOrder = lastOrder,
                averageCheck = averageCheck,
                ordersCount = ordersCount,
                totalOrdersSum = totalOrdersSum,
                segment = segment
            )
        }
        
        fun fromServer(
            serverId: String,
            name: String,
            fullName: String? = null,
            inn: String? = null,
            phone: String? = null,
            email: String? = null,
            address: String? = null,
            birthDate: Date? = null,
            passportSeries: String? = null,
            passportNumber: String? = null,
            passportIssuedBy: String? = null,
            passportIssueDate: Date? = null,
            notes: String? = null,
            lastOrder: String? = null,
            averageCheck: Double = 0.0,
            ordersCount: Int = 0,
            totalOrdersSum: Double = 0.0,
            segment: String? = null
        ): IndividualEntity {
            return IndividualEntity(
                id = UUID.randomUUID().toString(),
                serverId = serverId,
                name = name,
                fullName = fullName,
                inn = inn,
                phone = phone,
                email = email,
                address = address,
                birthDate = birthDate,
                passportSeries = passportSeries,
                passportNumber = passportNumber,
                passportIssuedBy = passportIssuedBy,
                passportIssueDate = passportIssueDate,
                notes = notes,
                lastOrder = lastOrder,
                averageCheck = averageCheck,
                ordersCount = ordersCount,
                totalOrdersSum = totalOrdersSum,
                segment = segment,
                syncStatus = com.helfkea.crm.data.sync.SyncStatus.SYNCED,
                lastSyncTime = Date()
            )
        }
    }
}