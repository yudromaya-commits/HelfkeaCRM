package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.model.ContactAddress
import com.helfkea.crm.model.Individual
import com.helfkea.crm.model.Interaction
import com.helfkea.crm.model.RelatedContragent

class IndividualRepository {
    private val apiService = TaskApi.service

    suspend fun getAllIndividuals(): List<Individual> {
        return try {
            val individuals = apiService.getIndividuals()
            Log.d("IndividualRepository", "Получено физлиц: ${individuals.size}")
            individuals
        } catch (e: Exception) {
            Log.e("IndividualRepository", "Ошибка: ${e.message}")
            e.printStackTrace()
            // Возвращаем тестовые данные при ошибке
            getMockIndividuals()
        }
    }

    private fun getMockIndividuals(): List<Individual> {
        return listOf(
            Individual(
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
                lastOrder = "13.11.2025 9:32:20",
                averageCheck = 11584.8,
                ordersCount = 61,
                totalOrdersSum = 706672.7,
                segment = "0-9 месяцев",
                pinnedComment = "VIP клиент. Любит оперативную доставку. Звонить до 18:00. Предпочтительный способ связи - Telegram.",
                relatedContragents = listOf(
                    RelatedContragent(
                        id = "c5d2d2ff-0a91-11e9-ae88-309c23aaf74e",
                        name = "Янулиди Эллина Григорьевна"
                    )
                )
            ),

            Individual(
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
                lastOrder = "13.11.2025 9:32:20",
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
                pinnedComment = "Предпочтительный способ связи - WhatsAp11p.",
                relatedContragents = listOf(
                    RelatedContragent(
                        id = "c5d2d2ff-0a91-11e9-ae88-309c23aaf74e",
                        name = "Янулиди Эллина Григорьевна"
                    )
                )
            ),

            Individual(
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
                lastOrder = "13.11.2025 9:32:20",
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
                pinnedComment = "Новый потенциальный клиент. Требуется активный обзвон.",
                relatedContragents = listOf(
                    RelatedContragent(
                        id = "c5d2d2ff-0a91-11e9-ae88-309c23aaf74e",
                        name = "Янулиди Эллина Григорьевна"
                    )
                )
            ),
            )

    }
}