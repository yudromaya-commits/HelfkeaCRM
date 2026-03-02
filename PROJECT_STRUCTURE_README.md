# 🏗️ Структура проекта RepresSale (после рефакторинга)

## 📁 Новая структура папок

```
app/src/main/java/com/example/repressales/
├── api/                    # API клиенты и сервисы
├── model/                  # Модели данных
├── repository/             # Репозитории для работы с данными
├── viewmodel/              # ViewModel'и
├── utils/                  # Утилиты и хелперы
├── ui/                     # Пользовательский интерфейс
│   ├── theme/              # Тема приложения
│   │   ├── Color.kt        # Цвета
│   │   ├── Theme.kt        # Основная тема
│   │   └── Type.kt         # Типографика
│   ├── navigation/         # Навигация
│   │   ├── components/     # Компоненты навигации
│   │   │   ├── NavigationPanel.kt     # Боковая панель навигации
│   │   │   └── ModernTabRow.kt        # Современные табы
│   │   └── screens/        # Экран навигации
│   │       └── DashboardView.kt       # Дашборд
│   ├── tasks/              # Задачи
│   │   ├── screens/        # Экраны задач
│   │   │   ├── TaskView.kt             # Список задач
│   │   │   ├── TaskDetailScreen.kt     # Детали задачи (содержит TaskDetailPanel)
│   │   │   └── CreateTaskPanel.kt      # Панель создания задачи
│   │   ├── dialogs/        # Диалоги задач
│   │   │   ├── CreateTaskDialog.kt     # Диалог создания задачи
│   │   │   └── EditTaskDialog.kt       # Диалог редактирования задачи
│   │   └── components/     # Компоненты задач (пока пусто)
│   ├── contragents/        # Юридические лица
│   │   ├── screens/        # Экраны юрлиц
│   │   │   ├── ContragentView.kt           # Список юрлиц
│   │   │   └── ContragentDetailScreen.kt   # Детали юрлица
│   │   ├── dialogs/        # Диалоги юрлиц
│   │   │   ├── EnhancedCreateContragentDialog.kt  # Диалог создания юрлица
│   │   │   └── SnapContragentDialog.kt     # Диалог привязки контрагента
│   │   └── components/     # Компоненты юрлиц
│   │       ├── ContragentFiltersPanel.kt   # Фильтры
│   │       ├── RelatedContragentsChip.kt   # Чип связанных контрагентов
│   │       └── RelatedContragentsComponents.kt # Компоненты связанных контрагентов
│   ├── individuals/        # Физические лица
│   │   ├── screens/        # Экраны физлиц
│   │   │   ├── IndividualView.kt           # Список физлиц
│   │   │   └── IndividualDetailScreen.kt   # Детали физлица
│   │   └── components/     # Компоненты физлиц
│   │       ├── IndividualCard.kt           # Карточка физлица
│   │       └── IndividualFiltersPanel.kt   # Фильтры
│   ├── calendar/           # Календарь
│   │   ├── screens/        # Экраны календаря
│   │   │   ├── CalendarView.kt             # Календарь
│   │   │   └── CalendarWithKanbanView.kt   # Календарь с канбаном
│   │   └── components/     # Компоненты календаря
│   │       ├── KanbanView.kt               # Канбан
│   │       ├── EnhancedKanbanView.kt       # Улучшенный канбан
│   │       └── KanbanUtils.kt              # Утилиты канбана
│   ├── deals/              # Сделки
│   │   ├── screens/        # Экраны сделок
│   │   │   ├── DealView.kt                 # Список сделок
│   │   │   └── DealTabsView.kt             # Вкладки сделок
│   │   └── components/     # Компоненты сделок
│   │       └── DealFiltersPanel.kt         # Фильтры сделок
│   ├── orders/             # Заказы
│   │   ├── screens/        # Экраны заказов
│   │   │   ├── OrdersListView.kt           # Список заказов
│   │   │   ├── CartView.kt                 # Корзина
│   │   │   └── CheckoutView.kt             # Оформление заказа
│   │   └── components/     # Компоненты заказов
│   │       ├── CartModal.kt                # Модальное окно корзины
│   │       └── CheckoutModal.kt            # Модальное окно оформления
│   ├── products/           # Товары
│   │   ├── screens/        # Экраны товаров
│   │   │   └── ProductView.kt              # Список товаров
│   │   └── components/     # Компоненты товаров
│   │       ├── ProductCard.kt              # Карточка товара
│   │       └── ProductFiltersPanel.kt      # Фильтры товаров
│   ├── common/             # Общие компоненты
│   │   ├── components/     # Общие компоненты
│   │   │   └── ContactsAndInteractions.kt  # Контакты и взаимодействия
│   │   └── dialogs/        # Общие диалоги
│   │       ├── CreateInteractionDialog.kt  # Диалог создания взаимодействия
│   │       ├── CreateInteractionSidePanel.kt # Боковая панель взаимодействий
│   │       ├── SelectContragentDialog.kt   # Диалог выбора контрагента
│   │       ├── DatePickerDialog.kt         # Диалог выбора даты
│   │       └── DateTimePickerDialog.kt     # Диалог выбора даты и времени
│   └── trash/              # Неиспользуемые файлы (дубликаты)
│       ├── EnhancedCreateContragentDialog.kt.backup
│       ├── EnhancedCreateContragentDialog.kt.current
│       └── SnapContragentViewModel.kt
└── MainActivity.kt         # Главная активность
```

## 🔧 Ключевые изменения

### ✅ Что было сделано:
1. **Полная реорганизация структуры** - файлы разложены по функциональным модулям
2. **Убраны дубликаты** - `.backup` и `.current` файлы перемещены в `trash/`
3. **Логичная группировка** - каждый модуль имеет свою папку с подпапками:
   - `screens/` - основные экраны
   - `components/` - компоненты для экранов
   - `dialogs/` - диалоги и модальные окна

### 📱 Основные модули:
- **`tasks/`** - Управление задачами
- **`contragents/`** - Юридические лица (автоматическое открытие карточки готово, ждем исправления метода в 1С)
- **`individuals/`** - Физические лица
- **`calendar/`** - Календарь и канбан
- **`deals/`** - Сделки
- **`orders/`** - Заказы и корзина
- **`products/`** - Товары
- **`navigation/`** - Навигация по приложению
- **`common/`** - Общие компоненты используемые в нескольких модулях

### 🎨 Тема:
В папке `theme/` остались только файлы связанные с оформлением:
- `Color.kt` - палитра цветов
- `Theme.kt` - основная тема Compose
- `Type.kt` - типографика

## 🚀 Текущий статус функционала

### ✅ Работает:
1. **Поле "Описание клиента" необязательное** - при создании юрлица/физлица
2. **Метод `getContragentById` подключен** - вызывается после создания контрагента
3. **Сервер 1С отвечает** - `result=true`, но возвращает пустые `name` и `inn`

### 🔧 Требует доработки в 1С:
1. **Метод `getContragentById`** - нужно чтобы возвращал `name` и `inn` (сейчас возвращает пустые строки)
2. **После исправления** - автоматическое открытие карточки заработает мгновенно

### 📦 Бэкап:
Создана ветка `backup-before-refactor-2026-02-25` с исходным состоянием проекта.

## 🛠️ Компиляция
Проект компилируется успешно после рефакторинга. Все импорты обновлены в соответствии с новой структурой.

## 📝 Примечания
1. Файлы в `trash/` можно безопасно удалить после проверки что они не используются
2. Структура масштабируема - легко добавлять новые модули
3. Код стал более читаемым и поддерживаемым

---

**Последний коммит:** `7c77b5a` - "Метод getContragentById работает, но возвращает пустые name и inn"

**Бэкап ветка:** `backup-before-refactor-2026-02-25`