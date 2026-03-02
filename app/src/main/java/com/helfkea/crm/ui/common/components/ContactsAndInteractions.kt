package com.helfkea.crm.ui.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helfkea.crm.model.ContactAddress
import com.helfkea.crm.model.Interaction
import com.helfkea.crm.utils.toReadableDateTime
import androidx.compose.foundation.BorderStroke
import com.helfkea.crm.ui.theme.PrimaryRed
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox

@Composable
fun ContactsSection(
    contacts: List<ContactAddress>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Контакты",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${contacts.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                contacts.forEach { contact ->
                    ContactCard(contact = contact)
                }
            }
        }
    }
}

@Composable
fun ContactCard(
    contact: ContactAddress,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Тип контакта с цветом
            contact.type?.let { type ->
                Surface(
                    color = getContactTypeColor(type).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = getContactTypeColor(type),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Отображаем представление
            contact.displayText.let { text ->
                if (text.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Выбираем иконку в зависимости от типа
                        val icon = when {
                            contact.isAddressType -> Icons.Default.LocationOn
                            contact.isPhoneType -> Icons.Default.Phone
                            contact.isEmailType -> Icons.Default.Email
                            else -> Icons.Default.Info
                        }

                        Icon(
                            icon,
                            contentDescription = contact.type ?: "Контакт",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactItemView(
    contactItem: ContactItem,
    modifier: Modifier = Modifier
) {
    Surface(
        color = contactItem.color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                contactItem.icon,
                contentDescription = null,
                tint = contactItem.color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = contactItem.text,
                style = MaterialTheme.typography.bodySmall,
                color = contactItem.color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

data class ContactItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val text: String,
    val color: Color
)

// Функция для получения цвета типа контакта
private fun getContactTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "фактический адрес" -> Color(0xFF1976D2)
        "юридический адрес" -> Color(0xFF388E3C)
        "основной" -> Color(0xFF9C27B0)
        "дополнительный" -> Color(0xFFFF9800)
        "дополнительный контакт" -> Color(0xFF0097A7)
        else -> Color.Gray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionsSection(
    interactions: List<Interaction>,
    contragentId: String,
    contragentName: String,
    onCreateInteraction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "История взаимодействий",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = PrimaryRed,
                            contentColor = Color.White
                        ) {
                            Text(interactions.size.toString())
                        }
                    }
                ) {
                    Text(
                        text = "История взаимодействий",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onCreateInteraction,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryRed
                ),
                border = BorderStroke(1.dp, PrimaryRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить взаимодействие")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (interactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Нет взаимодействий",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Нет истории взаимодействий",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    interactions.forEach { interaction ->
                        InteractionCard(interaction = interaction)
                    }
                }
            }
        }
    }
}

@Composable
fun InteractionCard(
    interaction: Interaction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (interaction.isPinned) {
                Color(0xFFFFF8E1) // Светло-желтый для закрепленных
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (interaction.isPinned) {
            BorderStroke(1.dp, Color(0xFFFFD54F))
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Верхняя строка с датой, типом и закреплением
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Иконка типа
                    val typeText = interaction.type ?: "Не указан"
                    val (icon, color) = getInteractionTypeInfo(interaction.type)
                    Icon(
                        icon,
                        contentDescription = "Тип",
                        modifier = Modifier.size(16.dp),
                        tint = color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = typeText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Дата
                    val dateText = interaction.date?.toReadableDateTime() ?: "Не указана"
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    // Иконка закрепления
                    if (interaction.isPinned) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Закреплено",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFF57C00)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Текст взаимодействия
            val interactionText = interaction.text ?: "Текст не указан"
            Text(
                text = interactionText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            // Пользователь и результат
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Пользователь
                interaction.user?.let { user ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Пользователь",
                            modifier = Modifier.size(12.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Результат
                interaction.result?.let { result ->
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = result,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color(0xFF1B5E20),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getInteractionTypeInfo(type: String?): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when (type?.lowercase()) {
        "звонок", "телефон" -> Pair(Icons.Default.Phone, Color(0xFF1976D2))
        "посещение", "встреча" -> Pair(Icons.Default.MeetingRoom, Color(0xFF388E3C))
        "email", "электронная почта" -> Pair(Icons.Default.Email, Color(0xFFD32F2F))
        "задача" -> Pair(Icons.Default.Task, Color(0xFF7B1FA2))
        "договор" -> Pair(Icons.Default.Description, Color(0xFFF57C00))
        "платеж" -> Pair(Icons.Default.Payments, Color(0xFF0097A7))
        else -> Pair(Icons.Default.History, Color.Gray)
    }
}