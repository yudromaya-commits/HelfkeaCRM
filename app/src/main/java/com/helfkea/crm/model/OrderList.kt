package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

// Для получения списка заказов
data class OrderResponse(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("contragentId")
    val contragentId: String,

    @SerializedName("contragentName")
    val contragentName: String,

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("totalSum")
    val totalSum: Double? = null,

    @SerializedName("products")
    val products: List<OrderProductItem>
)

data class OrderProductItem(
    @SerializedName("productId")
    val productId: String,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("count")
    val count: Int,

    @SerializedName("price")
    val price: Double
)

// Для создания заказа
data class OrderCreateRequest(
    @SerializedName("contragentId")
    val contragentId: String,

    @SerializedName("contragentName")
    val contragentName: String,

    @SerializedName("comment")
    val comment: String? = null,

    @SerializedName("products")
    val products: List<OrderProductItem>
)

data class OrderCreateResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("id")
    val orderId: String?,

    @SerializedName("error")
    val error: String?
)