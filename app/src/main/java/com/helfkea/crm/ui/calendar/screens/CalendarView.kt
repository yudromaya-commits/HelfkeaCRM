package com.helfkea.crm.ui.calendar.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helfkea.crm.model.CalendarEvent
import com.helfkea.crm.model.Task
import com.helfkea.crm.utils.toReadableDate
import com.helfkea.crm.utils.toReadableDateTime
import com.helfkea.crm.viewmodel.TaskViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {}
) {
    val viewModel: TaskViewModel = viewModel()
    // ИЗМЕНЯЕМ: используем filteredTasks вместо tasks
    val filteredTasks by viewModel.filteredTasks.collectAsState()

    // Загружаем задачи при открытии календаря
    LaunchedEffect(Unit) {
        viewModel.loadAllTasks() // ИЗМЕНЯЕМ: loadAllTasks вместо loadTasks
    }

    // Текущая дата
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    // Управление текущим месяцем
    var currentYear by remember { mutableStateOf(currentDate.year) }
    var currentMonthNumber by remember { mutableStateOf(currentDate.monthNumber) }

    // Состояние для выбранной даты и модального окна
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Преобразуем задачи в события календаря
    val calendarEvents = filteredTasks.mapNotNull { task -> // ИЗМЕНЯЕМ: используем filteredTasks
        try {
            val taskDate = parseDateFromString(task.executionDate)
            if (taskDate != null) {
                CalendarEvent(
                    id = task.name.hashCode().toLong(),
                    title = task.name,
                    date = taskDate,
                    clients = listOf(task.producer),
                    color = when (task.status) {
                        "Просрочена" -> Color(0xFFF44336)
                        "Выполнена" -> Color(0xFF4CAF50)
                        "В работе" -> Color(0xFFFF9800)
                        else -> Color(0xFF2196F3)
                    },
                    isTask = true
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        // Заголовок месяца
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = {
                if (currentMonthNumber > 1) {
                    currentMonthNumber--
                } else {
                    currentMonthNumber = 12
                    currentYear--
                }
            }) {
                Icon(Icons.Default.ArrowBack, "Предыдущий месяц")
            }

            Text(
                "${getMonthName(currentMonthNumber)} $currentYear",
                style = MaterialTheme.typography.headlineSmall
            )

            IconButton(onClick = {
                if (currentMonthNumber < 12) {
                    currentMonthNumber++
                } else {
                    currentMonthNumber = 1
                    currentYear++
                }
            }) {
                Icon(Icons.Default.ArrowForward, "Следующий месяц")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Дни недели
        Row(Modifier.fillMaxWidth()) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Сетка календаря
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            val calendarDays = generateCalendarDays(currentYear, currentMonthNumber)
            items(calendarDays) { day ->
                val dayEvents = calendarEvents.filter { it.date == day }
                CalendarDayCell(
                    day = day,
                    events = dayEvents,
                    currentMonth = currentMonthNumber,
                    onDayClick = { date ->
                        selectedDate = date
                        showBottomSheet = true
                    }
                )
            }
        }
    }

    // Bottom Sheet для отображения задач выбранного дня
    if (showBottomSheet && selectedDate != null) {
        // Фильтруем задачи для выбранной даты
        val tasksForSelectedDate = filteredTasks.filter { task -> // ИЗМЕНЯЕМ: используем filteredTasks
            val taskDate = parseDateFromString(task.executionDate)
            taskDate == selectedDate
        }

        DayTasksBottomSheet(
            date = selectedDate!!,
            tasks = tasksForSelectedDate,
            onDismiss = {
                showBottomSheet = false
                selectedDate = null
            },
            onTaskClick = onTaskClick
        )
    }
}

// Остальные функции остаются БЕЗ ИЗМЕНЕНИЙ...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDayCell(
    day: LocalDate,
    events: List<CalendarEvent>,
    currentMonth: Int,
    onDayClick: (LocalDate) -> Unit
) {
    val dayNumber = day.dayOfMonth
    val isToday = day == Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val isCurrentMonth = day.monthNumber == currentMonth

    Card(
        modifier = Modifier
            .padding(1.dp)
            .aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isToday -> Color(0xFFE3F2FD)
                !isCurrentMonth -> Color(0xFFF5F5F5)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        onClick = { onDayClick(day) }
    ) {
        Column(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxSize()
        ) {
            // Номер дня вверху - МЕНЬШЕ ОТСТУПОВ
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(start = 1.dp, top = 1.dp),
                color = when {
                    isToday -> MaterialTheme.colorScheme.primary
                    !isCurrentMonth -> Color.Gray
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )

            // Задачи - БОЛЬШЕ МЕСТА, БОЛЬШЕ ШРИФТ
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 1.dp), // Уменьшили отступ сверху
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                events.take(3).forEach { event ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp) // Убрали горизонтальные отступы
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(event.color.copy(alpha = 0.15f))
                            .border(
                                width = 0.5.dp,
                                color = event.color,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                    ) {
                        Text(
                            text = event.title.take(14) + if (event.title.length > 14) "..." else "", // Увеличили до 14 символов
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), // УВЕЛИЧИЛИ ШРИФТ
                            color = event.color,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(horizontal = 2.dp, vertical = 1.dp) // Минимальные отступы
                                .fillMaxWidth()
                        )
                    }
                }

                // Если задач больше 3, показываем счетчик
                if (events.size > 3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(Color.Gray.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "+${events.size - 3} ещё",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), // Увеличили
                            color = Color.Gray,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(horizontal = 2.dp, vertical = 1.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayTasksBottomSheet(
    date: LocalDate,
    tasks: List<Task>,
    onDismiss: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Задачи на ${formatDateForDisplay(date)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = "На эту дату задач нет",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Найдено задач: ${tasks.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Список задач
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks) { task ->
                        TaskItemCard(
                            task = task,
                            onClick = { onTaskClick(task) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItemCard(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Статус и важность
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusColor = when (task.status) {
                    "Просрочена" -> Color.Red
                    "Назначена" -> Color.Blue
                    "Выполнена" -> Color.Green
                    "В работе" -> Color(0xFFFF9800)
                    else -> Color.Gray
                }

                Text(
                    text = task.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )

                if (task.important) {
                    Text(
                        text = "❗ Важная",
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Название задачи
            Text(
                text = task.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            // Описание задачи
            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Информация о датах и исполнителе
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Создана:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        task.date.toReadableDateTime(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Исполнитель:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        task.producer.ifEmpty { "Не назначен" },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// Функции для работы с датами

// Функция для парсинга даты из строки
private fun parseDateFromString(dateString: String): LocalDate? {
    return try {
        when {
            dateString.contains(".") -> {
                val parts = dateString.split(" ")[0].split(".")
                if (parts.size == 3) {
                    val day = parts[0].toInt()
                    val month = parts[1].toInt()
                    val year = parts[2].toInt()
                    LocalDate(year, month, day)
                } else {
                    null
                }
            }
            dateString.contains("-") -> {
                val parts = dateString.split(" ")[0].split("-")
                if (parts.size == 3) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt()
                    val day = parts[2].toInt()
                    LocalDate(year, month, day)
                } else {
                    null
                }
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

// Функция для получения названия месяца
private fun getMonthName(monthNumber: Int): String {
    return when (monthNumber) {
        1 -> "ЯНВАРЬ"
        2 -> "ФЕВРАЛЬ"
        3 -> "МАРТ"
        4 -> "АПРЕЛЬ"
        5 -> "МАЙ"
        6 -> "ИЮНЬ"
        7 -> "ИЮЛЬ"
        8 -> "АВГУСТ"
        9 -> "СЕНТЯБРЬ"
        10 -> "ОКТЯБРЬ"
        11 -> "НОЯБРЬ"
        12 -> "ДЕКАБРЬ"
        else -> "НЕИЗВЕСТНО"
    }
}

// Функция для форматирования даты для отображения
private fun formatDateForDisplay(date: LocalDate): String {
    val day = date.dayOfMonth
    val month = when (date.monthNumber) {
        1 -> "января"
        2 -> "февраля"
        3 -> "марта"
        4 -> "апреля"
        5 -> "мая"
        6 -> "июня"
        7 -> "июля"
        8 -> "августа"
        9 -> "сентября"
        10 -> "октября"
        11 -> "ноября"
        12 -> "декабря"
        else -> ""
    }
    val year = date.year
    return "$day $month $year"
}

// Функция для генерации дней календаря с правильным смещением
// Функция для генерации дней календаря с правильным смещением
private fun generateCalendarDays(year: Int, month: Int): List<LocalDate> {
    val days = mutableListOf<LocalDate>()

    // Создаем Calendar для работы с датами
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    // Определяем первый день недели (1 = воскресенье, 2 = понедельник, etc.)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    // В Kotlin понедельник = 1, воскресенье = 7
    // В Calendar понедельник = 2, воскресенье = 1
    // Корректируем смещение: нам нужно чтобы понедельник был первым днем недели
    val daysFromPrevMonth = when (firstDayOfWeek) {
        Calendar.MONDAY -> 0      // Понедельник
        Calendar.TUESDAY -> 1     // Вторник
        Calendar.WEDNESDAY -> 2   // Среда
        Calendar.THURSDAY -> 3    // Четверг
        Calendar.FRIDAY -> 4      // Пятница
        Calendar.SATURDAY -> 5    // Суббота
        Calendar.SUNDAY -> 6      // Воскресенье
        else -> 0
    }

    // Добавляем дни предыдущего месяца для заполнения первой недели
    calendar.add(Calendar.DAY_OF_MONTH, -daysFromPrevMonth)
    for (i in 0 until daysFromPrevMonth) {
        days.add(LocalDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        ))
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    // Добавляем дни текущего месяца
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (day in 1..daysInMonth) {
        days.add(LocalDate(year, month, day))
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    // ИЗМЕНЕНО: Заполняем только 5 недель (35 дней) вместо 6 недель (42 дня)
    val totalDaysNeeded = 35 // 5 недель × 7 дней
    val remainingDays = totalDaysNeeded - days.size

    for (i in 1..remainingDays) {
        days.add(LocalDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        ))
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    return days
}