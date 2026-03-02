package com.helfkea.crm.model

data class GetContragentByIdResponse(
    val error: String,
    val result: Boolean,
    val id: String? = null,
    val name: String? = null,
    val inn: String? = null,
    val type: String? = null,
    val country: String? = null
)