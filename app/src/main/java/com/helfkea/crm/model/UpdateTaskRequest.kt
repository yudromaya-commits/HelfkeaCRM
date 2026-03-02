package com.helfkea.crm.model

// UpdateTaskRequest - модель для частичного обновления задачи
// Реализовано для задачи 1.1: Редактирование задач
// Отправляются только измененные поля

data class UpdateTaskRequest(
    val taskId: String,  // ИЗМЕНЕНО: id → taskId для совместимости с 1C
    val date: String? = null,
    val description: String? = null,
    val status: String? = null,
    val producer: String? = null,
    val producerId: String? = null,  // ID постановщика
    val executionDate: String? = null,
    val name: String? = null,
    val important: Boolean? = null,
    val executor: String? = null,
    val executorId: String? = null,  // ID исполнителя
    val contragentId: String? = null
)