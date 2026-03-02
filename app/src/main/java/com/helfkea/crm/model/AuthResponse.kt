package com.helfkea.crm.model

data class AuthResponse(
    val auth: Boolean,
    val error: String? = null,
    val result: Boolean? = null
)