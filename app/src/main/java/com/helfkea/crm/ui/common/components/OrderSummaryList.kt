package com.helfkea.crm.ui.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.helfkea.crm.model.CartItem

@Composable
fun OrderSummaryList(cartItems: List<CartItem>) {
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
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${item.quantity} × ${String.format("%,.0f ₽", item.pricePerUnit)}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}