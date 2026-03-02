#!/bin/bash

# Скрипт для автоматического инкремента версии при коммите
# Использование: ./increment_version.sh "Сообщение коммита"

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🚀 Начинаем процесс коммита с инкрементом версии...${NC}"

# Проверяем наличие изменений
if [ -z "$(git status --porcelain)" ]; then
    echo -e "${RED}❌ Нет изменений для коммита${NC}"
    exit 1
fi

# Получаем текущую версию из файла (если есть)
VERSION_FILE="app/version.properties"
if [ -f "$VERSION_FILE" ]; then
    CURRENT_VERSION=$(grep "version=" "$VERSION_FILE" | cut -d'=' -f2)
else
    CURRENT_VERSION="v1.02"
fi

# Извлекаем номер версии (v1.02 → 102)
VERSION_NUMBER=$(echo "$CURRENT_VERSION" | sed 's/v//' | sed 's/\.//')
NEW_VERSION_NUMBER=$((VERSION_NUMBER + 1))

# Форматируем новую версию (103 → v1.03)
MAJOR=$(($NEW_VERSION_NUMBER / 100))
MINOR=$(($NEW_VERSION_NUMBER % 100))
NEW_VERSION="v${MAJOR}.$(printf "%02d" $MINOR)"

echo -e "${GREEN}📈 Текущая версия: $CURRENT_VERSION${NC}"
echo -e "${GREEN}📈 Новая версия: $NEW_VERSION${NC}"

# Обновляем файл версии
echo "version=$NEW_VERSION" > "$VERSION_FILE"
echo "date=$(date '+%Y-%m-%d %H:%M:%S')" >> "$VERSION_FILE"
echo "commit_message=$1" >> "$VERSION_FILE"

# Добавляем все изменения
git add .

# Создаем коммит
COMMIT_MESSAGE="$1 (версия $NEW_VERSION)"
git commit -m "$COMMIT_MESSAGE"

echo -e "${GREEN}✅ Коммит создан: $COMMIT_MESSAGE${NC}"
echo -e "${YELLOW}📝 Для отправки на GitHub выполните: git push${NC}"

# Показываем историю последних коммитов
echo -e "\n${YELLOW}📋 История последних коммитов:${NC}"
git log --oneline -5