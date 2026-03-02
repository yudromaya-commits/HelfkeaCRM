package com.example.repressales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repressales.model.Task
import com.example.repressales.model.Contragent
import com.example.repressales.ui.components.CalendarWithKanbanView
import com.example.repressales.ui.components.DashboardView
import com.example.repressales.ui.components.NavigationPanel
import com.example.repressales.ui.components.TaskDetailScreen
import com.example.repressales.ui.components.TaskView
import com.example.repressales.ui.components.ContragentView
import com.example.repressales.ui.components.ContragentDetailScreen
import com.example.repressales.ui.components.CreateTaskDialog
import com.example.repressales.ui.theme.RepressaSalesTheme
import com.example.repressales.viewmodel.TaskViewModel

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
    var currentSection by remember { mutableStateOf("Задачи") }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var selectedContragent by remember { mutableStateOf<Contragent?>(null) }
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var taskForContragent by remember { mutableStateOf<String?>(null) }

    val taskViewModel: TaskViewModel = viewModel()
    val isLoading by taskViewModel.isLoading.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        if (selectedTask != null) {
            // Показываем экран детализации задачи
            TaskDetailScreen(
                task = selectedTask!!,
                onBackClick = { selectedTask = null }
            )
        } else if (selectedContragent != null) {
            // Показываем экран детализации контрагента
            ContragentDetailScreen(
                contragent = selectedContragent!!,
                onBackClick = { selectedContragent = null },
                onCreateTaskClick = {
                    taskForContragent = selectedContragent!!.name
                    showCreateTaskDialog = true
                }
            )
        } else {
            // Основной интерфейс с навигацией
            Row(Modifier.fillMaxSize()) {
                NavigationPanel { selectedSection ->
                    currentSection = selectedSection
                }

                // Показываем соответствующий раздел
                when (currentSection) {
                    "Задачи" -> TaskView(
                        modifier = Modifier.weight(1f),
                        onTaskClick = { task -> selectedTask = task }
                    )
                    "Календарь" -> CalendarWithKanbanView(
                        modifier = Modifier.weight(1f),
                        onTaskClick = { task -> selectedTask = task }
                    )
                    "Юрлица" -> ContragentView(
                        modifier = Modifier.weight(1f),
                        onContragentClick = { contragent ->
                            selectedContragent = contragent
                        },
                        onCreateTaskClick = { contragent ->
                            taskForContragent = contragent.name
                            showCreateTaskDialog = true
                        }
                    )
                    else -> DashboardView(modifier = Modifier.weight(1f))
                }
            }
        }

        // Диалог создания задачи для контрагента
        if (showCreateTaskDialog) {
            CreateTaskDialog(
                onDismiss = {
                    showCreateTaskDialog = false
                    taskForContragent = null
                },
                onCreateTask = { request ->
                    // Добавляем имя контрагента в описание задачи
                    val enhancedRequest = if (taskForContragent != null) {
                        request.copy(
                            description = "Контрагент: ${taskForContragent}\n\n${request.description}"
                        )
                    } else {
                        request
                    }

                    taskViewModel.createTask(enhancedRequest)
                    showCreateTaskDialog = false
                    taskForContragent = null
                },
                isLoading = isLoading
            )
        }
    }
}