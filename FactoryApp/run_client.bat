@echo off
echo Запуск клиента Factory Client...
echo.

echo Запуск клиента...
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED -cp "bin;lib\mysql-connector-java-8.jar;lib\h2-2.1.214.jar" client.MainClient
pause