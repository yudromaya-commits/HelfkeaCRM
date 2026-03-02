package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class ContactType(
    @SerializedName("typeContactID")
    val id: String,

    @SerializedName("typeContactNAME")
    val name: String
)