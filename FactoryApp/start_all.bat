@echo off
echo Запуск системы автоматизации кондитерской фабрики
echo.

echo 1. Запуск сервера...
start cmd /k "cd /d %~dp0 && run_server.bat"

echo 2. Ожидание запуска сервера...
timeout /t 5 /nobreak

echo 3. Запуск клиента...
start cmd /k "cd /d %~dp0 && run_client.bat"

echo.
echo Система запущена!
pause