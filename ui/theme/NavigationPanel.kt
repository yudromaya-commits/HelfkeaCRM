package com.example.repressales.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val isSelected: Boolean = false
)

@Composable
fun NavigationPanel(
    modifier: Modifier = Modifier,
    onSectionSelected: (String) -> Unit = {}
) {
    var selectedItem by remember { mutableStateOf("Задачи") }

    val navItems = listOf(
        NavigationItem("Задачи", Icons.Default.Checklist, selectedItem == "Задачи"),
        NavigationItem("Календарь", Icons.Default.DateRange, selectedItem == "Календарь"),
        NavigationItem("Физлица", Icons.Default.Person, selectedItem == "Физлица"),
        NavigationItem("Юрлица", Icons.Default.Business, selectedItem == "Юрлица"),
        NavigationItem("Сделки", Icons.Default.AttachMoney, selectedItem == "Сделки"),
        NavigationItem("Аналитика", Icons.Default.Analytics, selectedItem == "Аналитика")
    )

    Column(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(Color(0xFF2C3E50))
            .verticalScroll(rememberScrollState())
    ) {
        // Убрали CompanyHeader и UserInfo

        // Разделы навигации
        NavigationSections(
            navItems = navItems,
            selectedItem = selectedItem,
            onItemSelected = { item ->
                selectedItem = item
                onSectionSelected(item)
            }
        )

        // Контактная информация
        ContactSupport()
    }
}

@Composable
private fun NavigationSections(
    navItems: List<NavigationItem>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        navItems.forEach { item ->
            NavigationItemRow(
                item = item,
                isSelected = item.title == selectedItem,
                onItemClick = { onItemSelected(item.title) }
            )
        }
    }
}

@Composable
private fun NavigationItemRow(
    item: NavigationItem,
    isSelected: Boolean,
    onItemClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF3498DB) else Color.Transparent
    val textColor = if (isSelected) Color.White else Color(0xFFBDC3C7)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onItemClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = textColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun ContactSupport() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Есть вопросы? Напишите нам!",
            color = Color(0xFFBDC3C7),
            fontSize = 14.sp,
            modifier = Modifier.clickable { /* открыть чат поддержки */ }
        )
    }
}