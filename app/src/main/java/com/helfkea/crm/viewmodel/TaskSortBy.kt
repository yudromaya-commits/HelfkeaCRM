package com.helfkea.crm.viewmodel

enum class TaskSortBy {
    DATE_ASC,      // По дате (старые сначала)
    DATE_DESC,     // По дате (новые сначала)
    NAME_ASC,      // По названию (А-Я)
    NAME_DESC,     // По названию (Я-А)
    IMPORTANCE,    // По важности
    STATUS         // По статусу
}