package com.helfkea.crm.ui.tasks.screens

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helfkea.crm.model.ContragentInTask
import com.helfkea.crm.model.TaskStatus
import com.helfkea.crm.model.PreselectedContragent
import com.helfkea.crm.viewmodel.AttachmentUiState
import com.helfkea.crm.viewmodel.CreateTaskViewModel
import com.helfkea.crm.viewmodel.ContragentViewModel
import com.helfkea.crm.ui.common.dialogs.SelectContragentDialog
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CreateTaskPanel(
    show: Boolean,
    onClose: () -> Unit,
    onCreateSuccess: () -> Unit = {},
    onTaskCreated: () -> Unit = {},
    preselectedContragent: PreselectedContragent? = null
) {
    val viewModel: CreateTaskViewModel = viewModel()
    val context = LocalContext.current

    // Загружаем пользователей при открытии панели
    LaunchedEffect(show) {
        if (show) {
            viewModel.loadUsers()
        }
    }

    // Инициализация предвыбранного контрагента
    LaunchedEffect(preselectedContragent) {
        if (preselectedContragent != null) {
            println("DEBUG: CreateTaskPanel получил предвыбранного контрагента: name='${preselectedContragent.name}', id='${preselectedContragent.id}'")
            viewModel.updateContragentName(preselectedContragent.name)
            // Устанавливаем ID напрямую
            viewModel.selectContragentById(preselectedContragent.id)
        } else {
            println("DEBUG: CreateTaskPanel: preselectedContragent = null")
        }
    }

    // Лаунчер для выбора фото из галереи
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val fileName = getFileName(context, it) ?: "photo_${System.currentTimeMillis()}.jpg"
            viewModel.addAttachment(
                uri = it,
                fileName = fileName,
                fileSize = 0,
                fileType = "image"
            )
        }
    }

    // Лаунчер для создания фото с камеры
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        contract = TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                viewModel.addAttachment(
                    uri = uri,
                    fileName = "photo_${System.currentTimeMillis()}.jpg",
                    fileSize = 0,
                    fileType = "image"
                )
            }
        }
        cameraImageUri = null
    }

    // Лаунчер для выбора любого файла
    val pickFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val fileName = getFileName(context, it) ?: "file_${System.currentTimeMillis()}"
            viewModel.addAttachment(
                uri = it,
                fileName = fileName,
                fileSize = 0,
                fileType = "document"
            )
        }
    }

    // Диалог для выбора контрагента
    var showContragentDialog by remember { mutableStateOf(false) }

    if (show) {
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Затемненный фон
                AnimatedVisibility(
                    visible = show,
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
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                }

                // Панель создания задачи
                AnimatedVisibility(
                    visible = show,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(525.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxHeight(),
                        tonalElevation = 0.dp,
                        color = Color(0xFFF8FAFC),
                        contentColor = Color(0xFF1E293B)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Шапка
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White,
                                contentColor = Color(0xFF1E293B),
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 32.dp, vertical = 24.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Новая задача",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            "Создайте новую задачу для команды",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    IconButton(
                                        onClick = onClose,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                Color(0xFFF1F5F9),
                                                CircleShape
                                            ),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = Color(0xFF475569)
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Закрыть",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Форма создания задачи
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 32.dp, vertical = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                item {
                                    // Название задачи - БЕЗ KeyboardOptions
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        ) {
                                            Text(
                                                "Название задачи",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF334155)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "*",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEF4444)
                                            )
                                        }

                                        TextField(
                                            value = viewModel.name.collectAsState().value,
                                            onValueChange = { viewModel.updateName(it) },
                                            placeholder = {
                                                Text(
                                                    "Введите название задачи",
                                                    color = Color(0xFF94A3B8)
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .shadow(
                                                    elevation = 2.dp,
                                                    shape = RoundedCornerShape(12.dp),
                                                    clip = true
                                                ),
                                            singleLine = true,
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent,
                                                errorIndicatorColor = Color.Transparent,
                                                focusedTextColor = Color(0xFF0F172A),
                                                unfocusedTextColor = Color(0xFF0F172A),
                                                focusedPlaceholderColor = Color(0xFF94A3B8),
                                                unfocusedPlaceholderColor = Color(0xFF94A3B8),
                                                cursorColor = Color(0xFF3B82F6)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                            // KeyboardOptions убран!
                                        )
                                    }
                                }

                                item {
                                    // Описание - БЕЗ KeyboardOptions
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        ) {
                                            Text(
                                                "Описание",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF334155)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "*",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEF4444)
                                            )
                                        }

                                        TextField(
                                            value = viewModel.description.collectAsState().value,
                                            onValueChange = { viewModel.updateDescription(it) },
                                            placeholder = {
                                                Text(
                                                    "Опишите задачу подробнее...",
                                                    color = Color(0xFF94A3B8)
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .shadow(
                                                    elevation = 2.dp,
                                                    shape = RoundedCornerShape(12.dp),
                                                    clip = true
                                                ),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent,
                                                errorIndicatorColor = Color.Transparent,
                                                focusedTextColor = Color(0xFF0F172A),
                                                unfocusedTextColor = Color(0xFF0F172A),
                                                focusedPlaceholderColor = Color(0xFF94A3B8),
                                                unfocusedPlaceholderColor = Color(0xFF94A3B8),
                                                cursorColor = Color(0xFF3B82F6)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                            // KeyboardOptions убран!
                                        )
                                    }
                                }

                                item {
                                    // Контрагент - ГЛАВНОЕ
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "Контрагент",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF334155),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        val contragentName = viewModel.contragentName.collectAsState().value

                                        if (contragentName != null) {
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .shadow(
                                                        elevation = 2.dp,
                                                        shape = RoundedCornerShape(12.dp),
                                                        clip = true
                                                    ),
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFFF0F9FF),
                                                tonalElevation = 0.dp
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .background(
                                                                    Color(0xFF3B82F6).copy(alpha = 0.1f),
                                                                    CircleShape
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Business,
                                                                null,
                                                                tint = Color(0xFF3B82F6),
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(16.dp))
                                                        Column {
                                                            Text(
                                                                contragentName,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.Medium,
                                                                color = Color(0xFF0F172A)
                                                            )
                                                            Text(
                                                                "Выбранный контрагент",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = Color(0xFF64748B)
                                                            )
                                                        }
                                                    }

                                                    IconButton(
                                                        onClick = { viewModel.clearContragent() },
                                                        modifier = Modifier.size(32.dp),
                                                        colors = IconButtonDefaults.iconButtonColors(
                                                            containerColor = Color(0xFFFEF2F2),
                                                            contentColor = Color(0xFFDC2626)
                                                        )
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Close,
                                                            "Удалить",
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = {
                                                    showContragentDialog = true
                                                    viewModel.loadContragents()
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp)
                                                    .shadow(
                                                        elevation = 2.dp,
                                                        shape = RoundedCornerShape(12.dp),
                                                        clip = true
                                                    ),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = Color.White,
                                                    contentColor = Color(0xFF3B82F6)
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                border = null
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(
                                                            Color(0xFF3B82F6).copy(alpha = 0.1f),
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        null,
                                                        tint = Color(0xFF3B82F6),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    "Выбрать контрагента",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                // Статус и важность
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        // Статус
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                "Статус",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF334155),
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )

                                            var statusExpanded by remember { mutableStateOf(false) }
                                            val status = viewModel.status.collectAsState().value

                                            ExposedDropdownMenuBox(
                                                expanded = statusExpanded,
                                                onExpandedChange = { statusExpanded = !statusExpanded }
                                            ) {
                                                TextField(
                                                    value = if (status.isNotEmpty()) status else "Выберите статус",
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    trailingIcon = {
                                                        Icon(
                                                            if (statusExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                                            contentDescription = null,
                                                            tint = Color(0xFF64748B),
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    },
                                                    colors = TextFieldDefaults.colors(
                                                        focusedContainerColor = Color.White,
                                                        unfocusedContainerColor = Color.White,
                                                        focusedIndicatorColor = Color.Transparent,
                                                        unfocusedIndicatorColor = Color.Transparent,
                                                        disabledIndicatorColor = Color.Transparent,
                                                        errorIndicatorColor = Color.Transparent,
                                                        focusedTextColor = if (status.isNotEmpty()) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                                        unfocusedTextColor = if (status.isNotEmpty()) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                                        focusedTrailingIconColor = Color(0xFF64748B),
                                                        unfocusedTrailingIconColor = Color(0xFF64748B)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .menuAnchor()
                                                        .shadow(
                                                            elevation = 2.dp,
                                                            shape = RoundedCornerShape(12.dp),
                                                            clip = true
                                                        )
                                                )

                                                ExposedDropdownMenu(
                                                    expanded = statusExpanded,
                                                    onDismissRequest = { statusExpanded = false },
                                                    modifier = Modifier
                                                        .background(Color.White)
                                                        .shadow(
                                                            elevation = 8.dp,
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                ) {
                                                    TaskStatus.ALL.forEach { option ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Text(
                                                                    option,
                                                                    color = if (status == option) Color(0xFF3B82F6)
                                                                    else Color(0xFF0F172A)
                                                                )
                                                            },
                                                            onClick = {
                                                                viewModel.updateStatus(option)
                                                                statusExpanded = false
                                                            },
                                                            modifier = Modifier.background(
                                                                if (status == option) Color(0xFFF0F9FF)
                                                                else Color.Transparent
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Важность
                                        Column {
                                            Text(
                                                "Важность",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF334155),
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )

                                            val important = viewModel.important.collectAsState().value
                                            Surface(
                                                onClick = { viewModel.updateImportant(!important) },
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .height(56.dp)
                                                    .shadow(
                                                        elevation = if (important) 4.dp else 2.dp,
                                                        shape = RoundedCornerShape(12.dp),
                                                        clip = true
                                                    ),
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (important) Color(0xFFFEF2F2) else Color.White,
                                                tonalElevation = 0.dp
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            if (important) Icons.Filled.Warning else Icons.Outlined.Warning,
                                                            contentDescription = "Важная задача",
                                                            tint = if (important) Color(0xFFDC2626)
                                                            else Color(0xFF94A3B8),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Text(
                                                            if (important) "Важная" else "Обычная",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = if (important) FontWeight.SemiBold else FontWeight.Normal,
                                                            color = if (important) Color(0xFFDC2626)
                                                            else Color(0xFF64748B)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Исполнитель и дата
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        // Исполнитель
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                "Исполнитель",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF334155),
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )

                                            val usersList = viewModel.users.collectAsState().value
                                            val isLoadingUsers = viewModel.isLoadingUsers.collectAsState().value
                                            val selectedProducer = viewModel.executorId.collectAsState().value
                                            var producerExpanded by remember { mutableStateOf(false) }

                                            Box {
                                                Surface(
                                                    onClick = {
                                                        if (!isLoadingUsers && usersList.isNotEmpty()) {
                                                            producerExpanded = true
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .shadow(
                                                            elevation = 2.dp,
                                                            shape = RoundedCornerShape(12.dp),
                                                            clip = true
                                                        ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = Color.White,
                                                    tonalElevation = 0.dp
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 16.dp, vertical = 18.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            if (isLoadingUsers) {
                                                                CircularProgressIndicator(
                                                                    modifier = Modifier.size(20.dp),
                                                                    strokeWidth = 2.5.dp,
                                                                    color = Color(0xFF3B82F6)
                                                                )
                                                                Spacer(modifier = Modifier.width(12.dp))
                                                                Text(
                                                                    "Загрузка списка...",
                                                                    style = MaterialTheme.typography.bodyMedium,
                                                                    color = Color(0xFF94A3B8)
                                                                )
                                                            } else {
                                                                Icon(
                                                                    Icons.Default.Person,
                                                                    null,
                                                                    tint = Color(0xFF64748B),
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(12.dp))
                                                                Text(
                                                                    text = selectedProducer.ifEmpty { "Выберите исполнителя" },
                                                                    style = MaterialTheme.typography.bodyMedium,
                                                                    color = if (selectedProducer.isNotEmpty()) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                                                )
                                                            }
                                                        }

                                                        Icon(
                                                            if (producerExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                                            contentDescription = null,
                                                            tint = Color(0xFF64748B),
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }

                                                if (producerExpanded && usersList.isNotEmpty()) {
                                                    DropdownMenu(
                                                        expanded = producerExpanded,
                                                        onDismissRequest = { producerExpanded = false },
                                                        modifier = Modifier
                                                            .background(Color.White)
                                                            .shadow(
                                                                elevation = 8.dp,
                                                                shape = RoundedCornerShape(12.dp)
                                                            )
                                                    ) {
                                                        usersList.forEach { user ->
                                                            DropdownMenuItem(
                                                                text = {
                                                                    Text(
                                                                        user.nameUser,
                                                                        color = if (selectedProducer == user.nameUser) Color(0xFF3B82F6)
                                                                        else Color(0xFF0F172A)
                                                                    )
                                                                },
                                                                onClick = {
                                                                    viewModel.updateProducer(user.nameUser)
                                                                    producerExpanded = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Дата выполнения
                                        Column {
                                            Text(
                                                "Срок выполнения",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF334155),
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )

                                            var showDatePicker by remember { mutableStateOf(false) }
                                            val keyboardController = LocalSoftwareKeyboardController.current

                                            val executionDateState = viewModel.executionDate.collectAsState()
                                            val formattedDate = remember(executionDateState.value) {
                                                if (executionDateState.value.isNotEmpty()) {
                                                    try {
                                                        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                        val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                                        val date = inputFormat.parse(executionDateState.value)
                                                        date?.let { outputFormat.format(it) } ?: ""
                                                    } catch (e: Exception) {
                                                        executionDateState.value
                                                    }
                                                } else {
                                                    ""
                                                }
                                            }

                                            Surface(
                                                onClick = {
                                                    keyboardController?.hide()
                                                    showDatePicker = true
                                                },
                                                modifier = Modifier
                                                    .width(160.dp)
                                                    .shadow(
                                                        elevation = 2.dp,
                                                        shape = RoundedCornerShape(12.dp),
                                                        clip = true
                                                    ),
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color.White,
                                                tonalElevation = 0.dp
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 18.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = if (formattedDate.isNotEmpty()) formattedDate else "ДД.ММ.ГГГГ",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (formattedDate.isNotEmpty()) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                                    )
                                                    Icon(
                                                        Icons.Default.CalendarToday,
                                                        contentDescription = "Выбрать дату",
                                                        tint = Color(0xFF64748B),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            if (showDatePicker) {
                                                var selectedDate by remember { mutableStateOf(getCurrentDate()) }

                                                LaunchedEffect(executionDateState.value) {
                                                    if (executionDateState.value.isNotEmpty()) {
                                                        try {
                                                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                                            selectedDate = LocalDate.parse(executionDateState.value, formatter)
                                                        } catch (e: Exception) {
                                                        }
                                                    }
                                                }

                                                AlertDialog(
                                                    onDismissRequest = { showDatePicker = false },
                                                    title = {
                                                        Text(
                                                            "Выберите дату",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color(0xFF0F172A)
                                                        )
                                                    },
                                                    text = {
                                                        Column {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.padding(bottom = 8.dp)
                                                            ) {
                                                                Text("День:", modifier = Modifier.width(80.dp))
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                val daysInMonth = selectedDate.lengthOfMonth()
                                                                val days = (1..daysInMonth).toList()
                                                                var dayExpanded by remember { mutableStateOf(false) }

                                                                ExposedDropdownMenuBox(
                                                                    expanded = dayExpanded,
                                                                    onExpandedChange = { dayExpanded = !dayExpanded }
                                                                ) {
                                                                    OutlinedTextField(
                                                                        value = selectedDate.dayOfMonth.toString(),
                                                                        onValueChange = {},
                                                                        readOnly = true,
                                                                        modifier = Modifier
                                                                            .width(80.dp)
                                                                            .menuAnchor(),
                                                                        trailingIcon = {
                                                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded)
                                                                        }
                                                                    )

                                                                    ExposedDropdownMenu(
                                                                        expanded = dayExpanded,
                                                                        onDismissRequest = { dayExpanded = false }
                                                                    ) {
                                                                        days.forEach { day ->
                                                                            DropdownMenuItem(
                                                                                text = { Text(day.toString()) },
                                                                                onClick = {
                                                                                    selectedDate = selectedDate.withDayOfMonth(day)
                                                                                    dayExpanded = false
                                                                                }
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.padding(bottom = 16.dp)
                                                            ) {
                                                                Text("Месяц:", modifier = Modifier.width(80.dp))
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                var monthExpanded by remember { mutableStateOf(false) }
                                                                val months = listOf(
                                                                    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                                                                    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
                                                                )

                                                                ExposedDropdownMenuBox(
                                                                    expanded = monthExpanded,
                                                                    onExpandedChange = { monthExpanded = !monthExpanded }
                                                                ) {
                                                                    OutlinedTextField(
                                                                        value = months[selectedDate.monthValue - 1],
                                                                        onValueChange = {},
                                                                        readOnly = true,
                                                                        modifier = Modifier
                                                                            .width(150.dp)
                                                                            .menuAnchor(),
                                                                        trailingIcon = {
                                                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded)
                                                                        }
                                                                    )

                                                                    ExposedDropdownMenu(
                                                                        expanded = monthExpanded,
                                                                        onDismissRequest = { monthExpanded = false }
                                                                    ) {
                                                                        months.forEachIndexed { index, month ->
                                                                            DropdownMenuItem(
                                                                                text = { Text(month) },
                                                                                onClick = {
                                                                                    selectedDate = selectedDate.withMonth(index + 1)
                                                                                    monthExpanded = false
                                                                                }
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.padding(bottom = 16.dp)
                                                            ) {
                                                                Text("Год:", modifier = Modifier.width(80.dp))
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                OutlinedTextField(
                                                                    value = selectedDate.year.toString(),
                                                                    onValueChange = { yearStr ->
                                                                        yearStr.toIntOrNull()?.takeIf { it in 1900..2100 }?.let { year ->
                                                                            selectedDate = selectedDate.withYear(year)
                                                                        }
                                                                    },
                                                                    modifier = Modifier.width(100.dp)
                                                                    // KeyboardOptions убран!
                                                                )
                                                            }

                                                            Text(
                                                                "Выбрано: ${selectedDate.dayOfMonth}.${selectedDate.monthValue}.${selectedDate.year}",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = Color(0xFF3B82F6),
                                                                fontWeight = FontWeight.SemiBold,
                                                                modifier = Modifier.padding(top = 16.dp)
                                                            )
                                                        }
                                                    },
                                                    confirmButton = {
                                                        Button(
                                                            onClick = {
                                                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                                                viewModel.updateExecutionDate(selectedDate.format(formatter))
                                                                showDatePicker = false
                                                            },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Color(0xFF3B82F6),
                                                                contentColor = Color.White
                                                            ),
                                                            shape = RoundedCornerShape(12.dp)
                                                        ) {
                                                            Text("Выбрать")
                                                        }
                                                    },
                                                    dismissButton = {
                                                        OutlinedButton(
                                                            onClick = { showDatePicker = false },
                                                            shape = RoundedCornerShape(12.dp),
                                                            border = null,
                                                            colors = ButtonDefaults.outlinedButtonColors(
                                                                containerColor = Color(0xFFF1F5F9),
                                                                contentColor = Color(0xFF64748B)
                                                            )
                                                        ) {
                                                            Text("Отмена")
                                                        }
                                                    },
                                                    containerColor = Color.White,
                                                    tonalElevation = 0.dp,
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Вложения
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "Вложения",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF334155),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        val attachments = viewModel.attachments.collectAsState().value

                                        if (attachments.isEmpty()) {
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .shadow(
                                                        elevation = 2.dp,
                                                        shape = RoundedCornerShape(12.dp),
                                                        clip = true
                                                    ),
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color.White,
                                                tonalElevation = 0.dp
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 40.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(64.dp)
                                                            .background(
                                                                Color(0xFFF1F5F9),
                                                                CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Outlined.FolderOpen,
                                                            contentDescription = null,
                                                            tint = Color(0xFF94A3B8),
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        "Нет прикрепленных файлов",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color(0xFF64748B)
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        "Добавьте фото или документы",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF94A3B8)
                                                    )
                                                }
                                            }
                                        } else {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                attachments.forEachIndexed { index, attachment ->
                                                    AttachmentItem(
                                                        attachment = attachment,
                                                        onRemove = { viewModel.removeAttachment(index) }
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        var showPhotoOptions by remember { mutableStateOf(false) }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box {
                                                Button(
                                                    onClick = { showPhotoOptions = true },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color.White,
                                                        contentColor = Color(0xFF3B82F6)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    elevation = ButtonDefaults.buttonElevation(
                                                        defaultElevation = 2.dp,
                                                        pressedElevation = 4.dp,
                                                        disabledElevation = 0.dp
                                                    ),
                                                    border = null
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .background(
                                                                    Color(0xFF3B82F6).copy(alpha = 0.1f),
                                                                    CircleShape
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Photo,
                                                                null,
                                                                tint = Color(0xFF3B82F6),
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Фото")
                                                    }
                                                }

                                                DropdownMenu(
                                                    expanded = showPhotoOptions,
                                                    onDismissRequest = { showPhotoOptions = false },
                                                    modifier = Modifier
                                                        .background(Color.White)
                                                        .shadow(
                                                            elevation = 8.dp,
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                ) {
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.CameraAlt,
                                                                    null,
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(12.dp))
                                                                Text("Сделать фото")
                                                            }
                                                        },
                                                        onClick = {
                                                            showPhotoOptions = false
                                                            val uri = createCameraImageUri(context)
                                                            uri?.let {
                                                                cameraImageUri = uri
                                                                takePicture.launch(uri)
                                                            }
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.PhotoLibrary,
                                                                    null,
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(12.dp))
                                                                Text("Выбрать из галереи")
                                                            }
                                                        },
                                                        onClick = {
                                                            showPhotoOptions = false
                                                            pickMedia.launch(
                                                                PickVisualMediaRequest(
                                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                                )
                                                            )
                                                        }
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    pickFile.launch("*/*")
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.White,
                                                    contentColor = Color(0xFF3B82F6)
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = ButtonDefaults.buttonElevation(
                                                    defaultElevation = 2.dp,
                                                    pressedElevation = 4.dp,
                                                    disabledElevation = 0.dp
                                                ),
                                                border = null
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .background(
                                                                Color(0xFF3B82F6).copy(alpha = 0.1f),
                                                                CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Default.AttachFile,
                                                            null,
                                                            tint = Color(0xFF3B82F6),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Файл")
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(32.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.resetForm()
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFF1F5F9),
                                                contentColor = Color(0xFF64748B)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = ButtonDefaults.buttonElevation(
                                                defaultElevation = 2.dp,
                                                pressedElevation = 4.dp,
                                                disabledElevation = 0.dp
                                            ),
                                            border = null
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Очистить")
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.createTask(context)
                                            },
                                            modifier = Modifier.weight(2f),
                                            enabled = viewModel.name.collectAsState().value.isNotEmpty() &&
                                                    viewModel.description.collectAsState().value.isNotEmpty() &&
                                                    !viewModel.isLoading.collectAsState().value,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFDC2626),
                                                contentColor = Color.White,
                                                disabledContainerColor = Color(0xFFE2E8F0),
                                                disabledContentColor = Color(0xFF94A3B8)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = ButtonDefaults.buttonElevation(
                                                defaultElevation = 4.dp,
                                                pressedElevation = 8.dp,
                                                disabledElevation = 0.dp
                                            )
                                        ) {
                                            if (viewModel.isLoading.collectAsState().value) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.5.dp,
                                                    color = Color.White
                                                )
                                            } else {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .background(
                                                                Color.White.copy(alpha = 0.2f),
                                                                CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Add,
                                                            null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        "Создать задачу",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        "Поля, отмеченные * обязательны для заполнения",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог выбора контрагента
    if (showContragentDialog) {
        Dialog(
            onDismissRequest = { showContragentDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF3B82F6),
                        contentColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Выберите контрагента",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { showContragentDialog = false },
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Close, "Закрыть")
                            }
                        }
                    }

                    var searchQuery by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Поиск контрагента...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    val contragents = viewModel.contragents.collectAsState().value
                    val isLoading = viewModel.isLoadingContragents.collectAsState().value

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (contragents.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Нет доступных контрагентов",
                                    color = Color.Gray
                                )
                            }
                        } else {
                            val filteredContragents = contragents.filter { contragent ->
                                searchQuery.isEmpty() ||
                                        contragent.name.contains(searchQuery, ignoreCase = true)
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(filteredContragents) { contragent ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        onClick = {
                                            viewModel.setContragent(contragent.id, contragent.name)
                                            showContragentDialog = false
                                        },
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Business,
                                                contentDescription = "Контрагент",
                                                tint = Color(0xFF3B82F6),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = contragent.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentItem(
    attachment: AttachmentUiState,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                clip = true
            ),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            when (attachment.fileType) {
                                "image" -> Color(0xFFF0F9FF)
                                "document" -> Color(0xFFF0FDF4)
                                else -> Color(0xFFF1F5F9)
                            },
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (attachment.fileType) {
                            "image" -> Icons.Default.Photo
                            "document" -> Icons.Default.Description
                            else -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = when (attachment.fileType) {
                            "image" -> Color(0xFF3B82F6)
                            "document" -> Color(0xFF10B981)
                            else -> Color(0xFF64748B)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        attachment.fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        formatFileSize(attachment.fileSize),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                }
            }

            if (attachment.isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = Color(0xFF3B82F6)
                )
            } else {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = Color(0xFF64748B)
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        "Удалить",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Вспомогательные функции
private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size Б"
        size < 1024 * 1024 -> "${size / 1024} КБ"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} МБ"
        else -> "${size / (1024 * 1024 * 1024)} ГБ"
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    var cursor: Cursor? = null
    return try {
        cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName = it.getString(it.getColumnIndexOrThrow("_display_name"))
                displayName
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    } finally {
        cursor?.close()
    }
}

fun getCurrentDate(): LocalDate {
    return LocalDate.now()
}

fun createCameraImageUri(context: Context): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Repressales")
        }
    }
    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )
}