package com.helfkea.crm.data.local.mapper

import com.helfkea.crm.data.local.entity.ProductEntity
import com.helfkea.crm.model.Product
import java.util.*

object ProductMapper {
    
    fun toEntity(product: Product, syncStatus: com.helfkea.crm.data.sync.SyncStatus): ProductEntity {
        return ProductEntity(
            serverId = product.productId,
            name = product.name,
            article = product.article,
            category = product.category,
            stockCount = product.stockCount,
            price = product.price,
            priceWholesale = product.priceWholesale,
            syncStatus = syncStatus,
            lastSyncTime = if (syncStatus == com.helfkea.crm.data.sync.SyncStatus.SYNCED) Date() else null
        )
    }
    
    fun toDomain(entity: ProductEntity): Product {
        return Product(
            name = entity.name,
            stockCount = entity.stockCount,
            price = entity.price,
            priceWholesale = entity.priceWholesale,
            article = entity.article,
            productId = entity.serverId,
            category = entity.category
        )
    }
    
    fun toDomainList(entities: List<ProductEntity>): List<Product> {
        return entities.map { toDomain(it) }
    }
    
    fun toEntityList(products: List<Product>, syncStatus: com.helfkea.crm.data.sync.SyncStatus): List<ProductEntity> {
        return products.map { toEntity(it, syncStatus) }
    }
}