// TaskViewModel.kt
package com.helfkea.crm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helfkea.crm.model.*
import com.helfkea.crm.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

enum class TaskTab {
    ALL_TASKS,      // Все задачи
    MY_TASKS,       // От меня
    IMPORTANT,      // Важные
    OVERDUE,        // Просроченные
    IN_PROGRESS     // В работе
}

// ИЗМЕНЕНИЕ: Добавляем конструктор с параметром
class TaskViewModel(
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {
    // ... весь остальной код остается без изменений ...

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    private val _myTasks = MutableStateFlow<List<Task>>(emptyList())

    // Добавляем публичные геттеры
    val allTasks: StateFlow<List<Task>> get() = _allTasks.asStateFlow()
    val myTasks: StateFlow<List<Task>> get() = _myTasks.asStateFlow()

    private val _selectedTab = MutableStateFlow(TaskTab.ALL_TASKS)
    val selectedTab: StateFlow<TaskTab> = _selectedTab.asStateFlow()

    private val _filterSortState = MutableStateFlow(FilterSortState())
    val filterSortState: StateFlow<FilterSortState> = _filterSortState.asStateFlow()

    private val _filteredTasks = MutableStateFlow<List<Task>>(emptyList())
    val filteredTasks: StateFlow<List<Task>> = _filteredTasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _createTaskResult = MutableStateFlow<CreateTaskResponse?>(null)
    val createTaskResult: StateFlow<CreateTaskResponse?> = _createTaskResult.asStateFlow()

    // ДОБАВЛЯЕМ StateFlow для результата редактирования задачи
    private val _updateTaskResult = MutableStateFlow<CreateTaskResponse?>(null)
    val updateTaskResult: StateFlow<CreateTaskResponse?> = _updateTaskResult.asStateFlow()

    // ДОБАВЛЯЕМ StateFlow для результата добавления комментария
    private val _addCommentResult = MutableStateFlow<CreateTaskResponse?>(null)
    val addCommentResult: StateFlow<CreateTaskResponse?> = _addCommentResult.asStateFlow()

    // Статистика для фильтров
    private val _availableStatuses = MutableStateFlow<Set<String>>(emptySet())
    val availableStatuses: StateFlow<Set<String>> = _availableStatuses.asStateFlow()

    private val _availableExecutors = MutableStateFlow<Set<String>>(emptySet())
    val availableExecutors: StateFlow<Set<String>> = _availableExecutors.asStateFlow()

    private val _availableProducers = MutableStateFlow<Set<String>>(emptySet())
    val availableProducers: StateFlow<Set<String>> = _availableProducers.asStateFlow()

    init {
        loadAllTasks()
    }

    fun loadAllTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Загружаем обе категории задач
                val tasksMe = repository.getTasksMe()
                val tasksMy = repository.getTasksMy()
                
                _allTasks.value = tasksMe
                _myTasks.value = tasksMy

                // Извлекаем уникальные значения для фильтров
                updateFilterStats()
                applyFiltersAndSort()
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки задач: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateFilterStats() {
        val allTasks = _allTasks.value
        val myTasks = _myTasks.value
        val combinedTasks = allTasks + myTasks

        // Собираем уникальные статусы
        _availableStatuses.value = combinedTasks
            .map { it.status }
            .filter { it.isNotBlank() }
            .toSet()

        // Собираем уникальных исполнителей
        _availableExecutors.value = combinedTasks
            .mapNotNull { it.executor }
            .filter { it.isNotBlank() }
            .toSet()

        // Собираем уникальных постановщиков
        _availableProducers.value = combinedTasks
            .map { it.producer }
            .filter { it.isNotBlank() }
            .toSet()
    }

    // НОВЫЙ МЕТОД: смена вкладки
    fun selectTab(tab: TaskTab) {
        _selectedTab.value = tab
        applyTabFilter()
        applyFiltersAndSort()
    }

    private fun applyTabFilter() {
        when (_selectedTab.value) {
            TaskTab.ALL_TASKS -> {
                _filterSortState.value = _filterSortState.value.copy(
                    currentFilter = TaskFilter.ALL_TASKS,
                    showImportantOnly = false,
                    selectedStatus = null
                )
            }
            TaskTab.MY_TASKS -> {
                _filterSortState.value = _filterSortState.value.copy(
                    currentFilter = TaskFilter.MY_TASKS,
                    showImportantOnly = false,
                    selectedStatus = null
                )
            }
            TaskTab.IMPORTANT -> {
                _filterSortState.value = _filterSortState.value.copy(
                    currentFilter = TaskFilter.IMPORTANT_ONLY,
                    showImportantOnly = true,
                    selectedStatus = null
                )
            }
            TaskTab.OVERDUE -> {
                // Добавим фильтр по просроченным задачам
                _filterSortState.value = _filterSortState.value.copy(
                    selectedStatus = "Просрочена",
                    currentFilter = TaskFilter.STATUS,
                    showImportantOnly = false
                )
            }
            TaskTab.IN_PROGRESS -> {
                // Добавим фильтр по задачам в работе
                _filterSortState.value = _filterSortState.value.copy(
                    selectedStatus = "В работе",
                    currentFilter = TaskFilter.STATUS,
                    showImportantOnly = false
                )
            }
        }
    }

    fun updateStatusFilter(status: String?) {
        _filterSortState.value = _filterSortState.value.copy(
            selectedStatus = status,
            currentFilter = if (status != null) TaskFilter.STATUS else TaskFilter.ALL_TASKS
        )
        applyFiltersAndSort()
    }

    fun updateExecutorFilter(executor: String?) {
        _filterSortState.value = _filterSortState.value.copy(
            selectedExecutor = executor,
            currentFilter = if (executor != null) TaskFilter.EXECUTOR else TaskFilter.ALL_TASKS
        )
        applyFiltersAndSort()
    }

    fun updateProducerFilter(producer: String?) {
        _filterSortState.value = _filterSortState.value.copy(
            selectedProducer = producer,
            currentFilter = if (producer != null) TaskFilter.PRODUCER else TaskFilter.ALL_TASKS
        )
        applyFiltersAndSort()
    }

    fun updateSort(sort: TaskSort) {
        _filterSortState.value = _filterSortState.value.copy(currentSort = sort)
        applyFiltersAndSort()
    }

    fun updateSearchQuery(query: String) {
        _filterSortState.value = _filterSortState.value.copy(searchQuery = query)
        applyFiltersAndSort()
    }

    fun clearAllFilters() {
        _filterSortState.value = FilterSortState()
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val state = _filterSortState.value

        // Выбираем базовый список в зависимости от фильтра
        var tasks = when (state.currentFilter) {
            TaskFilter.ALL_TASKS -> _allTasks.value
            TaskFilter.MY_TASKS -> _myTasks.value
            else -> _allTasks.value
        }

        // Применяем фильтры
        tasks = tasks.filter { task ->
            var matches = true

            // Фильтр по статусу
            if (state.selectedStatus != null && state.selectedStatus != task.status) {
                matches = false
            }

            // Фильтр по исполнителю
            if (state.selectedExecutor != null && task.executor != state.selectedExecutor) {
                matches = false
            }

            // Фильтр по постановщику
            if (state.selectedProducer != null && task.producer != state.selectedProducer) {
                matches = false
            }

            // Фильтр по важности
            if (state.showImportantOnly && !task.important) {
                matches = false
            }

            // Поиск по всем текстовым полям
            if (state.searchQuery.isNotBlank()) {
                val query = state.searchQuery.lowercase()
                matches = matches && (
                        task.name.lowercase().contains(query) ||
                                task.description.lowercase().contains(query) ||
                                task.status.lowercase().contains(query) ||
                                task.producer.lowercase().contains(query) ||
                                (task.executor?.lowercase()?.contains(query) ?: false)
                        )
            }

            matches
        }

        // Применяем сортировку
        tasks = when (state.currentSort) {
            TaskSort.DATE_ASC -> tasks.sortedBy { it.date }
            TaskSort.DATE_DESC -> tasks.sortedByDescending { it.date }
            TaskSort.EXECUTION_ASC -> tasks.sortedBy { it.executionDate }
            TaskSort.EXECUTION_DESC -> tasks.sortedByDescending { it.executionDate }
            TaskSort.EXECUTOR -> tasks.sortedBy { it.executor ?: "" }
            TaskSort.PRODUCER -> tasks.sortedBy { it.producer }
            TaskSort.IMPORTANT -> tasks.sortedByDescending { it.important }
            TaskSort.STATUS -> tasks.sortedBy { it.status }
        }

        _filteredTasks.value = tasks
    }

    fun createTask(createTaskRequest: CreateTaskRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _createTaskResult.value = null

            try {
                val result = repository.createTask(createTaskRequest)
                _createTaskResult.value = result

                if (result.success) {
                    // Обновляем список задач после успешного создания
                    loadAllTasks()
                } else {
                    _error.value = result.error ?: "Неизвестная ошибка"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка при создании задачи: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCreateTaskResult() {
        _createTaskResult.value = null
    }

    fun refreshTasks() {
        loadAllTasks()
    }

    // ========== КАНБАН-ДОСКА ==========

    /**
     * Получает задачи для канбан-доски (группировка по статусам)
     */
    val tasks: StateFlow<List<Task>> get() = _allTasks.asStateFlow()

    // ДОБАВЛЯЕМ НОВЫЙ МЕТОД ДЛЯ РЕДАКТИРОВАНИЯ ЗАДАЧ
    fun updateTask(request: UpdateTaskRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _updateTaskResult.value = null

            // ПЕРВОЕ: Проверяем, загружены ли задачи. Если нет - загружаем.
            if (_allTasks.value.isEmpty() && _myTasks.value.isEmpty()) {
                loadAllTasks()
                // Ждем завершения загрузки
                delay(500)
            }

            val result = repository.updateTask(request)
            result.onSuccess { response ->
                _updateTaskResult.value = response
                
                // ВРЕМЕННОЕ РЕШЕНИЕ: Обновляем задачу локально, не перезагружая весь список
                // Потому что refreshTasks() возвращает пустые списки
                if (response.success) {
                    // Ищем задачу во всех возможных списках
                    var foundTask: Task? = null
                    var listName = ""
                    var allTasksIndex: Int? = null
                    var myTasksIndex: Int? = null
                    
                    // Ищем в allTasks
                    allTasksIndex = _allTasks.value.indexOfFirst { it.id == request.taskId }
                    if (allTasksIndex != -1) {
                        foundTask = _allTasks.value[allTasksIndex]
                        listName = "allTasks"
                    }
                    
                    // Если не нашли в allTasks, ищем в myTasks
                    if (foundTask == null) {
                        myTasksIndex = _myTasks.value.indexOfFirst { it.id == request.taskId }
                        if (myTasksIndex != -1) {
                            foundTask = _myTasks.value[myTasksIndex]
                            listName = "myTasks"
                        }
                    }
                    
                    if (foundTask != null) {
                        // Создаем обновленную задачу
                        val updatedTask = foundTask.copy(
                            name = request.name ?: foundTask.name,
                            description = request.description ?: foundTask.description,
                            status = request.status ?: foundTask.status,
                            executionDate = request.executionDate ?: foundTask.executionDate,
                            important = request.important ?: foundTask.important,
                            executor = request.executor ?: foundTask.executor,
                            contragentId = request.contragentId ?: foundTask.contragentId
                        )
                        
                        // Обновляем в соответствующем списке
                        when (listName) {
                            "allTasks" -> {
                                val updatedList = _allTasks.value.toMutableList()
                                updatedList[allTasksIndex!!] = updatedTask
                                _allTasks.value = updatedList
                            }
                            "myTasks" -> {
                                val updatedList = _myTasks.value.toMutableList()
                                updatedList[myTasksIndex!!] = updatedTask
                                _myTasks.value = updatedList
                            }
                        }
                    }
                }
                
                // TODO: Восстановить refreshTasks() когда API будет корректно возвращать задачи
                // refreshTasks()
            }.onFailure { exception ->
                _error.value = "Ошибка редактирования задачи: ${exception.message}"
            }

            _isLoading.value = false
        }
    }

    // ДОБАВЛЯЕМ НОВЫЙ МЕТОД ДЛЯ ДОБАВЛЕНИЯ КОММЕНТАРИЯ
    fun addComment(taskId: String, text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _addCommentResult.value = null

            // ЛОКАЛЬНОЕ ОБНОВЛЕНИЕ: сразу добавляем комментарий к задаче в UI
            val currentTasks = _allTasks.value.toMutableList()
            val taskIndex = currentTasks.indexOfFirst { it.id == taskId }
            if (taskIndex != -1) {
                val task = currentTasks[taskIndex]
                val newComment = TaskComment(
                    date = java.time.LocalDateTime.now().toString(),
                    user = "Текущий пользователь", // TODO: получить из настроек
                    text = text
                )
                // Создаем новую задачу с обновленными комментариями (без copy())
                val updatedTask = Task(
                    id = task.id,
                    date = task.date,
                    description = task.description,
                    status = task.status,
                    producer = task.producer,
                    executionDate = task.executionDate,
                    name = task.name,
                    important = task.important,
                    executor = task.executor,
                    contragent = task.contragent,
                    attachments = task.attachments,
                    comment = (task.comment ?: emptyList()) + newComment
                )
                currentTasks[taskIndex] = updatedTask
                _allTasks.value = currentTasks
            }

            val request = AddCommentRequest(
                taskId = taskId,
                text = text
                // userId можно добавить позже из настроек
            )

            val result = repository.addComment(request)
            result.onSuccess { response ->
                _addCommentResult.value = response
                // Обновляем задачи после успешного добавления комментария
                refreshTasks()
            }.onFailure { exception ->
                _error.value = "Ошибка добавления комментария: ${exception.message}"
            }

            _isLoading.value = false
        }
    }

    // Метод для получения задачи по ID (для обновления selectedTask)
    fun getTaskById(taskId: String): Task? {
        return _allTasks.value.find { it.id == taskId }
    }

    // Метод для сброса результата добавления комментария (опционально)
    fun resetAddCommentResult() {
        _addCommentResult.value = null
    }

    // Метод для сброса результата обновления задачи
    fun resetUpdateTaskResult() {
        _updateTaskResult.value = null
    }
}