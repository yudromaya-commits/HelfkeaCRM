package com.helfkea.crm.ui.individuals.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helfkea.crm.model.Individual
import com.helfkea.crm.ui.common.components.CreateInteractionSidePanel
import com.helfkea.crm.ui.common.components.ContactsSection
import com.helfkea.crm.ui.common.components.InteractionCard
import com.helfkea.crm.ui.contragents.components.RelatedContragentsSection
import com.helfkea.crm.utils.formatMoney
import com.helfkea.crm.utils.toReadableDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualDetailScreen(
    individual: Individual,
    onBackClick: () -> Unit,
    onCreateTaskClick: () -> Unit,
    onCreateInteraction: () -> Unit = {},
            onSnapContragentClick: () -> Unit // ДОБАВЛЯЕМ НОВЫЙ ПАРАМЕТР
) {
    var showInteractionPanel by remember { mutableStateOf(false) }

    // Состояние для управления затемнением
    var overlayClick by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ОСНОВНОЕ СОДЕРЖИМОЕ КАРТОЧКИ
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Карточка физлица") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    actions = {
                        // Кнопка привязки контрагента - ДОБАВЛЯЕМ ПЕРВОЙ
                        IconButton(onClick = onSnapContragentClick) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = "Привязать контрагента",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onCreateTaskClick) {
                            Icon(Icons.Default.AddTask, contentDescription = "Создать задачу")
                        }
                        IconButton(
                            onClick = { showInteractionPanel = true },
                            enabled = !showInteractionPanel
                        ) {
                            Icon(
                                Icons.Default.AddComment,
                                contentDescription = "Добавить взаимодействие",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Основная информация
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Типы контрагента
                        if (individual.types.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                individual.types.forEach { type ->
                                    Surface(
                                        color = getTypeColor(type).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = type,
                                            color = getTypeColor(type),
                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 3.dp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Название контрагента
                        Text(
                            text = "Физическое лицо",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = individual.name,
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.Bold
                        )

                        // Закрепленный комментарий
                        individual.pinnedComment?.let { comment ->
                            if (comment.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = Color(0xFFFFF8E1),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFD54F))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.PushPin,
                                            contentDescription = "Закреплено",
                                            tint = Color(0xFFF57C00),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Закрепленный комментарий",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = Color(0xFFF57C00),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = comment,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                                color = Color(0xFF5D4037)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Статистика
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Заказы",
                                value = individual.ordersCount.toString(),
                                icon = Icons.Default.ShoppingCart,
                                color = Color(0xFF2196F3),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Средний чек",
                                value = individual.averageCheck.formatMoney(),
                                icon = Icons.Default.AttachMoney,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Общая сумма",
                                value = individual.totalOrdersSum.formatMoney(),
                                icon = Icons.Default.AccountBalance,
                                color = Color(0xFF9C27B0),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Сегмент",
                                value = individual.segment,
                                icon = Icons.Default.TrendingUp,
                                color = getSegmentColor(individual.segment),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Последний заказ
                        if (individual.lastOrder.isNotEmpty() && individual.ordersCount > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = Color(0xFFE8F5E9).copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = "Последний заказ",
                                        tint = Color(0xFF388E3C),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Последний заказ: ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                    Text(
                                        individual.lastOrder.toReadableDateTime(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                            }
                        }
                    }
                }

                // Контакты и адреса
                if (individual.contactsAndAddresses.isNotEmpty()) {
                    item {
                        ContactsSection(
                            contacts = individual.contactsAndAddresses,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Взаимодействия
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "История взаимодействий",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { showInteractionPanel = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFD32F2F)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Добавить")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Добавить взаимодействие")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (individual.interactions.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.HistoryToggleOff,
                                            contentDescription = "Нет взаимодействий",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Нет истории взаимодействий",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    individual.interactions.forEach { interaction ->
                                        InteractionCard(interaction = interaction)
                                    }
                                }
                            }
                        }
                    }
                }

                // Связанные контрагенты
                if (individual.relatedContragents.isNotEmpty()) {
                    item {
                        RelatedContragentsSection(
                            relatedContragents = individual.relatedContragents,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Кнопка создания задачи
                item {
                    Button(
                        onClick = onCreateTaskClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddTask, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Создать задачу")
                    }
                }
            }
        }

        // ЗАТЕМНЕНИЕ ФОНА (прозрачный кликабельный слой)
        if (showInteractionPanel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { showInteractionPanel = false }
            )
        }

        // САЙД-ПАНЕЛЬ (должна быть поверх затемнения)
        if (showInteractionPanel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp), // Отступ слева для затемнения
                contentAlignment = Alignment.CenterEnd
            ) {
                CreateInteractionSidePanel(
                    contragentId = individual.id,
                    contragentName = individual.name,
                    isVisible = showInteractionPanel,
                    onClose = { showInteractionPanel = false },
                    onInteractionCreated = onCreateInteraction
                )
            }
        }
    }
}

// Функции для получения цветов
private fun getTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "доктор" -> Color(0xFF2196F3)
        "клиент" -> Color(0xFF4CAF50)
        "пациент" -> Color(0xFF9C27B0)
        "поставщик" -> Color(0xFFFF9800)
        else -> Color.Gray
    }
}

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

// Красивая версия StatCard как в ContragentDetailScreen
@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}