package com.helfkea.crm.data.local.mapper

import com.google.gson.Gson
import com.helfkea.crm.data.local.entity.OrderEntity
import com.helfkea.crm.model.OrderCreateRequest
import com.helfkea.crm.model.OrderCreateResponse
import com.helfkea.crm.model.OrderProductItem
import com.helfkea.crm.model.OrderResponse
import java.text.SimpleDateFormat
import java.util.*

object OrderMapper {
    
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    fun toEntity(orderResponse: OrderResponse, syncStatus: com.helfkea.crm.data.sync.SyncStatus): OrderEntity {
        return OrderEntity(
            id = orderResponse.id ?: UUID.randomUUID().toString(),
            serverId = orderResponse.id,
            contragentId = orderResponse.contragentId,
            contragentName = orderResponse.contragentName,
            comment = null, // Нет в OrderResponse
            date = parseDate(orderResponse.date),
            status = orderResponse.status ?: "Создан",
            totalSum = orderResponse.totalSum ?: 0.0,
            productsJson = gson.toJson(orderResponse.products),
            syncStatus = syncStatus,
            lastSyncTime = if (syncStatus == com.helfkea.crm.data.sync.SyncStatus.SYNCED) Date() else null
        )
    }
    
    fun toEntity(createRequest: OrderCreateRequest, syncStatus: com.helfkea.crm.data.sync.SyncStatus): OrderEntity {
        return OrderEntity.createLocal(
            contragentId = createRequest.contragentId,
            contragentName = createRequest.contragentName,
            comment = createRequest.comment,
            productsJson = gson.toJson(createRequest.products),
            totalSum = calculateTotalSum(createRequest.products),
            status = "Черновик"
        )
    }
    
    fun toOrderResponse(entity: OrderEntity): OrderResponse {
        val products = try {
            gson.fromJson(entity.productsJson, Array<OrderProductItem>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
        
        return OrderResponse(
            id = entity.serverId,
            contragentId = entity.contragentId,
            contragentName = entity.contragentName,
            date = formatDate(entity.date),
            status = entity.status,
            totalSum = entity.totalSum,
            products = products
        )
    }
    
    fun toOrderCreateRequest(entity: OrderEntity): OrderCreateRequest {
        val products = try {
            gson.fromJson(entity.productsJson, Array<OrderProductItem>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
        
        return OrderCreateRequest(
            contragentId = entity.contragentId,
            contragentName = entity.contragentName,
            comment = entity.comment,
            products = products
        )
    }
    
    fun toOrderResponseList(entities: List<OrderEntity>): List<OrderResponse> {
        return entities.map { toOrderResponse(it) }
    }
    
    fun toEntityList(orderResponses: List<OrderResponse>, syncStatus: com.helfkea.crm.data.sync.SyncStatus): List<OrderEntity> {
        return orderResponses.map { toEntity(it, syncStatus) }
    }
    
    fun updateEntityFromResponse(entity: OrderEntity, response: OrderCreateResponse): OrderEntity {
        return entity.copy(
            serverId = response.orderId,
            orderNumber = null, // response не содержит номера заказа
            syncStatus = if (response.success) com.helfkea.crm.data.sync.SyncStatus.SYNCED else com.helfkea.crm.data.sync.SyncStatus.FAILED,
            lastSyncTime = Date(),
            syncError = if (!response.success) response.error else null
        )
    }
    
    private fun calculateTotalSum(products: List<OrderProductItem>): Double {
        return products.sumOf { it.price * it.count }
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