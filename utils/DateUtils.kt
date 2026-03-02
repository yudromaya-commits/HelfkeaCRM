package com.example.repressales.utils

// Функции для форматирования дат (публичные)
fun String.toReadableDate(): String {
    return try {
        val parts = this.split(" ")[0].split(".")
        "${parts[0]}.${parts[1]}.${parts[2]}"
    } catch (e: Exception) {
        this
    }
}

fun String.toReadableDateTime(): String {
    return try {
        val dateTimeParts = this.split(" ")
        val date = dateTimeParts[0].toReadableDate()
        val time = dateTimeParts.getOrNull(1) ?: ""
        "$date ${time.take(5)}"
    } catch (e: Exception) {
        this
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