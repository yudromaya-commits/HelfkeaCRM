package com.example.repressales.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repressales.model.CreateTaskRequest
import com.example.repressales.viewmodel.TaskViewModel

// Функции для форматирования дат
private fun String.toReadableDate(): String {
    return try {
        val parts = this.split(" ")[0].split(".")
        "${parts[0]}.${parts[1]}.${parts[2]}"
    } catch (e: Exception) {
        this
    }
}

private fun String.toReadableDateTime(): String {
    return try {
        val dateTimeParts = this.split(" ")
        val date = dateTimeParts[0].toReadableDate()
        val time = dateTimeParts.getOrNull(1) ?: ""
        "$date ${time.take(5)}"
    } catch (e: Exception) {
        this
    }
}

@Composable
fun TaskView(
    modifier: Modifier = Modifier,
    onTaskClick: (com.example.repressales.model.Task) -> Unit = {}
) {
    val viewModel: TaskViewModel = viewModel()
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val createTaskResult by viewModel.createTaskResult.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    // Загружаем задачи при первом открытии
    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    // Показываем Snackbar при успешном создании задачи
    if (createTaskResult?.success == true) {
        LaunchedEffect(createTaskResult) {
            // Автоматически скрываем результат через 3 секунды
            // viewModel.clearCreateTaskResult() // Можно раскомментировать для авто-скрытия
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Заголовок и кнопки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Задачи",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(
                    onClick = { viewModel.refreshTasks() },
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        tint = if (isLoading) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = { showCreateDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить")
                }
            }
        }

        // Сообщение о результате создания
        createTaskResult?.let { result ->
            AlertDialog(
                onDismissRequest = { viewModel.clearCreateTaskResult() },
                title = {
                    Text(if (result.success) "Успех!" else "Ошибка")
                },
                text = {
                    Text(result.message ?: result.error ?: "")
                },
                confirmButton = {
                    Button(onClick = { viewModel.clearCreateTaskResult() }) {
                        Text("OK")
                    }
                }
            )
        }

        // Состояние загрузки
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Ошибка
        error?.let { errorMessage ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.refreshTasks() }) {
                    Text("Повторить")
                }
            }
        }

        // Список задач
        if (tasks.isEmpty() && !isLoading && error == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Задачи не найдены",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showCreateDialog = true }) {
                        Text("Создать первую задачу")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onTaskClick(task) }
                    )
                }
            }
        }
    }

    // Диалог создания задачи
    if (showCreateDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateDialog = false },
            onCreateTask = { request ->
                viewModel.createTask(request)
                showCreateDialog = false
            },
            isLoading = isLoading
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class) // ДОБАВЛЕНО: для Card с onClick
@Composable
fun TaskCard(
    task: com.example.repressales.model.Task,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Статус и приоритет
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Статус с цветом
                val statusColor = when (task.status) {
                    "Просрочена" -> Color.Red
                    "Назначена" -> Color.Blue
                    "Выполнена" -> Color.Green
                    "В работе" -> Color(0xFFFF9800)
                    else -> Color.Gray
                }

                Text(
                    text = task.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )

                if (task.important) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Важная задача",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Название задачи
            Text(
                text = task.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            // Описание задачи
            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Информация о датах
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Создана:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        task.date.toReadableDateTime(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Выполнить до:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        task.executionDate.toReadableDate(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Производитель
            if (task.producer.isNotEmpty()) {
                Text(
                    text = "Исполнитель: ${task.producer}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}