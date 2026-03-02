package com.helfkea.crm.ui.navigation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helfkea.crm.R
import com.helfkea.crm.ui.theme.*

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val isSelected: Boolean = false
)

@Composable
fun NavigationPanel(
    modifier: Modifier = Modifier,
    onSectionSelected: (String) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onVersionClick: () -> Unit = {}
) {
    var selectedItem by remember { mutableStateOf("Задачи") }

    val navItems = listOf(
        NavigationItem("Задачи", Icons.Default.Checklist, selectedItem == "Задачи"),
        NavigationItem("Календарь", Icons.Default.DateRange, selectedItem == "Календарь"),
        NavigationItem("Физлица", Icons.Default.Person, selectedItem == "Физлица"),
        NavigationItem("Юрлица", Icons.Default.Business, selectedItem == "Юрлица"),
        NavigationItem("Сделки", Icons.Default.ShoppingCart, selectedItem == "Сделки"),
        NavigationItem("Аналитика", Icons.Default.Analytics, selectedItem == "Аналитика")
    )

    Column(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Заголовок и логотип
        CompanyHeader()

        Spacer(modifier = Modifier.height(8.dp))

        // Разделы навигации
        NavigationSections(
            navItems = navItems,
            selectedItem = selectedItem,
            onItemSelected = { item ->
                selectedItem = item
                onSectionSelected(item)
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Логотип внизу
        BrandLogo()

        Spacer(modifier = Modifier.height(16.dp))
        
        // Кнопка выхода и версия в одной строке
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка выхода (уменьшена на 30%)
            androidx.compose.material3.Button(
                onClick = onLogoutClick,
                modifier = Modifier.weight(0.7f), // 70% вместо 100%
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.Logout,
                    contentDescription = "Выход",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                androidx.compose.material3.Text("Выйти")
            }
            
            // Версия приложения (кликабельная)
            androidx.compose.material3.Surface(
                onClick = onVersionClick,
                modifier = Modifier.weight(0.3f), // 30% ширины
                shape = androidx.compose.material3.MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = "v1.15",
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CompanyHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок CRM с красным акцентом
        Text(
            text = "YO ORTHO CRM",
            color = HelfkeaBlueDark,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Подзаголовок
        Text(
            text = "Управление продажами",
            color = Gray700,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NavigationSections(
    navItems: List<NavigationItem>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        navItems.forEach { item ->
            NavigationItemRow(
                item = item,
                isSelected = item.title == selectedItem,
                onItemClick = { onItemSelected(item.title) }
            )
        }
    }
}

@Composable
private fun NavigationItemRow(
    item: NavigationItem,
    isSelected: Boolean,
    onItemClick: () -> Unit
) {
    val backgroundColor = if (isSelected) HelfkeaBlue.copy(alpha = 0.1f) else Color.Transparent
    val iconColor = if (isSelected) HelfkeaBlue else Gray700
    val textColor = if (isSelected) HelfkeaBlueDark else Gray800

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 16.sp
        )

        // Акцентный элемент для выбранного пункта

    }
}

@Composable
private fun BrandLogo() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ВАШ PNG ЛОГОТИП
        // Замените R.drawable.logo_repres на имя вашего PNG файла
        Image(
            painter = painterResource(id = R.drawable.logo), // Имя вашего PNG файла
            contentDescription = "Логотип Represa Sales",
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Текст под логотипом
        Text(
            text = "Sales Representative",
            color = HelfkeaBlueDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "© 2025 Все права защищены",
            color = Gray500,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// Дополнительные стили для кнопок в красно-белой теме
@Composable
fun HelfkeaBlueButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = HelfkeaBlue,
            contentColor = White,
            disabledContainerColor = Gray300,
            disabledContentColor = Gray700
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun OutlinedRedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            contentColor = HelfkeaBlue,
            disabledContentColor = Gray500
        ),
        // Исправляем border - создаем новый BorderStroke
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (enabled) HelfkeaBlue else Gray400
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text)
    }
}

// Вспомогательные цвета
val Gray400 = Color(0xFFBDBDBD)
val Gray500 = Color(0xFF9E9E9E)
val Gray600 = Color(0xFF757575)