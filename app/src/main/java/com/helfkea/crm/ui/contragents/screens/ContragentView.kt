package com.helfkea.crm.ui.contragents.screens

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
import com.helfkea.crm.viewmodel.ContragentViewModel
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.text.style.TextOverflow
import com.helfkea.crm.utils.formatMoney
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log
import com.helfkea.crm.model.Contragent
import com.helfkea.crm.model.ContactAddress
import androidx.compose.runtime.rememberCoroutineScope
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.model.GetContragentByIdRequest
import com.helfkea.crm.model.GetContragentByIdFullResponse
import com.helfkea.crm.ui.contragents.components.ContragentFiltersPanel
import com.helfkea.crm.ui.contragents.dialogs.EnhancedCreateContragentDialog
import com.helfkea.crm.ui.common.components.CompactRelatedContragents

@Composable
fun ContragentView(
    modifier: Modifier = Modifier,
    onContragentClick: (com.helfkea.crm.model.Contragent) -> Unit = {},
    onCreateTaskClick: (com.helfkea.crm.model.Contragent) -> Unit = {}
) {
    val viewModel: ContragentViewModel = viewModel()
    val contragents by viewModel.contragents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    // Состояние для скрытия/показа фильтров
    var showFilters by remember { mutableStateOf(false) }

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
                    "Юридические лица",
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
                    onClick = { showCreateDialog = true },
                    enabled = !isLoading
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
                modifier = Modifier
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

    val scope = rememberCoroutineScope()
    
    // Функция для получения контрагента по ID
    fun openContragentById(contragentId: String) {
        Log.d("ContragentView", "✅ Контрагент создан с ID: $contragentId")
        scope.launch {
            try {
                Log.d("ContragentView", "🔄 ВЫЗЫВАЮ getContragentById для ID: $contragentId")
                
                // ВЫЗЫВАЕМ МЕТОД getContragentById
                val response = TaskApi.service.getContragentById(
                    GetContragentByIdRequest(contragentId)
                )
                
                Log.d("ContragentView", "📡 Ответ сервера: isSuccessful=${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    val fullResponse = response.body()
                    Log.d("ContragentView", "📡 Полный ответ: $fullResponse")
                    
                    if (fullResponse?.result == true && fullResponse.body != null) {
                        val contragent = fullResponse.body
                        Log.d("ContragentView", "📡 Контрагент: $contragent")
                        
                        if (contragent.id.isNotBlank()) {
                            Log.d("ContragentView", "✅ УСПЕХ! Метод getContragentById работает!")
                            Log.d("ContragentView", "✅ ID: ${contragent.id}, Имя: '${contragent.name}', ИНН: '${contragent.inn}'")
                            
                            // ПРОВЕРЯЕМ, ЕСТЬ ЛИ ДАННЫЕ В КОНТРАГЕНТЕ
                            if (contragent.name.isNotBlank() || contragent.inn != null) {
                                // Есть данные - открываем карточку
                                Log.d("ContragentView", "📱 Открываю карточку контрагента с данными")
                                onContragentClick(contragent)
                            } else {
                                // Данные пустые - обновляем список (пользователь увидит контрагента в списке)
                                Log.d("ContragentView", "ℹ️ Контрагент создан, но данные еще не загружены в 1С")
                                Log.d("ContragentView", "🔄 Обновляю список контрагентов")
                                
                                // Обновляем список - контрагент появится в списке
                                viewModel.refreshContragents()
                            }
                        } else {
                            Log.d("ContragentView", "❌ ОШИБКА: Контрагент не найден или пустой ID")
                            // Fallback: обновляем список
                            viewModel.refreshContragents()
                        }
                    } else {
                        Log.d("ContragentView", "❌ ОШИБКА от сервера: ${fullResponse?.error}")
                        // Fallback: обновляем список
                        viewModel.refreshContragents()
                    }
                } else {
                    Log.d("ContragentView", "❌ ОШИБКА HTTP: ${response.code()} - ${response.message()}")
                    // Fallback: обновляем список
                    viewModel.refreshContragents()
                }
                
            } catch (e: Exception) {
                Log.d("ContragentView", "❌ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
                // Fallback: обновляем список
                viewModel.refreshContragents()
            }
        }
    }
    
    // В конце Column после LazyColumn добавьте:
    if (showCreateDialog) {
        EnhancedCreateContragentDialog(
            isIndividual = false,
            onDismiss = { showCreateDialog = false },
            onCreateSuccess = { id, _, _ ->
                Log.d("ContragentView", "✅ onCreateSuccess ВЫЗВАН! ID: $id")
                if (id != null && id.isNotBlank()) {
                    Log.d("ContragentView", "✅ Контрагент создан, ID: $id")
                    openContragentById(id)
                } else {
                    Log.d("ContragentView", "✅ Контрагент создан, но ID не получен. Обновляю список.")
                    viewModel.refreshContragents()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContragentCard(
    contragent: com.helfkea.crm.model.Contragent,
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
            // Заголовок с типами и кнопкой создания задачи
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Типы (массив) - показываем до 2 основных
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    contragent.types.take(2).forEach { type ->
                        Surface(
                            color = getTypeColor(type).copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = type,
                                color = getTypeColor(type),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Если типов больше 2, показываем счетчик
                    if (contragent.types.size > 2) {
                        Surface(
                            color = Color.Gray.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "+${contragent.types.size - 2}",
                                color = Color.Gray,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
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
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ЗАКРЕПЛЕННЫЙ КОММЕНТАРИЙ (НОВОЕ)
            contragent.pinnedComment?.let { comment ->
                if (comment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFFFF8E1), // Светло-желтый фон
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, Color(0xFFFFD54F)), // Желтая рамка
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = "Закреплено",
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = comment,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Основной адрес (вместо старого адреса)
            val mainAddress = contragent.mainAddress
            if (mainAddress.isNotEmpty() && mainAddress != "Адрес не указан") {
                Spacer(modifier = Modifier.height(8.dp))
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
                        text = mainAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Телефоны, если есть (НОВОЕ)
            if (contragent.phones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "Телефон",
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = contragent.phones.first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1976D2),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (contragent.phones.size > 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = Color.Gray.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "+${contragent.phones.size - 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
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
                    value = contragent.averageCheck.formatMoney(),
                    color = Color(0xFF4CAF50)
                )

                // Общая сумма
                StatItem(
                    icon = Icons.Default.AccountBalance,
                    label = "Общая сумма",
                    value = contragent.totalOrdersSum.formatMoney(),
                    color = Color(0xFF9C27B0)
                )
            }

            // Связанные контрагенты (если есть)
            if (contragent.relatedContragents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                CompactRelatedContragents(
                    relatedContragents = contragent.relatedContragents.map { 
                        com.helfkea.crm.model.BasicContragent(
                            id = it.id,
                            name = it.name,
                            types = emptyList(),
                            address = "",
                            inn = ""
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Нижняя строка с дополнительной информацией
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Последний заказ
                if (contragent.lastOrder.isNotEmpty() && contragent.ordersCount > 0) {
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
                } else if (contragent.ordersCount == 0) {
                    Text(
                        text = "Нет заказов",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                // Сегмент с цветом
                Surface(
                    color = getSegmentColor(contragent.segment).copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = contragent.segment,
                        color = getSegmentColor(contragent.segment),
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

// Функция для получения цвета типа - ИСПРАВЛЕНО
private fun getTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "юрлицо" -> Color(0xFF2196F3)
        "ип" -> Color(0xFF4CAF50)
        "частное лицо" -> Color(0xFF9C27B0)
        "клиент" -> Color(0xFF4CAF50)
        "поставщик" -> Color(0xFFFF9800)
        "клиника" -> Color(0xFF009688)
        "доктор" -> Color(0xFF2196F3)
        "торговыйпредставитель" -> Color(0xFFFF5722)
        "прочиеотношения" -> Color(0xFF795548)
        else -> Color.Gray
    }
}

// Функция для получения цвета сегмента
private fun getSegmentColor(segment: String): Color {
    return when (segment) {
        "VIP клиент" -> Color(0xFFD32F2F)
        "Постоянный клиент" -> Color(0xFF388E3C)
        "Новый клиент" -> Color(0xFF1976D2)
        "12+ месяцев" -> Color(0xFF7B1FA2)
        "Нет заказов" -> Color.Gray
        else -> Color.Gray
    }
}

// Компактная версия для связанных контрагентов (если нет в отдельном файле)
