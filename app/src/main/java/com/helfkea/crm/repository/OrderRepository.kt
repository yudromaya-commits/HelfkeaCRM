
package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.model.CreateOrderResponse
import com.helfkea.crm.model.Order
import retrofit2.HttpException

class OrderRepository {
    private val apiService = TaskApi.service

    suspend fun createOrder(order: Order): CreateOrderResponse {
        return try {
            val response = apiService.createOrder(order)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: CreateOrderResponse(
                    result = false,
                    error = "Пустой ответ от сервера"
                )

                Log.d("OrderRepository", "✅ Заказ создан успешно: ${apiResponse.numberOrder}")
                Log.d("OrderRepository", "GUID: ${apiResponse.guid}")
                Log.d("OrderRepository", "Ошибка: ${apiResponse.error}")

                apiResponse
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("OrderRepository", "❌ Ошибка создания заказа: ${response.code()} - $errorBody")
                CreateOrderResponse(
                    result = false,
                    error = "Ошибка сервера: ${response.code()}"
                )
            }
        } catch (e: HttpException) {
            Log.e("OrderRepository", "HTTP ошибка: ${e.message}")
            CreateOrderResponse(
                result = false,
                error = "HTTP ошибка: ${e.message}"
            )
        } catch (e: Exception) {
            Log.e("OrderRepository", "Ошибка сети: ${e.message}")
            CreateOrderResponse(
                result = false,
                error = "Ошибка сети: ${e.message}"
            )
        }
    }
}
