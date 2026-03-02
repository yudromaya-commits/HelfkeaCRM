package com.example.repressales.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repressales.model.Product
import com.example.repressales.utils.formatMoney
import com.example.repressales.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier
) {
    val viewModel: ProductViewModel = viewModel()
    val isInCart by remember(product.productId) {
        derivedStateOf { viewModel.isInCart(product.productId) }
    }
    val cartQuantity by remember(product.productId) {
        derivedStateOf { viewModel.getCartQuantity(product.productId) }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок с артикулом
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Артикул
                    if (product.article.isNotBlank()) {
                        Text(
                            text = "Арт: ${product.article.trim()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    // Название товара
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Категория
                if (product.category.isNotBlank()) {
                    Surface(
                        color = Color(0xFF2196F3).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = product.category,
                            color = Color(0xFF2196F3),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Информация о цене и наличии
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // Цена
                    if (product.price != null && product.price > 0) {
                        Text(
                            text = product.price.formatMoney(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )

                        // Оптовая цена (если есть)
                        product.priceWholesale?.let { wholesalePrice ->
                            if (wholesalePrice > 0) {
                                Text(
                                    text = "Опт: ${wholesalePrice.formatMoney()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Цена не указана",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                // Наличие
                Surface(
                    color = when {
                        product.stockCount <= 0 -> Color(0xFFF44336).copy(alpha = 0.1f)
                        product.stockCount <= 3 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        else -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when {
                            product.stockCount <= 0 -> "Нет в наличии"
                            product.stockCount <= 3 -> "Осталось: ${product.stockCount}"
                            else -> "В наличии"
                        },
                        color = when {
                            product.stockCount <= 0 -> Color(0xFFF44336)
                            product.stockCount <= 3 -> Color(0xFFFF9800)
                            else -> Color(0xFF4CAF50)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопка добавления в корзину или управление количеством
            if (product.stockCount > 0 && product.price != null && product.price > 0) {
                if (isInCart) {
                    // Если товар уже в корзине - показываем управление количеством
                    CartQuantityControl(
                        productId = product.productId,
                        currentQuantity = cartQuantity,
                        maxQuantity = product.stockCount,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Кнопка добавления в корзину
                    Button(
                        onClick = { viewModel.addToCart(product) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = product.stockCount > 0 && product.price != null
                    ) {
                        Icon(
                            Icons.Default.AddShoppingCart,
                            contentDescription = "Добавить в корзину",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("В корзину")
                    }
                }
            } else {
                // Товар недоступен для заказа
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                ) {
                    Text("Недоступно для заказа")
                }
            }
        }
    }
}

@Composable
fun CartQuantityControl(
    productId: String,
    currentQuantity: Int,
    maxQuantity: Int,
    modifier: Modifier = Modifier
) {
    val viewModel: ProductViewModel = viewModel()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка уменьшения
        IconButton(
            onClick = { viewModel.decreaseCartItemQuantity(productId) },
            enabled = currentQuantity > 1
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Уменьшить",
                tint = if (currentQuantity > 1) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }

        // Количество
        Text(
            text = "$currentQuantity шт",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Кнопка увеличения
        IconButton(
            onClick = { viewModel.increaseCartItemQuantity(productId) },
            enabled = currentQuantity < maxQuantity
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Увеличить",
                tint = if (currentQuantity < maxQuantity) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}