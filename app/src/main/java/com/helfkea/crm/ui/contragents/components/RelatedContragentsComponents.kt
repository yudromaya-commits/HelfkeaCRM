
package com.helfkea.crm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helfkea.crm.model.RelatedContragent

@Composable
fun RelatedContragentsChip(
    relatedContragents: List<RelatedContragent>,
    modifier: Modifier = Modifier,
    onContragentClick: (String, String) -> Unit = { _, _ -> }
) {
    Column(modifier = modifier) {
        if (relatedContragents.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(relatedContragents.take(3)) { relatedContragent ->
                    RelatedContragentChipItem(
                        relatedContragent = relatedContragent,
                        onClick = { onContragentClick(relatedContragent.id, relatedContragent.name) }
                    )
                }

                // Если есть еще связанные контрагенты, показываем счетчик
                if (relatedContragents.size > 3) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "+${relatedContragents.size - 3}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RelatedContragentChipItem(
    relatedContragent: RelatedContragent,
    onClick: () -> Unit = {}
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = relatedContragent.name,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Связанный контрагент",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@Composable
fun CompactRelatedContragents(
    relatedContragents: List<RelatedContragent>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(24.dp), // Уменьшаем высоту
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Более компактная иконка
        Icon(
            Icons.Default.Person,
            contentDescription = "Связанные контрагенты",
            modifier = Modifier
                .size(14.dp) // Уменьшаем размер иконки
                .padding(0.dp), // Убираем padding
            tint = Color(0xFF1976D2)
        )

        Spacer(modifier = Modifier.width(6.dp)) // Уменьшаем отступ

        // Компактный текст
        Text(
            text = if (relatedContragents.size == 1) {
                relatedContragents.first().name
            } else {
                "${relatedContragents.size}: ${relatedContragents.take(2).joinToString { it.name }}"
            },
            style = MaterialTheme.typography.labelSmall, // Уменьшаем размер текста
            color = Color.Gray,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}