package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("Клиент")
    val clientId: String? = null,

    @SerializedName("Комментарий")
    val comment: String? = null,

    @SerializedName("Товары")
    val products: List<OrderProduct>
)

data class OrderProduct(
    @SerializedName("Товар")
    val productId: String,

    @SerializedName("Количество")
    val quantity: Int,

    @SerializedName("Цена")
    val price: Double,

    @SerializedName("Сумма")
    val total: Double
)

// Ответ от сервера при создании заказа
// Обновленная модель ответа
data class CreateOrderResponse(
    @SerializedName("numberOrder")
    val numberOrder: String? = null,  // Новое поле

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("guid")
    val guid: String? = null,  // Новое поле

    @SerializedName("result")
    val result: Boolean = false,  // Изменили с success на result

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("id")
    val orderId: String? = null  // Оставляем для совместимости
) {
    // Вспомогательное свойство для совместимости со старым кодом
    val success: Boolean
        get() = result && error.isNullOrEmpty()
}