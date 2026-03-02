package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class GetContragentByIdFullResponse(
    @SerializedName("body")
    val body: Contragent? = null,
    
    @SerializedName("error")
    val error: String = "",
    
    @SerializedName("result")
    val result: Boolean = false
)