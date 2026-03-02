package com.helfkea.crm.ui.calendar.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helfkea.crm.model.KanbanStatus
import com.helfkea.crm.model.Task
import com.helfkea.crm.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KanbanView(
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {}
) {
    val viewModel: TaskViewModel = viewModel()
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Загружаем задачи при открытии канбана
    LaunchedEffect(Unit) {
        viewModel.loadAllTasks()
    }

    // Группируем задачи по статусам
    val tasksByStatus = remember(tasks) {
        tasks.groupBy { task ->
            KanbanStatus.from1CStatus(task.status)
        }
    }

    // Состояние для drag & drop
    var draggedTask by remember { mutableStateOf<Task?>(null) }
    var targetStatus by remember { mutableStateOf<KanbanStatus?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Заголовок и кнопка обновления
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Канбан-доска",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { viewModel.loadAllTasks() },
                enabled = !isLoading
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Обновить",
                    tint = if (isLoading) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    else MaterialTheme.colorScheme.primary
                )
            }
        }

        if (isLoading && tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Канбан-доска с колонками
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KanbanStatus.allInOrder().forEach { status ->
                    KanbanColumn(
                        status = status,
                        tasks = tasksByStatus[status] ?: emptyList(),
                        draggedTask = draggedTask,
                        onTaskDragStart = { task -> draggedTask = task },
                        onTaskDragEnd = {
                            // Drag & drop только визуальный (как в календаре)
                            // Не обновляем статус в 1С
                            draggedTask = null
                            targetStatus = null
                        },
                        onTaskDragOverColumn = { targetStatus = status },
                        onTaskClick = onTaskClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KanbanColumn(
    status: KanbanStatus,
    tasks: List<Task>,
    draggedTask: Task?,
    onTaskDragStart: (Task) -> Unit,
    onTaskDragEnd: () -> Unit,
    onTaskDragOverColumn: () -> Unit,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val columnColor = Color(android.graphics.Color.parseColor(status.colorHex))
    val isDragOver = draggedTask != null && draggedTask !in tasks

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(
                columnColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = if (isDragOver) columnColor.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        // Заголовок колонки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = columnColor
            )

            Badge(
                containerColor = columnColor.copy(alpha = 0.2f),
                contentColor = columnColor
            ) {
                Text(
                    text = tasks.size.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Список задач в колонке
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(tasks) { task ->
                KanbanTaskCard(
                    task = task,
                    columnColor = columnColor,
                    onTaskClick = onTaskClick,
                    onDragStart = { onTaskDragStart(task) },
                    onDragEnd = onTaskDragEnd,
                    onDragOverColumn = onTaskDragOverColumn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()
                )
            }

            // Пустое место для drop zone
            if (tasks.isEmpty() || isDragOver) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isDragOver) columnColor.copy(alpha = 0.2f)
                                else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = if (isDragOver) 2.dp else 1.dp,
                                color = if (isDragOver) columnColor.copy(alpha = 0.5f)
                                else columnColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isDragOver) "Отпустите чтобы переместить" else "Нет задач",
                            style = MaterialTheme.typography.bodySmall,
                            color = columnColor.copy(alpha = if (isDragOver) 0.8f else 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KanbanTaskCard(
    task: Task,
    columnColor: Color,
    onTaskClick: (Task) -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragOverColumn: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        isDragging = true
                        onDragStart()
                    },
                    onDrag = { _, _ ->
                        // При drag над колонкой
                        onDragOverColumn()
                    },
                    onDragEnd = {
                        isDragging = false
                        onDragEnd()
                    },
                    onDragCancel = {
                        isDragging = false
                        onDragEnd()
                    }
                )
            }
            .border(
                width = if (isDragging) 2.dp else 0.dp,
                color = if (isDragging) columnColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 2.dp
        ),
        onClick = { onTaskClick(task) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Статус и важность
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (task.important) {
                    Text(
                        text = "❗",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = task.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = columnColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Название задачи
            Text(
                text = task.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            // Краткое описание
            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description.take(60) + if (task.description.length > 60) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Информация о дате и контрагенте
            Column {
                if (task.contragent != null) {
                    Text(
                        text = "Контрагент: ${task.contragent.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }

                Text(
                    text = "Срок: ${task.executionDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}