package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class BasicContragent(
    @SerializedName("id")
    val id: String,

    @SerializedName("Контрагент")
    val name: String,

    @SerializedName("ТипЛица")
    val types: List<String>,

    @SerializedName("Адрес")
    val address: String,

    @SerializedName("ИНН")
    val inn: String? = null
) {
    // Вспомогательное свойство для отображения типов
    val typesString: String
        get() = types.joinToString(", ")
}

