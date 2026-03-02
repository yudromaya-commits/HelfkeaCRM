@echo off
echo Подсчет строк кода в проекте...
echo.

:: Переход в корень проекта (если нужно)
:: cd /d "C:\path\to\your\android\project"

setlocal enabledelayedexpansion
set total=0

:: Ищем файлы и подсчитываем строки
for /r %%f in (*.kt, *.java, *.xml, *.gradle, *.pro, *.txt) do (
    for /f %%c in ('type "%%f" ^| find /c /v ""') do (
        set /a total+=%%c
        echo %%f: %%c строк
    )
)

echo.
echo =================================
echo Всего строк: %total%
echo =================================
pause