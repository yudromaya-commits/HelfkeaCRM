package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("product")
    val name: String,

    @SerializedName("count")
    val stockCount: Int,

    @SerializedName("price")
    val price: Double?,

    @SerializedName("priceWholesale")
    val priceWholesale: Double?,

    @SerializedName("article")
    val article: String,

    @SerializedName("productID")
    val productId: String,

    @SerializedName("category")
    val category: String
)