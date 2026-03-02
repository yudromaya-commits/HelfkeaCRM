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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    onDismissRequest: () -> Unit,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    initialDateTime: LocalDateTime = LocalDateTime.now()
) {
    var selectedDate by remember { mutableStateOf(initialDateTime.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(initialDateTime.toLocalTime()) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var showTimePicker by remember { mutableStateOf(false) }
    
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
                        text = if (showTimePicker) "Выбор времени" else "Выбор даты и времени",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (showTimePicker) {
                    // TimePicker
                    TimePickerSection(
                        selectedTime = selectedTime,
                        onTimeChanged = { newTime ->
                            selectedTime = newTime
                        }
                    )
                } else {
                    // DatePicker
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
                        var dayCounter = 1
                        
                        for (week in 0 until 6) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (dayOfWeek in 0 until 7) {
                                    val dayIndex = week * 7 + dayOfWeek
                                    
                                    if (week == 0 && dayOfWeek < dayOfWeekOffset) {
                                        // Пустая ячейка
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
                                        // Пустая ячейка
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
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Выбранные дата и время
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
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%02d:%02d", selectedTime.hour, selectedTime.minute),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Кнопки навигации и выбора
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Кнопка переключения между датой и временем
                    Button(
                        onClick = { showTimePicker = !showTimePicker },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            if (showTimePicker) Icons.Default.CalendarToday else Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showTimePicker) "Выбрать дату" else "Выбрать время")
                    }
                    
                    Row {
                        // Кнопка отмены
                        TextButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Отмена")
                        }
                        
                        // Кнопка выбора
                        Button(
                            onClick = {
                                val selectedDateTime = LocalDateTime.of(selectedDate, selectedTime)
                                onDateTimeSelected(selectedDateTime)
                            }
                        ) {
                            Text("Выбрать")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerSection(
    selectedTime: LocalTime,
    onTimeChanged: (LocalTime) -> Unit
) {
    var hours by remember { mutableStateOf(selectedTime.hour) }
    var minutes by remember { mutableStateOf(selectedTime.minute) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Большие цифры времени
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Часы
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        hours = (hours + 1) % 24
                        onTimeChanged(LocalTime.of(hours, minutes))
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.ArrowDropUp, contentDescription = "Увеличить часы")
                }
                
                Text(
                    text = String.format("%02d", hours),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = {
                        hours = if (hours == 0) 23 else hours - 1
                        onTimeChanged(LocalTime.of(hours, minutes))
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Уменьшить часы")
                }
                
                Text(
                    text = "часы",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            // Двоеточие
            Text(
                text = ":",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Минуты
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        minutes = (minutes + 1) % 60
                        onTimeChanged(LocalTime.of(hours, minutes))
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.ArrowDropUp, contentDescription = "Увеличить минуты")
                }
                
                Text(
                    text = String.format("%02d", minutes),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = {
                        minutes = if (minutes == 0) 59 else minutes - 1
                        onTimeChanged(LocalTime.of(hours, minutes))
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Уменьшить минуты")
                }
                
                Text(
                    text = "минуты",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Быстрый выбор времени
        Text(
            text = "Быстрый выбор:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                "09:00" to LocalTime.of(9, 0),
                "12:00" to LocalTime.of(12, 0),
                "15:00" to LocalTime.of(15, 0),
                "18:00" to LocalTime.of(18, 0)
            ).forEach { (label, time) ->
                FilterChip(
                    selected = hours == time.hour && minutes == time.minute,
                    onClick = {
                        hours = time.hour
                        minutes = time.minute
                        onTimeChanged(time)
                    },
                    label = { Text(label) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}