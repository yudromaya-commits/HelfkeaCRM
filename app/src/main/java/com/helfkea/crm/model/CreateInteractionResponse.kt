package com.helfkea.crm.model



import com.google.gson.annotations.SerializedName

data class CreateInteractionResponse(
    @SerializedName("error")
    val error: String? = null,

    @SerializedName("result")
    val result: Boolean
)