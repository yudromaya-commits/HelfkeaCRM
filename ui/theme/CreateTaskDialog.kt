package com.example.repressales.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment // ДОБАВЬТЕ ЭТОТ ИМПОРТ
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.repressales.model.CreateTaskRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreateTask: (CreateTaskRequest) -> Unit,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Назначена") }
    var producer by remember { mutableStateOf("") }
    var executionDate by remember { mutableStateOf("") }
    var important by remember { mutableStateOf(false) }

    val statusOptions = listOf("Назначена", "В работе", "Выполнена", "Просрочена", "Отменена")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "Создать новую задачу",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поля формы
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название задачи *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = name.isEmpty()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    isError = description.isEmpty()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Статус
                var statusExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        label = { Text("Статус") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    status = option
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = producer,
                    onValueChange = { producer = it },
                    label = { Text("Исполнитель") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = executionDate,
                    onValueChange = { executionDate = it },
                    label = { Text("Дата выполнения (ГГГГ-ММ-ДД)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("2024-12-31") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Важная задача
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically // Теперь Alignment распознается
                ) {
                    Text("Важная задача")
                    Switch(
                        checked = important,
                        onCheckedChange = { important = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = {
                            if (name.isNotEmpty() && description.isNotEmpty()) {
                                val request = CreateTaskRequest(
                                    name = name,
                                    description = description,
                                    status = status.ifEmpty { null },
                                    producer = producer.ifEmpty { null },
                                    executionDate = executionDate.ifEmpty { null },
                                    important = if (important) true else null
                                )
                                onCreateTask(request)
                            }
                        },
                        enabled = name.isNotEmpty() && description.isNotEmpty() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Создать")
                        }
                    }
                }
            }
        }
    }
}