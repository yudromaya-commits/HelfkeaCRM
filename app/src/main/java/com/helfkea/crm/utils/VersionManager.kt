package com.helfkea.crm.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// DataStore для хранения версии
private val Context.dataStore by preferencesDataStore(name = "version_prefs")

object VersionManager {
    private const val INITIAL_VERSION = "v1.02"
    private val VERSION_KEY = stringPreferencesKey("app_version")
    private val VERSION_HISTORY_KEY = stringPreferencesKey("version_history")
    
    // История версий (будет пополняться)
    private val versionHistory = listOf(
        VersionInfo(
            version = "v1.02",
            date = "2026-02-26",
            changes = listOf(
                "✅ Полная реорганизация структуры проекта",
                "✅ Профессиональная система авторизации",
                "✅ Реальная интеграция с 1С API",
                "✅ Красивая форма входа из 2 блоков",
                "✅ Безопасное хранение учетных данных",
                "✅ Автоматическое открытие карточек после создания",
                "✅ Унификация логики для юрлиц и физлиц",
                "✅ Восстановление красивых компонентов статистики",
                "✅ Компактный дизайн карточек"
            )
        ),
        VersionInfo(
            version = "v1.03",
            date = "2026-02-26",
            changes = listOf(
                "✅ Добавлена система версионирования",
                "✅ VersionManager для управления версиями",
                "✅ VersionHistoryScreen с историей изменений",
                "✅ Кнопка версии в навигационной панели",
                "✅ Кликабельная версия открывает историю",
                "✅ Автоматический инкремент версий"
            )
        ),
        VersionInfo(
            version = "v1.04",
            date = "2026-02-26",
            changes = listOf(
                "✅ Убран серый цвет с кнопки версии",
                "✅ Изменен фон с surfaceVariant на surface",
                "✅ Обновлен цвет контура на primary",
                "✅ Более гармоничное сочетание с дизайном"
            )
        ),
        VersionInfo(
            version = "v1.05",
            date = "2026-02-26",
            changes = listOf(
                "✅ Убран контур с кнопки версии",
                "✅ Только надпись на белом фоне",
                "✅ Минималистичный дизайн",
                "✅ Обновлен текст версии на v1.05"
            )
        ),
        VersionInfo(
            version = "v1.06",
            date = "2026-02-26",
            changes = listOf(
                "✅ Обновлена история версий в VersionManager",
                "✅ Добавлены версии v1.03, v1.04, v1.05"
            )
        ),
        VersionInfo(
            version = "v1.07",
            date = "2026-02-26",
            changes = listOf(
                "✅ Обновлена версия в NavigationPanel с v1.04 на v1.06"
            )
        ),
        VersionInfo(
            version = "v1.08",
            date = "2026-02-26",
            changes = listOf(
                "✅ Исправлена сортировка истории версий",
                "✅ Последняя версия теперь отображается сверху"
            )
        ),
        VersionInfo(
            version = "v1.09",
            date = "2026-02-27",
            changes = listOf(
                "✅ Обновлена версия в NavigationPanel с v1.07 на v1.08"
            )
        ),
        VersionInfo(
            version = "v1.10",
            date = "2026-03-02",
            changes = listOf(
                "🚀 РЕЛИЗ НА ПРОДАКШН",
                "✅ Изменен BASE_URL на продакшн: http://yo.serverworkdev.ru:8055/ortho/",
                "✅ Исправлена архитектура передачи контрагента",
                "✅ При создании задачи из карточки контрагента передаются и имя, и ID",
                "✅ Устранен баг с contragentId=null в API-запросах",
                "✅ Исправлен баг с обновлением контрагента в EditTaskDialog",
                "✅ Автоматическое закрытие панели после отметки задачи как выполненной",
                "✅ Включено подробное логирование API (HttpLoggingInterceptor.Level.BODY)",
                "✅ Исправлена логика сравнения полей при обновлении задачи",
                "✅ Добавлена система поиска контрагента по имени и ID"
            )
        ),
        VersionInfo(
            version = "v1.11",
            date = "2026-03-02",
            changes = listOf(
                "🎨 ОБНОВЛЕН БРЕНДИНГ",
                "✅ Изменено название приложения: RepressaSales → HELFKEA CRM",
                "✅ Обновлена иконка приложения",
                "✅ Созданы иконки для всех плотностей экрана (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)",
                "✅ Обновлены адаптивные иконки (adaptive icons)",
                "✅ Добавлен белый фон для иконок"
            )
        ),
        VersionInfo(
            version = "v1.12",
            date = "2026-03-02",
            changes = listOf(
                "🔧 ИСПРАВЛЕНИЯ МАНИФЕСТА",
                "✅ Исправлен android:label в AndroidManifest.xml",
                "✅ Исправлен android:icon (drawable → mipmap)",
                "✅ Теперь название и иконка берутся из ресурсов"
            )
        ),
        VersionInfo(
            version = "v1.13",
            date = "2026-03-02",
            changes = listOf(
                "🖼️ ИСПРАВЛЕНИЕ ИКОНКИ",
                "✅ Заменены WEBP файлы на PNG (Android лучше поддерживает PNG для иконок)",
                "✅ Удалены adaptive icons XML файлы",
                "✅ Добавлены PNG иконки для всех плотностей экрана",
                "✅ Иконки теперь должны отображаться правильно"
            )
        ),
        VersionInfo(
            version = "v1.14",
            date = "2026-03-02",
            changes = listOf(
                "🔧 ИСПРАВЛЕНИЕ ОШИБКИ СБОРКИ",
                "✅ Исправлена ошибка 'Duplicate resources'",
                "✅ Удалены дублирующиеся WEBP файлы",
                "✅ Удалены старые XML файлы иконок из drawable",
                "✅ Теперь только PNG файлы в mipmap папках"
            )
        ),
        VersionInfo(
            version = "v1.15",
            date = "2026-03-02",
            changes = listOf(
                "🎨 ОБНОВЛЕНИЕ БРЕНДИНГА HELFKEA CRM",
                "✅ Заменена иконка приложения на новую",
                "✅ Обновлены все размеры иконок (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)",
                "✅ Обновлены круглые версии иконок",
                "✅ Исправлено название проекта в settings.gradle.kts",
                "✅ Удалены старые файлы из корневой директории"
            )
        )
    )
    
    data class VersionInfo(
        val version: String,
        val date: String,
        val changes: List<String>
    )
    
    // Получить текущую версию
    fun getCurrentVersion(context: Context): String {
        return runBlocking {
            context.dataStore.data.first()[VERSION_KEY] ?: INITIAL_VERSION
        }
    }
    
    // Установить новую версию
    fun setVersion(context: Context, version: String) {
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[VERSION_KEY] = version
                
                // Добавляем в историю
                val currentHistory = preferences[VERSION_HISTORY_KEY] ?: ""
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val newEntry = "$version|$date\n"
                preferences[VERSION_HISTORY_KEY] = currentHistory + newEntry
            }
        }
    }
    
    // Инкрементировать версию (v1.02 → v1.03)
    fun incrementVersion(context: Context): String {
        val currentVersion = getCurrentVersion(context)
        val versionNumber = currentVersion.substring(1).toDouble()
        val newVersion = "v${String.format("%.2f", versionNumber + 0.01)}"
        
        setVersion(context, newVersion)
        return newVersion
    }
    
    // Получить историю версий
    fun getVersionHistory(): List<VersionInfo> {
        return versionHistory
    }
    
    // Получить информацию о конкретной версии
    fun getVersionInfo(version: String): VersionInfo? {
        return versionHistory.find { it.version == version }
    }
    
    // Получить последние изменения
    fun getLatestChanges(): List<String> {
        return versionHistory.last().changes
    }
    
    // Форматированная версия для отображения
    fun getDisplayVersion(context: Context): String {
        return "Версия: ${getCurrentVersion(context)}"
    }
}