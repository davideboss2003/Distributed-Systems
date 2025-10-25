-- Script pentru repararea tabelei users pentru a accepta UUID-uri

USE energy_users_db;

-- Modifică coloana id pentru a accepta UUID-uri (36 caractere)
ALTER TABLE users MODIFY COLUMN id VARCHAR(36) NOT NULL;

-- Verifică structura tabelei
DESCRIBE users;
