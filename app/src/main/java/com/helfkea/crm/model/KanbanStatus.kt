package com.helfkea.crm.model

/**
 * Статусы для канбан-доски
 */
enum class KanbanStatus(
    val displayName: String,
    val colorHex: String,
    val order: Int
) {
    TODO(
        displayName = "К выполнению",
        colorHex = "#FFA726", // Orange
        order = 0
    ),
    IN_PROGRESS(
        displayName = "В работе",
        colorHex = "#29B6F6", // Blue
        order = 1
    ),
    REVIEW(
        displayName = "На проверке",
        colorHex = "#AB47BC", // Purple
        order = 2
    ),
    DONE(
        displayName = "Выполнено",
        colorHex = "#66BB6A", // Green
        order = 3
    ),
    OVERDUE(
        displayName = "Просрочено",
        colorHex = "#EF5350", // Red
        order = 4
    );

    companion object {
        /**
         * Преобразует строковый статус из 1С в KanbanStatus
         */
        fun from1CStatus(status: String): KanbanStatus {
            return when (status.lowercase()) {
                "назначена", "новая", "к выполнению" -> TODO
                "в работе", "выполняется" -> IN_PROGRESS
                "выполнена", "завершена", "готово" -> DONE
                "просрочена", "просрочено" -> OVERDUE
                "отменена", "отменено" -> DONE // Отмененные задачи идут в выполненные
                else -> TODO // По умолчанию
            }
        }

        /**
         * Преобразует KanbanStatus обратно в строку для 1С
         */
        fun to1CStatus(kanbanStatus: KanbanStatus): String {
            return when (kanbanStatus) {
                TODO -> "Назначена"
                IN_PROGRESS -> "В работе"
                REVIEW -> "В работе" // REVIEW нет в 1С, мапим в "В работе"
                DONE -> "Выполнена"
                OVERDUE -> "Просрочена"
            }
        }

        /**
         * Все статусы в порядке отображения
         */
        fun allInOrder(): List<KanbanStatus> {
            return values().sortedBy { it.order }
        }
    }
}