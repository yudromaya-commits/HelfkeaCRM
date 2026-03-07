package com.helfkea.crm.viewmodel

data class TaskFilterSortState(
    val searchQuery: String? = null,
    val statusFilter: String? = null,
    val executorFilter: String? = null,
    val producerFilter: String? = null,
    val contragentFilter: String? = null,
    val importantOnly: Boolean = false,
    val overdueOnly: Boolean = false,
    val sortBy: TaskSortBy = TaskSortBy.DATE_DESC
)