package com.helfkea.crm.utils

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object FileUtils {

    suspend fun convertUriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val byteArrayOutputStream = ByteArrayOutputStream()

            inputStream?.use { input ->
                byteArrayOutputStream.use { output ->
                    val buffer = ByteArray(1024 * 8) // 8KB буфер
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

    fun getMimeType(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri) ?: "application/octet-stream"
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun getFileName(context: Context, uri: Uri): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex("_display_name")
                    if (displayNameIndex != -1) {
                        it.getString(displayNameIndex) ?: "file_${System.currentTimeMillis()}"
                    } else {
                        "file_${System.currentTimeMillis()}"
                    }
                } else {
                    "file_${System.currentTimeMillis()}"
                }
            } ?: "file_${System.currentTimeMillis()}"
        } catch (e: Exception) {
            "file_${System.currentTimeMillis()}"
        }
    }
}

// Функции-расширения для удобства
suspend fun Uri.toBase64(context: Context): String? = FileUtils.convertUriToBase64(context, this)
fun Uri.getMimeType(context: Context): String = FileUtils.getMimeType(context, this)
fun Uri.getFileSize(context: Context): Long = FileUtils.getFileSize(context, this)
fun Uri.getFileName(context: Context): String = FileUtils.getFileName(context, this)