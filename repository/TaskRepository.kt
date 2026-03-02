package com.example.repressales.repository

import android.util.Log
import com.example.repressales.api.TaskApi
import com.example.repressales.model.CreateTaskRequest
import com.example.repressales.model.CreateTaskResponse
import com.example.repressales.model.Task

class TaskRepository {
    private val apiService = TaskApi.service

    suspend fun getAllTasks(): List<Task> {
        return try {
            // Пытаемся получить реальные данные из API
            val apiTasks = apiService.getTasks()
            Log.d("TaskRepository", "Получено задач из API: ${apiTasks.size}")

            // Логируем первые несколько задач для отладки
            apiTasks.take(3).forEachIndexed { index, task ->
                Log.d("TaskRepository", "Задача $index: ${task.name}, дата выполнения: ${task.executionDate}")
            }

            if (apiTasks.isNotEmpty()) {
                apiTasks // Возвращаем реальные задачи из API
            } else {
                Log.d("TaskRepository", "API вернул пустой список, используем mock данные")
                // Если API вернул пустой список, используем mock данные
                getMockTasks()
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Ошибка при получении задач: ${e.message}")
            e.printStackTrace()
            // При ошибке возвращаем mock данные
            getMockTasks()
        }
    }

    suspend fun createTask(createTaskRequest: CreateTaskRequest): CreateTaskResponse {
        return try {
            val response = apiService.createTask(createTaskRequest)
            if (response.isSuccessful) {
                response.body() ?: CreateTaskResponse(
                    success = false,
                    error = "Пустой ответ от сервера"
                )
            } else {
                // Пробуем прочитать ошибку от сервера
                val errorBody = response.errorBody()?.string()
                CreateTaskResponse(
                    success = false,
                    error = "Ошибка сервера: ${response.code()} - $errorBody"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CreateTaskResponse(
                success = false,
                error = "Ошибка сети: ${e.message}"
            )
        }
    }

    // Mock данные для демонстрации (используются только при ошибке API)
    private fun getMockTasks(): List<Task> {
        return listOf(
            Task(
                date = "23.06.2025 13:53:45",
                description = "Тестовая задача 1 - согласование договора",
                status = "Просрочена",
                producer = "Program",
                executionDate = "23.06.2025 0:00:00",
                name = "Согласование с ООО Медком",
                important = true
            ),
            Task(
                date = "18.11.2025 12:00:00",
                description = "Подготовка коммерческого предложения",
                status = "Назначена",
                producer = "Program",
                executionDate = "21.11.2025 0:00:00",
                name = "КП для ИП Иванов",
                important = false
            ),
            Task(
                date = "15.11.2024 10:00:00",
                description = "Тестовая задача на текущий месяц",
                status = "В работе",
                producer = "Program",
                executionDate = "20.11.2024 0:00:00",
                name = "Тестовая задача",
                important = false
            )
        )
    }
}