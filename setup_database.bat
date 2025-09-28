@echo off
echo Setting up database...
echo Please make sure MySQL is running and you have the correct credentials
echo.

REM Check if mysql command is available
mysql --version >nul 2>&1
if errorlevel 1 (
    echo MySQL client not found. Please install MySQL client or add it to PATH.
    echo You can also manually create the database using the SQL file: database_setup.sql
    pause
    exit /b 1
)

REM Try to connect and setup database
echo Connecting to MySQL...
mysql -u root -pSIN.login@235 -e "DROP DATABASE IF EXISTS ShopDatabase; CREATE DATABASE ShopDatabase;" 2>nul
if errorlevel 1 (
    echo Failed to create database. Please check your MySQL credentials and connection.
    echo You can also manually create the database using the SQL file: database_setup.sql
    pause
    exit /b 1
)

echo Database created successfully. Now importing data...
mysql -u root -pSIN.login@235 ShopDatabase < database_setup.sql

if errorlevel 1 (
    echo Failed to import data. Please check the SQL file and try again.
    pause
    exit /b 1
)

echo Database setup completed successfully!
pause
