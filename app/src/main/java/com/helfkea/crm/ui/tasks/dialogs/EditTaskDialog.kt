package com.helfkea.crm.ui.tasks.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helfkea.crm.model.Task
import com.helfkea.crm.model.TaskStatus
import com.helfkea.crm.model.UpdateTaskRequest
import com.helfkea.crm.model.User
import com.helfkea.crm.viewmodel.ContragentViewModel
import com.helfkea.crm.viewmodel.EditTaskViewModel
import com.helfkea.crm.viewmodel.TaskViewModel
import com.helfkea.crm.ui.common.dialogs.SelectContragentDialog
import com.helfkea.crm.ui.common.dialogs.DateTimePickerDialog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (UpdateTaskRequest) -> Unit,
    onTaskUpdated: (Task) -> Unit = {} // Новый callback для обновленной задачи
) {
    val editTaskViewModel: EditTaskViewModel = viewModel()
    val taskViewModel: TaskViewModel = viewModel()
    
    // Используем State для хранения актуальной задачи
    var displayTask by remember { mutableStateOf(task) }
    
    // Подписываемся на обновления задачи
    LaunchedEffect(task.id) {
        // Создаём поток который будет обновлять displayTask при изменениях
        snapshotFlow { taskViewModel.allTasks.value }
            .collect { allTasks ->
                val updatedTask = allTasks.find { it.id == task.id }
                if (updatedTask != null) {
                    // Сравниваем по содержимому, а не по ссылке
                    val currentJson = displayTask.toString()
                    val updatedJson = updatedTask.toString()
                    if (currentJson != updatedJson) {
                        displayTask = updatedTask
                    }
                }
            }
    }
    
    var name by remember { mutableStateOf(displayTask.name) }
    var description by remember { mutableStateOf(displayTask.description) }
    var status by remember { mutableStateOf(displayTask.status) }
    var executionDate by remember { mutableStateOf(displayTask.executionDate) }
    var important by remember { mutableStateOf(displayTask.important) }
    var executor by remember { mutableStateOf(displayTask.executor ?: "") }
    var executorId by remember { mutableStateOf("") }
    var contragentId by remember { mutableStateOf(displayTask.contragentId ?: "") }
    var contragentName by remember { mutableStateOf(displayTask.contragentName ?: "") }
    
    // Загружаем пользователей при открытии
    LaunchedEffect(Unit) {
        editTaskViewModel.loadUsers()
        
        // Находим ID пользователей по именам
        val executorUser = editTaskViewModel.findUserByName(displayTask.executor ?: "")
        executorId = executorUser?.id ?: ""
    }
    
    // Обновляем локальные состояния при изменении задачи
    LaunchedEffect(displayTask) {
        name = displayTask.name
        description = displayTask.description
        status = displayTask.status
        executionDate = displayTask.executionDate
        important = displayTask.important
        executor = displayTask.executor ?: ""
        contragentId = displayTask.contragentId ?: ""
        contragentName = displayTask.contragentName ?: ""
        
        // Обновляем executorId
        val executorUser = editTaskViewModel.findUserByName(displayTask.executor ?: "")
        executorId = executorUser?.id ?: ""
    }
    
    val users by editTaskViewModel.users.collectAsState()
    val isLoading by editTaskViewModel.isLoading.collectAsState()
    val taskIsLoading by taskViewModel.isLoading.collectAsState()
    
    val statusOptions = TaskStatus.ALL
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxHeight(0.8f) // Ограничиваем высоту диалога
            ) {
                // Заголовок
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Редактирование задачи",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Форма с прокруткой
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f) // Занимает все доступное пространство
                ) {
                    // Поле: Название задачи
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название задачи") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        singleLine = true
                    )
                    
                    // Поле: Описание
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Описание") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        maxLines = 3
                    )
                    
                    // Поле: Статус
                    var statusExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded }
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            label = { Text("Статус") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .padding(bottom = 12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            statusOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        status = option
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Поле: Дата исполнения с DateTimePicker
                    var showDateTimePicker by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        OutlinedTextField(
                            value = executionDate,
                            onValueChange = { executionDate = it },
                            label = { Text("Дата исполнения") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = { showDateTimePicker = true }
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Выбрать дату и время")
                                }
                            },
                            readOnly = true // Поле только для чтения, выбор через DateTimePicker
                        )
                    }
                    
                    // DateTimePicker диалог
                    if (showDateTimePicker) {
                        DateTimePickerDialog(
                            onDismissRequest = { showDateTimePicker = false },
                            onDateTimeSelected = { selectedDateTime ->
                                // Форматируем дату и время в формат ДД.ММ.ГГГГ ЧЧ:ММ
                                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                                executionDate = selectedDateTime.format(formatter)
                                showDateTimePicker = false
                            },
                            initialDateTime = try {
                                // Пытаемся распарсить текущую дату и время
                                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                                LocalDateTime.parse(executionDate, formatter)
                            } catch (e: DateTimeParseException) {
                                try {
                                    // Пытаемся распарсить только дату (старый формат)
                                    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                    LocalDate.parse(executionDate, dateFormatter).atTime(9, 0)
                                } catch (e2: DateTimeParseException) {
                                    // Если не удалось распарсить, используем сегодня 9:00
                                    LocalDateTime.now().withHour(9).withMinute(0)
                                }
                            }
                        )
                    }
                    
                    // Поле: Исполнитель
                    var executorExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = executorExpanded,
                        onExpandedChange = { executorExpanded = !executorExpanded }
                    ) {
                        OutlinedTextField(
                            value = executor,
                            onValueChange = {},
                            label = { Text("Исполнитель") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = executorExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .padding(bottom = 12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = executorExpanded,
                            onDismissRequest = { executorExpanded = false }
                        ) {
                            // Опция "Не назначен"
                            DropdownMenuItem(
                                text = { Text("Не назначен") },
                                onClick = {
                                    executor = ""
                                    executorId = ""
                                    executorExpanded = false
                                }
                            )
                            Divider()
                            // Список пользователей
                            if (isLoading) {
                                DropdownMenuItem(
                                    text = { Text("Загрузка...") },
                                    onClick = {}
                                )
                            } else {
                                users.forEach { user ->
                                    DropdownMenuItem(
                                        text = { Text(user.nameUser) },
                                        onClick = {
                                            executor = user.nameUser
                                            executorId = user.id
                                            executorExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Поле: Контрагент
                    var showContragentDialog by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = contragentName,
                        onValueChange = {},
                        label = { Text("Контрагент") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showContragentDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Выбрать контрагента")
                            }
                        }
                    )
                    
                    // Диалог выбора контрагента
                    if (showContragentDialog) {
                        SelectContragentDialog(
                            currentContragentId = contragentId,
                            currentContragentName = contragentName,
                            onDismiss = { showContragentDialog = false },
                            onContragentSelected = { newContragentId, newContragentName ->
                                contragentId = newContragentId
                                contragentName = newContragentName
                            }
                        )
                    }
                    
                    // Чекбокс: Важная задача
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = important,
                            onCheckedChange = { important = it }
                        )
                        Text(
                            text = "Важная задача",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                
                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Отмена")
                    }
                    
                    Button(
                        onClick = {
                            // Сохраняем оригинальную задачу ДО любых изменений
                            val originalTask = displayTask
                            
                            // Создаем обновленную задачу для callback
                            val updatedTask = displayTask.copy(
                                name = if (name != originalTask.name) name else originalTask.name,
                                description = if (description != originalTask.description) description else originalTask.description,
                                status = if (status != originalTask.status) status else originalTask.status,
                                executionDate = if (executionDate != originalTask.executionDate) executionDate else originalTask.executionDate,
                                important = if (important != originalTask.important) important else originalTask.important,
                                executor = if (executor != originalTask.executor) executor else originalTask.executor,
                                contragentId = if (contragentId != originalTask.contragentId) contragentId else originalTask.contragentId
                            )
                            
                            // Вызываем callback с обновленной задачей
                            onTaskUpdated(updatedTask)
                            
                            // Создаем запрос на обновление
                            val request = UpdateTaskRequest(
                                taskId = originalTask.id ?: "",
                                name = if (name != originalTask.name) name else null,
                                description = if (description != originalTask.description) description else null,
                                status = if (status != originalTask.status) status else null,
                                executionDate = if (executionDate != originalTask.executionDate) executionDate else null,
                                important = if (important != originalTask.important) important else null,
                                executor = if (executor != originalTask.executor) executor else null,
                                executorId = if (executorId.isNotEmpty() && executor != originalTask.executor) executorId else null,
                                contragentId = if (contragentId != originalTask.contragentId) contragentId else null
                            )
                            
                            // Логирование для отладки
                            println("DEBUG: EditTaskDialog - отправка запроса:")
                            println("DEBUG:   taskId=${request.taskId}")
                            println("DEBUG:   contragentId=${request.contragentId} (изменено: ${contragentId != originalTask.contragentId})")
                            println("DEBUG:   name=${request.name} (изменено: ${name != originalTask.name})")
                            
                            onSave(request)
                            // НЕ обновляем displayTask здесь - он обновится через snapshotFlow после ответа от сервера
                            // НЕ закрываем диалог сразу - пусть пользователь видит что сохранение прошло
                            // Диалог обновится через allTasks когда задача обновится на сервере
                        },
                        enabled = !taskIsLoading
                    ) {
                        if (taskIsLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Сохранить")
                        }
                    }
                }
            }
        }
    }
}