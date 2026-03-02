package com.helfkea.crm.ui.calendar.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Улучшенная канбан-доска с фильтрацией и поиском
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EnhancedKanbanView(
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {}
) {
    val viewModel: TaskViewModel = viewModel()
    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Состояние фильтров
    var showFilters by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedContragent by remember { mutableStateOf<String?>(null) }
    var dateFilter by remember { mutableStateOf<DateFilter>(DateFilter.ALL) }

    // Уникальные контрагенты для фильтра
    val contragents = remember(tasks) {
        tasks.mapNotNull { it.contragent?.name }
            .distinct()
            .sorted()
    }

    // Загружаем задачи при открытии канбана
    LaunchedEffect(Unit) {
        viewModel.loadAllTasks()
    }

    // Фильтруем задачи
    val filteredTasks = remember(tasks, searchQuery, selectedContragent, dateFilter) {
        tasks.filter { task ->
            var matches = true

            // Поиск по тексту
            if (searchQuery.isNotBlank()) {
                val query = searchQuery.lowercase()
                matches = matches && (
                        task.name.lowercase().contains(query) ||
                                task.description.lowercase().contains(query) ||
                                task.status.lowercase().contains(query) ||
                                task.producer.lowercase().contains(query) ||
                                (task.contragent?.name?.lowercase()?.contains(query) ?: false)
                        )
            }

            // Фильтр по контрагенту
            if (selectedContragent != null) {
                matches = matches && (task.contragent?.name == selectedContragent)
            }

            // Фильтр по дате
            matches = matches && when (dateFilter) {
                DateFilter.ALL -> true
                DateFilter.TODAY -> isToday(task.executionDate)
                DateFilter.TOMORROW -> isTomorrow(task.executionDate)
                DateFilter.THIS_WEEK -> isThisWeek(task.executionDate)
                DateFilter.OVERDUE -> isOverdue(task.executionDate)
            }

            matches
        }
    }

    // Группируем задачи по статусам
    val tasksByStatus = remember(filteredTasks) {
        filteredTasks.groupBy { task ->
            KanbanStatus.from1CStatus(task.status)
        }
    }

    // Статистика по колонкам
    val columnStats = remember(tasksByStatus) {
        KanbanStatus.allInOrder().associateWith { status ->
            val tasksInColumn = tasksByStatus[status] ?: emptyList()
            val importantCount = tasksInColumn.count { it.important }
            val overdueCount = tasksInColumn.count { isOverdue(it.executionDate) }
            ColumnStats(
                total = tasksInColumn.size,
                important = importantCount,
                overdue = overdueCount
            )
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Заголовок и кнопки управления
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

            Row {
                // Кнопка фильтров
                FilterChip(
                    selected = showFilters,
                    onClick = { showFilters = !showFilters },
                    label = { Text("Фильтры") },
                    leadingIcon = if (showFilters) {
                        { Icon(Icons.Filled.FilterAlt, "Фильтры") }
                    } else {
                        { Icon(Icons.Outlined.FilterAlt, "Фильтры") }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Кнопка обновления
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
        }

        // Панель фильтров
        if (showFilters) {
            FilterPanel(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                contragents = contragents,
                selectedContragent = selectedContragent,
                onContragentSelected = { selectedContragent = it },
                dateFilter = dateFilter,
                onDateFilterChange = { dateFilter = it },
                onClearFilters = {
                    searchQuery = ""
                    selectedContragent = null
                    dateFilter = DateFilter.ALL
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Статистика
        KanbanStats(
            columnStats = columnStats,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (isLoading && tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = "Нет задач",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = if (showFilters && (searchQuery.isNotBlank() || selectedContragent != null || dateFilter != DateFilter.ALL)) {
                            "Задачи не найдены\nПопробуйте изменить фильтры"
                        } else {
                            "Нет задач для отображения"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
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
                    EnhancedKanbanColumn(
                        status = status,
                        tasks = tasksByStatus[status] ?: emptyList(),
                        stats = columnStats[status] ?: ColumnStats(),
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
fun EnhancedKanbanColumn(
    status: KanbanStatus,
    tasks: List<Task>,
    stats: ColumnStats,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val columnColor = Color(android.graphics.Color.parseColor(status.colorHex))

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(
                columnColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        // Заголовок колонки со статистикой
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
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
                        text = stats.total.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Детальная статистика
            if (stats.important > 0 || stats.overdue > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (stats.important > 0) {
                        Text(
                            text = "❗${stats.important}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Red
                        )
                    }
                    if (stats.overdue > 0) {
                        Text(
                            text = "⏰${stats.overdue}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF44336)
                        )
                    }
                }
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
                EnhancedKanbanTaskCard(
                    task = task,
                    columnColor = columnColor,
                    onTaskClick = onTaskClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()
                )
            }

            // Пустое место
            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                columnColor.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = columnColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет задач",
                            style = MaterialTheme.typography.bodySmall,
                            color = columnColor.copy(alpha = 0.4f),
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
fun EnhancedKanbanTaskCard(
    task: Task,
    columnColor: Color,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverdue = remember(task.executionDate) {
        isOverdue(task.executionDate)
    }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onTaskClick(task) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Заголовок с важностью и просрочкой
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    if (task.important) {
                        Text(
                            text = "❗",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (isOverdue) {
                        Text(
                            text = "⏰",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red
                        )
                    }
                }

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
                    text = task.description.take(80) + if (task.description.length > 80) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Детали задачи
            Column {
                if (task.contragent != null) {
                    Text(
                        text = "👤 ${task.contragent.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📅 ${task.executionDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Text(
                        text = "👨‍💼 ${task.producer}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPanel(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    contragents: List<String>,
    selectedContragent: String?,
    onContragentSelected: (String?) -> Unit,
    dateFilter: DateFilter,
    onDateFilterChange: (DateFilter) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Поиск
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Поиск задач") },
                leadingIcon = { Icon(Icons.Default.Search, "Поиск") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Фильтр по контрагенту
            var contragentExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = contragentExpanded,
                onExpandedChange = { contragentExpanded = !contragentExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedContragent ?: "Все контрагенты",
                    onValueChange = {},
                    label = { Text("Контрагент") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = contragentExpanded)
                    },
                    leadingIcon = { Icon(Icons.Default.Person, "Контрагент") }
                )

                ExposedDropdownMenu(
                    expanded = contragentExpanded,
                    onDismissRequest = { contragentExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Все контрагенты") },
                        onClick = {
                            onContragentSelected(null)
                            contragentExpanded = false
                        }
                    )
                    contragents.forEach { contragentName ->
                        DropdownMenuItem(
                            text = { Text(contragentName) },
                            onClick = {
                                onContragentSelected(contragentName)
                                contragentExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}