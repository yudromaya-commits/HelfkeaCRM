package com.helfkea.crm.ui.individuals.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helfkea.crm.viewmodel.IndividualViewModel
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.text.style.TextAlign
import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.helfkea.crm.api.TaskApi
import com.helfkea.crm.model.GetContragentByIdRequest
import com.helfkea.crm.model.GetContragentByIdFullResponse
import com.helfkea.crm.model.Individual
import com.helfkea.crm.model.ContactAddress
import com.helfkea.crm.ui.individuals.components.IndividualFiltersPanel
import com.helfkea.crm.ui.individuals.components.IndividualCard
import com.helfkea.crm.ui.contragents.dialogs.EnhancedCreateContragentDialog



@Composable
fun IndividualView(
    modifier: Modifier = Modifier,
    onIndividualClick: (com.helfkea.crm.model.Individual) -> Unit = {},
    onCreateTaskClick: (com.helfkea.crm.model.Individual) -> Unit = {}
) {
    val viewModel: IndividualViewModel = viewModel()
    val individuals by viewModel.individuals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Состояние для скрытия/показа фильтров
    var showFilters by remember { mutableStateOf(false) }

    // Загружаем данные при первом открытии
    LaunchedEffect(Unit) {
        viewModel.loadIndividuals()
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Заголовок и кнопки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Физические лица",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Управление физическими лицами",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Row {
                // Кнопка показа/скрытия фильтров
                IconButton(
                    onClick = { showFilters = !showFilters }
                ) {
                    Icon(
                        if (showFilters) Icons.Default.FilterAltOff else Icons.Default.FilterAlt,
                        contentDescription = if (showFilters) "Скрыть фильтры" else "Показать фильтры"
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshIndividuals() },
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        tint = if (isLoading) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                }

                // Временная кнопка "Создать физлицо"
                Button(
                    onClick = { showCreateDialog = true },
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Создать физлицо")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Создать")
                }
            }
        }

        // Панель поиска и фильтров (показываем/скрываем с анимацией)
        AnimatedVisibility(
            visible = showFilters,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            IndividualFiltersPanel(
                modifier = Modifier
            )
        }

        // Состояние загрузки
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Загрузка физлиц...")
                }
            }
        }

        // Ошибка
        error?.let { errorMessage ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Ошибка",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.refreshIndividuals() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Повторить загрузку")
                }
            }
        }

        // Список физлиц
        if (individuals.isEmpty() && !isLoading && error == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Нет физлиц",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Физлица не найдены",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Измените параметры поиска или фильтры",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.clearFilters() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.ClearAll, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Сбросить фильтры")
                    }
                }
            }
        } else {
            // В IndividualView.kt найти LazyColumn и изменить:
            // В LazyColumn изменить:
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Немного больше spacing между карточками
            ) {
                items(individuals) { individual ->
                    IndividualCard(
                        individual = individual,
                        onIndividualClick = { onIndividualClick(individual) },
                        onCreateTaskClick = { onCreateTaskClick(individual) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }


            }
        }
    }
    val scope = rememberCoroutineScope()
    
    // Функция для получения физлица по ID
    fun openIndividualById(individualId: String) {
        Log.d("IndividualView", "✅ Физлицо создано с ID: $individualId")
        
        scope.launch {
            try {
                Log.d("IndividualView", "🔄 ВЫЗЫВАЮ getContragentById для ID: $individualId")
                
                // ВЫЗЫВАЕМ МЕТОД getContragentById (должен работать и для физлиц)
                val response = TaskApi.service.getContragentById(
                    GetContragentByIdRequest(individualId)
                )
                
                Log.d("IndividualView", "📡 Ответ сервера: isSuccessful=${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    val fullResponse = response.body()
                    Log.d("IndividualView", "📡 Полный ответ: $fullResponse")
                    
                    if (fullResponse?.result == true && fullResponse.body != null) {
                        val contragent = fullResponse.body
                        Log.d("IndividualView", "📡 Контрагент: $contragent")
                        
                        if (contragent.id.isNotBlank()) {
                            Log.d("IndividualView", "✅ УСПЕХ! Получено физлицо: ${contragent.name}")
                            
                            // ПРОВЕРЯЕМ, ЕСТЬ ЛИ ДАННЫЕ В КОНТРАГЕНТЕ
                            if (contragent.name.isNotBlank() || contragent.inn != null) {
                                // Есть данные - создаем Individual и открываем карточку
                                val individual = Individual(
                                    id = contragent.id,
                                    name = contragent.name,
                                    types = contragent.types,
                                    contactsAndAddresses = contragent.contactsAndAddresses,
                                    lastOrder = contragent.lastOrder,
                                    averageCheck = contragent.averageCheck,
                                    ordersCount = contragent.ordersCount,
                                    totalOrdersSum = contragent.totalOrdersSum,
                                    segment = contragent.segment,
                                    interactions = contragent.interactions,
                                    pinnedComment = contragent.pinnedComment,
                                    relatedContragents = contragent.relatedContragents
                                )
                                
                                Log.d("IndividualView", "📱 Открываю карточку физлица с данными")
                                onIndividualClick(individual)
                            } else {
                                // Данные пустые - показываем уведомление и обновляем список
                                Log.d("IndividualView", "ℹ️ Физлицо создано, но данные еще не загружены в 1С")
                                Log.d("IndividualView", "🔄 Обновляю список физлиц")
                                
                                // Пока просто обновляем список
                                viewModel.refreshIndividuals()
                            }
                        } else {
                            Log.d("IndividualView", "❌ ОШИБКА: Физлицо не найдено или пустой ID")
                            // Fallback: обновляем список
                            viewModel.refreshIndividuals()
                        }
                    } else {
                        Log.d("IndividualView", "❌ ОШИБКА от сервера: ${fullResponse?.error}")
                        // Fallback: обновляем список
                        viewModel.refreshIndividuals()
                    }
                } else {
                    Log.d("IndividualView", "❌ ОШИБКА HTTP: ${response.code()} - ${response.message()}")
                    // Fallback: обновляем список
                    viewModel.refreshIndividuals()
                }
                
            } catch (e: Exception) {
                Log.d("IndividualView", "❌ ИСКЛЮЧЕНИЕ: ${e.message}")
                e.printStackTrace()
                // Fallback: обновляем список
                viewModel.refreshIndividuals()
            }
        }
    }
    
    if (showCreateDialog) {
        EnhancedCreateContragentDialog(
            isIndividual = true,
            onDismiss = { showCreateDialog = false },
            onCreateSuccess = { id, _, _ ->
                Log.d("IndividualView", "✅ onCreateSuccess ВЫЗВАН! ID: $id")
                if (id != null && id.isNotBlank()) {
                    openIndividualById(id)
                } else {
                    Log.d("IndividualView", "✅ Физлицо создано, но ID не получен. Обновляю список.")
                    viewModel.refreshIndividuals()
                }
            }
        )
    }
}