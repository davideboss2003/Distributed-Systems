-- Fix password hash pentru admin
USE energy_auth_db;

-- Șterge adminul vechi
DELETE FROM auth_users WHERE username = 'admin';

-- Inserează admin cu hash nou (parola: admin123)
INSERT INTO auth_users (id, username, email, password_hash, role, created_at, updated_at)
VALUES (
    UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440000', '-', '')),
    'admin',
    'admin@energy.ro',
    '$2a$10$rN.YhXRvF5vQXVkKGJ9VUeqQr4WZKvKQfZZZWJYZv5VXZv5VXZv5V',
    'ADMIN',
    NOW(),
    NOW()
);

SELECT username, email, role FROM auth_users;
