package com.helfkea.crm.data.sync

enum class SyncStatus {
    SYNCED,      // Синхронизировано с сервером
    PENDING,     // Ожидает отправки
    FAILED       // Ошибка при отправке
}