package com.helfkea.crm.ui.tasks.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.helfkea.crm.viewmodel.TaskTab
import com.helfkea.crm.viewmodel.TaskViewModel
import com.helfkea.crm.utils.toReadableDate

@Composable
fun TaskView(
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {},
    onCreateTaskClick: () -> Unit = {}  // ★ ДОБАВЛЯЕМ КОЛБЭК ДЛЯ ОТКРЫТИЯ ПАНЕЛИ
) {
    val viewModel: TaskViewModel = viewModel()
    val filteredTasks by viewModel.filteredTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val myTasks by viewModel.myTasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val createTaskResult by viewModel.createTaskResult.collectAsState()
    val filterSortState by viewModel.filterSortState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    
    // Тумблер для показа выполненных задач
    var showCompleted by remember { mutableStateOf(false) }

    val availableStatuses by viewModel.availableStatuses.collectAsState()
    val availableExecutors by viewModel.availableExecutors.collectAsState()
    val availableProducers by viewModel.availableProducers.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    // Загружаем задачи при первом открытии
    LaunchedEffect(Unit) {
        viewModel.loadAllTasks()
    }

    // Получаем задачи для текущей вкладки
    val currentTaskList = when (selectedTab) {
        TaskTab.ALL_TASKS -> allTasks
        TaskTab.MY_TASKS -> myTasks
        else -> filteredTasks
    }

    // Применяем фильтры и сортировку к текущему списку задач
    val tasksToDisplay = remember(currentTaskList, filterSortState, selectedTab, showCompleted) {
        if (selectedTab == TaskTab.ALL_TASKS || selectedTab == TaskTab.MY_TASKS) {
            applyFiltersAndSort(currentTaskList, filterSortState, showCompleted)
        } else {
            // Для других вкладок тоже нужно фильтровать выполненные
            if (!showCompleted) {
                filteredTasks.filter { !TaskStatus.isCompleted(it.status) }
            } else {
                filteredTasks
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Заголовок и кнопки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Задачи",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row {
                // Кнопка фильтра
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Фильтры",
                        tint = if (filterSortState.selectedStatus != null ||
                            filterSortState.selectedExecutor != null ||
                            filterSortState.selectedProducer != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Gray
                        }
                    )
                }

                // Кнопка сортировки
                IconButton(onClick = { showSortDialog = true }) {
                    Icon(
                        Icons.Default.Sort,
                        contentDescription = "Сортировка",
                        tint = if (filterSortState.currentSort != TaskSort.DATE_DESC) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Gray
                        }
                    )
                }

                // Кнопка обновления
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

                // Кнопка добавления ★ ИЗМЕНЕНО: открываем панель вместо диалога
                Button(
                    onClick = onCreateTaskClick,  // ★ ИСПОЛЬЗУЕМ ПЕРЕДАННЫЙ КОЛБЭК
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить")
                }
            }
        }

        // КРАСИВЫЕ ВКЛАДКИ С ИКОНКАМИ И ТУМБЛЕРОМ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Вкладки занимают всё доступное пространство
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                tabs = listOf(
                    TabItem(
                        title = "Мне",
                        icon = Icons.Default.PersonOutline,
                        selectedIcon = Icons.Default.Person
                    ),
                    TabItem(
                        title = "От меня",
                        icon = Icons.Default.PersonPin,
                        selectedIcon = Icons.Default.PersonPinCircle
                    ),
                    TabItem(
                        title = "Важные",
                        icon = Icons.Default.StarOutline,
                        selectedIcon = Icons.Default.Star,
                        iconColor = Color(0xFFFF9800),
                        selectedIconColor = Color(0xFFFF9800)
                    ),
                    TabItem(
                        title = "Просрочка",
                        icon = Icons.Default.Warning,
                        selectedIcon = Icons.Default.Warning,
                        iconColor = Color.Red,
                        selectedIconColor = Color.Red
                    ),
                    TabItem(
                        title = "В работе",
                        icon = Icons.Default.Build,
                        selectedIcon = Icons.Default.Build,
                        iconColor = Color.Green,
                        selectedIconColor = Color.Green
                    )
                ),
                onTabSelected = { index ->
                    val tab = TaskTab.values()[index]
                    viewModel.selectTab(tab)
                },
                modifier = Modifier.weight(1f)
            )
            
            // Тумблер "Показывать выполненные" справа
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .clickable { showCompleted = !showCompleted }
            ) {
                Text(
                    text = "Выполненные",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Switch(
                    checked = showCompleted,
                    onCheckedChange = { showCompleted = it },
                    modifier = Modifier.size(48.dp, 32.dp)
                )
            }
        }

        // Строка поиска
        SearchBar(
            query = filterSortState.searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 8.dp)
        )

        // Индикаторы активных фильтров
        ActiveFiltersBar(
            filterSortState = filterSortState,
            onClearAll = { viewModel.clearAllFilters() },
            onClearStatus = { viewModel.updateStatusFilter(null) },
            onClearExecutor = { viewModel.updateExecutorFilter(null) },
            onClearProducer = { viewModel.updateProducerFilter(null) }
        )

        // Счетчик задач
        TaskCounter(
            count = tasksToDisplay.size,
            total = when (selectedTab) {
                TaskTab.ALL_TASKS -> {
                    if (filterSortState.selectedStatus == null &&
                        filterSortState.selectedExecutor == null &&
                        filterSortState.selectedProducer == null &&
                        filterSortState.searchQuery.isEmpty()) {
                        allTasks.size
                    } else {
                        tasksToDisplay.size
                    }
                }
                TaskTab.MY_TASKS -> {
                    if (filterSortState.selectedStatus == null &&
                        filterSortState.selectedExecutor == null &&
                        filterSortState.selectedProducer == null &&
                        filterSortState.searchQuery.isEmpty()) {
                        myTasks.size
                    } else {
                        tasksToDisplay.size
                    }
                }
                else -> filteredTasks.size
            },
            selectedTab = selectedTab
        )

        // Сообщение о результате создания ★ УБИРАЕМ АЛЕРТ, ОН В ПАНЕЛИ
        // createTaskResult?.let { result ->
        //     AlertDialog(...)
        // }

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
        if (tasksToDisplay.isEmpty() && !isLoading && error == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = when (selectedTab) {
                            TaskTab.ALL_TASKS -> Icons.Default.PersonOutline
                            TaskTab.MY_TASKS -> Icons.Default.Person
                            TaskTab.IMPORTANT -> Icons.Default.StarOutline
                            TaskTab.OVERDUE -> Icons.Default.WatchLater
                            TaskTab.IN_PROGRESS -> Icons.Default.Build
                        },
                        contentDescription = "Нет задач",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (selectedTab) {
                            TaskTab.ALL_TASKS -> "Нет задач, назначенных вам"
                            TaskTab.MY_TASKS -> "У вас пока нет задач"
                            TaskTab.IMPORTANT -> "Нет важных задач"
                            TaskTab.OVERDUE -> "Нет просроченных задач"
                            TaskTab.IN_PROGRESS -> "Нет задач в работе"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onCreateTaskClick) {  // ★ ИСПОЛЬЗУЕМ КОЛБЭК ДЛЯ ПАНЕЛИ
                        Text("Создать задачу")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasksToDisplay) { task ->
                    TaskCard(
                        task = task,
                        selectedTab = selectedTab,
                        onClick = { 
                            println("DEBUG TaskView: Клик по задаче ${task.id}")
                            onTaskClick(task) 
                        }
                    )
                }
            }
        }
    }

    // ★ УБИРАЕМ СТАРЫЙ ДИАЛОГ СОЗДАНИЯ
    // Диалог создания задачи
    // if (showCreateDialog) {
    //     CreateTaskDialog(...)
    // }

    // Диалог фильтров
    if (showFilterDialog) {
        TaskFilterDialog(
            filterSortState = filterSortState,
            availableStatuses = availableStatuses,
            availableExecutors = availableExecutors,
            availableProducers = availableProducers,
            onDismiss = { showFilterDialog = false },
            onStatusSelect = { status: String? ->
                viewModel.updateStatusFilter(status)
            },
            onExecutorSelect = { executor: String? ->
                viewModel.updateExecutorFilter(executor)
            },
            onProducerSelect = { producer: String? ->
                viewModel.updateProducerFilter(producer)
            }
        )
    }

    // Диалог сортировки
    if (showSortDialog) {
        SortDialog(
            currentSort = filterSortState.currentSort,
            onDismiss = { showSortDialog = false },
            onSortSelect = { sort -> viewModel.updateSort(sort) }
        )
    }
}

// Вспомогательная функция для применения фильтров и сортировки
private fun applyFiltersAndSort(
    tasks: List<Task>,
    state: FilterSortState,
    showCompleted: Boolean = false
): List<Task> {
    var filteredTasks = tasks.filter { task ->
        var matches = true

        // Фильтр по выполненным задачам (скрываем если showCompleted = false)
        if (!showCompleted && TaskStatus.isCompleted(task.status)) {
            return@filter false
        }

        // Фильтр по статусу
        if (state.selectedStatus != null && state.selectedStatus != task.status) {
            matches = false
        }

        // Фильтр по исполнителю
        if (state.selectedExecutor != null && task.executor != state.selectedExecutor) {
            matches = false
        }

        // Фильтр по постановщику
        if (state.selectedProducer != null && task.producer != state.selectedProducer) {
            matches = false
        }

        // Фильтр по важности
        if (state.showImportantOnly && !task.important) {
            matches = false
        }

        // Поиск по всем текстовым полям
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            matches = matches && (
                    task.name.lowercase().contains(query) ||
                            task.description.lowercase().contains(query) ||
                            task.status.lowercase().contains(query) ||
                            task.producer.lowercase().contains(query) ||
                            (task.executor?.lowercase()?.contains(query) ?: false)
                    )
        }

        matches
    }

    // Применяем сортировку
    filteredTasks = when (state.currentSort) {
        TaskSort.DATE_ASC -> filteredTasks.sortedBy { it.date }
        TaskSort.DATE_DESC -> filteredTasks.sortedByDescending { it.date }
        TaskSort.EXECUTION_ASC -> filteredTasks.sortedBy { it.executionDate }
        TaskSort.EXECUTION_DESC -> filteredTasks.sortedByDescending { it.executionDate }
        TaskSort.EXECUTOR -> filteredTasks.sortedBy { it.executor ?: "" }
        TaskSort.PRODUCER -> filteredTasks.sortedBy { it.producer }
        TaskSort.IMPORTANT -> filteredTasks.sortedByDescending { it.important }
        TaskSort.STATUS -> filteredTasks.sortedBy { it.status }
    }

    return filteredTasks
}

@Composable
fun TaskCounter(
    count: Int,
    total: Int,
    selectedTab: TaskTab,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when (selectedTab) {
                TaskTab.ALL_TASKS -> "Задачи мне"
                TaskTab.MY_TASKS -> "Мои задачи"
                TaskTab.IMPORTANT -> "Важные задачи"
                TaskTab.OVERDUE -> "Просроченные"
                TaskTab.IN_PROGRESS -> "В работе"
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = if (count == total) "$count" else "$count/$total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActiveFiltersBar(
    filterSortState: FilterSortState,
    onClearAll: () -> Unit,
    onClearStatus: () -> Unit,
    onClearExecutor: () -> Unit,
    onClearProducer: () -> Unit
) {
    val hasAnyFilter = filterSortState.selectedStatus != null ||
            filterSortState.selectedExecutor != null ||
            filterSortState.selectedProducer != null ||
            filterSortState.searchQuery.isNotBlank()

    if (hasAnyFilter) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Фильтры:",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(end = 8.dp)
            )

            // Фильтр по статусу
            filterSortState.selectedStatus?.let { status ->
                FilterChip(
                    label = { Text("Статус: $status") },
                    onDismiss = onClearStatus,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            // Фильтр по исполнителю
            filterSortState.selectedExecutor?.let { executor ->
                FilterChip(
                    label = { Text("Исполнитель: $executor") },
                    onDismiss = onClearExecutor,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            // Фильтр по постановщику
            filterSortState.selectedProducer?.let { producer ->
                FilterChip(
                    label = { Text("Постановщик: $producer") },
                    onDismiss = onClearProducer,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onClearAll) {
                Text("Сбросить все")
            }
        }
    }
}

@Composable
fun ScrollableTabRow(
    selectedTabIndex: Int,
    tabs: List<TabItem>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(tabs) { index, tab ->
            ScrollableTab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                tabItem = tab
            )
        }
    }
}

data class TabItem(
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val iconColor: Color? = null,
    val selectedIconColor: Color? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollableTab(
    selected: Boolean,
    onClick: () -> Unit,
    tabItem: TabItem,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val actualIconColor = tabItem.iconColor ?: contentColor
    val actualSelectedIconColor = tabItem.selectedIconColor ?: contentColor

    val icon = if (selected && tabItem.selectedIcon != null) {
        tabItem.selectedIcon
    } else {
        tabItem.icon
    }

    val iconTint = if (selected) actualSelectedIconColor else actualIconColor

    Card(
        modifier = modifier
            .width(120.dp)
            .height(56.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (selected) 6.dp else 2.dp),
        shape = MaterialTheme.shapes.large,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tabItem.title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tabItem.title,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

// Компонент SearchBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Поиск задач...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Поиск")
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Очистить")
                }
            }
        },
        singleLine = true,
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    selectedTab: TaskTab,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                task.important -> 6.dp
                selectedTab == TaskTab.IMPORTANT -> 4.dp
                selectedTab == TaskTab.OVERDUE && task.status == "Просрочена" -> 4.dp
                else -> 2.dp
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                TaskStatus.isCompleted(task.status) ->
                    Color(0xFFF5F5F5) // Серый фон для выполненных задач
                selectedTab == TaskTab.OVERDUE && task.status == "Просрочена" ->
                    Color(0xFFFFF8E1).copy(alpha = 0.9f)
                selectedTab == TaskTab.IMPORTANT && task.important ->
                    Color(0xFFF3E5F5).copy(alpha = 0.9f)
                selectedTab == TaskTab.IN_PROGRESS && task.status == "В работе" ->
                    Color(0xFFE8F5E8).copy(alpha = 0.9f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Иконка
                    Icon(
                        imageVector = when {
                            selectedTab == TaskTab.MY_TASKS -> Icons.Default.Person
                            selectedTab == TaskTab.IMPORTANT && task.important -> Icons.Default.Star
                            selectedTab == TaskTab.OVERDUE && task.status == "Просрочена" -> Icons.Default.Warning
                            selectedTab == TaskTab.IN_PROGRESS && task.status == "В работе" -> Icons.Default.Build
                            else -> Icons.Default.TaskAlt
                        },
                        contentDescription = null,
                        tint = when {
                            selectedTab == TaskTab.IMPORTANT && task.important -> Color(0xFFFF9800)
                            selectedTab == TaskTab.OVERDUE && task.status == "Просрочена" -> Color.Red
                            selectedTab == TaskTab.IN_PROGRESS && task.status == "В работе" -> Color.Green
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.name?.ifEmpty { "Без названия" } ?: "Без названия",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                            color = if (TaskStatus.isCompleted(task.status)) 
                                Color.Gray else MaterialTheme.colorScheme.onSurface
                        )

                        // КОНТРАГЕНТ
                        task.contragent?.let { contragent ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = "Контрагент",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    contragent.name ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Бейдж важности
                if (task.important) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Важная задача",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Статус
            val status = task.status ?: "Не указан"
            val statusColor = when (status) {
                "Просрочена" -> Color.Red
                "Назначена" -> Color.Blue
                "Выполнена" -> Color.Green
                "В работе" -> Color(0xFFFF9800)
                else -> Color.Gray
            }

            Text(
                text = status,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Описание
            if (task.description?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Информация о людях и датах
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (task.producer?.isNotEmpty() == true) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PersonOutline,
                                contentDescription = "Постановщик",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Постановщик: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                task.producer,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        
                    // Контрагент
                    if (task.contragentName?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = "Контрагент",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Контрагент: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                task.contragentName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                    }
                    if (task.executor?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WorkOutline,
                                contentDescription = "Исполнитель",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Исполнитель: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                task.executor,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Дата выполнения
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Срок",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            task.executionDate.toReadableDate(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    // ВЛОЖЕНИЯ
                    if (task.attachments?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Attachment,
                                contentDescription = "Вложения",
                                tint = Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${task.attachments.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: @Composable () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onDismiss,
        label = label,
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "Удалить фильтр",
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

// Диалог сортировки
@Composable
fun SortDialog(
    currentSort: TaskSort,
    onDismiss: () -> Unit,
    onSortSelect: (TaskSort) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сортировка задач") },
        text = {
            Column {
                SortOption(
                    label = "По дате создания (новые сверху)",
                    selected = currentSort == TaskSort.DATE_DESC,
                    onClick = { onSortSelect(TaskSort.DATE_DESC) }
                )
                SortOption(
                    label = "По дате создания (старые сверху)",
                    selected = currentSort == TaskSort.DATE_ASC,
                    onClick = { onSortSelect(TaskSort.DATE_ASC) }
                )
                SortOption(
                    label = "По дате исполнения (ближайшие сверху)",
                    selected = currentSort == TaskSort.EXECUTION_ASC,
                    onClick = { onSortSelect(TaskSort.EXECUTION_ASC) }
                )
                SortOption(
                    label = "По дате исполнения (дальние сверху)",
                    selected = currentSort == TaskSort.EXECUTION_DESC,
                    onClick = { onSortSelect(TaskSort.EXECUTION_DESC) }
                )
                SortOption(
                    label = "По исполнителю (А-Я)",
                    selected = currentSort == TaskSort.EXECUTOR,
                    onClick = { onSortSelect(TaskSort.EXECUTOR) }
                )
                SortOption(
                    label = "По постановщику (А-Я)",
                    selected = currentSort == TaskSort.PRODUCER,
                    onClick = { onSortSelect(TaskSort.PRODUCER) }
                )
                SortOption(
                    label = "По важности (важные сверху)",
                    selected = currentSort == TaskSort.IMPORTANT,
                    onClick = { onSortSelect(TaskSort.IMPORTANT) }
                )
                SortOption(
                    label = "По статусу",
                    selected = currentSort == TaskSort.STATUS,
                    onClick = { onSortSelect(TaskSort.STATUS) }
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Применить")
            }
        }
    )
}

@Composable
fun SortOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
fun TaskFilterDialog(
    filterSortState: FilterSortState,
    availableStatuses: Set<String>,
    availableExecutors: Set<String>,
    availableProducers: Set<String>,
    onDismiss: () -> Unit,
    onStatusSelect: (String?) -> Unit,
    onExecutorSelect: (String?) -> Unit,
    onProducerSelect: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Дополнительные фильтры") },
        text = {
            Column {
                // Фильтр по статусу
                if (availableStatuses.isNotEmpty()) {
                    Text("Статус:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                        item {
                            FilterChipButton(
                                label = "Все",
                                selected = filterSortState.selectedStatus == null,
                                onClick = { onStatusSelect(null) }
                            )
                        }
                        items(availableStatuses.toList()) { status ->
                            Spacer(modifier = Modifier.width(4.dp))
                            FilterChipButton(
                                label = status,
                                selected = filterSortState.selectedStatus == status,
                                onClick = { onStatusSelect(status) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Фильтр по исполнителю
                if (availableExecutors.isNotEmpty()) {
                    Text("Исполнитель:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                        item {
                            FilterChipButton(
                                label = "Все",
                                selected = filterSortState.selectedExecutor == null,
                                onClick = { onExecutorSelect(null) }
                            )
                        }
                        items(availableExecutors.toList()) { executor ->
                            Spacer(modifier = Modifier.width(4.dp))
                            FilterChipButton(
                                label = executor,
                                selected = filterSortState.selectedExecutor == executor,
                                onClick = { onExecutorSelect(executor) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Фильтр по постановщику
                if (availableProducers.isNotEmpty()) {
                    Text("Постановщик:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                        item {
                            FilterChipButton(
                                label = "Все",
                                selected = filterSortState.selectedProducer == null,
                                onClick = { onProducerSelect(null) }
                            )
                        }
                        items(availableProducers.toList()) { producer ->
                            Spacer(modifier = Modifier.width(4.dp))
                            FilterChipButton(
                                label = producer,
                                selected = filterSortState.selectedProducer == producer,
                                onClick = { onProducerSelect(producer) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Применить")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onStatusSelect(null)
                onExecutorSelect(null)
                onProducerSelect(null)
            }) {
                Text("Сбросить")
            }
        }
    )
}