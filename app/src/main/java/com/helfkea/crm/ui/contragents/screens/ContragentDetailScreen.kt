package com.helfkea.crm.ui.contragents.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.helfkea.crm.model.Contragent
import com.helfkea.crm.ui.common.components.CreateInteractionSidePanel
import com.helfkea.crm.ui.common.components.ContactsSection
import com.helfkea.crm.ui.common.components.InteractionCard
import com.helfkea.crm.ui.contragents.components.RelatedContragentsSection
import com.helfkea.crm.utils.formatMoney
import com.helfkea.crm.utils.toReadableDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContragentDetailScreen(
    contragent: Contragent,
    onBackClick: () -> Unit,
    onCreateTaskClick: () -> Unit,
    onCreateInteraction: () -> Unit = {}
) {
    var showInteractionPanel by remember { mutableStateOf(false) }

    // Состояние для управления затемнением
    var overlayClick by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ОСНОВНОЕ СОДЕРЖИМОЕ КАРТОЧКИ
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Карточка контрагента") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    actions = {
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
                        if (contragent.types.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                contragent.types.forEach { type ->
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
                            text = "Контрагент",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = contragent.name,
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.Bold
                        )

                        // Закрепленный комментарий
                        contragent.pinnedComment?.let { comment ->
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
                                value = contragent.ordersCount.toString(),
                                icon = Icons.Default.ShoppingCart,
                                color = Color(0xFF2196F3),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Средний чек",
                                value = contragent.averageCheck.formatMoney(),
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
                                value = contragent.totalOrdersSum.formatMoney(),
                                icon = Icons.Default.AccountBalance,
                                color = Color(0xFF9C27B0),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Сегмент",
                                value = contragent.segment,
                                icon = Icons.Default.TrendingUp,
                                color = getSegmentColor(contragent.segment),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Последний заказ
                        if (contragent.lastOrder.isNotEmpty() && contragent.ordersCount > 0) {
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
                                        contragent.lastOrder.toReadableDateTime(),
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
                if (contragent.contactsAndAddresses.isNotEmpty()) {
                    item {
                        ContactsSection(
                            contacts = contragent.contactsAndAddresses,
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

                            if (contragent.interactions.isEmpty()) {
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
                                    contragent.interactions.forEach { interaction ->
                                        InteractionCard(interaction = interaction)
                                    }
                                }
                            }
                        }
                    }
                }

                // Связанные контрагенты
                if (contragent.relatedContragents.isNotEmpty()) {
                    item {
                        RelatedContragentsSection(
                            relatedContragents = contragent.relatedContragents,
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
                    contragentId = contragent.id,
                    contragentName = contragent.name,
                    isVisible = showInteractionPanel,
                    onClose = { showInteractionPanel = false },
                    onInteractionCreated = onCreateInteraction
                )
            }
        }
    }
}

@Composable
fun StatCard(
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

@Composable
fun RelatedContragentsSection(
    relatedContragents: List<com.helfkea.crm.model.RelatedContragent>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Связанные контрагенты",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            relatedContragents.forEach { related ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = related.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

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

private fun getSegmentColor(segment: String): Color {
    return when (segment) {
        "VIP клиент" -> Color(0xFFD32F2F)
        "Постоянный клиент" -> Color(0xFF388E3C)
        "Новый клиент" -> Color(0xFF1976D2)
        "12+ месяцев" -> Color(0xFF7B1FA2)
        "0-9 месяцев" -> Color(0xFF1976D2)
        "Нет заказов" -> Color.Gray
        else -> Color.Gray
    }
}



private fun getInteractionIcon(type: String?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type?.lowercase()) {
        "звонок" -> Icons.Default.Phone
        "посещение" -> Icons.Default.MeetingRoom
        "email" -> Icons.Default.Email
        else -> Icons.Default.History
    }
}

private fun getInteractionColor(type: String?): Color {
    return when (type?.lowercase()) {
        "звонок" -> Color(0xFF1976D2)
        "посещение" -> Color(0xFF388E3C)
        "email" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }
}