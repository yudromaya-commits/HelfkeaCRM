package com.helfkea.crm.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helfkea.crm.data.sync.UniversalSyncManager
import com.helfkea.crm.data.sync.SyncState
import kotlinx.coroutines.delay

@Composable
fun SyncStatusBar(
    syncManager: UniversalSyncManager,
    modifier: Modifier = Modifier
) {
    val syncState by syncManager.syncState.collectAsState()
    val pendingCount by syncManager.pendingCount.collectAsState()
    val failedCount by syncManager.failedCount.collectAsState()
    
    // Автоматически скрываем успешные сообщения через 3 секунды
    var showSuccess by remember { mutableStateOf(false) }
    
    LaunchedEffect(syncState) {
        if (syncState is SyncState.Success) {
            showSuccess = true
            delay(3000)
            showSuccess = false
        }
    }
    
    // Показываем только если есть что показать
    val shouldShow = when {
        syncState is SyncState.Syncing -> true
        showSuccess -> true
        syncState is SyncState.Error -> true
        pendingCount > 0 || failedCount > 0 -> true
        else -> false
    }
    
    if (!shouldShow) return
    
    val (icon, text, color) = when {
        syncState is SyncState.Syncing -> Triple(
            Icons.Default.Sync,
            "Синхронизация...",
            MaterialTheme.colorScheme.primary
        )
        
        syncState is SyncState.Success -> {
            val successState = syncState as SyncState.Success
            val message = if (successState.failedCount > 0) {
                "Синхронизировано: ${successState.sentCount} отправлено, ${successState.failedCount} ошибок"
            } else if (successState.sentCount > 0) {
                "Синхронизировано: ${successState.sentCount} отправлено"
            } else {
                "Всё синхронизировано"
            }
            Triple(
                Icons.Default.CloudDone,
                message,
                MaterialTheme.colorScheme.primary
            )
        }
        
        syncState is SyncState.Error -> Triple(
            Icons.Default.Error,
            (syncState as SyncState.Error).message,
            MaterialTheme.colorScheme.error
        )
        
        failedCount > 0 -> Triple(
            Icons.Default.Error,
            "$failedCount задач с ошибкой синхронизации",
            MaterialTheme.colorScheme.error
        )
        
        pendingCount > 0 -> Triple(
            Icons.Default.CloudUpload,
            "$pendingCount задач ожидают синхронизации",
            MaterialTheme.colorScheme.secondary
        )
        
        else -> Triple(
            Icons.Default.CloudDone,
            "Всё синхронизировано",
            MaterialTheme.colorScheme.primary
        )
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = text,
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Кнопка синхронизации, если есть что синхронизировать
            if (pendingCount > 0 || failedCount > 0) {
                IconButton(
                    onClick = { syncManager.syncAll() },
                    modifier = Modifier.size(36.dp),
                    enabled = syncState !is SyncState.Syncing
                ) {
                    if (syncState is SyncState.Syncing) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                            color = color
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Синхронизировать",
                            tint = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactSyncIndicator(
    syncManager: UniversalSyncManager,
    modifier: Modifier = Modifier
) {
    val pendingCount by syncManager.pendingCount.collectAsState()
    val failedCount by syncManager.failedCount.collectAsState()
    val syncState by syncManager.syncState.collectAsState()
    
    val (icon, color, tooltip) = when {
        syncState is SyncState.Syncing -> Triple(
            Icons.Default.Sync,
            MaterialTheme.colorScheme.primary,
            "Синхронизация..."
        )
        
        failedCount > 0 -> Triple(
            Icons.Default.Error,
            MaterialTheme.colorScheme.error,
            "$failedCount ошибок синхронизации"
        )
        
        pendingCount > 0 -> Triple(
            Icons.Default.CloudUpload,
            MaterialTheme.colorScheme.secondary,
            "$pendingCount ожидают синхронизации"
        )
        
        else -> Triple(
            Icons.Default.CloudDone,
            MaterialTheme.colorScheme.primary,
            "Всё синхронизировано"
        )
    }
    
    Box(
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        
        // Простой индикатор количества
        if (pendingCount > 0 || failedCount > 0) {
            val count = pendingCount + failedCount
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-4).dp)
                        .size(16.dp)
                        .background(
                            color = color,
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (count > 9) "9+" else count.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}