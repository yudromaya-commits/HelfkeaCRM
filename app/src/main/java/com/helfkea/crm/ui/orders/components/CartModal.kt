package com.helfkea.crm.ui.orders.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.helfkea.crm.model.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartModal(
    cartItems: List<CartItem>,
    totalPrice: Double,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearCart: () -> Unit,
    onDismiss: () -> Unit,
    onCheckout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
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
                        "Корзина",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        // Кнопка очистки
                        if (cartItems.isNotEmpty()) {
                            IconButton(
                                onClick = onClearCart,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Очистить корзину",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // Кнопка закрытия
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Список товаров в корзине
                if (cartItems.isEmpty()) {
                    EmptyCartContent()
                } else {
                    CartItemList(
                        cartItems = cartItems,
                        onUpdateQuantity = onUpdateQuantity,
                        onRemoveItem = onRemoveItem
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Итого и кнопки
                if (cartItems.isNotEmpty()) {
                    Column {
                        // Итоговая сумма
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Итого:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                String.format("%,.0f ₽", totalPrice),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Общее количество
                        Text(
                            "Товаров: ${cartItems.size}, единиц: ${cartItems.sumOf { it.quantity }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Кнопки действий
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
                                )
                            ) {
                                Text("Продолжить покупки")
                            }

                            Button(
                                onClick = onCheckout,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Оформить")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = "Корзина пуста",
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Корзина пуста",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Добавьте товары из каталога",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun CartItemList(
    cartItems: List<CartItem>,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 400.dp)
    ) {
        items(cartItems) { item ->
            CartItemRow(
                item = item,
                onUpdateQuantity = { newQuantity ->
                    onUpdateQuantity(item.product.productId, newQuantity)
                },
                onRemove = {
                    onRemoveItem(item.product.productId)
                }
            )
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Информация о товаре
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.product.article.trim(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${String.format("%,.0f ₽", item.pricePerUnit)} ×",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Управление количеством
            QuantitySelector(
                quantity = item.quantity,
                maxQuantity = item.product.stockCount,
                onQuantityChange = onUpdateQuantity
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Сумма и удаление
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format("%,.0f ₽", item.totalPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    maxQuantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Кнопка уменьшения
        IconButton(
            onClick = { onQuantityChange(quantity - 1) },
            enabled = quantity > 1,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Уменьшить",
                tint = if (quantity > 1) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }

        // Количество
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Кнопка увеличения
        IconButton(
            onClick = { onQuantityChange(quantity + 1) },
            enabled = quantity < maxQuantity,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Увеличить",
                tint = if (quantity < maxQuantity) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}