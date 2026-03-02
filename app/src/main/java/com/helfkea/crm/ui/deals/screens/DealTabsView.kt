package com.helfkea.crm.ui.deals.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helfkea.crm.repository.OrdersRepository
import com.helfkea.crm.repository.OrderRepository
import com.helfkea.crm.repository.ProductRepository
import com.helfkea.crm.viewmodel.DealViewModel
import com.helfkea.crm.viewmodel.DealViewModelFactory
import com.helfkea.crm.viewmodel.OrdersViewModel
import com.helfkea.crm.viewmodel.OrdersViewModelFactory
import com.helfkea.crm.model.OrderResponse
import com.helfkea.crm.ui.deals.screens.DealView
import com.helfkea.crm.ui.orders.screens.OrdersListView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealTabsView(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }

    // ViewModels
    val productRepository = remember { ProductRepository() }
    val ordersRepository = remember { OrdersRepository() }
    val orderRepository = remember { OrderRepository() }

    val dealViewModel: DealViewModel = viewModel(
        factory = DealViewModelFactory(productRepository, orderRepository)
    )

    val ordersViewModel: OrdersViewModel = viewModel(
        factory = OrdersViewModelFactory(ordersRepository)
    )

    // Загружаем заказы при открытии вкладки
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            ordersViewModel.loadOrders()
        }
    }

    val dealUiState by dealViewModel.uiState.collectAsState()
    val cartItemCount = dealUiState.cartItems.sumOf { it.quantity }

    Scaffold(
        topBar = {
            Column {
                // Красивые табы
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Таб "Товары"
                    Box(modifier = Modifier.weight(1f)) {
                        TabItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = Icons.Default.ShoppingBag,
                            text = "Товары",
                            badgeCount = cartItemCount
                        )
                    }

                    // Таб "Заказы"
                    Box(modifier = Modifier.weight(1f)) {
                        TabItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = Icons.Default.ListAlt,
                            text = "Заказы"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Контент вкладок
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> {
                        DealContentView(
                            dealViewModel = dealViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    1 -> {
                        OrdersContentView(
                            ordersViewModel = ordersViewModel,
                            onBackToProducts = { selectedTab = 0 },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    badgeCount: Int = 0
) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 2.dp else 0.dp,
            pressedElevation = if (selected) 4.dp else 2.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Icon(
                    icon,
                    contentDescription = text,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )

                if (badgeCount > 0 && text == "Товары") {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun DealContentView(
    dealViewModel: DealViewModel,
    modifier: Modifier = Modifier
) {
    DealView(modifier = modifier)
}

@Composable
fun OrdersContentView(
    ordersViewModel: OrdersViewModel,
    onBackToProducts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by ordersViewModel.uiState.collectAsState()

    OrdersListView(
        orders = uiState.orders,
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = ordersViewModel::updateSearchQuery,
        onBackToProducts = onBackToProducts,
        isLoading = uiState.isLoading,
        error = uiState.error,
        modifier = modifier
    )
}

// OrdersViewModel (нужно создать)
data class OrdersUiState(
    val orders: List<OrderResponse> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)