package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class SnapContragentResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("errorCode")
    val errorCode: String? = null,

    @SerializedName("details")
    val details: Map<String, Any>? = null
)