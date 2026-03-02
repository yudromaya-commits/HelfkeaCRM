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
import com.example.repressales.viewmodel.ContragentViewModel
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility

@Composable
fun ContragentView(
    modifier: Modifier = Modifier,
    onContragentClick: (com.example.repressales.model.Contragent) -> Unit = {},
    onCreateTaskClick: (com.example.repressales.model.Contragent) -> Unit = {}
) {
    val viewModel: ContragentViewModel = viewModel()
    val contragents by viewModel.contragents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Состояние для скрытия/показа фильтров
    var showFilters by remember { mutableStateOf(true) }

    // Загружаем данные при первом открытии
    LaunchedEffect(Unit) {
        viewModel.loadContragents()
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
            Column {
                Text(
                    "Юрлица",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Управление контрагентами",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Row {
                // Кнопка показа/скрытия фильтров
                IconButton(
                    onClick = { showFilters = !showFilters }
                ) {
                    Icon(
                        if (showFilters) Icons.Default.FilterAltOff else Icons.Default.FilterAlt,
                        contentDescription = if (showFilters) "Скрыть фильтры" else "Показать фильтры"
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshContragents() },
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        tint = if (isLoading) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                }

                // Временная кнопка "Создать юрлицо"
                Button(
                    onClick = { /* TODO: Добавить логику создания юрлица */ },
                    enabled = false
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = "Создать юрлицо")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Создать")
                }
            }
        }

        // Панель поиска и фильтров (показываем/скрываем с анимацией)
        AnimatedVisibility(
            visible = showFilters,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            ContragentFiltersPanel(
                modifier = Modifier  // ✅ ПЕРЕДАЕМ modifier без animateContentSize()
            )
        }

        // Состояние загрузки
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Загрузка контрагентов...")
                }
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
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Ошибка",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.refreshContragents() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Повторить загрузку")
                }
            }
        }

        // Список контрагентов
        if (contragents.isEmpty() && !isLoading && error == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = "Нет контрагентов",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Контрагенты не найдены",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Измените параметры поиска или фильтры",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.clearFilters() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.ClearAll, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Сбросить фильтры")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contragents) { contragent ->
                    ContragentCard(
                        contragent = contragent,
                        onContragentClick = { onContragentClick(contragent) },
                        onCreateTaskClick = { onCreateTaskClick(contragent) }
                    )
                }

                // Добавляем отступ внизу
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContragentCard(
    contragent: com.example.repressales.model.Contragent,
    onContragentClick: () -> Unit = {},
    onCreateTaskClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onContragentClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок с типом и кнопкой создания задачи
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Бейдж типа
                Surface(
                    color = when (contragent.type) {
                        "Юрлицо" -> Color(0xFF2196F3).copy(alpha = 0.1f)
                        "ИП" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        "Частное лицо" -> Color(0xFF9C27B0).copy(alpha = 0.1f)
                        else -> Color.Gray.copy(alpha = 0.1f)
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = contragent.type,
                        color = when (contragent.type) {
                            "Юрлицо" -> Color(0xFF2196F3)
                            "ИП" -> Color(0xFF4CAF50)
                            "Частное лицо" -> Color(0xFF9C27B0)
                            else -> Color.Gray
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Кнопка создания задачи
                IconButton(
                    onClick = onCreateTaskClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.AddTask,
                        contentDescription = "Создать задачу",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Название контрагента
            Text(
                text = contragent.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Адрес
            if (contragent.address.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Адрес",
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = contragent.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Статистика в виде индикаторов
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Количество заказов
                StatItem(
                    icon = Icons.Default.ShoppingCart,
                    label = "Заказов",
                    value = contragent.ordersCount.toString(),
                    color = Color(0xFF2196F3)
                )

                // Средний чек
                StatItem(
                    icon = Icons.Default.AttachMoney,
                    label = "Средний чек",
                    value = String.format("%,.0f ₽", contragent.averageCheck),
                    color = Color(0xFF4CAF50)
                )

                // Общая сумма
                StatItem(
                    icon = Icons.Default.AccountBalance,
                    label = "Общая сумма",
                    value = String.format("%,.0f ₽", contragent.totalOrdersSum),
                    color = Color(0xFF9C27B0)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Нижняя строка с дополнительной информацией
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Последний заказ
                if (contragent.lastOrder.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Последний заказ",
                            modifier = Modifier.size(12.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Последний: ${contragent.lastOrder.split(" ")[0]}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Сегмент с цветом
                Surface(
                    color = when (contragent.segment) {
                        "VIP клиент" -> Color(0xFFD32F2F).copy(alpha = 0.1f)
                        "Постоянный клиент" -> Color(0xFF388E3C).copy(alpha = 0.1f)
                        "Новый клиент" -> Color(0xFF1976D2).copy(alpha = 0.1f)
                        "12+ месяцев" -> Color(0xFF7B1FA2).copy(alpha = 0.1f)
                        else -> Color.Gray.copy(alpha = 0.1f)
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = contragent.segment,
                        color = when (contragent.segment) {
                            "VIP клиент" -> Color(0xFFD32F2F)
                            "Постоянный клиент" -> Color(0xFF388E3C)
                            "Новый клиент" -> Color(0xFF1976D2)
                            "12+ месяцев" -> Color(0xFF7B1FA2)
                            else -> Color.Gray
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(12.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}