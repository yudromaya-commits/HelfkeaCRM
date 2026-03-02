#!/bin/bash
find app/src/main/java/com/example/repressales/ -name "*.kt" -type f | while read file; do
    filename=$(basename "$file")
    classname="${filename%.kt}"
    
    # Проверяем, используется ли класс/функция/объект
    if ! grep -r "\b$classname\b" app/src/main/java/com/example/repressales/ --include="*.kt" | grep -v "$file" | grep -q "."; then
        # Проверяем, может быть это точка входа (MainActivity) или что-то важное
        if [[ "$filename" != "MainActivity.kt" ]] && [[ ! "$file" =~ "/trash/" ]]; then
            echo "Возможно не используется: $file"
        fi
    fi
done
