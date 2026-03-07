package com.helfkea.crm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.helfkea.crm.data.local.database.Converters
import java.util.*

@Entity(tableName = "orders")
@TypeConverters(Converters::class)
data class OrderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Серверный ID (заполняется после синхронизации)
    val serverId: String? = null,
    val orderNumber: String? = null,
    
    // Данные заказа
    val contragentId: String,
    val contragentName: String,
    val comment: String? = null,
    val date: Date = Date(),
    val status: String = "Черновик",
    val totalSum: Double = 0.0,
    
    // Продукты (сериализованный JSON)
    val productsJson: String,
    
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
            contragentName: String,
            comment: String? = null,
            productsJson: String,
            totalSum: Double = 0.0,
            status: String = "Черновик"
        ): OrderEntity {
            return OrderEntity(
                contragentId = contragentId,
                contragentName = contragentName,
                comment = comment,
                productsJson = productsJson,
                totalSum = totalSum,
                status = status
            )
        }
        
        fun fromServer(
            serverId: String,
            orderNumber: String? = null,
            contragentId: String,
            contragentName: String,
            comment: String? = null,
            date: Date = Date(),
            status: String = "Создан",
            totalSum: Double = 0.0,
            productsJson: String
        ): OrderEntity {
            return OrderEntity(
                id = UUID.randomUUID().toString(),
                serverId = serverId,
                orderNumber = orderNumber,
                contragentId = contragentId,
                contragentName = contragentName,
                comment = comment,
                date = date,
                status = status,
                totalSum = totalSum,
                productsJson = productsJson,
                syncStatus = com.helfkea.crm.data.sync.SyncStatus.SYNCED,
                lastSyncTime = Date()
            )
        }
    }
}