@echo off
echo Компиляция проекта FactoryApp...
echo.

echo 1. Установка Java версии...
java -version

echo 2. Очистка папки bin...
if exist bin rmdir /s /q bin
mkdir bin

echo 3. Создание папок в bin...
mkdir bin\client 2>nul
mkdir bin\client\ui 2>nul
mkdir bin\server 2>nul
mkdir bin\server\database 2>nul
mkdir bin\shared 2>nul
mkdir bin\shared\models 2>nul

echo 4. Скачивание драйверов БД...
if not exist lib mkdir lib

if not exist lib\mysql-connector-java-8.0.33.jar (
    echo Скачивание MySQL драйвера...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar' -OutFile 'lib\mysql-connector-java-8.0.33.jar'"
)

if not exist lib\h2-2.1.214.jar (
    echo Скачивание H2 драйвера...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/h2database/h2/2.1.214/h2-2.1.214.jar' -OutFile 'lib\h2-2.1.214.jar'"
)

echo 5. Компиляция shared.models...
javac -d bin -cp "src" src\shared\models\*.java

echo 6. Компиляция shared...
javac -d bin -cp "bin;src" src\shared\*.java

echo 7. Компиляция client.ui...
javac -d bin -cp "bin;src" src\client\ui\*.java

echo 8. Компиляция client...
javac -d bin -cp "bin;src;lib\mysql-connector-java-8.0.33.jar;lib\h2-2.1.214.jar" src\client\*.java

echo 9. Компиляция server.database...
javac -d bin -cp "bin;src;lib\mysql-connector-java-8.0.33.jar;lib\h2-2.1.214.jar" src\server\database\*.java

echo 10. Компиляция server...
javac -d bin -cp "bin;src;lib\mysql-connector-java-8.0.33.jar;lib\h2-2.1.214.jar" src\server\*.java

echo.
echo =============================================
echo КОМПИЛЯЦИЯ ЗАВЕРШЕНА!
echo.
echo Для запуска сервера: run_server.bat
echo Для запуска клиента: run_client.bat
echo =============================================
pause