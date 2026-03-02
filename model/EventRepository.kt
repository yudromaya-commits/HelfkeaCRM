package com.example.repressales.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate

object EventRepository {
    fun getSampleEvents(): List<CalendarEvent> {
        return listOf(
            CalendarEvent(
                id = 1,
                title = "ИП Суханов, ИП Кравченко ООО Дентал",
                date = LocalDate(2024, 11, 1),
                clients = listOf("ИП Суханов", "ИП Кравченко", "ООО Дентал")
            ),
            CalendarEvent(
                id = 2,
                title = "Задача: завести карточку на платеж ИП",
                date = LocalDate(2024, 11, 3),
                clients = emptyList(),
                color = Color(0xFFF44336),
                isTask = true
            ),
            CalendarEvent(
                id = 3,
                title = "ИП Иванов ИП Петров ООО Эуб",
                date = LocalDate(2024, 11, 4),
                clients = listOf("ИП Иванов", "ИП Петров", "ООО Эуб")
            )
        )
    }

    fun getEventsForDay(date: LocalDate): List<CalendarEvent> {
        return getSampleEvents().filter { it.date == date }
    }
}