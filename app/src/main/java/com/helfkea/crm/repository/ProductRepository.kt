package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.model.Product

class ProductRepository {
    private val apiService = TaskApi.service

    suspend fun getAllProducts(): List<Product> {
        return try {
            val products = apiService.getProducts()
            Log.d("ProductRepository", "Получено товаров: ${products.size}")
            products
        } catch (e: Exception) {
            Log.e("ProductRepository", "Ошибка загрузки товаров: ${e.message}")
            e.printStackTrace()
            // Возвращаем тестовые данные при ошибке
            getMockProducts()
        }
    }

    private fun getMockProducts(): List<Product> {
        return listOf(
            Product(
                name = "Дистальные кусачки с безопасным держателем (до .022\"x.028\")",
                stockCount = 4,
                price = 24719.0,
                priceWholesale = null,
                article = "65510 ",
                productId = "adfb5132-ddd9-11ea-81cc-309c23aaf74e",
                category = "TASK instruments"
            ),
            Product(
                name = "Щипцы Кима с кусачками для изгибания многопетлевой дуги",
                stockCount = 2,
                price = 23697.0,
                priceWholesale = null,
                article = "64303 ",
                productId = "617c0be8-ddda-11ea-81cc-309c23aaf74e",
                category = "TASK instruments"
            ),
            Product(
                name = "Лигатурные кусачки TS-15",
                stockCount = 1,
                price = 16473.0,
                priceWholesale = null,
                article = "60015 ",
                productId = "c1f8b148-f348-11ea-81da-309c23aaf74e",
                category = "TASK instruments"
            )
        )
    }
}