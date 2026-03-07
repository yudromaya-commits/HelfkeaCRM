package com.helfkea.crm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.helfkea.crm.data.local.database.Converters
import java.util.*

@Entity(tableName = "products")
@TypeConverters(Converters::class)
data class ProductEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Серверный ID
    val serverId: String,
    
    // Данные продукта
    val name: String,
    val article: String,
    val category: String,
    val stockCount: Int = 0,
    val price: Double? = null,
    val priceWholesale: Double? = null,
    
    // Статус синхронизации (продукты только для чтения, всегда синхронизированы)
    val syncStatus: com.helfkea.crm.data.sync.SyncStatus = com.helfkea.crm.data.sync.SyncStatus.SYNCED,
    val lastSyncTime: Date? = null,
    val syncError: String? = null,
    
    // Метаданные
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    companion object {
        fun fromServer(
            serverId: String,
            name: String,
            article: String,
            category: String,
            stockCount: Int = 0,
            price: Double? = null,
            priceWholesale: Double? = null
        ): ProductEntity {
            return ProductEntity(
                serverId = serverId,
                name = name,
                article = article,
                category = category,
                stockCount = stockCount,
                price = price,
                priceWholesale = priceWholesale,
                lastSyncTime = Date()
            )
        }
    }
}