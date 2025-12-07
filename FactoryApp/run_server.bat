@echo off
echo Запуск сервера Factory Server...
echo.

echo Проверка драйверов БД...
if not exist lib\mysql-connector-java-8.0.33.jar (
    echo Ошибка: MySQL драйвер не найден!
    echo Запустите compile.bat сначала.
    pause
    exit /b 1
)

echo Запуск сервера на порту 5555...
java -cp "bin;lib\mysql-connector-java-8.0.33.jar;lib\h2-2.1.214.jar" server.MainServer

echo.
echo Сервер остановлен.
pause