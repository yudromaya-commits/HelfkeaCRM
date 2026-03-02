package com.helfkea.crm.ui.tasks.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.helfkea.crm.model.Task
import com.helfkea.crm.model.TaskAttachment
import com.helfkea.crm.model.TaskComment
import com.helfkea.crm.model.TaskStatus
import com.helfkea.crm.utils.formatIsoDateTime
import com.helfkea.crm.utils.toReadableDate
import com.helfkea.crm.utils.toReadableDateTime
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailPanel(
    task: Task?,
    onClose: () -> Unit,
    onAddComment: (String) -> Unit = {},
    onMarkComplete: () -> Unit = {},
    onEditTask: () -> Unit = {},
    onOpenContragent: (String) -> Unit = {}  // Новый callback для открытия контрагента
) {
    var showAddCommentDialog by remember { mutableStateOf(false) }
    
    // Логируем открытие панели
    LaunchedEffect(task?.id) {
        if (task != null) {
            println("DEBUG TaskDetailPanel: Открываем задачу ${task.id}, комментариев: ${task.comment?.size ?: 0}")
        }
    }

    // Используем диалог с прозрачным фоном и анимациями
    if (task != null) {
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Затемненный фон с анимацией
                AnimatedVisibility(
                    visible = task != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                onClick = onClose,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            )
                            .background(Color.Black.copy(alpha = 0.5f))
                    )
                }

                // Панель с задачей с анимацией
                AnimatedVisibility(
                    visible = task != null,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 300)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(400.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxHeight(),
                        shape = MaterialTheme.shapes.extraLarge.copy(
                            topStart = CornerSize(16.dp),
                            bottomStart = CornerSize(16.dp)
                        ),
                        tonalElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Заголовок панели
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = getStatusColor(task.status).copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.onSurface
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
                                            "Детали задачи",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            task.date.toReadableDateTime(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = onClose,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Закрыть",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Основной контент
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                // Статус и важность
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusBadgeCompact(
                                        status = task.status,
                                        important = task.important
                                    )

                                    if (task.important) {
                                        Icon(
                                            Icons.Default.PriorityHigh,
                                            contentDescription = "Важная задача",
                                            tint = Color.Red,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Название задачи
                                Text(
                                    text = task.name.ifEmpty { "Без названия" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Описание задачи
                                if (task.description.isNotEmpty()) {
                                    SectionCard(
                                        title = "Описание задачи",
                                        icon = Icons.Default.Description,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = task.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Участники
                                SectionCard(
                                    title = "Участники",
                                    icon = Icons.Default.People,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ParticipantCompact(
                                            role = "Постановщик",
                                            name = task.producer.ifEmpty { "Не указан" },
                                            icon = Icons.Default.PersonAdd
                                        )
                                        ParticipantCompact(
                                            role = "Исполнитель",
                                            name = task.executor ?: "Не назначен",
                                            icon = Icons.Default.Work
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // ★★★★ ВСТАВЛЯЕМ ЗДЕСЬ - КОНТРАГЕНТ ★★★★
                                // Контрагент (НОВОЕ ПОЛЕ)
                                task.contragent?.let { contragent ->
                                    SectionCard(
                                        title = "Контрагент",
                                        icon = Icons.Default.Business,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(
                                                    enabled = contragent.id != null,
                                                    onClick = {
                                                        contragent.id?.let { contragentId ->
                                                            onOpenContragent(contragentId)
                                                        }
                                                    }
                                                ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Business,
                                                contentDescription = "Контрагент",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    contragent.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                if (contragent.id != null) {
                                                    Text(
                                                        "GUID: ${contragent.id}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                            // Добавляем стрелочку если контрагент кликабельный
                                            if (contragent.id != null) {
                                                Icon(
                                                    Icons.Default.ArrowForward,
                                                    contentDescription = "Перейти к контрагенту",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Сроки
                                SectionCard(
                                    title = "Сроки",
                                    icon = Icons.Default.CalendarToday,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    DeadlineCompact(
                                        date = task.executionDate,
                                        isOverdue = task.status == "Просрочена"
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // Дополнительная информация (ПОДНЯТО ВВЕРХ)
                                SectionCard(
                                    title = "Детали",
                                    icon = Icons.Default.Info,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        DetailCompact(
                                            label = "Приоритет",
                                            value = if (task.important) "Высокий" else "Обычный",
                                            color = if (task.important) Color.Red else MaterialTheme.colorScheme.primary
                                        )
                                        DetailCompact(
                                            label = "Дата создания",
                                            value = task.date.toReadableDateTime(),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        // Контрагент
                                        if (task.contragentName?.isNotEmpty() == true && task.contragentId != null) {
                                            DetailCompact(
                                                label = "Контрагент",
                                                value = task.contragentName,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                onClick = {
                                                    onOpenContragent(task.contragentId!!)
                                                }
                                            )
                                        } else if (task.contragentName?.isNotEmpty() == true) {
                                            DetailCompact(
                                                label = "Контрагент",
                                                value = task.contragentName,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                // Комментарии (ОПУЩЕНО ВНИЗ)
                                val comments = task.comment ?: emptyList()
                                if (comments.isNotEmpty()) {
                                    SectionCard(
                                        title = "Комментарии (${comments.size})",
                                        icon = Icons.Default.Comment,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            comments.forEach { comment ->
                                                CommentCompact(comment = comment)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Кнопки действий
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onMarkComplete,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (task.status == "Выполнена") Color.Green else MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            if (task.status == TaskStatus.COMPLETED) "Уже выполнена" else "Отметить как выполненную"
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showAddCommentDialog = true },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Коммент")
                                        }

                                        OutlinedButton(
                                            onClick = onEditTask,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Изменить")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог добавления комментария
    if (showAddCommentDialog) {
        AddCommentDialog(
            onDismiss = { showAddCommentDialog = false },
            onAddComment = { commentText ->
                onAddComment(commentText)
                showAddCommentDialog = false
            }
        )
    }
}

// ... остальной код остается без изменений ...

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium
            ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
fun StatusBadgeCompact(
    status: String,
    important: Boolean
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = getStatusColor(status).copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            getStatusColor(status).copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = getStatusColor(status),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun AttachmentCompact(
    attachment: TaskAttachment,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f))
            .clickable {
                // TODO: Открыть файл
            },
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Иконка в зависимости от типа файла
            Surface(
                shape = CircleShape,
                color = when (attachment.fileType) {
                    "image" -> Color(0xFFE3F2FD)
                    "document" -> Color(0xFFE8F5E8)
                    else -> Color(0xFFF3E5F5)
                }
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (attachment.fileType) {
                            "image" -> Icons.Default.Photo
                            "document" -> Icons.Default.Description
                            else -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = attachment.fileName,
                        tint = when (attachment.fileType) {
                            "image" -> Color(0xFF1976D2)
                            "document" -> Color(0xFF388E3C)
                            else -> Color(0xFF7B1FA2)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = attachment.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Text(
                    text = formatFileSize(attachment.fileSize),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = {
                    // TODO: Скачать файл
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Скачать",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Вспомогательная функция для форматирования размера файла
private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size Б"
        size < 1024 * 1024 -> "${size / 1024} КБ"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} МБ"
        else -> "${size / (1024 * 1024 * 1024)} ГБ"
    }
}

@Composable
fun ParticipantCompact(
    role: String,
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = role,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = role,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DeadlineCompact(
    date: String,
    isOverdue: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.CalendarToday,
            contentDescription = "Срок",
            tint = if (isOverdue) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Срок выполнения",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = date.toReadableDate(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isOverdue) Color.Red else MaterialTheme.colorScheme.onSurface
                )

                if (isOverdue) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = Color.Red.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "ПРОСРОЧЕНО",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentCompact(
    comment: TaskComment,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Пользователь",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = comment.user,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = comment.date.formatIsoDateTime(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = comment.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun DetailCompact(
    label: String,
    value: String,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
            // Добавляем стрелочку если есть onClick
            if (onClick != null) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Перейти",
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun AddCommentDialog(
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Добавить комментарий")
        },
        text = {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Текст комментария") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                singleLine = false
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onAddComment(commentText)
                    }
                },
                enabled = commentText.isNotBlank()
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun getStatusColor(status: String): Color {
    return when (status) {
        "Просрочена" -> Color(0xFFFF5252) // Красный
        "Выполнена" -> Color(0xFF4CAF50)  // Зеленый
        "В работе" -> Color(0xFFFF9800)   // Оранжевый
        "Назначена" -> Color(0xFF2196F3)  // Синий
        else -> MaterialTheme.colorScheme.primary
    }
}

// Для обратной совместимости с MainActivity


