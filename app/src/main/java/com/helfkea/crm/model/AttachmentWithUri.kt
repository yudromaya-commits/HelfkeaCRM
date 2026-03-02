package com.helfkea.crm.model

import android.net.Uri

// Модель для хранения вложения с URI (для локального использования)
data class AttachmentWithUri(
    val uri: Uri,
    val fileName: String,
    val fileType: String
)

// Оставляем старую модель для JSON (для API запросов)
data class AttachmentRequest(
    val fileName: String,
    val fileData: String,  // Base64 encoded
    val fileType: String
)