package com.helfkea.crm.ui.common.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helfkea.crm.viewmodel.InteractionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInteractionSidePanel(
    contragentId: String,
    contragentName: String,
    isVisible: Boolean,
    onClose: () -> Unit,
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
    var selectedTypeName by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // Загружаем виды контактов при открытии
    LaunchedEffect(isVisible) {
        if (isVisible) {
            viewModel.loadContactTypes()
        }
    }

    // Обработка успешного создания
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            // Сбрасываем форму
            isSecure = false
            selectedTypeId = null
            selectedTypeName = ""
            comment = ""
            expanded = false

            // Уведомляем об успехе и закрываем
            onInteractionCreated()
            viewModel.resetState() // ВАЖНО: сбрасываем isSuccess
            onClose()
        }
    }

    // Сбрасываем форму при закрытии
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            isSecure = false
            selectedTypeId = null
            selectedTypeName = ""
            comment = ""
            expanded = false
            viewModel.resetState()
        }
    }

    // Анимация появления
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(400.dp)
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // Заголовок панели
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD32F2F))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Новое взаимодействие",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = contragentName,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = Color.White
                        )
                    }
                }

                // Форма - ОСНОВНОЙ КОЛОНКА (все элементы на одном уровне)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Чекбокс "Закрепить"
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF5F5F5),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Закрепить",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Поместит взаимодействие в начало списка",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // Маленький аккуратный тумблер
                            Switch(
                                checked = isSecure,
                                onCheckedChange = { isSecure = it },
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(44.dp),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFD32F2F),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFBDBDBD)
                                )
                            )
                        }
                    }

                    // 2. Выбор вида контакта
                    Column {
                        Text(
                            text = "Вид контакта *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                TextField(
                                    value = selectedTypeName,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = {
                                        Text(
                                            "Выберите из списка",
                                            color = Color.Gray
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = expanded
                                        )
                                    },
                                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        unfocusedPlaceholderColor = Color.Gray,
                                        focusedPlaceholderColor = Color.Gray
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
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
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text("Загрузка...")
                                                }
                                            },
                                            onClick = {}
                                        )
                                    } else if (contactTypes.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Нет доступных видов") },
                                            onClick = {}
                                        )
                                    } else {
                                        contactTypes.forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type.name) },
                                                onClick = {
                                                    selectedTypeId = type.id
                                                    selectedTypeName = type.name
                                                    expanded = false
                                                },
                                                trailingIcon = {
                                                    if (selectedTypeId == type.id) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = "Выбрано",
                                                            tint = Color(0xFFD32F2F)
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Информация о выбранном типе
                        if (selectedTypeName.isNotEmpty()) {
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Выбрано: $selectedTypeName",
                                    fontSize = 12.sp,
                                    color = Color(0xFFB71C1C)
                                )
                            }
                        }
                    }

                    // 3. Поле комментария
                    Column {
                        Text(
                            text = "Комментарий *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                        ) {
                            TextField(
                                value = comment,
                                onValueChange = { comment = it },
                                placeholder = {
                                    Text("Опишите суть взаимодействия...")
                                },
                                maxLines = 6,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    unfocusedPlaceholderColor = Color.Gray,
                                    focusedPlaceholderColor = Color.Gray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp)
                            )
                        }

                        // Счетчик символов
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${comment.length}/500",
                                fontSize = 11.sp,
                                color = if (comment.length > 500) Color.Red else Color.Gray
                            )
                        }
                    }

                    // 4. Ошибка (если есть)
                    error?.let { errorMessage ->
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF9A9A))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Ошибка",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // 5. Кнопки действий
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Основная кнопка сохранения
                        Button(
                            onClick = {
                                if (selectedTypeId != null && comment.isNotBlank() && comment.length <= 500) {
                                    viewModel.createInteraction(
                                        contragentId = contragentId,
                                        typeContactId = selectedTypeId!!,
                                        secure = isSecure,
                                        comment = comment
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading &&
                                    selectedTypeId != null &&
                                    comment.isNotBlank() &&
                                    comment.length <= 500,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Сохранение...")
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Сохранить взаимодействие")
                                }
                            }
                        }

                        // Вторичная кнопка отмены
                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Gray
                            ),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Отмена")
                        }

                        // Информация о заполнении
                        if (selectedTypeId == null || comment.isBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Заполните обязательные поля (*)",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}