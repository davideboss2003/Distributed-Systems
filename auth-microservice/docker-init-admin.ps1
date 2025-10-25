# Script PowerShell pentru creare admin în MySQL Docker

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  CREARE ADMIN în MySQL Docker" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Verificare container MySQL
Write-Host "[1/2] Verificare container MySQL..." -ForegroundColor Yellow
$mysqlContainer = docker ps --filter "name=energy-auth-mysql" --format "{{.Names}}"
if (-not $mysqlContainer) {
    Write-Host "❌ Container-ul energy-auth-mysql nu rulează!" -ForegroundColor Red
    Write-Host "   Pornește-l cu: docker-compose up -d" -ForegroundColor Red
    Read-Host "Apasă Enter pentru a ieși"
    exit 1
}
Write-Host "✅ Container MySQL găsit!" -ForegroundColor Green
Write-Host ""

# Creare admin
Write-Host "[2/2] Creare admin în baza de date..." -ForegroundColor Yellow
$sql = @"
INSERT INTO auth_users (id, username, email, password_hash, role, created_at, updated_at)
VALUES (
    UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440000', '-', '')),
    'admin',
    'admin@energy.ro',
    '`$2a`$10`$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE username=username;
"@

docker exec -i energy-auth-mysql mysql -u root -proot energy_auth_db -e $sql

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Eroare la creare admin!" -ForegroundColor Red
    Read-Host "Apasă Enter pentru a ieși"
    exit 1
}

Write-Host "✅ Admin creat cu succes!" -ForegroundColor Green
Write-Host ""

# Verificare
Write-Host "Verificare..." -ForegroundColor Yellow
docker exec -i energy-auth-mysql mysql -u root -proot energy_auth_db -e "SELECT username, email, role FROM auth_users;"

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  CREDENȚIALE ADMIN:" -ForegroundColor Cyan
Write-Host "  Email: admin@energy.ro" -ForegroundColor White
Write-Host "  Password: admin123" -ForegroundColor White
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Acum poți testa în Postman!" -ForegroundColor Green
Write-Host ""
Read-Host "Apasă Enter pentru a ieși"
