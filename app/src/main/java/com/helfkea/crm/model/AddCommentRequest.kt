package com.helfkea.crm.model

// AddCommentRequest - модель для добавления комментария к задаче

data class AddCommentRequest(
    val taskId: String,
    val text: String,
    val userId: String? = null  // Опционально, можно определить из настроек
)