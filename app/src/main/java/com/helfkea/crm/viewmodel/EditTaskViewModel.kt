package com.helfkea.crm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helfkea.crm.model.Task
import com.helfkea.crm.model.User
import com.helfkea.crm.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel для управления данными в диалоге редактирования задачи
class EditTaskViewModel : ViewModel() {
    private val repository = TaskRepository() // Создаем экземпляр репозитория
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask.asStateFlow()
    
    // Загружаем пользователей из API
    fun loadUsers() {
        if (_users.value.isNotEmpty()) return // Уже загружены
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Загружаем пользователей из реального API
                val usersList = repository.getUsers()
                // Фильтруем пользователей (как в CreateTaskViewModel)
                val filteredUsers = usersList.filter { it.nameUser != "<Не указан>" }
                _users.value = filteredUsers
            } catch (e: Exception) {
                // Ошибка загрузки - оставляем пустой список
                _users.value = emptyList()
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Находим пользователя по имени
    fun findUserByName(name: String): User? {
        return _users.value.find { it.nameUser == name }
    }
    
    // Установить текущую задачу для редактирования
    fun setCurrentTask(task: Task) {
        _currentTask.value = task
    }
    
    // Обновить текущую задачу (например, после сохранения)
    fun updateCurrentTask(updatedTask: Task) {
        _currentTask.value = updatedTask
    }
}