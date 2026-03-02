package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class RelatedContragent(
    @SerializedName("id")
    val id: String,

    @SerializedName("НаименованиеКонтрагента")
    val name: String
)