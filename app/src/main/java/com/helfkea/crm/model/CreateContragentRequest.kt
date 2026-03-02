// CreateContragentRequest.kt
package com.helfkea.crm.model

data class CreateContragentRequest(
    val name: String,
    val type: String, // "Юр.лицо" или "Физ.лицо"
    val inn: String = "",
    val number: String = "",
    val adress: String = "",
    val description: String = "", // Новое обязательное поле
    val doctor: Boolean = false,
    val clinic: Boolean = false,
    val lead: Boolean = false,
    val client: Boolean = false, // Новое поле: Клиент
    val pasient: Boolean = false,
    val tradingOrganization: Boolean = false // Новое поле: Торгующая организация
)

data class CreateContragentResponse(
    val error: String,
    val result: Boolean,
    val id: String? = null,
    val name: String? = null,
    val inn: String? = null
)