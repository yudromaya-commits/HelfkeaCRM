// Создаем новый файл: ModernTabRow.kt
package com.helfkea.crm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModernTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val selected = selectedTabIndex == index
            val tabModifier = Modifier.weight(1f)

            ModernTab(
                selected = selected,
                onClick = { onTabSelected(index) },
                modifier = tabModifier,
                text = title,
                containerColor = if (selected) selectedContainerColor else unselectedContainerColor,
                contentColor = if (selected) selectedContentColor else unselectedContentColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (selected) 4.dp else 1.dp),
        shape = MaterialTheme.shapes.medium,
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}