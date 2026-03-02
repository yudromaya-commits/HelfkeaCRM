package com.example.repressales.repository

import android.util.Log
import com.example.repressales.api.TaskApi
import com.example.repressales.model.Contragent

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
                name = "ООО Ромашка",
                type = "Юрлицо",
                address = "г. Москва, ул. Пушкина, д. 1",
                lastOrder = "20.11.2024 14:30:00",
                averageCheck = 150000.0,
                ordersCount = 12,
                totalOrdersSum = 1800000.0,
                segment = "VIP клиент"
            ),
            Contragent(
                name = "ИП Иванов",
                type = "ИП",
                address = "г. Санкт-Петербург, Невский пр., д. 10",
                lastOrder = "15.11.2024 10:15:00",
                averageCheck = 75000.0,
                ordersCount = 8,
                totalOrdersSum = 600000.0,
                segment = "Постоянный клиент"
            )
        )
    }
}