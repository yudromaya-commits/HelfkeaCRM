package com.helfkea.crm.utils

// Функция для форматирования ISO даты (2025-12-17T14:30:00)
fun String?.formatIsoDateTime(): String {
    return if (this == null || this.isBlank()) {
        "Не указана"
    } else {
        try {
            // Пробуем разные форматы
            // Формат ISO: 2025-12-17T14:30:00
            if (this.contains("T")) {
                val parts = this.split("T")
                val datePart = parts[0]
                val timePart = parts.getOrNull(1) ?: ""

                // Форматируем дату из ISO (2025-12-17 -> 17.12.2025)
                val dateParts = datePart.split("-")
                if (dateParts.size >= 3) {
                    val day = dateParts[2]
                    val month = dateParts[1]
                    val year = dateParts[0]

                    // Берем только часы и минуты из времени
                    val timeDisplay = timePart.take(5)

                    "$day.$month.$year ${if (timeDisplay.isNotBlank()) timeDisplay else ""}".trim()
                } else {
                    this
                }
            }
            // Старый формат: 13.11.2024 9:32:20
            else if (this.contains(".") && this.contains(":")) {
                val parts = this.split(" ")
                val date = this.formatDate()
                val time = parts.getOrNull(1) ?: ""
                val timeDisplay = time.take(5)

                "$date ${if (timeDisplay.isNotBlank()) timeDisplay else ""}".trim()
            }
            else {
                this
            }
        } catch (e: Exception) {
            this
        }
    }
}

// Функции для форматирования дат
fun String.toReadableDate(): String {
    return try {
        val parts = this.split(".").take(3)
        if (parts.size >= 3) {
            "${parts[0]}.${parts[1]}.${parts[2]}"
        } else {
            this
        }
    } catch (e: Exception) {
        this
    }
}

// Переименованная функция для nullable строк
fun String?.formatDate(): String {
    return if (this == null || this.isBlank()) {
        "Не указана"
    } else {
        try {
            val parts = this.split(".").take(3)
            if (parts.size >= 3) {
                "${parts[0]}.${parts[1]}.${parts[2]}"
            } else {
                this
            }
        } catch (e: Exception) {
            this
        }
    }
}

fun String?.toReadableDateTime(): String {
    return if (this == null || this.isBlank()) {
        "Не указана"
    } else {
        try {
            // Сначала пробуем новый формат ISO
            if (this.contains("T")) {
                return formatIsoDateTime()
            }

            // Затем старый формат
            val dateTimeParts = this.split(" ")
            if (dateTimeParts.isNotEmpty()) {
                val date = dateTimeParts[0].toReadableDate()
                val time = dateTimeParts.getOrNull(1) ?: ""
                val timeDisplay = time.take(5)

                "$date ${if (timeDisplay.isNotBlank()) timeDisplay else ""}".trim()
            } else {
                this
            }
        } catch (e: Exception) {
            this
        }
    }
}

// Функция для форматирования денежных значений
fun Double.formatMoney(): String {
    return String.format("%,.0f ₽", this)
}

fun Int.formatMoney(): String {
    return String.format("%,d ₽", this)
}

fun Double?.formatMoney(default: String = "—"): String {
    return if (this != null && this > 0) {
        String.format("%,.0f ₽", this)
    } else {
        default
    }
}

fun Int?.formatMoney(default: String = "—"): String {
    return if (this != null && this > 0) {
        String.format("%,d ₽", this)
    } else {
        default
    }
}