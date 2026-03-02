package com.helfkea.crm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.data.AuthCredentials
import com.helfkea.crm.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // Состояние UI
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    // Данные формы
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()
    
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    private val _rememberMe = MutableStateFlow(true)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()
    
    // Обновление данных формы
    fun updateUsername(value: String) {
        _username.value = value
    }
    
    fun updatePassword(value: String) {
        _password.value = value
    }
    
    fun updateRememberMe(value: Boolean) {
        _rememberMe.value = value
    }
    
    // Авторизация
    fun login(onSuccess: () -> Unit) {
        if (_username.value.isEmpty() || _password.value.isEmpty()) {
            _uiState.value = AuthUiState.Error("Заполните все поля")
            return
        }
        
        _uiState.value = AuthUiState.Loading
        
        viewModelScope.launch {
            try {
                // Устанавливаем учетные данные для этого запроса
                TaskApi.setCredentials(_username.value, _password.value)
                
                // Реальная проверка с сервером 1С
                val response = TaskApi.service.auth()
                
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    
                    if (authResponse?.auth == true) {
                        // Сохраняем учетные данные
                        val credentials = AuthCredentials(
                            username = _username.value,
                            password = _password.value,
                            rememberMe = _rememberMe.value
                        )
                        
                        if (_rememberMe.value) {
                            authRepository.saveCredentials(credentials)
                            authRepository.setLoggedIn(true)
                        } else {
                            // Для одноразового входа не сохраняем, но отмечаем как авторизованного
                            authRepository.setLoggedIn(true)
                        }
                        
                        _uiState.value = AuthUiState.Success
                        onSuccess()
                    } else {
                        _uiState.value = AuthUiState.Error("Неверный логин или пароль")
                    }
                } else {
                    _uiState.value = AuthUiState.Error("Ошибка сервера: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Ошибка сети: ${e.message}")
            }
        }
    }
    
    // Проверка авторизации
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
    
    // Получение текущих учетных данных
    fun getCurrentCredentials(): AuthCredentials? {
        return authRepository.getCurrentCredentials()
    }
    
    // Выход
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _username.value = ""
            _password.value = ""
            _uiState.value = AuthUiState.Idle
        }
    }
}

// Состояния UI для авторизации
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}