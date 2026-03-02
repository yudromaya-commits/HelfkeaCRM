package com.helfkea.crm.ui.individuals.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helfkea.crm.model.Individual
import com.helfkea.crm.utils.formatMoney
import com.helfkea.crm.ui.common.components.CompactRelatedContragents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualCard(
    individual: Individual,
    onIndividualClick: () -> Unit = {},
    onCreateTaskClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onIndividualClick,
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
                    individual.types.take(2).forEach { type ->
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
                    if (individual.types.size > 2) {
                        Surface(
                            color = Color.Gray.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "+${individual.types.size - 2}",
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
                text = individual.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ЗАКРЕПЛЕННЫЙ КОММЕНТАРИЙ (НОВОЕ)
            individual.pinnedComment?.let { comment ->
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
            val mainAddress = individual.mainAddress
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
            if (individual.phones.isNotEmpty()) {
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
                        text = individual.phones.first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1976D2),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (individual.phones.size > 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = Color.Gray.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "+${individual.phones.size - 1}",
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
                    value = individual.ordersCount.toString(),
                    color = Color(0xFF2196F3)
                )

                // Средний чек
                StatItem(
                    icon = Icons.Default.AttachMoney,
                    label = "Средний чек",
                    value = individual.averageCheck.formatMoney(),
                    color = Color(0xFF4CAF50)
                )

                // Общая сумма
                StatItem(
                    icon = Icons.Default.AccountBalance,
                    label = "Общая сумма",
                    value = individual.totalOrdersSum.formatMoney(),
                    color = Color(0xFF9C27B0)
                )
            }

            // Связанные контрагенты (если есть)
            if (individual.relatedContragents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                CompactRelatedContragents(
                    relatedContragents = individual.relatedContragents.map { 
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
                if (individual.lastOrder.isNotEmpty() && individual.ordersCount > 0) {
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
                            text = "Последний: ${individual.lastOrder.split(" ")[0]}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                } else if (individual.ordersCount == 0) {
                    Text(
                        text = "Нет заказов",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                // Сегмент с цветом
                Surface(
                    color = getSegmentColor(individual.segment).copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = individual.segment,
                        color = getSegmentColor(individual.segment),
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
fun StatCard(
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

@Composable
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

@Composable
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

// Локальная версия StatItem с иконкой - компактная как раньше
@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
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