package com.helfkea.crm.ui.common.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate = LocalDate.now()
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Понедельник, 6 = Воскресенье
    
    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val monthNames = listOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )
    
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Заголовок
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Выбор даты",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Навигация по месяцам
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            currentMonth = currentMonth.minusMonths(1)
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Предыдущий месяц")
                    }
                    
                    Text(
                        text = "${monthNames[currentMonth.monthValue - 1]} ${currentMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    IconButton(
                        onClick = {
                            currentMonth = currentMonth.plusMonths(1)
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Следующий месяц")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Дни недели
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Календарь
                Column {
                    // Пустые ячейки для смещения первого дня
                    var dayCounter = 1
                    
                    for (week in 0 until 6) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (dayOfWeek in 0 until 7) {
                                val dayIndex = week * 7 + dayOfWeek
                                
                                if (week == 0 && dayOfWeek < dayOfWeekOffset) {
                                    // Пустая ячейка перед первым днем месяца
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(4.dp)
                                    )
                                } else if (dayCounter <= daysInMonth) {
                                    val currentDay = dayCounter
                                    val isSelected = selectedDate.year == currentMonth.year &&
                                            selectedDate.month == currentMonth.month &&
                                            selectedDate.dayOfMonth == currentDay
                                    val isToday = LocalDate.now().year == currentMonth.year &&
                                            LocalDate.now().month == currentMonth.month &&
                                            LocalDate.now().dayOfMonth == currentDay
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(4.dp)
                                            .clickable {
                                                selectedDate = LocalDate.of(
                                                    currentMonth.year,
                                                    currentMonth.month,
                                                    currentDay
                                                )
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(MaterialTheme.shapes.small)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                    else if (isToday) MaterialTheme.colorScheme.primaryContainer
                                                    else Color.Transparent
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = currentDay.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                    dayCounter++
                                } else {
                                    // Пустая ячейка после последнего дня месяца
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Выбранная дата
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedDate.format(
                            DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru"))
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Кнопка отмены
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Отмена")
                    }
                    
                    // Кнопка выбора
                    Button(
                        onClick = { onDateSelected(selectedDate) }
                    ) {
                        Text("Выбрать")
                    }
                }
            }
        }
    }
}