@echo off
setlocal EnableDelayedExpansion

echo ==================================================================
echo           STARTING FULL SYSTEM (PostgreSQL, Backend, Frontend)
echo ==================================================================

REM Step 1: Start PostgreSQL with Docker Compose
echo.
echo STEP 1: Starting PostgreSQL database with Docker Compose...
cd backend
docker-compose up -d
cd ..

REM Wait for PostgreSQL to be ready (port 5432)
echo Waiting for PostgreSQL to start on port 5432...
set /a attempts=0
set /a max_attempts=30

:checkPostgres
netstat -an | find "5432" | find "LISTENING" > nul
if %ERRORLEVEL% equ 0 (
    echo PostgreSQL is running on port 5432.
    goto startBackend
)
set /a attempts+=1
if %attempts% gtr %max_attempts% (
    echo PostgreSQL did not start within the expected time. Exiting.
    exit /b 1
)
timeout /t 1 > nul
echo .
goto checkPostgres

:startBackend
REM Step 2: Start the backend
echo.
echo STEP 2: Starting Backend (Spring Boot)...
start "Backend" cmd /c "cd backend && .\mvnw spring-boot:run"

REM Wait for the backend to be ready (typically runs on port 8080)
echo Waiting for Backend to start on port 8080...
set /a attempts=0
set /a max_attempts=60

:checkBackend
netstat -an | find "8080" | find "LISTENING" > nul
if %ERRORLEVEL% equ 0 (
    echo Backend is running on port 8080.
    goto startFrontend
)
set /a attempts+=1
if %attempts% gtr %max_attempts% (
    echo Backend did not start within the expected time. Exiting.
    exit /b 1
)
timeout /t 1 > nul
echo .
goto checkBackend

:startFrontend
REM Step 3: Start the frontend
echo.
echo STEP 3: Starting Frontend (Angular)...
start "Frontend" cmd /c "cd frontend && npm start"

REM Wait for the frontend to be ready (typically runs on port 4200)
echo Waiting for Frontend to start on port 4200...
set /a attempts=0
set /a max_attempts=60

:checkFrontend
netstat -an | find "4200" | find "LISTENING" > nul
if %ERRORLEVEL% equ 0 (
    echo Frontend is running on port 4200.
    goto allServicesRunning
)
set /a attempts+=1
if %attempts% gtr %max_attempts% (
    echo Frontend did not start within the expected time. Exiting.
    exit /b 1
)
timeout /t 1 > nul
echo .
goto checkFrontend

:allServicesRunning
echo.
echo ==================================================================
echo           SYSTEM IS UP AND RUNNING
echo ==================================================================
echo Frontend URL: http://localhost:4200
echo Backend API URL: http://localhost:8080
echo PostgreSQL: localhost:5432
echo.
echo To stop all services, close the terminal windows and run:
echo cd backend ^&^& docker-compose down

echo.
echo Press any key to shut down all services...
pause > nul

echo Shutting down services...
taskkill /FI "WINDOWTITLE eq Backend*" /F
taskkill /FI "WINDOWTITLE eq Frontend*" /F
cd backend
docker-compose down
cd ..
echo All services stopped. 