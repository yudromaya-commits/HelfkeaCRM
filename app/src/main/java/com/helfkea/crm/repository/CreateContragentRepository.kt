// CreateContragentRepository.kt
package com.helfkea.crm.repository

import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.model.CreateContragentRequest
import com.helfkea.crm.model.CreateContragentResponse


class CreateContragentRepository {

    suspend fun createContragent(request: CreateContragentRequest): Result<CreateContragentResponse> {
        return try {
            android.util.Log.d("CreateContragentRepository", "🔄 Отправляю запрос createContragent: $request")
            val response = TaskApi.service.createContragent(request)
            android.util.Log.d("CreateContragentRepository", "📡 Ответ createContragent: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                android.util.Log.d("CreateContragentRepository", "✅ Успех createContragent: result=${body.result}, id=${body.id}, name=${body.name}, inn=${body.inn}, error=${body.error}")
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.d("CreateContragentRepository", "❌ Ошибка createContragent: $errorBody")
                Result.failure(Exception("Ошибка создания: $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.d("CreateContragentRepository", "❌ Исключение createContragent: ${e.message}")
            Result.failure(e)
        }
    }
}