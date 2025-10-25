@echo off
chcp 65001 >nul
echo ============================================
echo   CREARE ADMIN în MySQL Docker
echo ============================================
echo.

echo [1/2] Verificare container MySQL...
docker ps | findstr energy-auth-mysql >nul 2>&1
if errorlevel 1 (
    echo ❌ Container-ul energy-auth-mysql nu rulează!
    echo    Pornește-l cu: docker-compose up -d
    pause
    exit /b 1
)
echo ✅ Container MySQL găsit!
echo.

echo [2/2] Creare admin în baza de date...
docker exec -i energy-auth-mysql mysql -u root -proot energy_auth_db -e "INSERT INTO auth_users (id, username, email, password_hash, role, created_at, updated_at) VALUES (UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440000', '-', '')), 'admin', 'admin@energy.ro', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NOW(), NOW()) ON DUPLICATE KEY UPDATE username=username;"

if errorlevel 1 (
    echo ❌ Eroare la creare admin!
    pause
    exit /b 1
)

echo ✅ Admin creat cu succes!
echo.

echo Verificare...
docker exec -i energy-auth-mysql mysql -u root -proot energy_auth_db -e "SELECT username, email, role FROM auth_users;"

echo.
echo ============================================
echo   CREDENȚIALE ADMIN:
echo   Email: admin@energy.ro
echo   Password: admin123
echo ============================================
echo.
echo Acum poți testa în Postman!
echo.
pause
