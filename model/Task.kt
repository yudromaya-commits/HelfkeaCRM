package com.example.repressales.model

import com.google.gson.annotations.SerializedName

data class Task(
    val date: String,
    val description: String,
    val status: String,
    val producer: String,
    val executionDate: String,
    val name: String,
    val important: Boolean
)

// Модель для создания задачи
data class CreateTaskRequest(
    val name: String,
    val description: String,
    val status: String? = null,
    val producer: String? = null,
    val executionDate: String? = null,
    val important: Boolean? = null,
    val comments: List<Comment>? = null
)

data class Comment(
    val comment: String
)

// Модель для ответа при создании задачи
data class CreateTaskResponse(
    val success: Boolean,
    val id: String? = null,
    val message: String? = null,
    val task: Task? = null,
    val error: String? = null
)