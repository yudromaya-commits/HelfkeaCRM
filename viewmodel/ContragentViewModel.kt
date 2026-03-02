package com.example.repressales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repressales.model.Contragent
import com.example.repressales.repository.ContragentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortBy {
    NAME, ORDERS_COUNT, TOTAL_SUM, AVERAGE_CHECK
}

class ContragentViewModel : ViewModel() {
    private val repository = ContragentRepository()

    // Состояние для поиска
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Фильтры
    private val _typeFilter = MutableStateFlow<String?>(null)
    private val _segmentFilter = MutableStateFlow<String?>(null)
    private val _sortBy = MutableStateFlow(SortBy.NAME)

    // Исходные данные
    private val _allContragents = MutableStateFlow<List<Contragent>>(emptyList())
    val allContragents: StateFlow<List<Contragent>> = _allContragents.asStateFlow()  // ✅ ДОБАВЛЕНО

    private val _contragents = MutableStateFlow<List<Contragent>>(emptyList())
    val contragents: StateFlow<List<Contragent>> = _contragents.asStateFlow()

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Ошибка
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Списки для фильтров
    val availableTypes: StateFlow<List<String>> = _allContragents.map {
        it.map { c -> c.type }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableSegments: StateFlow<List<String>> = _allContragents.map {
        it.map { c -> c.segment }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Объединяем все фильтры
        combine(
            _searchQuery,
            _typeFilter,
            _segmentFilter,
            _sortBy,
            _allContragents
        ) { query, type, segment, sortBy, all ->
            var filtered = all

            // Поиск по имени и адресу
            if (query.isNotBlank()) {
                val lowercaseQuery = query.lowercase()
                filtered = filtered.filter {
                    it.name.lowercase().contains(lowercaseQuery) ||
                            it.address.lowercase().contains(lowercaseQuery)
                }
            }

            // Фильтр по типу
            type?.let {
                filtered = filtered.filter { it.type == type }
            }

            // Фильтр по сегменту
            segment?.let {
                filtered = filtered.filter { it.segment == segment }
            }

            // Сортировка
            filtered = when (sortBy) {
                SortBy.NAME -> filtered.sortedBy { it.name }
                SortBy.ORDERS_COUNT -> filtered.sortedByDescending { it.ordersCount }
                SortBy.TOTAL_SUM -> filtered.sortedByDescending { it.totalOrdersSum }
                SortBy.AVERAGE_CHECK -> filtered.sortedByDescending { it.averageCheck }
            }

            filtered
        }.onEach { filtered ->
            _contragents.value = filtered
        }.launchIn(viewModelScope)
    }

    suspend fun loadContragents() {
        _isLoading.value = true
        _error.value = null
        try {
            val result = repository.getAllContragents()
            _allContragents.value = result
        } catch (e: Exception) {
            _error.value = "Ошибка загрузки: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun refreshContragents() {
        viewModelScope.launch {
            loadContragents()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateTypeFilter(type: String?) {
        _typeFilter.value = type
    }

    fun updateSegmentFilter(segment: String?) {
        _segmentFilter.value = segment
    }

    fun updateSortBy(sortBy: SortBy) {
        _sortBy.value = sortBy
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _typeFilter.value = null
        _segmentFilter.value = null
        _sortBy.value = SortBy.NAME
    }
}