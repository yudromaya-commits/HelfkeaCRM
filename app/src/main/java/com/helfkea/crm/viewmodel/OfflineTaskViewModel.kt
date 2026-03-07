package com.helfkea.crm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helfkea.crm.data.sync.SyncManager
import com.helfkea.crm.model.*
import com.helfkea.crm.repository.OfflineTaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OfflineTaskViewModel(
    private val taskRepository: OfflineTaskRepository,
    private val syncManagerParam: com.helfkea.crm.data.sync.UniversalSyncManager
) : ViewModel() {
    
    // Состояния загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Потоки задач
    val allTasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val myTasks: StateFlow<List<Task>> = taskRepository.getMyTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val tasksAssignedToMe: StateFlow<List<Task>> = taskRepository.getTasksAssignedToMe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Выбранная вкладка
    private val _selectedTab = MutableStateFlow(TaskTab.ALL_TASKS)
    val selectedTab: StateFlow<TaskTab> = _selectedTab.asStateFlow()
    
    // Состояние фильтрации и сортировки
    private val _filterSortState = MutableStateFlow(TaskFilterSortState())
    val filterSortState: StateFlow<TaskFilterSortState> = _filterSortState.asStateFlow()
    
    // Отфильтрованные задачи
    val filteredTasks: StateFlow<List<Task>> = combine(
        allTasks,
        selectedTab,
        filterSortState
    ) { tasks, tab, filterState ->
        filterTasks(tasks, tab, filterState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Результат создания задачи
    private val _createTaskResult = MutableStateFlow<CreateTaskResponse?>(null)
    val createTaskResult: StateFlow<CreateTaskResponse?> = _createTaskResult.asStateFlow()
    
    // Статус синхронизации
    val syncManager = syncManagerParam
    val syncState = syncManagerParam.syncState
    val pendingCount = syncManagerParam.pendingCount
    val failedCount = syncManagerParam.failedCount
    
    init {
        loadInitialData()
    }
    
    /**
     * Загрузить начальные данные
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Загружаем данные с сервера (если есть интернет)
                taskRepository.loadTasksFromServer()
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
                // Не критично - работаем с локальными данными
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Создать задачу
     */
    fun createTask(createTaskRequest: CreateTaskRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _createTaskResult.value = null
            
            try {
                val result = taskRepository.createTask(createTaskRequest)
                _createTaskResult.value = result
                
                if (result.success) {
                    // Успешно создано
                    if (result.id != null) {
                        // Запускаем синхронизацию, если задача создана локально
                        syncManagerParam.syncTasks()
                    }
                } else {
                    _error.value = result.error
                }
            } catch (e: Exception) {
                _error.value = "Ошибка создания задачи: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Создать задачу локально (упрощенный вариант)
     */
    fun createTaskLocal(
        name: String,
        description: String,
        executionDate: String,
        important: Boolean = false,
        executor: String? = null,
        contragentId: String? = null,
        contragentName: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                taskRepository.createTaskLocal(
                    name = name,
                    description = description,
                    executionDate = executionDate,
                    important = important,
                    executor = executor,
                    contragentId = contragentId,
                    contragentName = contragentName
                )
                
                // Запускаем синхронизацию
                syncManagerParam.syncTasks()
                
            } catch (e: Exception) {
                _error.value = "Ошибка создания задачи: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Обновить задачу
     */
    fun updateTask(request: UpdateTaskRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = taskRepository.updateTask(request)
                if (result.isSuccess) {
                    // Успешно обновлено
                    syncManagerParam.syncTasks()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Ошибка обновления задачи"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка обновления задачи: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Добавить комментарий
     */
    fun addComment(request: AddCommentRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = taskRepository.addComment(request)
                if (result.isSuccess) {
                    // Комментарий добавлен
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Ошибка добавления комментария"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка добавления комментария: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Запустить синхронизацию вручную
     */
    fun sync() {
        syncManagerParam.syncTasks()
    }
    
    /**
     * Обновить выбранную вкладку
     */
    fun selectTab(tab: TaskTab) {
        _selectedTab.value = tab
    }
    
    /**
     * Обновить состояние фильтрации
     */
    fun updateFilterSortState(newState: TaskFilterSortState) {
        _filterSortState.value = newState
    }
    
    /**
     * Сбросить ошибку
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Сбросить результат создания задачи
     */
    fun clearCreateTaskResult() {
        _createTaskResult.value = null
    }
    
    /**
     * Фильтрация задач по вкладке и состоянию
     */
    private fun filterTasks(
        tasks: List<Task>,
        tab: TaskTab,
        filterState: TaskFilterSortState
    ): List<Task> {
        var filtered = when (tab) {
            TaskTab.ALL_TASKS -> tasks
            TaskTab.MY_TASKS -> tasks.filter { it.executor == "Program" }
            TaskTab.IMPORTANT -> tasks.filter { it.important }
            TaskTab.OVERDUE -> tasks.filter { it.status == TaskStatus.OVERDUE }
            TaskTab.IN_PROGRESS -> tasks.filter { it.status == TaskStatus.IN_PROGRESS }
        }
        
        // Применяем текстовый фильтр
        filterState.searchQuery?.takeIf { it.isNotBlank() }?.let { query ->
            filtered = filtered.filter { task ->
                task.name.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true) ||
                task.contragentName?.contains(query, ignoreCase = true) == true
            }
        }
        
        // Применяем дополнительные фильтры
        if (filterState.importantOnly) {
            filtered = filtered.filter { it.important }
        }
        
        if (filterState.overdueOnly) {
            filtered = filtered.filter { it.status == TaskStatus.OVERDUE }
        }
        
        filterState.statusFilter?.takeIf { it.isNotBlank() }?.let { status ->
            filtered = filtered.filter { it.status == status }
        }
        
        filterState.executorFilter?.takeIf { it.isNotBlank() }?.let { executor ->
            filtered = filtered.filter { it.executor == executor }
        }
        
        filterState.contragentFilter?.takeIf { it.isNotBlank() }?.let { contragent ->
            filtered = filtered.filter { it.contragentName == contragent }
        }
        
        // Применяем сортировку
        filtered = when (filterState.sortBy) {
            TaskSortBy.DATE_ASC -> filtered.sortedBy { it.executionDate }
            TaskSortBy.DATE_DESC -> filtered.sortedByDescending { it.executionDate }
            TaskSortBy.NAME_ASC -> filtered.sortedBy { it.name }
            TaskSortBy.NAME_DESC -> filtered.sortedByDescending { it.name }
            TaskSortBy.IMPORTANCE -> filtered.sortedByDescending { it.important }
            TaskSortBy.STATUS -> filtered.sortedBy { it.status }
        }
        
        return filtered
    }
    
    /**
     * Получить задачу по ID
     */
    fun getTaskById(id: String): Task? {
        return allTasks.value.find { it.id == id }
    }
    
    /**
     * Получить количество ожидающих синхронизации задач
     */
    suspend fun getPendingTaskCount(): Int {
        return taskRepository.getPendingCount()
    }
    
    /**
     * Получить количество задач с ошибкой синхронизации
     */
    suspend fun getFailedTaskCount(): Int {
        return taskRepository.getFailedCount()
    }
}