# Установка канбан-доски в RepresSales2

## 📦 Содержимое архива

```
kanban_update/
├── INSTALL.md                 # Эта инструкция
├── KANBAN_README.md          # Документация по канбану
├── app/
│   └── src/
│       └── main/
│           └── java/
│               └── com/
│                   └── example/
│                       └── repressales/
│                           ├── MainActivity.kt              # Обновлённый
│                           ├── model/
│                           │   └── KanbanStatus.kt          # НОВЫЙ
│                           ├── ui/
│                           │   └── components/
│                           │       ├── CalendarWithKanbanView.kt  # НОВЫЙ
│                           │       ├── EnhancedKanbanView.kt      # НОВЫЙ
│                           │       ├── KanbanUtils.kt             # НОВЫЙ
│                           │       └── KanbanView.kt              # НОВЫЙ
│                           └── viewmodel/
│                               └── TaskViewModel.kt         # Обновлённый
```

## 🚀 Шаги установки

### 1. **Создайте резервную копию**
Перед началом создайте резервную копию вашего проекта.

### 2. **Скопируйте новые файлы**
Скопируйте файлы из архива в соответствующие директории вашего проекта:

```bash
# Модель статусов
cp kanban_update/app/src/main/java/com/example/repressales/model/KanbanStatus.kt \
   ваш_проект/app/src/main/java/com/example/repressales/model/

# UI компоненты
cp kanban_update/app/src/main/java/com/example/repressales/ui/components/*.kt \
   ваш_проект/app/src/main/java/com/example/repressales/ui/components/

# Обновлённые файлы
cp kanban_update/app/src/main/java/com/example/repressales/viewmodel/TaskViewModel.kt \
   ваш_проект/app/src/main/java/com/example/repressales/viewmodel/

cp kanban_update/app/src/main/java/com/example/repressales/MainActivity.kt \
   ваш_проект/app/src/main/java/com/example/repressales/
```

### 3. **Проверьте импорты**
Убедитесь, что в `MainActivity.kt` есть импорт:
```kotlin
import com.example.repressales.ui.components.CalendarWithKanbanView
```

И что вызов `CalendarView` заменён на `CalendarWithKanbanView`:
```kotlin
// Было:
"Календарь" -> CalendarView(...)

// Стало:
"Календарь" -> CalendarWithKanbanView(...)
```

### 4. **Проверьте зависимости**
Убедитесь, что в `build.gradle` есть необходимые зависимости:
```kotlin
dependencies {
    // Kotlin datetime (должна быть уже)
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    
    // Compose (должны быть уже)
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
}
```

### 5. **Соберите проект**
```bash
./gradlew clean
./gradlew assembleDebug
```

### 6. **Протестируйте**
1. Запустите приложение
2. Перейдите в раздел "Календарь"
3. Убедитесь, что есть две вкладки: "Календарь" и "Канбан"
4. Проверьте работу фильтров и поиска в канбане

## 🔧 Возможные проблемы и решения

### Проблема 1: Ошибки компиляции
**Симптом:** `Unresolved reference: CalendarWithKanbanView`
**Решение:** Проверьте правильность импортов и пути к файлам.

### Проблема 2: Отсутствует Kotlin datetime
**Симптом:** `Unresolved reference: kotlinx.datetime`
**Решение:** Добавьте в `build.gradle`:
```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
```

### Проблема 3: Конфликты с существующим кодом
**Симптом:** Ошибки в `TaskViewModel.kt`
**Решение:** Вместо замены всего файла, добавьте только новые методы:
```kotlin
// В конце TaskViewModel.kt добавьте:
// ========== КАНБАН-ДОСКА ==========
val tasks: StateFlow<List<Task>> get() = _allTasks.asStateFlow()
```

### Проблема 4: Не отображаются вкладки
**Симптом:** Видна только одна вкладка
**Решение:** Проверьте, что `CalendarWithKanbanView` правильно импортирован и используется.

## 📱 Использование

После успешной установки:
1. **Переключение вкладок** - вверху календаря
2. **Фильтрация** - кнопка "Фильтры" в канбане
3. **Поиск** - поле поиска в фильтрах
4. **Drag & drop** - визуальное перемещение (не изменяет 1С)

## 📞 Поддержка

Если возникли проблемы:
1. Проверьте логи компиляции
2. Сравните с оригинальными файлами из архива
3. Убедитесь в корректности путей и импортов

## 🔄 Откат изменений

Если нужно вернуться к старой версии:
1. Восстановите `MainActivity.kt` из резервной копии
2. Удалите файлы канбана:
   - `KanbanStatus.kt`
   - `CalendarWithKanbanView.kt`
   - `EnhancedKanbanView.kt`
   - `KanbanUtils.kt`
   - `KanbanView.kt`
3. Восстановите оригинальный `TaskViewModel.kt`

---
*Установка занимает 5-10 минут. Рекомендуется тестирование на эмуляторе перед использованием на production.*