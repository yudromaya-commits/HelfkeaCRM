// EnhancedCreateContragentDialog.kt
package com.helfkea.crm.ui.contragents.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.layout.layoutId
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helfkea.crm.model.CreateContragentRequest
import com.helfkea.crm.viewmodel.CreateContragentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.helfkea.crm.repository.InnRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCreateContragentDialog(
    isIndividual: Boolean = false,
    onDismiss: () -> Unit,
    onCreateSuccess: (id: String?, name: String?, inn: String?) -> Unit = { _, _, _ -> }
) {
    var name by remember { mutableStateOf("") }
    var inn by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Основной тип - Лид или Клиент (по умолчанию Лид)
    var isLead by remember { mutableStateOf(true) }
    var isClient by remember { mutableStateOf(false) }

    // Дополнительные типы
    var isDoctor by remember { mutableStateOf(false) }
    var isPatient by remember { mutableStateOf(false) }
    var isTradingOrganization by remember { mutableStateOf(false) }
    var isClinic by remember { mutableStateOf(false) }

    val viewModel: CreateContragentViewModel = viewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    
    // Состояния для работы с ИНН
    var isLoadingInn by remember { mutableStateOf(false) }
    var innError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val innRepository = InnRepository()

    val title = if (isIndividual) "Новое физическое лицо" else "Новое юридическое лицо"
    val placeholder = if (isIndividual) "ФИО полностью" else "Название организации"
  //  val primaryColor = if (isIndividual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    // ★ ИСПРАВЛЯЕМ ЦВЕТА:
    val headerColor = if (isIndividual)
        MaterialTheme.colorScheme.primary
    else
        Color(0xFF1976D2) // Яркий синий для юрлиц

    val primaryColor = headerColor // Используем тот же цвет для всех акцентов

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Шапка с цветом
                Surface(
                    color = primaryColor,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isIndividual)
                                        "Добавьте нового клиента или сотрудника"
                                    else
                                        "Добавьте новую организацию",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(40.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                    contentColor = MaterialTheme.colorScheme.onPrimary
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
                }

                // Форма
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Основные поля (все обязательные)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Основная информация",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor
                        )

                        // Название/ФИО
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = {
                                Text(
                                    placeholder,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            placeholder = {
                                Text(
                                    if (isIndividual) "Иванов Иван Иванович"
                                    else "ООО 'Ромашка'"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = name.isBlank(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor,
                                cursorColor = primaryColor
                            ),
                            shape = MaterialTheme.shapes.large,
                            leadingIcon = {
                                Icon(
                                    if (isIndividual) Icons.Default.Person else Icons.Default.Business,
                                    contentDescription = null,
                                    tint = primaryColor
                                )
                            },
                            supportingText = {
                                if (name.isBlank()) {
                                    Text("Обязательное поле", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        // ИНН (обязательное) с кнопкой заполнения
                        OutlinedTextField(
                            value = inn,
                            onValueChange = { inn = it },
                            label = { Text("ИНН") },
                            placeholder = { Text("10 или 12 цифр") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = inn.isBlank() || innError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = MaterialTheme.shapes.large,
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null)
                            },
                            trailingIcon = {
                                Box {
                                    if (isLoadingInn) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = "Заполнить по ИНН",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable(
                                                    enabled = inn.isNotBlank() && !isLoadingInn,
                                                    onClick = {
                                                        if (inn.isNotBlank() && !isLoadingInn) {
                                                            scope.launch {
                                                                isLoadingInn = true
                                                                innError = null
                                                                
                                                                val result = innRepository.getInnData(
                                                                    inn = inn,
                                                                    isCompany = !isIndividual // Для юрлиц isCompany = true
                                                                )
                                                                
                                                                isLoadingInn = false
                                                                
                                                                result.onSuccess { innData ->
                                                                    // Заполняем поля полученными данными
                                                                    innData.name?.let { name = it }
                                                                    innData.phone?.let { phone = it }
                                                                    innData.address?.let { address = it }
                                                                }.onFailure { error ->
                                                                    innError = error.message ?: "Ошибка получения данных"
                                                                }
                                                            }
                                                        }
                                                    }
                                                ),
                                            tint = if (inn.isNotBlank() && !isLoadingInn) 
                                                primaryColor 
                                            else 
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        )
                                    }
                                }
                            },
                            supportingText = {
                                if (inn.isBlank()) {
                                    Text("Обязательное поле", color = MaterialTheme.colorScheme.error)
                                } else if (innError != null) {
                                    Text(innError!!, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        // Телефон (обязательное)
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Телефон") },
                            placeholder = { Text("+7 900 123-45-67") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = phone.isBlank(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = MaterialTheme.shapes.large,
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null)
                            },
                            supportingText = {
                                if (phone.isBlank()) {
                                    Text("Обязательное поле", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        // Адрес (обязательное)
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Адрес") },
                            placeholder = { Text("Город, улица, дом") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            minLines = 2,
                            maxLines = 3,
                            isError = address.isBlank(),
                            shape = MaterialTheme.shapes.large,
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                            },
                            supportingText = {
                                if (address.isBlank()) {
                                    Text("Обязательное поле", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        // Описание клиента (необязательное)
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Описание клиента") },
                            placeholder = { Text("Дополнительная информация о клиенте (необязательно)...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            minLines = 3,
                            maxLines = 5,
                            isError = false,
                            shape = MaterialTheme.shapes.large,
                            leadingIcon = {
                                Icon(Icons.Default.Description, contentDescription = null)
                            },
                            supportingText = {
                                Text("Необязательное поле", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        )
                    }

                    // Основной тип: Лид или Клиент
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Основной тип",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Чекбокс Лид
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = isLead,
                                    onCheckedChange = {
                                        isLead = it
                                        if (it) isClient = false
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = primaryColor,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Text(
                                    text = "📞 Лид",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            // Чекбокс Клиент
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = isClient,
                                    onCheckedChange = {
                                        isClient = it
                                        if (it) isLead = false
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = primaryColor,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Text(
                                    text = "👤 Клиент",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    // Дополнительные типы
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Дополнительные типы",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor
                        )

                        Text(
                            text = "Отметьте все подходящие типы",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isIndividual) {
                            // Для физлица
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Доктор
                                FilterChip(
                                    selected = isDoctor,
                                    onClick = { isDoctor = !isDoctor },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isDoctor) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text("👨‍⚕️ Доктор")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (isDoctor)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = if (isDoctor)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = if (isDoctor)
                                        FilterChipDefaults.filterChipBorder(
                                            borderColor = primaryColor,
                                            borderWidth = 1.dp
                                        )
                                    else null
                                )

                                // Пациент
                                FilterChip(
                                    selected = isPatient,
                                    onClick = { isPatient = !isPatient },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isPatient) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text("👤 Пациент")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (isPatient)
                                            Color(0xFFF3E5F5)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = if (isPatient)
                                            Color(0xFF7B1FA2)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = if (isPatient)
                                        FilterChipDefaults.filterChipBorder(
                                            borderColor = Color(0xFF9C27B0),
                                            borderWidth = 1.dp
                                        )
                                    else null
                                )
                            }
                        } else {
                            // Для юрлица
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Первая строка
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Доктор
                                    FilterChip(
                                        selected = isDoctor,
                                        onClick = { isDoctor = !isDoctor },
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isDoctor) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text("👨‍⚕️ Доктор")
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = if (isDoctor)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = if (isDoctor)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = if (isDoctor)
                                            FilterChipDefaults.filterChipBorder(
                                                borderColor = primaryColor,
                                                borderWidth = 1.dp
                                            )
                                        else null
                                    )

                                    // Торгующая организация
                                    FilterChip(
                                        selected = isTradingOrganization,
                                        onClick = { isTradingOrganization = !isTradingOrganization },
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isTradingOrganization) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text("🏢 Торгующая организация")
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = if (isTradingOrganization)
                                                Color(0xFFE8F5E9)
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = if (isTradingOrganization)
                                                Color(0xFF2E7D32)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = if (isTradingOrganization)
                                            FilterChipDefaults.filterChipBorder(
                                                borderColor = Color(0xFF4CAF50),
                                                borderWidth = 1.dp
                                            )
                                        else null
                                    )
                                }

                                // Вторая строка
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Клиника
                                    FilterChip(
                                        selected = isClinic,
                                        onClick = { isClinic = !isClinic },
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isClinic) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text("🏥 Клиника")
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = if (isClinic)
                                                MaterialTheme.colorScheme.secondaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = if (isClinic)
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = if (isClinic)
                                            FilterChipDefaults.filterChipBorder(
                                                borderColor = MaterialTheme.colorScheme.secondary,
                                                borderWidth = 1.dp
                                            )
                                        else null
                                    )

                                    // Пациент
                                    FilterChip(
                                        selected = isPatient,
                                        onClick = { isPatient = !isPatient },
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isPatient) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text("👤 Пациент")
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = if (isPatient)
                                                Color(0xFFF3E5F5)
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = if (isPatient)
                                                Color(0xFF7B1FA2)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = if (isPatient)
                                            FilterChipDefaults.filterChipBorder(
                                                borderColor = Color(0xFF9C27B0),
                                                borderWidth = 1.dp
                                            )
                                        else null
                                    )
                                }
                            }
                        }
                    }

                    // Статус
                    if (error != null || success) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (success)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.errorContainer
                            ),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (success) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = if (success) "Успех" else "Ошибка",
                                    tint = if (success)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (success) "Успешно создано!" else "Ошибка",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (success)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = if (success)
                                            "${if (isIndividual) "Физлицо" else "Юрлицо"} добавлено в систему"
                                        else (error ?: ""),
                                        color = if (success)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // Кнопки
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        ) {
                            Text("Отмена")
                        }

                        Button(
                            onClick = {
                                val type = if (isIndividual) "Физ.лицо" else "Юр.лицо"
                                val request = CreateContragentRequest(
                                    name = name.trim(),
                                    type = type,
                                    inn = inn.trim(),
                                    number = phone.trim(),
                                    adress = address.trim(),
                                    // Передаем описание
                                    description = description.trim(),
                                    doctor = isDoctor,
                                    clinic = isClinic,
                                    lead = isLead,
                                    pasient = isPatient,
                                    // Добавляем новые поля
                                    client = isClient,
                                    tradingOrganization = isTradingOrganization
                                )

                                viewModel.createContragent(request) { id, name, inn ->
                                    onCreateSuccess(id, name, inn)
                                    onDismiss()
                                    viewModel.resetState()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = name.isNotBlank() && inn.isNotBlank() &&
                                    phone.isNotBlank() && address.isNotBlank() && !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor
                            )
                        ) {
                            if (isLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Создание...")
                                }
                            } else {
                                Text("Создать")
                            }
                        }
                    }
                }
            }
        }
    }
}