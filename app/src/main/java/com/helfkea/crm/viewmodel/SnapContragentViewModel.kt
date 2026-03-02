
package com.helfkea.crm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helfkea.crm.model.BasicContragent
import com.helfkea.crm.repository.SnapContragentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SnapContragentViewModel : ViewModel() {
    private val repository = SnapContragentRepository()

    // Состояние поиска
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Выбранные типы для фильтрации
    private val _selectedTypes = MutableStateFlow<Set<String>>(emptySet())
    val selectedTypes: StateFlow<Set<String>> = _selectedTypes.asStateFlow()

    // Исходные данные
    private val _allContragents = MutableStateFlow<List<BasicContragent>>(emptyList())
    val allContragents: StateFlow<List<BasicContragent>> = _allContragents.asStateFlow()

    // Отфильтрованные данные
    private val _filteredContragents = MutableStateFlow<List<BasicContragent>>(emptyList())
    val filteredContragents: StateFlow<List<BasicContragent>> = _filteredContragents.asStateFlow()

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Состояние ошибки
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Статус операции привязки
    private val _snapStatus = MutableStateFlow<SnapStatus?>(null)
    val snapStatus: StateFlow<SnapStatus?> = _snapStatus.asStateFlow()

    // Доступные типы для фильтра
    val availableTypes: StateFlow<List<String>> = _allContragents.map { list ->
        list.flatMap { it.types }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Комбинируем фильтры
        combine(
            _searchQuery,
            _selectedTypes,
            _allContragents
        ) { query, types, all ->
            repository.searchContragents(query, all, types)
        }.onEach { filtered ->
            _filteredContragents.value = filtered
        }.launchIn(viewModelScope)
    }

    suspend fun loadContragents(forceRefresh: Boolean = false) {
        _isLoading.value = true
        _error.value = null
        try {
            val result = repository.getAllContragents(forceRefresh)
            _allContragents.value = result
        } catch (e: Exception) {
            _error.value = "Ошибка загрузки контрагентов: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun refreshContragents() {
        viewModelScope.launch {
            loadContragents(true)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTypeFilter(type: String) {
        val current = _selectedTypes.value.toMutableSet()
        if (current.contains(type)) {
            current.remove(type)
        } else {
            current.add(type)
        }
        _selectedTypes.value = current
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedTypes.value = emptySet()
    }

    suspend fun snapContragent(individualId: String, contragentId: String): Boolean {
        _isLoading.value = true
        _snapStatus.value = null
        try {
            val response = repository.snapContragent(individualId, contragentId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    _snapStatus.value = SnapStatus.Success(body.message)
                    // Обновляем список после успешной привязки
                    loadContragents(true)
                    return true
                } else {
                    _snapStatus.value = SnapStatus.Error(
                        body?.message ?: "Неизвестная ошибка",
                        body?.errorCode
                    )
                }
            } else {
                _snapStatus.value = SnapStatus.Error(
                    "Ошибка сервера: ${response.code()}",
                    "HTTP_${response.code()}"
                )
            }
        } catch (e: Exception) {
            _snapStatus.value = SnapStatus.Error(
                "Ошибка соединения: ${e.message}",
                "NETWORK_ERROR"
            )
        } finally {
            _isLoading.value = false
        }
        return false
    }

    fun clearSnapStatus() {
        _snapStatus.value = null
    }

    sealed  class SnapStatus {
        data class Success(val message: String) : SnapStatus()
        data class Error(val message: String, val errorCode: String?) : SnapStatus()
    }
}

