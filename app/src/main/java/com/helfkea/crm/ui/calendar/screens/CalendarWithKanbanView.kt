package com.helfkea.crm.ui.calendar.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.helfkea.crm.model.Task
import com.helfkea.crm.ui.calendar.components.EnhancedKanbanView
import com.helfkea.crm.ui.calendar.screens.CalendarView

/**
 * Обертка для календаря с канбан-доской
 * Добавляет вкладки для переключения между календарем и канбаном
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarWithKanbanView(
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Календарь", "Канбан")

    Column(modifier = modifier.fillMaxSize()) {
        // Вкладки для переключения - УПРОЩЕННАЯ ВЕРСИЯ
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == index) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                )
            }
        }

        // Контент в зависимости от выбранной вкладки
        when (selectedTab) {
            0 -> CalendarView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                onTaskClick = onTaskClick
            )
            1 -> EnhancedKanbanView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                onTaskClick = onTaskClick
            )
        }
    }
}