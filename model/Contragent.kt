package com.example.repressales.model

import com.google.gson.annotations.SerializedName

data class Contragent(
    @SerializedName("Контрагент")
    val name: String,

    @SerializedName("ТипЛица")
    val type: String,

    @SerializedName("Адрес")
    val address: String,

    @SerializedName("ПоследнийЗаказ")
    val lastOrder: String,

    @SerializedName("СреднийЧек")
    val averageCheck: Double,

    @SerializedName("КоличествоЗаказов")
    val ordersCount: Int,

    @SerializedName("ОбщаяСуммаЗаказов")
    val totalOrdersSum: Double,

    @SerializedName("Сегмент")
    val segment: String
)