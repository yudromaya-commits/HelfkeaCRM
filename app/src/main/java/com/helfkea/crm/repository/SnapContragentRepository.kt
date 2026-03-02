package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.model.BasicContragent
import com.helfkea.crm.model.SnapContragentRequest
import com.helfkea.crm.model.SnapContragentResponse
import retrofit2.Response

class SnapContragentRepository {
    private val apiService = TaskApi.service

    // Кэшированные контрагенты
    private var cachedContragents: List<BasicContragent>? = null
    private var lastCacheTime: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000 // 5 минут

    suspend fun getAllContragents(forceRefresh: Boolean = false): List<BasicContragent> {
        return try {
            // Проверяем кэш
            val currentTime = System.currentTimeMillis()
            if (!forceRefresh &&
                cachedContragents != null &&
                (currentTime - lastCacheTime) < CACHE_DURATION) {
                Log.d("SnapContragentRepository", "Используем кэшированные данные")
                return cachedContragents!!
            }

            Log.d("SnapContragentRepository", "Загружаем данные с сервера")
            val contragents = apiService.getAllContragents()

            // Кэшируем результат
            cachedContragents = contragents
            lastCacheTime = currentTime

            contragents
        } catch (e: Exception) {
            Log.e("SnapContragentRepository", "Ошибка загрузки контрагентов: ${e.message}")
            e.printStackTrace()
            // Возвращаем кэш если есть, иначе пустой список
            cachedContragents ?: emptyList()
        }
    }

    suspend fun clearCache() {
        cachedContragents = null
        lastCacheTime = 0
    }

    suspend fun snapContragent(idIndividual: String, idContragent: String): Response<SnapContragentResponse> {
        return try {
            val request = SnapContragentRequest(idIndividual, idContragent)
            val response = apiService.snapContragent(request)

            // Очищаем кэш при успешной привязке
            if (response.isSuccessful && response.body()?.success == true) {
                clearCache()
            }

            response
        } catch (e: Exception) {
            Log.e("SnapContragentRepository", "Ошибка привязки контрагента: ${e.message}")
            throw e
        }
    }

    // Метод для поиска контрагентов
    fun searchContragents(
        query: String,
        contragents: List<BasicContragent>,
        selectedTypes: Set<String> = emptySet()
    ): List<BasicContragent> {
        var filtered = contragents

        // Поиск по имени и ИНН
        if (query.isNotBlank()) {
            val lowercaseQuery = query.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(lowercaseQuery) ||
                        (it.inn?.lowercase()?.contains(lowercaseQuery) == true) ||
                        it.address.lowercase().contains(lowercaseQuery)
            }
        }

        // Фильтр по типам
        if (selectedTypes.isNotEmpty()) {
            filtered = filtered.filter { contragent ->
                contragent.types.any { it in selectedTypes }
            }
        }

        return filtered
    }
}