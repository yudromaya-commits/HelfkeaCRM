package com.helfkea.crm.ui.contragents.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helfkea.crm.model.BasicContragent
import com.helfkea.crm.viewmodel.SnapContragentViewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class)
@Composable
fun SnapContragentDialog(
    individualId: String,
    individualName: String,
    onDismiss: () -> Unit,
    onContragentSnapped: () -> Unit
) {
    val viewModel: SnapContragentViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val filteredContragents by viewModel.filteredContragents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTypes by viewModel.selectedTypes.collectAsState()
    val availableTypes by viewModel.availableTypes.collectAsState()
    val snapStatus by viewModel.snapStatus.collectAsState()

    var showTypeFilter by remember { mutableStateOf(false) }

    // Загружаем данные при открытии
    LaunchedEffect(Unit) {
        viewModel.loadContragents()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp, max = 600.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Заголовок
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Привязать контрагента",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "К физлицу: $individualName",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Статус привязки
                snapStatus?.let { status ->
                    when (status) {
                        is SnapContragentViewModel.SnapStatus.Success -> {
                            AlertDialog(
                                onDismissRequest = { viewModel.clearSnapStatus() },
                                title = { Text("Успешно!") },
                                text = { Text(status.message) },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.clearSnapStatus()
                                            onContragentSnapped()
                                            onDismiss()
                                        }
                                    ) {
                                        Text("OK")
                                    }
                                }
                            )
                        }
                        is SnapContragentViewModel.SnapStatus.Error -> {
                            AlertDialog(
                                onDismissRequest = { viewModel.clearSnapStatus() },
                                title = { Text("Ошибка") },
                                text = {
                                    Column {
                                        Text(status.message)
                                        status.errorCode?.let {
                                            Text("Код: $it", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { viewModel.clearSnapStatus() }
                                    ) {
                                        Text("Закрыть")
                                    }
                                }
                            )
                        }
                    }
                }

                // Панель поиска и фильтров
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    label = { Text("Поиск контрагента") },
                    placeholder = { Text("Введите название, ИНН или адрес...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Поиск")
                    },
                    trailingIcon = {
                        Row {
                            // Кнопка фильтра по типам
                            IconButton(
                                onClick = { showTypeFilter = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Фильтр по типам",
                                    tint = if (selectedTypes.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }

                            // Кнопка обновления
                            IconButton(
                                onClick = { viewModel.refreshContragents() },
                                enabled = !isLoading,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Обновить",
                                    tint = if (isLoading) Color.Gray else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )

                // Выбранные фильтры
                if (selectedTypes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        selectedTypes.forEach { type ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.toggleTypeFilter(type) },
                                label = { Text(type, fontSize = 11.sp) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Убрать фильтр",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Список контрагентов
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        error != null -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Ошибка",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    error!!,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.refreshContragents() }
                                ) {
                                    Text("Повторить загрузку")
                                }
                            }
                        }

                        filteredContragents.isEmpty() -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = "Нет контрагентов",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Контрагенты не найдены",
                                    color = Color.Gray
                                )
                                if (searchQuery.isNotBlank() || selectedTypes.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Попробуйте изменить параметры поиска",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.clearFilters() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Text("Сбросить фильтры")
                                    }
                                }
                            }
                        }

                        else -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(filteredContragents) { contragent ->
                                    BasicContragentCard(
                                        contragent = contragent,
                                        onClick = {
                                            coroutineScope.launch { // <-- Используем scope
                                                viewModel.snapContragent(individualId, contragent.id)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог выбора типов
    if (showTypeFilter) {
        AlertDialog(
            onDismissRequest = { showTypeFilter = false },
            title = { Text("Выберите типы") },
            text = {
                Column {
                    availableTypes.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedTypes.contains(type),
                                onCheckedChange = { viewModel.toggleTypeFilter(type) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(type)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showTypeFilter = false }) {
                    Text("Готово")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTypeFilter = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicContragentCard(
    contragent: BasicContragent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Типы
            if (contragent.types.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    contragent.types.take(2).forEach { type ->
                        Surface(
                            color = getTypeColor(type).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = type,
                                color = getTypeColor(type),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (contragent.types.size > 2) {
                        Surface(
                            color = Color.Gray.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "+${contragent.types.size - 2}",
                                color = Color.Gray,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Название
            Text(
                text = contragent.name,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ИНН если есть
            contragent.inn?.let { inn ->
                if (inn.isNotBlank()) {
                    Text(
                        text = "ИНН: $inn",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            // Адрес если есть
            if (contragent.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Адрес",
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = contragent.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun getTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "доктор" -> Color(0xFF2196F3)
        "клиент" -> Color(0xFF4CAF50)
        "пациент" -> Color(0xFF9C27B0)
        "поставщик" -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }
}

@Composable
private fun getSegmentColor(segment: String): Color {
    return when (segment) {
        "VIP клиент" -> Color(0xFFD32F2F)
        "Постоянный клиент" -> Color(0xFF388E3C)
        "Новый клиент" -> Color(0xFF1976D2)
        "12+ месяцев" -> Color(0xFF7B1FA2)
        "Нет заказов" -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }
}