package com.helfkea.crm.ui.products.screens

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
import com.helfkea.crm.viewmodel.ProductViewModel
import androidx.compose.foundation.background
import com.helfkea.crm.ui.products.components.ProductFiltersPanel
import com.helfkea.crm.ui.products.components.ProductCard


@Composable
fun ProductView(
    modifier: Modifier = Modifier,
    onShowCart: () -> Unit = {}
) {
    val viewModel: ProductViewModel = viewModel()
    val products by viewModel.filteredProducts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val cartItemsCount by viewModel.cartItemsCount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Заголовок и кнопка корзины
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Сделки",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Каталог товаров",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Кнопка корзины с бейджем
            Box {
                IconButton(
                    onClick = onShowCart,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Корзина"
                    )
                }

                // Бейдж с количеством товаров
                if (cartItemsCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cartItemsCount > 9) "9+" else cartItemsCount.toString(),
                            color = Color.White,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Панель поиска и фильтров (БЕЗ параметров)
        ProductFiltersPanel()

        // Состояние загрузки
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Загрузка товаров...")
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.loadProducts() },
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

        // Список товаров
        if (products.isEmpty() && !isLoading && error == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = "Нет товаров",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Товары не найдены",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Измените параметры поиска или фильтры",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.clearFilters()
                        },
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
        } else if (products.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(products) { product ->
                    ProductCard(
                        product = product,
                        onAddToCart = { viewModel.addToCart(product) }
                    )
                }

                // Добавляем отступ внизу
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}