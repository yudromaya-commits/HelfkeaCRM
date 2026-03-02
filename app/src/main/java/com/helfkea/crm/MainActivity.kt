package com.helfkea.crm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.helfkea.crm.model.Task
import com.helfkea.crm.model.Contragent
import com.helfkea.crm.model.Individual
import com.helfkea.crm.model.UpdateTaskRequest
import com.helfkea.crm.model.TaskStatus
import com.helfkea.crm.model.PreselectedContragent
import com.helfkea.crm.ui.theme.RepressaSalesTheme
import com.helfkea.crm.ui.tasks.screens.TaskView
import com.helfkea.crm.ui.tasks.screens.CreateTaskPanel
import com.helfkea.crm.ui.tasks.dialogs.EditTaskDialog
import com.helfkea.crm.ui.calendar.screens.CalendarWithKanbanView
import com.helfkea.crm.ui.individuals.screens.IndividualView
import com.helfkea.crm.ui.individuals.screens.IndividualDetailScreen
import com.helfkea.crm.ui.contragents.screens.ContragentView
import com.helfkea.crm.ui.contragents.screens.ContragentDetailScreen
import com.helfkea.crm.ui.contragents.dialogs.SnapContragentDialog
import com.helfkea.crm.ui.deals.screens.DealTabsView
import com.helfkea.crm.ui.navigation.screens.DashboardView
import com.helfkea.crm.ui.auth.screens.LoginScreen
import com.helfkea.crm.viewmodel.AuthViewModel
import com.helfkea.crm.repository.AuthRepository
import com.helfkea.crm.api.TaskApi
import androidx.compose.ui.platform.LocalContext
import com.helfkea.crm.ui.navigation.components.NavigationPanel
import com.helfkea.crm.ui.common.screens.VersionHistoryScreen
import com.helfkea.crm.ui.tasks.screens.TaskDetailPanel
import com.helfkea.crm.viewmodel.TaskViewModel
import com.helfkea.crm.viewmodel.CreateTaskViewModel
import com.helfkea.crm.viewmodel.TaskViewModelFactory
import com.helfkea.crm.viewmodel.CreateTaskViewModelFactory
import com.helfkea.crm.viewmodel.ContragentViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RepressaSalesTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    // Состояние авторизации
    var isLoggedIn by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val authViewModel = remember { AuthViewModel(authRepository) }
    
    // Проверка авторизации при запуске
    LaunchedEffect(Unit) {
        val loggedIn = authViewModel.isLoggedIn()
        isLoggedIn = loggedIn
        
        if (loggedIn) {
            // Устанавливаем учетные данные в TaskApi
            val credentials = authViewModel.getCurrentCredentials()
            credentials?.let {
                TaskApi.setCredentials(it.username, it.password)
            }
        }
    }
    
    // Если не авторизован - показываем экран входа
    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = {
                isLoggedIn = true
                // Устанавливаем учетные данные после успешного входа
                val credentials = authViewModel.getCurrentCredentials()
                credentials?.let {
                    TaskApi.setCredentials(it.username, it.password)
                }
            }
        )
        return
    }
    
    var currentSection by remember { mutableStateOf("Задачи") }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var selectedContragent by remember { mutableStateOf<Contragent?>(null) }
    var showCreateTaskPanel by remember { mutableStateOf(false) }
    var taskForContragent by remember { mutableStateOf<PreselectedContragent?>(null) }
    var selectedIndividual by remember { mutableStateOf<Individual?>(null) }
    var pendingContragentId by remember { mutableStateOf<String?>(null) }
    var showSnapContragentDialog by remember { mutableStateOf(false) }
    var showEditTaskDialog by remember { mutableStateOf(false) }  // ДОБАВЛЯЕМ для редактирования задач
    var showVersionHistory by remember { mutableStateOf(false) }  // Экран истории версий

    // ★ ИСПРАВЛЕНО: сначала объявляем ViewModel
    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory()
    )

    val createTaskViewModel: CreateTaskViewModel = viewModel(
        factory = CreateTaskViewModelFactory()
    )
    
    val contragentViewModel: ContragentViewModel = viewModel()
    
    // НЕ ИСПОЛЬЗУЕМ ЭТОТ ПОДХОД - вызывает бесконечный цикл и ANR
    // Вместо этого будем обновлять selectedTask только когда это действительно нужно

    val isLoading by taskViewModel.isLoading.collectAsState()
    val taskCreated by createTaskViewModel.taskCreated.collectAsState()
    val addCommentResult by taskViewModel.addCommentResult.collectAsState()
    val updateTaskResult by taskViewModel.updateTaskResult.collectAsState()
    val contragents by contragentViewModel.contragents.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            // Основной интерфейс с навигацией (всегда показываем)
            Row(Modifier.fillMaxSize()) {
                NavigationPanel(
                    onSectionSelected = { selectedSection ->
                        currentSection = selectedSection
                    },
                    onLogoutClick = {
                        // Выход из системы
                        authViewModel.logout()
                        isLoggedIn = false
                        TaskApi.clearCredentials()
                    },
                    onVersionClick = {
                        showVersionHistory = true
                    }
                )

                if (selectedContragent != null) {
                    // Показываем экран детализации контрагента
                    ContragentDetailScreen(
                        contragent = selectedContragent!!,
                        onBackClick = { selectedContragent = null },
                        onCreateTaskClick = {
                            taskForContragent = PreselectedContragent(
                                name = selectedContragent!!.name,
                                id = selectedContragent!!.id ?: ""
                            )
                            showCreateTaskPanel = true
                        },
                        onCreateInteraction = {
                            // TODO: Открыть диалог создания взаимодействия
                        }
                    )
                } else if (selectedIndividual != null) {
                    // Показываем экран детализации физлица
                    IndividualDetailScreen(
                        individual = selectedIndividual!!,
                        onBackClick = { selectedIndividual = null },
                        onCreateTaskClick = {
                            taskForContragent = PreselectedContragent(
                                name = selectedIndividual!!.name,
                                id = selectedIndividual!!.id ?: ""
                            )
                            showCreateTaskPanel = true
                        },
                        onCreateInteraction = {
                            // TODO: Открыть диалог создания взаимодействия
                        },
                        onSnapContragentClick = {
                            showSnapContragentDialog = true
                        }
                    )
                } else {
                    // Показываем соответствующий раздел
                    when (currentSection) {
                        "Задачи" -> TaskView(
                            modifier = Modifier.weight(1f),
                            onTaskClick = { task -> selectedTask = task },
                            onCreateTaskClick = {
                                taskForContragent = null // Сбрасываем, если создаем общую задачу
                                showCreateTaskPanel = true
                            }
                        )
                        "Календарь" -> CalendarWithKanbanView(
                            modifier = Modifier.weight(1f),
                            onTaskClick = { task -> selectedTask = task }
                        )
                        "Физлица" -> IndividualView(
                            modifier = Modifier.weight(1f),
                            onIndividualClick = { individual ->
                                selectedIndividual = individual
                            },
                            onCreateTaskClick = { individual ->
                                taskForContragent = PreselectedContragent(
                                    name = individual.name,
                                    id = individual.id ?: ""
                                )
                                showCreateTaskPanel = true
                            }
                        )
                        "Юрлица" -> ContragentView(
                            modifier = Modifier.weight(1f),
                            onContragentClick = { contragent ->
                                selectedContragent = contragent
                            },
                            onCreateTaskClick = { contragent ->
                                taskForContragent = PreselectedContragent(
                                    name = contragent.name,
                                    id = contragent.id ?: ""
                                )
                                showCreateTaskPanel = true
                            }
                        )
                        "Сделки" -> DealTabsView(
                            modifier = Modifier.weight(1f)
                        )
                        else -> DashboardView(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Панель деталей задачи (поверх всего интерфейса)
        TaskDetailPanel(
            task = selectedTask,
            onClose = { selectedTask = null },
            onAddComment = { commentText ->
                // Добавляем комментарий через ViewModel
                selectedTask?.id?.let { taskId ->
                    taskViewModel.addComment(taskId, commentText)
                }
            },
            onMarkComplete = {
                println("DEBUG: Кнопка 'Отметить задачу как выполненную' нажата")
                // Отметить задачу как выполненную
                selectedTask?.let { task ->
                    println("DEBUG: Обновление задачи ${task.id} на статус COMPLETED")
                    val request = UpdateTaskRequest(
                        taskId = task.id ?: "",
                        status = TaskStatus.COMPLETED,
                        // Оставляем остальные поля как есть (null = не менять)
                        name = null,
                        description = null,
                        executor = null,
                        executionDate = null,
                        important = null,
                        contragentId = null
                    )
                    taskViewModel.updateTask(request)
                }
                // НЕ сбрасываем selectedTask сразу - пусть пользователь видит обновлённую задачу
                // Панель закроется автоматически после успешного обновления через updateTaskResult
            },
            onEditTask = {
                // Открываем диалог редактирования задачи
                showEditTaskDialog = true
            },
            onOpenContragent = { contragentId ->
                // Находим контрагента по ID и открываем его карточку
                val contragent = contragentViewModel.getContragentById(contragentId)
                if (contragent != null) {
                    selectedContragent = contragent
                    // Закрываем панель задачи
                    selectedTask = null
                } else {
                    // Если контрагент не найден в локальном списке, загружаем его
                    // TODO: Реализовать загрузку контрагента по ID
                    println("Контрагент с ID $contragentId не найден")
                    // Сохраняем ID контрагента для последующей загрузки
                    pendingContragentId = contragentId
                    // Загружаем контрагентов
                    contragentViewModel.refreshContragents()
                }
            }
        )
    }

    // ДИАЛОГ РЕДАКТИРОВАНИЯ ЗАДАЧИ
    if (showEditTaskDialog && selectedTask != null) {
        EditTaskDialog(
            task = selectedTask!!,
            onDismiss = { 
                showEditTaskDialog = false 
            },
            onSave = { request: UpdateTaskRequest ->
                taskViewModel.updateTask(request)
                // НЕ закрываем диалог сразу - пусть пользователь видит обновления в форме
                // Диалог закроется автоматически когда задача обновится через snapshotFlow
            },
            onTaskUpdated = { updatedTask ->
                selectedTask = updatedTask
            }
        )
    } else if (showEditTaskDialog && selectedTask == null) {
        showEditTaskDialog = false
    }

    // НОВАЯ ПАНЕЛЬ СОЗДАНИЯ ЗАДАЧИ
    CreateTaskPanel(
        show = showCreateTaskPanel,
        onClose = {
            showCreateTaskPanel = false
            taskForContragent = null
            createTaskViewModel.resetTaskCreated() // Сбрасываем флаг
        },
        preselectedContragent = taskForContragent,
        onTaskCreated = {
            showCreateTaskPanel = false
            taskForContragent = null
            taskViewModel.refreshTasks() // Обновляем список задач
        }
    )

    // Автоматически закрываем панель при успешном создании задачи
    LaunchedEffect(taskCreated) {
        if (taskCreated) {
            // Даем небольшую задержку для показа состояния успеха
            delay(1000)
            showCreateTaskPanel = false
            taskForContragent = null
            createTaskViewModel.resetTaskCreated()
            taskViewModel.refreshTasks()
        }
    }

    // Обновляем selectedTask после успешного добавления комментария
    LaunchedEffect(addCommentResult) {
        if (addCommentResult != null && selectedTask != null) {
            // Ждем немного, чтобы refreshTasks() успел обновить задачи
            delay(300)
            
            // Просто обновляем selectedTask из обновленного списка задач
            val updatedTask = taskViewModel.getTaskById(selectedTask!!.id ?: "")
            if (updatedTask != null) {
                selectedTask = updatedTask
            }
            // Сбрасываем результат
            taskViewModel.resetAddCommentResult()
        }
    }

    // Автоматически закрываем диалог редактирования после успешного обновления
    LaunchedEffect(updateTaskResult) {
        if (updateTaskResult != null) {
            if (showEditTaskDialog) {
                // Даем небольшую задержку, чтобы пользователь увидел успешное сохранение
                delay(1000)
                showEditTaskDialog = false
            }
            
            // Если задача была отмечена как выполненная через кнопку в TaskDetailPanel
            // Обновляем список задач и закрываем панель
            taskViewModel.refreshTasks()
            
            // Даем небольшую задержку перед закрытием панели, чтобы пользователь увидел успех
            delay(500)
            selectedTask = null
            
            // Сбрасываем результат
            taskViewModel.resetUpdateTaskResult()
        }
    }

    // Обрабатываем pendingContragentId после загрузки контрагентов
    LaunchedEffect(contragents) {
        pendingContragentId?.let { contragentId ->
            // Ищем контрагента в загруженном списке
            val contragent = contragentViewModel.getContragentById(contragentId)
            if (contragent != null) {
                selectedContragent = contragent
                selectedTask = null
                pendingContragentId = null
            }
        }
    }

    // Диалог привязки контрагента
    if (showSnapContragentDialog && selectedIndividual != null) {
        SnapContragentDialog(
            individualId = selectedIndividual!!.id,
            individualName = selectedIndividual!!.name,
            onDismiss = { showSnapContragentDialog = false },
            onContragentSnapped = {
                showSnapContragentDialog = false
            }
        )
    }

    // ★ УДАЛЯЕМ ВСЕ ССЫЛКИ НА СТАРЫЙ ДИАЛОГ CreateTaskDialog
    // Диалог создания задачи для контрагента - больше не используется
    // if (showCreateTaskDialog) { ... }
    
    // Экран истории версий
    if (showVersionHistory) {
        VersionHistoryScreen(
            onBackClick = { showVersionHistory = false }
        )
    }
}