@echo off
echo ========================================
echo   FAZA 4 - Client-to-Admin Chat Setup
echo ========================================
echo.

echo [1/5] Starting Docker services...
docker-compose up -d postgres rabbitmq
timeout /t 10 /nobreak > nul

echo.
echo [2/5] Building WebSocket Microservice...
cd websocket-microservice
call mvn clean package -DskipTests
cd ..

echo.
echo [3/5] Building Customer Support Microservice...
cd customer-support-microservice
call mvn clean package -DskipTests
cd ..

echo.
echo [4/5] Starting microservices...
docker-compose up -d websocket-microservice customer-support-microservice

echo.
echo [5/5] Starting Frontend...
cd frontend
start cmd /k "npm run dev"
cd ..

echo.
echo ========================================
echo   Services Started!
echo ========================================
echo.
echo WebSocket Microservice: http://localhost:8085
echo Customer Support: http://localhost:8086
echo Frontend: http://localhost:3000
echo RabbitMQ Management: http://localhost:15672
echo.
echo Test WebSocket: Open test-chat-websocket.html in browser
echo.
echo Press any key to view logs...
pause > nul

docker-compose logs -f websocket-microservice customer-support-microservice
