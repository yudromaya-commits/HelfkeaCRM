package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class InnDataRequest(
    @SerializedName("inn")
    val inn: String,
    
    @SerializedName("itsCompany")
    val itsCompany: Boolean
)