package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("nameUser")
    val nameUser: String
)