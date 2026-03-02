package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class SnapContragentRequest(
    @SerializedName("idIndividual")
    val idIndividual: String,

    @SerializedName("idContragent")
    val idContragent: String
)