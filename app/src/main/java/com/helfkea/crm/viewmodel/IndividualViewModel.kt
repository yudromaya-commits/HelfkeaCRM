package com.helfkea.crm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helfkea.crm.model.Contragent
import com.helfkea.crm.model.Individual
import com.helfkea.crm.repository.ContragentRepository
import com.helfkea.crm.repository.IndividualRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.collections.distinct
import kotlin.collections.filter
import kotlin.collections.flatMap
import kotlin.collections.map

// Переиспользуем существующий SortBy из ContragentViewModel
// или создаем отдельный если нужно

class IndividualViewModel : ViewModel() {
    private val repository = IndividualRepository()

    // Состояние для поиска
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Фильтры
    private val _typeFilter = MutableStateFlow<String?>(null)
    private val _segmentFilter = MutableStateFlow<String?>(null)
    private val _sortBy = MutableStateFlow(SortBy.NAME)

    // Исходные данные
    private val _allIndividuals = MutableStateFlow<List<Individual>>(emptyList())
    val allIndividuals: StateFlow<List<Individual>> = _allIndividuals.asStateFlow()

    private val _individuals = MutableStateFlow<List<Individual>>(emptyList())
    val individuals: StateFlow<List<Individual>> = _individuals.asStateFlow()

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Ошибка
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Списки для фильтров
    val availableTypes: StateFlow<List<String>> = _allIndividuals.map { list ->
        list.flatMap { it.types }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableSegments: StateFlow<List<String>> = _allIndividuals.map {
        it.map { i -> i.segment }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        combine(
            _searchQuery,
            _typeFilter,
            _segmentFilter,
            _sortBy,
            _allIndividuals
        ) { query, type, segment, sortBy, all ->
            var filtered = all

            // Поиск по имени и адресу (ОБНОВЛЕНО: используем mainAddress)
            if (query.isNotBlank()) {
                val lowercaseQuery = query.lowercase()
                filtered = filtered.filter {
                    it.name.lowercase().contains(lowercaseQuery) ||
                            it.mainAddress.lowercase().contains(lowercaseQuery) ||
                            // Также ищем по телефону если есть
                            it.phones.any { phone -> phone.lowercase().contains(lowercaseQuery) } ||
                            // И по email если есть
                            it.emails.any { email -> email.lowercase().contains(lowercaseQuery) }
                }
            }

            // Фильтр по типу
            type?.let { filterType ->
                filtered = filtered.filter { Individual ->
                    Individual.types.any { it == filterType }
                }
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
            _individuals.value = filtered
        }.launchIn(viewModelScope)
    }

    suspend fun loadIndividuals() {
        _isLoading.value = true
        _error.value = null
        try {
            val result = repository.getAllIndividuals()
            _allIndividuals.value = result
        } catch (e: Exception) {
            _error.value = "Ошибка загрузки контрагентов: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun refreshIndividuals() {
        viewModelScope.launch {
            loadIndividuals()
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