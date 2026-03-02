package com.example.repressales.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repressales.model.Contragent
import com.example.repressales.utils.toReadableDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContragentDetailScreen(
    contragent: Contragent,
    onBackClick: () -> Unit,
    onCreateTaskClick: () -> Unit
) {
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Тип лица
            val typeColor = when (contragent.type) {
                "Юрлицо" -> Color(0xFF2196F3)
                "ИП" -> Color(0xFF4CAF50)
                "Частное лицо" -> Color(0xFF9C27B0)
                else -> Color.Gray
            }

            Text(
                text = contragent.type,
                color = typeColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Название контрагента
            Text(
                text = "Контрагент",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Text(
                text = contragent.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Адрес
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Адрес",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = contragent.address,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Статистика
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Статистика",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Количество заказов
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Количество заказов:")
                        Text(
                            contragent.ordersCount.toString(),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Средний чек
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Средний чек:")
                        Text(
                            String.format("%,.0f ₽", contragent.averageCheck),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Общая сумма
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Общая сумма заказов:")
                        Text(
                            String.format("%,.0f ₽", contragent.totalOrdersSum),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Последний заказ
                    if (contragent.lastOrder.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Последний заказ:")
                            Text(
                                contragent.lastOrder.toReadableDateTime(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Сегмент
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Сегмент",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = contragent.segment,
                        style = MaterialTheme.typography.bodyLarge,
                        color = when (contragent.segment) {
                            "VIP клиент" -> Color(0xFFD32F2F)
                            "Постоянный клиент" -> Color(0xFF388E3C)
                            "Новый клиент" -> Color(0xFF1976D2)
                            "12+ месяцев" -> Color(0xFF7B1FA2)
                            "Нет заказов" -> Color.Gray
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Кнопка создания задачи
            Button(
                onClick = onCreateTaskClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.AddTask, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать задачу для этого контрагента")
            }
        }
    }
}