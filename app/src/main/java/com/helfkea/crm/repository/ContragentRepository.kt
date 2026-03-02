package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.model.ContactAddress
import com.helfkea.crm.model.Contragent
import com.helfkea.crm.model.Interaction
import com.helfkea.crm.model.RelatedContragent

class ContragentRepository {
    private val apiService = TaskApi.service

    suspend fun getAllContragents(): List<Contragent> {
        return try {
            val contragents = apiService.getContragents()
            Log.d("ContragentRepository", "Получено контрагентов: ${contragents.size}")
            contragents
        } catch (e: Exception) {
            Log.e("ContragentRepository", "Ошибка: ${e.message}")
            e.printStackTrace()
            // Возвращаем тестовые данные при ошибке
            getMockContragents()
        }
    }

    private fun getMockContragents(): List<Contragent> {
        return listOf(
            Contragent(
                id = "88784a5f-7633-11e9-8105-309c23aaf74e",
                name = "ЗЕД В КУБЕ ООО",
                types = listOf("Клиент"),
                contactsAndAddresses = listOf(
                    ContactAddress(
                        type = "Фактический адрес",
                        representation = "350062, Краснодарский край, городской округ город Краснодар, им Яна Полуяна ул, дом № 22, офис 64"
                    ),
                    ContactAddress(
                        type = "Юридический адрес",
                        representation = "350000, Краснодарский край, г. Краснодар, ул. Красная, д. 1"
                    ),
                    ContactAddress(
                        type = "Телефон",
                        representation = "+7 (999) 123-45-67"
                    ),
                    ContactAddress(
                        type = "Электронная почта",
                        representation = "sales@zedcube.ru"
                    )
                ),
                lastOrder = "13.11.2024 9:32:20",
                averageCheck = 11584.8,
                ordersCount = 61,
                totalOrdersSum = 706672.7,
                segment = "0-9 месяцев",
                interactions = listOf(
                    Interaction(
                        date = "2025-12-17T14:30:00",
                        contactType = "Звонок",
                        comment = "Обсуждение нового заказа. Клиент заинтересован в расширении ассортимента.",
                        manager = "Иванов И.И.",
                        pinned = "Да"
                    ),
                    Interaction(
                        date = "2025-12-20T11:15:00",
                        contactType = "Посещение",
                        comment = "Презентация новых продуктов. Клиент проявил интерес к линейке премиум.",
                        manager = "Петрова А.С.",
                        pinned = "Нет"
                    ),
                    Interaction(
                        date = "2025-12-05T16:45:00",
                        contactType = "Email",
                        comment = "Отправлен прайс-лист на новую коллекцию.",
                        manager = "Сидоров В.П.",
                        pinned = "Нет"
                    )
                ),
                pinnedComment = "VIP клиент. Любит оперативную доставку. Звонить до 18:00. Предпочтительный способ связи - Telegram.",
                relatedContragents = listOf(
                    RelatedContragent(
                        id = "c5d2d2ff-0a91-11e9-ae88-309c23aaf74e",
                        name = "Янулиди Эллина Григорьевна"
                    )
                )
            ),
            Contragent(
                id = "565a0251-7bed-11e9-8109-309c23aaf74e",
                name = "ЙО ОРТО ООО",
                types = listOf("Поставщик", "Клиент"),
                contactsAndAddresses = listOf(
                    ContactAddress(
                        type = "Фактический адрес",
                        representation = "354065, Краснодарский край, Сочи г, Гагарина ул, дом № 72, помещение 31"
                    ),
                    ContactAddress(
                        type = "Телефон",
                        representation = "+7 (862) 299-88-77"
                    )
                ),
                lastOrder = "",
                averageCheck = 0.0,
                ordersCount = 0,
                totalOrdersSum = 0.0,
                segment = "Нет заказов",
                interactions = emptyList(),
                pinnedComment = "Новый потенциальный клиент. Требуется активный обзвон.",
                relatedContragents = emptyList()
            )
        )
    }
}