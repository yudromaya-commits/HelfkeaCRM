package com.helfkea.crm.ui.common.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helfkea.crm.model.Contragent
import com.helfkea.crm.viewmodel.ContragentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectContragentDialog(
    currentContragentId: String = "",
    currentContragentName: String = "",
    onDismiss: () -> Unit,
    onContragentSelected: (contragentId: String, contragentName: String) -> Unit
) {
    val contragentViewModel: ContragentViewModel = viewModel()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedContragentId by remember { mutableStateOf(currentContragentId) }
    var selectedContragentName by remember { mutableStateOf(currentContragentName) }
    
    // Загружаем контрагентов при открытии
    LaunchedEffect(Unit) {
        contragentViewModel.loadContragents()
    }
    
    val contragents by contragentViewModel.contragents.collectAsState()
    val isLoading by contragentViewModel.isLoading.collectAsState()
    
    // Фильтруем контрагентов по поисковому запросу
    val filteredContragents = if (searchQuery.isBlank()) {
        contragents
    } else {
        contragents.filter { contragent ->
            contragent.name.contains(searchQuery, ignoreCase = true) ||
            contragent.inn?.contains(searchQuery, ignoreCase = true) == true
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
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
                        text = "Выбор контрагента",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Поиск
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск контрагента") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Очистить")
                            }
                        }
                    } else null
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Список контрагентов
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (filteredContragents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) {
                                    "Контрагенты не найдены"
                                } else {
                                    "Нет доступных контрагентов"
                                },
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Опция "Без контрагента"
                        item {
                            Card(
                                onClick = {
                                    selectedContragentId = ""
                                    selectedContragentName = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedContragentId.isEmpty()) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.PersonOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Без контрагента",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        items(filteredContragents) { contragent ->
                            Card(
                                onClick = {
                                    selectedContragentId = contragent.id
                                    selectedContragentName = contragent.name
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedContragentId == contragent.id) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = contragent.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (contragent.inn?.isNotEmpty() == true) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "ИНН: ${contragent.inn}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Кнопка отмены
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Отмена")
                    }
                    
                    // Кнопка выбора
                    Button(
                        onClick = {
                            onContragentSelected(selectedContragentId, selectedContragentName)
                            onDismiss()
                        },
                        enabled = selectedContragentId.isNotEmpty() || selectedContragentName.isEmpty()
                    ) {
                        Text("Выбрать")
                    }
                }
            }
        }
    }
}