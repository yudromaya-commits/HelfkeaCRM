package com.example.repressales.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate

data class CalendarEvent(
    val id: Long,
    val title: String,
    val date: LocalDate,
    val clients: List<String>,
    val color: Color = Color(0xFF2196F3),
    val isTask: Boolean = false
)