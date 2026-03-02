package com.example.repressales.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.repressales.viewmodel.ContragentViewModel
import com.example.repressales.viewmodel.SortBy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContragentFiltersPanel(
    modifier: Modifier = Modifier
) {
    val viewModel: ContragentViewModel = viewModel()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Для управления видимостью выпадающих меню
    var showTypeFilter by remember { mutableStateOf(false) }
    var showSegmentFilter by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Получаем данные для фильтров
    val availableTypes by viewModel.availableTypes.collectAsState()
    val availableSegments by viewModel.availableSegments.collectAsState()

    // Текущие значения фильтров
    val selectedTypeFilter = remember { mutableStateOf<String?>(null) }
    val selectedSegmentFilter = remember { mutableStateOf<String?>(null) }
    val selectedSortBy = remember { mutableStateOf(SortBy.NAME) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Поисковая строка
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Поиск контрагентов") },
                placeholder = { Text("Введите название или адрес...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Поиск")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.updateSearchQuery("") }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Панель фильтров
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Фильтр по типу
                FilterChip(
                    selected = selectedTypeFilter.value != null,
                    onClick = { showTypeFilter = true },
                    label = {
                        Text(
                            selectedTypeFilter.value ?: "Тип",
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = "Тип",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Фильтр по сегменту
                FilterChip(
                    selected = selectedSegmentFilter.value != null,
                    onClick = { showSegmentFilter = true },
                    label = {
                        Text(
                            selectedSegmentFilter.value ?: "Сегмент",
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = "Сегмент",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Сортировка
                FilterChip(
                    selected = true,
                    onClick = { showSortMenu = true },
                    label = {
                        Text(
                            getSortLabel(selectedSortBy.value),
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Sort,
                            contentDescription = "Сортировка",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Кнопка сброса фильтров
                IconButton(
                    onClick = {
                        viewModel.clearFilters()
                        selectedTypeFilter.value = null
                        selectedSegmentFilter.value = null
                        selectedSortBy.value = SortBy.NAME
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.ClearAll,
                        contentDescription = "Сбросить фильтры",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Информация о результатах фильтрации
            ContragentStatsView()
        }
    }

    // Выпадающее меню для фильтра по типу
    DropdownMenu(
        expanded = showTypeFilter,
        onDismissRequest = { showTypeFilter = false },
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        DropdownMenuItem(
            text = { Text("Все типы") },
            onClick = {
                viewModel.updateTypeFilter(null)
                selectedTypeFilter.value = null
                showTypeFilter = false
            }
        )
        Divider()
        availableTypes.forEach { type ->
            DropdownMenuItem(
                text = { Text(type) },
                onClick = {
                    viewModel.updateTypeFilter(type)
                    selectedTypeFilter.value = type
                    showTypeFilter = false
                }
            )
        }
    }

    // Выпадающее меню для фильтра по сегменту
    DropdownMenu(
        expanded = showSegmentFilter,
        onDismissRequest = { showSegmentFilter = false },
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        DropdownMenuItem(
            text = { Text("Все сегменты") },
            onClick = {
                viewModel.updateSegmentFilter(null)
                selectedSegmentFilter.value = null
                showSegmentFilter = false
            }
        )
        Divider()
        availableSegments.forEach { segment ->
            DropdownMenuItem(
                text = { Text(segment) },
                onClick = {
                    viewModel.updateSegmentFilter(segment)
                    selectedSegmentFilter.value = segment
                    showSegmentFilter = false
                }
            )
        }
    }

    // Выпадающее меню для сортировки
    DropdownMenu(
        expanded = showSortMenu,
        onDismissRequest = { showSortMenu = false },
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        DropdownMenuItem(
            text = { Text("По названию (А-Я)") },
            onClick = {
                viewModel.updateSortBy(SortBy.NAME)
                selectedSortBy.value = SortBy.NAME
                showSortMenu = false
            }
        )
        DropdownMenuItem(
            text = { Text("По количеству заказов") },
            onClick = {
                viewModel.updateSortBy(SortBy.ORDERS_COUNT)
                selectedSortBy.value = SortBy.ORDERS_COUNT
                showSortMenu = false
            }
        )
        DropdownMenuItem(
            text = { Text("По общей сумме") },
            onClick = {
                viewModel.updateSortBy(SortBy.TOTAL_SUM)
                selectedSortBy.value = SortBy.TOTAL_SUM
                showSortMenu = false
            }
        )
        DropdownMenuItem(
            text = { Text("По среднему чеку") },
            onClick = {
                viewModel.updateSortBy(SortBy.AVERAGE_CHECK)
                selectedSortBy.value = SortBy.AVERAGE_CHECK
                showSortMenu = false
            }
        )
    }
}

@Composable
fun ContragentStatsView() {
    val viewModel: ContragentViewModel = viewModel()
    val contragents by viewModel.contragents.collectAsState()
    val allContragents by viewModel.allContragents.collectAsState() // ✅ Теперь работает

    if (contragents.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Найдено: ${contragents.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (contragents.size < allContragents.size) {
                Text(
                    text = "Всего: ${allContragents.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// ✅ Функция для получения текстовой метки сортировки
private fun getSortLabel(sortBy: SortBy): String {
    return when (sortBy) {
        SortBy.NAME -> "По названию"
        SortBy.ORDERS_COUNT -> "По заказам"
        SortBy.TOTAL_SUM -> "По сумме"
        SortBy.AVERAGE_CHECK -> "По чеку"
    }
}