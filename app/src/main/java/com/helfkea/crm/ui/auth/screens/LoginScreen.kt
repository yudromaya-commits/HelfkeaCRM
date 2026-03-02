package com.helfkea.crm.ui.auth.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.helfkea.crm.R
import com.helfkea.crm.repository.AuthRepository
import com.helfkea.crm.ui.auth.components.AuthTextField
import com.helfkea.crm.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val viewModel = remember { AuthViewModel(authRepository) }
    
    val uiState = viewModel.uiState.collectAsState().value
    val username = viewModel.username.collectAsState().value
    val password = viewModel.password.collectAsState().value
    val rememberMe = viewModel.rememberMe.collectAsState().value
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Обработка успешной авторизации
    LaunchedEffect(uiState) {
        if (uiState is com.helfkea.crm.viewmodel.AuthUiState.Success) {
            onLoginSuccess()
        }
    }
    
    // Показ ошибок
    LaunchedEffect(uiState) {
        if (uiState is com.helfkea.crm.viewmodel.AuthUiState.Error) {
            val error = uiState.message
            scope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Левый блок - картинка
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Логотип Sales Representative",
                        modifier = Modifier
                            .fillMaxWidth(0.4f)  // В 2 раза меньше (было 0.8)
                            .fillMaxHeight(0.4f)  // В 2 раза меньше (было 0.8)
                            .padding(32.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Правый блок - форма авторизации
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(
                        topStart = 32.dp,
                        bottomStart = 32.dp
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Заголовок
                        Text(
                            text = "Sales Representative",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Авторизация",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
                        )
                        
                        // Поле логина
                        AuthTextField(
                            value = username,
                            onValueChange = viewModel::updateUsername,
                            label = "Логин",
                            placeholder = "Введите логин",
                            isError = false
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Поле пароля
                        AuthTextField(
                            value = password,
                            onValueChange = viewModel::updatePassword,
                            label = "Пароль",
                            placeholder = "Введите пароль",
                            isPassword = true,
                            isError = false
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Запомнить меня
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = viewModel::updateRememberMe
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Запомнить меня",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Кнопка входа
                        Button(
                            onClick = { viewModel.login(onLoginSuccess) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = uiState != com.helfkea.crm.viewmodel.AuthUiState.Loading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState == com.helfkea.crm.viewmodel.AuthUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text(
                                    text = "Войти",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(64.dp))
                        
                        // Информация о версии
                        Text(
                            text = "© 2025 Sales Representative",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}