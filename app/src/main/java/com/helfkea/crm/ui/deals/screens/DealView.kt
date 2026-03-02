
package com.helfkea.crm.ui.deals.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.helfkea.crm.viewmodel.DealViewModel
import com.helfkea.crm.viewmodel.DealViewModelFactory
import com.helfkea.crm.repository.ProductRepository
import com.helfkea.crm.repository.OrderRepository
import com.helfkea.crm.viewmodel.CreateTaskViewModel
import com.helfkea.crm.model.ContragentInTask
import androidx.compose.ui.window.Dialog
import com.helfkea.crm.ui.deals.components.DealFiltersPanel
import com.helfkea.crm.ui.deals.components.CartModal
import com.helfkea.crm.ui.orders.components.OrderSuccessContent
import com.helfkea.crm.ui.orders.components.OrderErrorContent
import com.helfkea.crm.ui.products.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealView(
    modifier: Modifier = Modifier
) {
    val productRepository = remember { ProductRepository() }
    val orderRepository = remember { OrderRepository() }

    val viewModel: DealViewModel = viewModel(
        factory = DealViewModelFactory(productRepository, orderRepository)
    )

    val createTaskViewModel: CreateTaskViewModel = viewModel()

    val uiState by viewModel.uiState.collectAsState()

    val totalCartItems = uiState.cartItems.sumOf { it.quantity }
    val totalCartPrice = uiState.cartItems.sumOf { it.totalPrice }

    // Управление отображением корзины
    var showCart by remember { mutableStateOf(false) }
    var showCheckout by remember { mutableStateOf(false) }

    // Для диалога выбора контрагента
    var showContragentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Выберите товар",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    // Иконка корзины с количеством товаров
                    Box {
                        IconButton(
                            onClick = { showCart = true },
                            enabled = uiState.cartItems.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Корзина"
                            )
                        }
                        if (totalCartItems > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-8).dp, y = 8.dp)
                            ) {
                                Text(totalCartItems.toString())
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.cartItems.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showCheckout = true },
                    icon = {
                        Icon(Icons.Default.CheckCircle, "Оформить заказ")
                    },
                    text = {
                        Text("Оформить (${String.format("%,.0f ₽", totalCartPrice)})")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Панель поиска и фильтров
            DealFiltersPanel(
                searchQuery = uiState.searchQuery,
                selectedCategory = uiState.selectedCategory,
                categories = viewModel.categories,
                filteredProductsCount = uiState.filteredProducts.size,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onCategorySelect = viewModel::selectCategory,
                onRefresh = viewModel::loadProducts
            )

            // Список товаров
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                ErrorState(
                    error = uiState.error!!,
                    onRetry = viewModel::loadProducts
                )
            } else if (uiState.filteredProducts.isEmpty()) {
                EmptyState(
                    searchQuery = uiState.searchQuery,
                    selectedCategory = uiState.selectedCategory,
                    onClearFilters = {
                        viewModel.updateSearchQuery("")
                        viewModel.selectCategory(null)
                    }
                )
            } else {
                ProductGrid(
                    products = uiState.filteredProducts,
                    cartItems = uiState.cartItems,
                    onAddToCart = viewModel::addToCart
                )
            }
        }

        // Модальные окна
        if (showCart) {
            CartModal(
                cartItems = uiState.cartItems,
                totalPrice = totalCartPrice,
                onUpdateQuantity = viewModel::updateCartItemQuantity,
                onRemoveItem = viewModel::removeFromCart,
                onClearCart = viewModel::clearCart,
                onDismiss = { showCart = false },
                onCheckout = {
                    showCart = false
                    showCheckout = true
                }
            )
        }

        if (showCheckout) {
            EnhancedCheckoutModal(
                cartItems = uiState.cartItems,
                totalPrice = totalCartPrice,
                comment = uiState.orderComment,
                isCreatingOrder = uiState.isCreatingOrder,
                orderSuccess = uiState.orderSuccess,
                orderMessage = uiState.orderMessage,
                onCommentChange = viewModel::updateOrderComment,
                onCreateOrder = viewModel::createOrder,
                onDismiss = {
                    showCheckout = false
                    if (uiState.orderSuccess == true) {
                        viewModel.clearOrderStatus()
                    }
                },
                viewModel = viewModel // ← ПЕРЕДАЕМ ViewModel
            )
        }

        // Диалог выбора контрагента (такой же как в CreateTaskPanel)
        if (showContragentDialog) {
            ContragentSelectionDialog(
                show = true,
                contragents = createTaskViewModel.contragents.collectAsState().value,
                isLoading = createTaskViewModel.isLoadingContragents.collectAsState().value,
                onDismiss = { showContragentDialog = false },
                onContragentSelected = { contragent ->
                    createTaskViewModel.setContragent(contragent.id, contragent.name)
                    showContragentDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCheckoutModal(
    cartItems: List<com.helfkea.crm.model.CartItem>,
    totalPrice: Double,
    comment: String,
    isCreatingOrder: Boolean,
    orderSuccess: Boolean?,
    orderMessage: String?,
    onCommentChange: (String) -> Unit,
    onCreateOrder: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: DealViewModel // ← ПЕРЕДАЕМ ViewModel
) {
    // Получаем состояние из ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val selectedContragent = uiState.selectedContragent

    var showContragentDialog by remember { mutableStateOf(false) }

    // Загружаем контрагентов при открытии диалога
    LaunchedEffect(showContragentDialog) {
        if (showContragentDialog) {
            viewModel.loadContragents()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            when {
                orderSuccess == true -> {
                    OrderSuccessContent(
                        message = orderMessage ?: "Заказ успешно создан",
                        onDismiss = onDismiss
                    )
                }

                orderSuccess == false -> {
                    OrderErrorContent(
                        message = orderMessage ?: "Ошибка создания заказа",
                        onRetry = onCreateOrder,
                        onDismiss = onDismiss
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Заголовок
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Оформление заказа",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Закрыть")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Клиент
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Клиент",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                if (selectedContragent != null) {
                                    // Выбранный контрагент
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = "Клиент",
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    selectedContragent.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    "Выбранный клиент",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { viewModel.clearContragent() },
                                            modifier = Modifier.size(32.dp),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                "Убрать клиента",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                } else {
                                    // Кнопка выбора контрагента
                                    OutlinedButton(
                                        onClick = { showContragentDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            contentColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Добавить клиента",
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Выбрать клиента")
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Сводка заказа
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Сводка заказа",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OrderSummaryList(cartItems = cartItems)

                                Spacer(modifier = Modifier.height(12.dp))

                                // Итоговая сумма
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Итого к оплате:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        String.format("%,.0f ₽", totalPrice),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Комментарий к заказу
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Комментарий к заказу",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = comment,
                                    onValueChange = onCommentChange,
                                    placeholder = { Text("Введите комментарий (не обязательно)...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Кнопки
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                enabled = !isCreatingOrder
                            ) {
                                Text("Отмена")
                            }

                            Button(
                                onClick = onCreateOrder,
                                modifier = Modifier.weight(1f),
                                enabled = !isCreatingOrder && cartItems.isNotEmpty() && selectedContragent != null
                            ) {
                                if (isCreatingOrder) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Создание...")
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Создать заказ")
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
        ContragentSelectionDialog(
            show = true,
            contragents = uiState.contragents,
            isLoading = uiState.isLoadingContragents,
            onDismiss = { showContragentDialog = false },
            onContragentSelected = { contragent ->
                viewModel.selectContragent(contragent)
                showContragentDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContragentSelectionDialog(
    show: Boolean,
    contragents: List<ContragentInTask>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onContragentSelected: (ContragentInTask) -> Unit
) {
    if (!show) return

    var searchQuery by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Заголовок
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Выберите клиента",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Close, "Закрыть")
                        }
                    }
                }

                // Поиск
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск клиента...") },
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

                // Список контрагентов
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
                                "Нет доступных клиентов",
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
                                        onContragentSelected(contragent)
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
                                            Icons.Default.Person,
                                            contentDescription = "Клиент",
                                            tint = MaterialTheme.colorScheme.primary,
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

@Composable
fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = "Ошибка",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Повторить")
        }
    }
}

@Composable
fun EmptyState(
    searchQuery: String,
    selectedCategory: String?,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = "Нет товаров",
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (searchQuery.isNotEmpty() || selectedCategory != null) {
                "Товары не найдены"
            } else {
                "Товары отсутствуют"
            },
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (searchQuery.isNotEmpty()) {
                "По запросу \"$searchQuery\" ничего не найдено"
            } else if (selectedCategory != null) {
                "В категории \"$selectedCategory\" товаров нет"
            } else {
                "Загрузите товары с сервера"
            },
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (searchQuery.isNotEmpty() || selectedCategory != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClearFilters) {
                Icon(Icons.Default.ClearAll, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Сбросить фильтры")
            }
        }
    }
}

@Composable
fun ProductGrid(
    products: List<com.helfkea.crm.model.Product>,
    cartItems: List<com.helfkea.crm.model.CartItem>,
    onAddToCart: (com.helfkea.crm.model.Product) -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(products) { product ->
            ProductCard(
                product = product,
                onAddToCart = { onAddToCart(product) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun OrderSummaryList(cartItems: List<com.helfkea.crm.model.CartItem>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.heightIn(max = 150.dp)
    ) {
        items(cartItems) { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${item.product.name.take(30)}${if (item.product.name.length > 30) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${item.quantity} × ${String.format("%,.0f ₽", item.pricePerUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
