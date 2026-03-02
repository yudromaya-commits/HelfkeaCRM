package com.helfkea.crm.ui.deals.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealFiltersPanel(
    searchQuery: String,
    selectedCategory: String?,
    categories: List<String>,
    filteredProductsCount: Int, // Правильное количество товаров
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCategoryMenu by remember { mutableStateOf(false) }

    // Фильтруем реальные категории (без "Все" и "Без категории")
    val realCategories = categories.filterNot {
        it == "Все" || it == "Без категории"
    }
    val realCategoriesCount = realCategories.size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Компактный поиск
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Поиск") },
                placeholder = { Text("Название или артикул") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Поиск",
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Очистить",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Компактная строка с фильтрами и статистикой
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка категории
                FilterChip(
                    selected = selectedCategory != null,
                    onClick = { showCategoryMenu = true },
                    label = {
                        Text(
                            selectedCategory ?: "Все категории",
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = "Категория",
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderWidth = 0.5.dp
                    ),
                    modifier = Modifier.height(28.dp)
                )

                // Статистика
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "$filteredProductsCount шт",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$realCategoriesCount кат.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Кнопка обновления
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Выпадающее меню категорий
    DropdownMenu(
        expanded = showCategoryMenu,
        onDismissRequest = { showCategoryMenu = false },
        modifier = Modifier.fillMaxWidth(0.6f)
    ) {
        categories.forEach { category ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = category,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                },
                onClick = {
                    onCategorySelect(if (category == "Все") null else category)
                    showCategoryMenu = false
                },
                modifier = Modifier.height(36.dp)
            )
        }
    }
}