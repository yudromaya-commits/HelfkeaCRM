package com.helfkea.crm.repository

import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.model.InnDataRequest
import com.helfkea.crm.model.InnDataResponse

class InnRepository {

    suspend fun getInnData(inn: String, isCompany: Boolean): Result<InnDataResponse> {
        return try {
            val request = InnDataRequest(inn = inn, itsCompany = isCompany)
            val response = TaskApi.service.getInnData(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.error ?: "Ошибка получения данных по ИНН"))
                }
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}