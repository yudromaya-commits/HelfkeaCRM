package com.helfkea.crm.model

object TaskStatus {
    const val ASSIGNED = "Назначена"
    const val IN_PROGRESS = "В работе"
    const val CANCELLED = "Отменена"
    const val COMPLETED = "Завершена"
    const val OVERDUE = "Просрочена"
    const val FOR_APPROVAL = "На согласовании"
    const val RETURNED_TO_WORK = "Возвращена в работу"

    val ALL = listOf(
        ASSIGNED,
        IN_PROGRESS,
        CANCELLED,
        COMPLETED,
        OVERDUE,
        FOR_APPROVAL,
        RETURNED_TO_WORK
    )

    val ACTIVE = listOf(
        ASSIGNED,
        IN_PROGRESS,
        FOR_APPROVAL,
        RETURNED_TO_WORK
    )

    val COMPLETED_STATUSES = listOf(
        COMPLETED,
        CANCELLED
    )

    fun isCompleted(status: String): Boolean {
        return status == COMPLETED || status == CANCELLED
    }

    fun getColor(status: String): String {
        return when (status) {
            ASSIGNED -> "#FFA726" // оранжевый
            IN_PROGRESS -> "#29B6F6" // голубой
            CANCELLED -> "#9E9E9E" // серый
            COMPLETED -> "#66BB6A" // зелёный
            OVERDUE -> "#EF5350" // красный
            FOR_APPROVAL -> "#AB47BC" // фиолетовый
            RETURNED_TO_WORK -> "#FF7043" // оранжево-красный
            else -> "#9E9E9E"
        }
    }
}
