package com.helfkea.crm.ui.common.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helfkea.crm.ui.theme.PrimaryRed
import com.helfkea.crm.ui.theme.PrimaryRedDark
import com.helfkea.crm.viewmodel.InteractionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInteractionDialog(
    contragentId: String,
    contragentName: String,
    onDismiss: () -> Unit,
    onInteractionCreated: () -> Unit
) {
    val viewModel: InteractionViewModel = viewModel()
    val contactTypes by viewModel.contactTypes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    // Состояние формы
    var isSecure by remember { mutableStateOf(false) }
    var selectedTypeId by remember { mutableStateOf<String?>(null) }
    var comment by remember { mutableStateOf("") }

    // ДОБАВЬ ЭТОТ ЛОГ
    LaunchedEffect(contactTypes) {
        println("DEBUG: ContactTypes updated: ${contactTypes.size}")
        contactTypes.forEach {
            println("DEBUG: Type: ${it.name}, ID: ${it.id}")
        }
    }

    // Загружаем виды контактов при открытии
    LaunchedEffect(Unit) {
        viewModel.loadContactTypes()
    }

    // Обработка успешного создания
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onInteractionCreated()
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .heightIn(min = 300.dp, max = 600.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Заголовок
                Text(
                    text = "Добавить взаимодействие",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryRedDark,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Имя контрагента
                Text(
                    text = contragentName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Чекбокс "Закрепить"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSecure,
                        onCheckedChange = { isSecure = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryRed,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Закрепить",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // КАСТОМНЫЙ ВЫПАДАЮЩИЙ СПИСОК ДЛЯ ВИДОВ КОНТАКТОВ
                var showTypesDropdown by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedTypeId?.let { id ->
                            contactTypes.find { it.id == id }?.name ?: "Выберите вид контакта"
                        } ?: "Выберите вид контакта",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Вид контакта *") },
                        trailingIcon = {
                            Icon(
                                if (showTypesDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Раскрыть список",
                                tint = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = PrimaryRedDark,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTypesDropdown = !showTypesDropdown }
                    )

                    DropdownMenu(
                        expanded = showTypesDropdown,
                        onDismissRequest = { showTypesDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        if (contactTypes.isEmpty() && isLoading) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Загрузка...")
                                    }
                                },
                                onClick = { }
                            )
                        } else if (contactTypes.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Нет доступных видов контактов") },
                                onClick = { }
                            )
                        } else {
                            contactTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        selectedTypeId = type.id
                                        showTypesDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Поле для комментария
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Комментарий *") },
                    placeholder = { Text("Введите текст взаимодействия...") },
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = PrimaryRedDark,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                )

                // Сообщение об ошибке
                error?.let { errorMessage ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопки действий
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Кнопка отмены
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Отмена")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Кнопка сохранения
                    Button(
                        onClick = {
                            if (selectedTypeId != null && comment.isNotBlank()) {
                                viewModel.createInteraction(
                                    contragentId = contragentId,
                                    typeContactId = selectedTypeId!!,
                                    secure = isSecure,
                                    comment = comment
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && selectedTypeId != null && comment.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Сохранить")
                        }
                    }
                }

                // Сообщение о необходимости заполнения полей
                if (selectedTypeId == null || comment.isBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "* Обязательные поля",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }
        }
    }
}