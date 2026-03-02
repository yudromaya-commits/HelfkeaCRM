package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class InnDataResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("isCompany")
    val isCompany: Boolean? = null,
    
    @SerializedName("error")
    val error: String? = null
)