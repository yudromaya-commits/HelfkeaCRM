package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.model.*
import retrofit2.Response
import retrofit2.HttpException

class OrdersRepository {
    private val apiService = TaskApi.service

    suspend fun getOrders(): List<OrderResponse> {
        Log.d("OrdersRepository", "Запрашиваем заказы из API")

        return try {
            // 1. Получаем Response
            val response: Response<List<OrderResponse>> = apiService.getOrders()
            Log.d("OrdersRepository", "Response получен, код: ${response.code()}")

            // 2. Проверяем успешность
            if (response.isSuccessful) {
                // 3. Получаем тело ответа
                val orders = response.body()
                Log.d("OrdersRepository", "Тело ответа: ${orders?.size ?: 0} заказов")

                if (orders == null) {
                    Log.w("OrdersRepository", "Тело ответа null")
                    emptyList()
                } else {
                    // Логируем детали
                    if (orders.isNotEmpty()) {
                        Log.d("OrdersRepository", "Первый заказ: ID=${orders[0].id}, Контрагент='${orders[0].contragentName}'")
                    }
                    orders
                }
            } else {
                // 4. Обработка ошибки HTTP
                val errorMessage = "HTTP ошибка: ${response.code()}"
                val errorBody = response.errorBody()?.string()
                Log.e("OrdersRepository", "$errorMessage, тело ошибки: $errorBody")
                throw HttpException(response)
            }
        } catch (e: HttpException) {
            Log.e("OrdersRepository", "HTTP исключение: ${e.message}", e)
            // Возвращаем тестовые данные при ошибке
            getMockOrders()
        } catch (e: Exception) {
            Log.e("OrdersRepository", "Общее исключение: ${e.message}", e)
            e.printStackTrace()
            // Возвращаем тестовые данные при ошибке
            getMockOrders()
        }
    }

    private fun getMockOrders(): List<OrderResponse> {
        Log.d("OrdersRepository", "Используем тестовые данные")
        return listOf(
            OrderResponse(
                id = "1",
                date = "2024-01-15T14:30:00",
                contragentName = "ООО 'МедТех'",
                totalSum = 150000.0,
                status = "Выполнен",
                products = listOf(
                    OrderProductItem(
                        productId = "prod_001",
                        productName = "Дистальные кусачки",
                        count = 2,
                        price = 25000.0
                    ),
                    OrderProductItem(
                        productId = "prod_002",
                        productName = "Щипцы Кима",
                        count = 1,
                        price = 100000.0
                    )
                ),
                contragentId = "test_id_1"
            )
        )
    }

    suspend fun createOrder(order: OrderCreateRequest): Result<OrderCreateResponse> {
        return try {
            val response = apiService.createOrderV2(order)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.error ?: "Неизвестная ошибка"))
                }
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Метод для конвертации текущей корзины в формат для API
    fun convertCartToOrderRequest(
        contragentId: String,
        contragentName: String,
        cartItems: List<CartItem>,
        comment: String? = null
    ): OrderCreateRequest {
        val products = cartItems.map { item ->
            OrderProductItem(
                productId = item.product.productId,
                productName = item.product.name,
                count = item.quantity,
                price = item.pricePerUnit
            )
        }

        return OrderCreateRequest(
            contragentId = contragentId,
            contragentName = contragentName,
            comment = comment,
            products = products
        )
    }


}

