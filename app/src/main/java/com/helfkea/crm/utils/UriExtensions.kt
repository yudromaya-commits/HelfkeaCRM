package com.helfkea.crm.utils

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

suspend fun convertUriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val byteArrayOutputStream = ByteArrayOutputStream()

        inputStream?.use { input ->
            byteArrayOutputStream.use { output ->
                val buffer = ByteArray(1024)
                var length: Int
                while (input.read(buffer).also { length = it } != -1) {
                    output.write(buffer, 0, length)
                }
            }
        }

        val bytes = byteArrayOutputStream.toByteArray()
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Функция для определения MIME типа
fun getMimeType(context: Context, uri: Uri): String {
    return context.contentResolver.getType(uri) ?: "application/octet-stream"
}

// Функция для получения размера файла
fun getFileSize(context: Context, uri: Uri): Long {
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex("_size")
                if (sizeIndex != -1) {
                    it.getLong(sizeIndex)
                } else {
                    0L
                }
            } else {
                0L
            }
        } ?: 0L
    } catch (e: Exception) {
        0L
    }
}