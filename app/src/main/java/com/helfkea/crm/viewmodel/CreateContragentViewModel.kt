// CreateContragentViewModel.kt
package com.helfkea.crm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helfkea.crm.model.CreateContragentRequest
import com.helfkea.crm.repository.CreateContragentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class CreateContragentViewModel : ViewModel() {

    private val repository = CreateContragentRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    // Данные созданного контрагента
    private val _createdContragentData = MutableStateFlow<CreatedContragentData?>(null)
    val createdContragentData: StateFlow<CreatedContragentData?> = _createdContragentData

    data class CreatedContragentData(
        val id: String?,
        val name: String?,
        val inn: String?
    )

    // В методе createContragent
    fun createContragent(request: CreateContragentRequest, onSuccess: (id: String?, name: String?, inn: String?) -> Unit = { _, _, _ -> }) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = false

            android.util.Log.d("CreateContragentViewModel", "🔄 Начинаю создание контрагента: $request")
            
            val result = repository.createContragent(request)

            result.onSuccess { response ->
                android.util.Log.d("CreateContragentViewModel", "📡 Получен ответ от репозитория: result=${response.result}, id=${response.id}")
                
                if (response.result) {
                    _success.value = true
                    // Сохраняем данные созданного контрагента
                    _createdContragentData.value = CreatedContragentData(
                        id = response.id,
                        name = response.name,
                        inn = response.inn
                    )
                    
                    android.util.Log.d("CreateContragentViewModel", "✅ Контрагент создан! Вызываю onSuccess с id=${response.id}")
                    
                    // Автоматически сбрасываем успех через 3 секунды
                    viewModelScope.launch {
                        delay(3000)
                        _success.value = false
                        _createdContragentData.value = null
                    }
                    onSuccess(response.id, response.name, response.inn)
                } else {
                    // Показываем ошибку от сервера
                    android.util.Log.d("CreateContragentViewModel", "❌ Ошибка от сервера: ${response.error}")
                    _error.value = response.error.ifEmpty { "Неизвестная ошибка сервера" }
                }
            }.onFailure { exception ->
                // Показываем ошибку сети
                android.util.Log.d("CreateContragentViewModel", "❌ Исключение: ${exception.message}")
                _error.value = when {
                    exception.message?.contains("network", ignoreCase = true) == true ->
                        "Ошибка сети. Проверьте подключение"
                    exception.message?.contains("timeout", ignoreCase = true) == true ->
                        "Таймаут соединения"
                    else -> exception.message ?: "Неизвестная ошибка"
                }
            }

            _isLoading.value = false
        }
    }

    fun resetState() {
        _error.value = null
        _success.value = false
    }
}