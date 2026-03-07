package com.helfkea.crm.ui.tasks.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helfkea.crm.model.*
import com.helfkea.crm.ui.common.components.CompactSyncIndicator
import com.helfkea.crm.ui.common.components.SyncStatusBar
import com.helfkea.crm.viewmodel.*
import com.helfkea.crm.viewmodel.TaskSortBy
import com.helfkea.crm.viewmodel.TaskFilterSortState
import com.helfkea.crm.utils.toReadableDate

data class TabInfo(
    val type: TaskTab,
    val title: String,
    val icon: ImageVector,
    val count: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineTaskView(
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {},
    onCreateTaskClick: () -> Unit = {}
) {
    val viewModel: OfflineTaskViewModel = viewModel(
        factory = OfflineTaskViewModelFactory.getInstance(
            androidx.compose.ui.platform.LocalContext.current
        )
    )
    
    // Состояния из ViewModel
    val filteredTasks by viewModel.filteredTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val myTasks by viewModel.myTasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val createTaskResult by viewModel.createTaskResult.collectAsState()
    val filterSortState by viewModel.filterSortState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    
    // Статус синхронизации
    val syncState by viewModel.syncState.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val failedCount by viewModel.failedCount.collectAsState()
    
    // Локальные состояния UI
    var showCompleted by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    
    // Обработка ошибок
    LaunchedEffect(error) {
        error?.let {
            // Можно показать Snackbar
        }
    }
    
    // Обработка результата создания задачи
    LaunchedEffect(createTaskResult) {
        createTaskResult?.let { result ->
            if (result.success) {
                // Задача создана успешно
                viewModel.clearCreateTaskResult()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Задачи")
                        CompactSyncIndicator(
                            syncManager = viewModel.syncManager,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    // Кнопка фильтрации
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтры")
                    }
                    
                    // Кнопка сортировки
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Сортировка")
                    }
                    
                    // Кнопка синхронизации
                    IconButton(
                        onClick = { viewModel.sync() },
                        enabled = syncState !is com.helfkea.crm.data.sync.SyncState.Syncing
                    ) {
                        if (syncState is com.helfkea.crm.data.sync.SyncState.Syncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = "Синхронизировать",
                                tint = if (pendingCount > 0 || failedCount > 0) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTaskClick
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить задачу")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Панель статуса синхронизации
            SyncStatusBar(
                syncManager = viewModel.syncManager,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Вкладки
            TaskTabs(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) },
                allTasksCount = allTasks.size,
                myTasksCount = myTasks.size,
                importantCount = allTasks.count { it.important },
                overdueCount = allTasks.count { it.status == TaskStatus.OVERDUE },
                inProgressCount = allTasks.count { it.status == TaskStatus.IN_PROGRESS }
            )
            
            // Индикатор загрузки
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Список задач
                if (filteredTasks.isEmpty()) {
                    EmptyTasksView(selectedTab)
                } else {
                    TaskList(
                        tasks = filteredTasks,
                        onTaskClick = onTaskClick,
                        showCompleted = showCompleted,
                        onToggleCompleted = { showCompleted = !showCompleted }
                    )
                }
            }
        }
        
        // Диалог фильтрации
        if (showFilterDialog) {
            FilterDialog(
                filterSortState = filterSortState,
                onDismiss = { showFilterDialog = false },
                onApply = { newState ->
                    viewModel.updateFilterSortState(newState)
                    showFilterDialog = false
                }
            )
        }
        
        // Диалог сортировки
        if (showSortDialog) {
            SortDialog(
                currentSort = filterSortState.sortBy,
                onDismiss = { showSortDialog = false },
                onSortSelected = { sortBy ->
                    viewModel.updateFilterSortState(filterSortState.copy(sortBy = sortBy))
                    showSortDialog = false
                }
            )
        }
    }
}

@Composable
private fun TaskTabs(
    selectedTab: TaskTab,
    onTabSelected: (TaskTab) -> Unit,
    allTasksCount: Int,
    myTasksCount: Int,
    importantCount: Int,
    overdueCount: Int,
    inProgressCount: Int
) {
    val tabs = listOf(
        TabInfo(TaskTab.ALL_TASKS, "Все", Icons.Default.List, allTasksCount),
        TabInfo(TaskTab.MY_TASKS, "Мои", Icons.Default.Person, myTasksCount),
        TabInfo(TaskTab.IMPORTANT, "Важные", Icons.Default.Star, importantCount),
        TabInfo(TaskTab.OVERDUE, "Просроченные", Icons.Default.Warning, overdueCount),
        TabInfo(TaskTab.IN_PROGRESS, "В работе", Icons.Default.PlayArrow, inProgressCount)
    )
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(tabs) { tab ->
            TaskTabChip(
                tab = tab,
                isSelected = selectedTab == tab.type,
                onClick = { onTabSelected(tab.type) }
            )
        }
    }
}

@Composable
private fun TaskTabChip(
    tab: TabInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (isSelected) null else CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = tab.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (tab.count > 0) {
                Text(
                    text = tab.count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    showCompleted: Boolean,
    onToggleCompleted: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Заголовок с переключателем выполненных задач
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Задачи (${tasks.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Показать выполненные",
                        fontSize = 14.sp
                    )
                    Switch(
                        checked = showCompleted,
                        onCheckedChange = { onToggleCompleted() }
                    )
                }
            }
        }
        
        // Список задач
        items(tasks.filter { showCompleted || it.status != TaskStatus.COMPLETED }) { task ->
            TaskCard(
                task = task,
                onClick = { onTaskClick(task) }
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                TaskStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer
                TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
                TaskStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок и важность
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                if (task.important) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Важная",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Описание
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Детали
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Контрагент
                task.contragentName?.let { contragentName ->
                    Text(
                        text = contragentName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Дата выполнения
                Text(
                    text = task.executionDate.toReadableDate(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Статус
            Text(
                text = task.status,
                fontSize = 12.sp,
                color = when (task.status) {
                    TaskStatus.OVERDUE -> MaterialTheme.colorScheme.error
                    TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun EmptyTasksView(selectedTab: TaskTab) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = when (selectedTab) {
                    TaskTab.ALL_TASKS -> Icons.Default.List
                    TaskTab.MY_TASKS -> Icons.Default.Person
                    TaskTab.IMPORTANT -> Icons.Default.Star
                    TaskTab.OVERDUE -> Icons.Default.Warning
                    TaskTab.IN_PROGRESS -> Icons.Default.PlayArrow
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = when (selectedTab) {
                    TaskTab.ALL_TASKS -> "Нет задач"
                    TaskTab.MY_TASKS -> "Нет ваших задач"
                    TaskTab.IMPORTANT -> "Нет важных задач"
                    TaskTab.OVERDUE -> "Нет просроченных задач"
                    TaskTab.IN_PROGRESS -> "Нет задач в работе"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = when (selectedTab) {
                    TaskTab.ALL_TASKS -> "Создайте первую задачу"
                    TaskTab.MY_TASKS -> "Вам пока не назначили задач"
                    TaskTab.IMPORTANT -> "Отметьте задачи как важные"
                    TaskTab.OVERDUE -> "Все задачи выполнены вовремя!"
                    TaskTab.IN_PROGRESS -> "Начните работу над задачами"
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun FilterDialog(
    filterSortState: TaskFilterSortState,
    onDismiss: () -> Unit,
    onApply: (TaskFilterSortState) -> Unit
) {
    var searchQuery by remember { mutableStateOf(filterSortState.searchQuery ?: "") }
    var importantOnly by remember { mutableStateOf(filterSortState.importantOnly) }
    var overdueOnly by remember { mutableStateOf(filterSortState.overdueOnly) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Фильтры") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = importantOnly,
                        onCheckedChange = { importantOnly = it }
                    )
                    Text("Только важные")
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Checkbox(
                        checked = overdueOnly,
                        onCheckedChange = { overdueOnly = it }
                    )
                    Text("Только просроченные")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApply(filterSortState.copy(
                        searchQuery = searchQuery.ifBlank { null },
                        importantOnly = importantOnly,
                        overdueOnly = overdueOnly
                    ))
                }
            ) {
                Text("Применить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun SortDialog(
    currentSort: TaskSortBy,
    onDismiss: () -> Unit,
    onSortSelected: (TaskSortBy) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сортировка") },
        text = {
            Column {
                TaskSortBy.values().forEach { sortBy ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(sortBy) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSort == sortBy,
                            onClick = { onSortSelected(sortBy) }
                        )
                        Text(
                            text = when (sortBy) {
                                TaskSortBy.DATE_ASC -> "По дате (старые сначала)"
                                TaskSortBy.DATE_DESC -> "По дате (новые сначала)"
                                TaskSortBy.NAME_ASC -> "По названию (А-Я)"
                                TaskSortBy.NAME_DESC -> "По названию (Я-А)"
                                TaskSortBy.IMPORTANCE -> "По важности"
                                TaskSortBy.STATUS -> "По статусу"
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Готово")
            }
        }
    )
}
