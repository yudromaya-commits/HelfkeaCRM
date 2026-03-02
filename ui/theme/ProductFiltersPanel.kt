package com.example.repressales.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repressales.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFiltersPanel(
    modifier: Modifier = Modifier
) {
    val viewModel: ProductViewModel = viewModel()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showCategoryFilter by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Поисковая строка
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Поиск товаров") },
                placeholder = { Text("Название или артикул...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Поиск")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.updateSearchQuery("") }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Панель фильтров
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Фильтр по категории
                FilterChip(
                    selected = selectedCategory != null,
                    onClick = { showCategoryFilter = true },
                    label = {
                        Text(
                            selectedCategory ?: "Все категории",
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = "Категория",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Кнопка сброса фильтров
                IconButton(
                    onClick = {
                        viewModel.clearFilters()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.ClearAll,
                        contentDescription = "Сбросить фильтры",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Выпадающее меню для фильтра по категории
    DropdownMenu(
        expanded = showCategoryFilter,
        onDismissRequest = { showCategoryFilter = false },
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        DropdownMenuItem(
            text = { Text("Все категории") },
            onClick = {
                viewModel.updateSelectedCategory(null)
                showCategoryFilter = false
            }
        )
        Divider()
        categories.forEach { category ->
            DropdownMenuItem(
                text = { Text(category) },
                onClick = {
                    viewModel.updateSelectedCategory(category)
                    showCategoryFilter = false
                }
            )
        }
    }
}