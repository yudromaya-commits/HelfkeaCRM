package com.helfkea.crm.model

data class CartItem(
    val product: Product,
    var quantity: Int = 1,
    val pricePerUnit: Double = 0.0
) {
    val totalPrice: Double
        get() = pricePerUnit * quantity

    fun increaseQuantity(max: Int = product.stockCount): Boolean {
        return if (quantity < max) {
            quantity++
            true
        } else false
    }

    fun decreaseQuantity(min: Int = 1): Boolean {
        return if (quantity > min) {
            quantity--
            true
        } else false
    }

    fun canIncrease(max: Int = product.stockCount): Boolean {
        return quantity < max
    }

    fun canDecrease(min: Int = 1): Boolean {
        return quantity > min
    }
}