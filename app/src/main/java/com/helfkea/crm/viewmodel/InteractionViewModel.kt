package com.helfkea.crm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.model.ContactType
import com.helfkea.crm.model.CreateInteractionRequest
import com.helfkea.crm.model.CreateInteractionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class InteractionViewModel : ViewModel() {

    private val apiService = TaskApi.service

    // Состояние для видов контактов
    private val _contactTypes = MutableStateFlow<List<ContactType>>(emptyList())
    val contactTypes: StateFlow<List<ContactType>> = _contactTypes.asStateFlow()

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Ошибка
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Состояние успешного создания
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    // Загружаем виды контактов
    fun loadContactTypes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val types = apiService.getContactTypes()
                _contactTypes.value = types // Вот здесь данные должны обновиться
                Log.d("InteractionViewModel", "Загружено видов контактов: ${types.size}")
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки видов контактов: ${e.message}"
                Log.e("InteractionViewModel", "Ошибка: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Создаем взаимодействие
    fun createInteraction(
        contragentId: String,
        typeContactId: String,
        secure: Boolean,
        comment: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            try {
                val request = CreateInteractionRequest(
                    contragentId = contragentId,
                    typeContactId = typeContactId,
                    secure = secure,
                    comment = comment
                )

                val response = apiService.createInteraction(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.result == true) {
                        _isSuccess.value = true
                    } else {
                        _error.value = body?.error ?: "Неизвестная ошибка"
                    }
                } else {
                    _error.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка создания взаимодействия: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Сброс состояния
    fun resetState() {
        _isSuccess.value = false
        _error.value = null
    }
}