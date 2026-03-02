package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class Individual(

    @SerializedName("id")
    val id: String,

    @SerializedName("Контрагент")
    val name: String,

    @SerializedName("ТипЛица")
    val types: List<String>,

    @SerializedName("КонтактыИАдреса")
    val contactsAndAddresses: List<ContactAddress> = emptyList(),

    @SerializedName("ПоследнийЗаказ")
    val lastOrder: String,

    @SerializedName("СреднийЧек")
    val averageCheck: Double,

    @SerializedName("КоличествоЗаказов")
    val ordersCount: Int,

    @SerializedName("ОбщаяСуммаЗаказов")
    val totalOrdersSum: Double,

    @SerializedName("Сегмент")
    val segment: String,

    @SerializedName("Взаимодействия")
    val interactions: List<Interaction> = emptyList(),

    @SerializedName("ЗакрепленныйКомментарий")
    val pinnedComment: String? = null,

    @SerializedName("СвязанныеКонтрагенты")
    val relatedContragents: List<RelatedContragent> = emptyList()
) {
    // Обновляем свойство для основного адреса
    val mainAddress: String
        get() = contactsAndAddresses
            .firstOrNull { it.isAddressType }
            ?.displayText
            ?: "Адрес не указан"

    // Телефоны
    val phones: List<String>
        get() = contactsAndAddresses
            .filter { it.isPhoneType && !it.displayText.isNullOrBlank() }
            .map { it.displayText!! }

    // Emails
    val emails: List<String>
        get() = contactsAndAddresses
            .filter { it.isEmailType && !it.displayText.isNullOrBlank() }
            .map { it.displayText!! }
}