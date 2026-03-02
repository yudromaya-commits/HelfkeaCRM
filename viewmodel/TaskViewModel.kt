package com.example.repressales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repressales.model.CreateTaskRequest
import com.example.repressales.model.CreateTaskResponse
import com.example.repressales.model.Task
import com.example.repressales.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    private val repository = TaskRepository()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _createTaskResult = MutableStateFlow<CreateTaskResponse?>(null)
    val createTaskResult: StateFlow<CreateTaskResponse?> = _createTaskResult.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                _tasks.value = repository.getAllTasks()
            } catch (e: Exception) {
                _error.value = "Нет подключения к интернету. Показаны тестовые данные."
            } finally {
                _isLoading.value = false
            }
        }
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
                    loadTasks()
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
        loadTasks()
    }
}