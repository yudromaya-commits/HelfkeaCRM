package com.helfkea.crm.model

import com.google.gson.annotations.SerializedName

data class Task(
    @SerializedName("id")
    val id: String? = null,  // ID задачи (должен передаваться из 1C)
    val date: String,
    val description: String,
    val status: String,
    val producer: String,
    val executionDate: String,
    val name: String,
    val important: Boolean,
    val executor: String? = null,
    @SerializedName("contragentId")
    val contragentId: String? = null,  // ID контрагента из 1C
    
    @SerializedName("contragentName") 
    val contragentName: String? = null,  // Название контрагента из 1C
    
    val contragent: ContragentInTask? = null,  // Старое поле (возможно deprecated)
    val attachments: List<TaskAttachment>? = null,  // ИЗМЕНЕНО: nullable с null по умолчанию
    val comment: List<TaskComment>? = null          // ИЗМЕНЕНО: nullable с null по умолчанию
)

// Модель для контрагента в задаче (упрощенная)
data class ContragentInTask(
    val id: String,
    val name: String
)

// Модель для прикрепленного файла
data class TaskAttachment(
    val id: String? = null,
    val fileName: String,
    val fileSize: Long,
    val fileType: String,  // "image", "document", "other"
    val fileUrl: String? = null,
    val localUri: String? = null,  // Для временного хранения URI локального файла
    val uploadDate: String? = null
)

// Модель для комментария задачи
data class TaskComment(
    @SerializedName("Дата")
    val date: String,

    @SerializedName("Пользователь")
    val user: String,

    @SerializedName("Комментарий")
    val text: String
)





// Модель для создания задачи (ОБНОВЛЯЕМ)
data class CreateTaskRequest(
    val name: String,
    val description: String,
    val status: String? = null,
    val executorId: String? = null,
    val executionDate: String? = null,
    val important: Boolean? = null,
    val contragentId: String? = null,
    val attachments: List<AttachmentRequest>? = null,  // ИЗМЕНЯЕМ ТИП
    val comments: List<Comment>? = null
)
// Модель для запроса прикрепления файла


data class Comment(
    val comment: String
)

data class LocalAttachment(
    val uri: String,  // URI как строка
    val fileName: String,
    val fileSize: Long,
    val fileType: String
)

// Модель для ответа при создании задачи
data class CreateTaskResponse(
    val success: Boolean,
    val id: String? = null,
    val message: String? = null,
    val task: Task? = null,
    val error: String? = null
)

// Модель для ответа загрузки файла
data class UploadFileResponse(
    val success: Boolean,
    val fileId: String? = null,
    val fileUrl: String? = null,
    val message: String? = null,
    val error: String? = null
)