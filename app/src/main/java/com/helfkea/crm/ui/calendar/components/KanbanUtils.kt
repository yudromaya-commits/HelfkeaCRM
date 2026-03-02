package com.helfkea.crm.ui.calendar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek

/**
 * Вспомогательные классы и функции для канбан-доски
 */

// Статистика колонки
data class ColumnStats(
    val total: Int = 0,
    val important: Int = 0,
    val overdue: Int = 0
)

// Фильтры по дате
enum class DateFilter(val displayName: String) {
    ALL("Все даты"),
    TODAY("Сегодня"),
    TOMORROW("Завтра"),
    THIS_WEEK("Эта неделя"),
    OVERDUE("Просроченные")
}

/**
 * Панель статистики канбана
 */
@Composable
fun KanbanStats(
    columnStats: Map<com.helfkea.crm.model.KanbanStatus, ColumnStats>,
    modifier: Modifier = Modifier
) {
    val totalTasks = columnStats.values.sumOf { it.total }
    val importantTasks = columnStats.values.sumOf { it.important }
    val overdueTasks = columnStats.values.sumOf { it.overdue }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                value = totalTasks.toString(),
                label = "Всего задач",
                icon = Icons.Default.List,
                color = MaterialTheme.colorScheme.primary
            )

            if (importantTasks > 0) {
                StatItem(
                    value = importantTasks.toString(),
                    label = "Важных",
                    icon = Icons.Default.PriorityHigh,
                    color = Color.Red
                )
            }

            if (overdueTasks > 0) {
                StatItem(
                    value = overdueTasks.toString(),
                    label = "Просрочено",
                    icon = Icons.Default.Schedule,
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Функции для работы с датами
 */

// Парсинг даты из строки
fun parseDate(dateString: String): LocalDate? {
    return try {
        when {
            dateString.contains(".") -> {
                val parts = dateString.split(" ")[0].split(".")
                if (parts.size == 3) {
                    val day = parts[0].toInt()
                    val month = parts[1].toInt()
                    val year = parts[2].toInt()
                    LocalDate.of(year, month, day)
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
                    LocalDate.of(year, month, day)
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

// Проверка, является ли дата сегодняшней
fun isToday(dateString: String): Boolean {
    val date = parseDate(dateString) ?: return false
    val today = LocalDate.now()
    return date == today
}

// Проверка, является ли дата завтрашней
fun isTomorrow(dateString: String): Boolean {
    val date = parseDate(dateString) ?: return false
    val tomorrow = LocalDate.now().plusDays(1)
    return date == tomorrow
}

// Проверка, является ли дата просроченной
fun isOverdue(dateString: String): Boolean {
    val date = parseDate(dateString) ?: return false
    val today = LocalDate.now()
    return date.isBefore(today)
}

// Проверка, находится ли дата на этой неделе
fun isThisWeek(dateString: String): Boolean {
    val date = parseDate(dateString) ?: return false
    val today = LocalDate.now()
    
    // Начало недели (понедельник)
    val startOfWeek = today.minusDays(today.dayOfWeek.value - 1L)
    // Конец недели (воскресенье)
    val endOfWeek = startOfWeek.plusDays(6)
    
    return !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek)
}

// Получение дня недели из даты
fun getDayOfWeek(dateString: String): String {
    val date = parseDate(dateString) ?: return ""
    val daysOfWeek = listOf(
        "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"
    )
    return daysOfWeek[date.dayOfWeek.value - 1]
}

// Форматирование даты в читаемый вид
fun formatDate(dateString: String): String {
    val date = parseDate(dateString) ?: return dateString
    
    return when {
        isToday(dateString) -> "Сегодня"
        isTomorrow(dateString) -> "Завтра"
        isOverdue(dateString) -> "Просрочено"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            date.format(formatter)
        }
    }
}