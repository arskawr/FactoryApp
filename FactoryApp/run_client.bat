@echo off
echo Запуск клиента Factory Client...
echo.

echo Запуск клиента...
java -cp "bin;lib\mysql-connector-java-8.0.33.jar;lib\h2-2.1.214.jar" client.MainClient

echo.
echo Клиент закрыт.
pause