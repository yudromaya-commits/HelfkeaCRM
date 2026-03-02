package com.helfkea.crm.model

enum class TaskFilter {
    ALL_TASKS,      // Все задачи (getTasksMe)
    MY_TASKS,       // От меня (getTasksMy)
    STATUS,         // Фильтр по статусу
    EXECUTOR,       // Фильтр по исполнителю
    PRODUCER,       // Фильтр по постановщику
    IMPORTANT_ONLY  // Только важные задачи
}

enum class TaskSort {
    DATE_ASC,       // По дате создания (возрастание)
    DATE_DESC,      // По дате создания (убывание)
    EXECUTION_ASC,  // По дате исполнения (возрастание)
    EXECUTION_DESC, // По дате исполнения (убывание)
    EXECUTOR,       // По исполнителю
    PRODUCER,       // По постановщику
    IMPORTANT,      // По важности
    STATUS          // По статусу
}

data class FilterSortState(
    val currentFilter: TaskFilter = TaskFilter.ALL_TASKS,
    val selectedStatus: String? = null,
    val selectedExecutor: String? = null,
    val selectedProducer: String? = null,
    val showImportantOnly: Boolean = false,
    val currentSort: TaskSort = TaskSort.DATE_DESC,
    val searchQuery: String = ""
)