package com.helfkea.crm.repository

import android.util.Log
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.model.CreateTaskRequest
import com.helfkea.crm.model.CreateTaskResponse
import com.helfkea.crm.model.Task
import com.helfkea.crm.model.TaskComment
import com.helfkea.crm.model.ContragentInTask  // ★ ДОБАВИТЬ ЭТОТ ИМПОРТ
import com.helfkea.crm.model.TaskAttachment
import com.helfkea.crm.model.User
import com.helfkea.crm.model.UpdateTaskRequest
import com.helfkea.crm.model.AddCommentRequest
import com.helfkea.crm.model.TaskStatus
import retrofit2.http.Url

class TaskRepository {
    private val apiService = TaskApi.service

    suspend fun getTasksMe(): List<Task> {
        return try {
            val apiTasks = apiService.getTasksMe()
            Log.d("TaskRepository", "Получено задач мне: ${apiTasks.size}")

            // Логируем первую задачу с комментариями для отладки
            if (apiTasks.isNotEmpty()) {
                val firstTask = apiTasks.first()
                Log.d("TaskRepository", "Первая задача: ${firstTask.name}, id: ${firstTask.id}, комментариев: ${firstTask.comment?.size ?: 0}")
                if (firstTask.comment?.isNotEmpty() == true) {
                    Log.d("TaskRepository", "Первый комментарий: ${firstTask.comment.first().text}")
                }
            }

            apiTasks
        } catch (e: Exception) {
            Log.e("TaskRepository", "Ошибка при получении задач мне: ${e.message}")
            getMockTasks()
        }
    }

    // ДОБАВЛЯЕМ НОВЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ КОНТРАГЕНТОВ
    suspend fun getContragentsForTaskSelection(): List<ContragentInTask> {
        return try {
            // Получаем контрагентов из API
            val contragents = apiService.getAllContragents()
            Log.d("TaskRepository", "Получено контрагентов: ${contragents.size}")

            // Преобразуем в нужный формат
            contragents.map { contragent ->
                ContragentInTask(
                    id = contragent.id,
                    name = contragent.name
                )
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Ошибка при получении контрагентов: ${e.message}")
            emptyList()
        }
    }




    suspend fun getTasksMy(): List<Task> {
        return try {
            val apiTasks = apiService.getTasksMy()
            Log.d("TaskRepository", "Получено моих задач: ${apiTasks.size}")
            apiTasks
        } catch (e: Exception) {
            Log.e("TaskRepository", "Ошибка при получении моих задач: ${e.message}")
            getMockTasks().filter { it.executor == "Program" }
        }
    }

    suspend fun getContragentsForTask(): List<ContragentInTask> {
        return try {
            // Получаем контрагентов из API
            val contragents = apiService.getAllContragents()
            contragents.map { contragent ->
                ContragentInTask(
                    id = contragent.id,
                    name = contragent.name
                )
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Ошибка при получении контрагентов: ${e.message}")
            emptyList()
        }
    }
    // Используем существующий метод:
    suspend fun createTask(createTaskRequest: CreateTaskRequest): CreateTaskResponse {
        return try {
            println("DEBUG: TaskRepository.createTask() вызывается")
            println("DEBUG: Запрос: name='${createTaskRequest.name}', contragentId='${createTaskRequest.contragentId}'")
            
            val response = apiService.createTask(createTaskRequest)
            
            println("DEBUG: Ответ от API: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                println("DEBUG: Тело ответа: success=${body?.success}, id=${body?.id}, error=${body?.error}")
                body ?: CreateTaskResponse(
                    success = false,
                    error = "Пустой ответ от сервера"
                )
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG: Ошибка от API: ${response.code()} - $errorBody")
                CreateTaskResponse(
                    success = false,
                    error = "Ошибка сервера: ${response.code()} - $errorBody"
                )
            }
        } catch (e: Exception) {
            println("DEBUG: Исключение в createTask: ${e.message}")
            e.printStackTrace()
            CreateTaskResponse(
                success = false,
                error = "Ошибка сети: ${e.message}"
            )
        }
    }

    suspend fun getUsers(): List<User> {
        return apiService.getUsers()
    }
    // Обновляем mock данные с комментариями
    private fun getMockTasks(): List<Task> {
        return listOf(
            Task(
                id = "1",
                date = "23.06.2025 13:53:45",
                description = "Описание тестовой задачи 1",
                status = TaskStatus.OVERDUE,
                producer = "Program",
                executionDate = "23.06.2025 0:00:00",
                name = "Согласование с ООО Медком",
                important = true,
                executor = "Program",
                contragent = ContragentInTask(
                    id = "123",
                    name = "ООО Медком"
                ),
                attachments = listOf(),
                comment = listOf()

            ),
            Task(
                id = "2",
                date = "18.11.2025 12:00:00",
                description = "Описание тестовой задачи 2",
                status = TaskStatus.ASSIGNED,
                producer = "Manager1",
                executionDate = "21.11.2025 0:00:00",
                name = "КП для ИП Иванов",
                important = false,
                contragent = ContragentInTask(
                    id = "123",
                    name = "ООО Медком"
                ),
                attachments = listOf(),
                comment = listOf()

            ),
            Task(
                date = "24.11.2025 15:59:31",
                description = "Описание важной задачи",
                status = TaskStatus.IN_PROGRESS,
                producer = "менеджер Краснодар",
                executionDate = "18.01.2026 0:00:00",
                name = "МП для ТП",
                important = true,
                executor = "Program",
                contragent = ContragentInTask(
                    id = "123",
                    name = "ООО Медком"
                ),
                attachments = listOf(),
                comment = listOf()

            )
        )
    }

    // ДОБАВЛЯЕМ НОВЫЙ МЕТОД ДЛЯ РЕДАКТИРОВАНИЯ ЗАДАЧ
    suspend fun updateTask(request: UpdateTaskRequest): Result<CreateTaskResponse> {
        return try {
            val response = apiService.updateTask(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка обновления задачи: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Ошибка при обновлении задачи: ${e.message}")
            Result.failure(e)
        }
    }

    // ДОБАВЛЯЕМ НОВЫЙ МЕТОД ДЛЯ ДОБАВЛЕНИЯ КОММЕНТАРИЯ
    suspend fun addComment(request: AddCommentRequest): Result<CreateTaskResponse> {
        return try {
            val response = apiService.addComment(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка добавления комментария: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Ошибка при добавлении комментария: ${e.message}")
            Result.failure(e)
        }
    }
}