// CreateInteractionRequest.kt
package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class CreateInteractionRequest(
    @SerializedName("contragentID")
    val contragentId: String,

    @SerializedName("typeContactID")
    val typeContactId: String,

    @SerializedName("secure")
    val secure: Boolean,

    @SerializedName("comment")
    val comment: String
)

