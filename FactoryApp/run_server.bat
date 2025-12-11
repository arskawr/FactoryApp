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

if not exist lib\h2-2.1.214.jar (
    echo Внимание: H2 драйвер не найден, но MySQL доступен.
)

echo Запуск сервера на порту 5555...
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED -cp "bin;lib\mysql-connector-java-8.0.33.jar;lib\h2-2.1.214.jar" server.MainServer
pause