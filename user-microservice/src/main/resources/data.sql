-- Populare tabela users cu personaje Looney Tunes
-- Password pentru toti: "password123" (hash BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)

INSERT INTO users (id, username, password, role, full_name, email, created_at, updated_at) VALUES
  ('11111111-1111-1111-1111-111111111111', 'bugs.bunny', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'Bugs Bunny', 'bugs.bunny@looneytunes.com', NOW(), NOW()),
  ('22222222-2222-2222-2222-222222222222', 'daffy.duck', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT', 'Daffy Duck', 'daffy.duck@looneytunes.com', NOW(), NOW()),
  ('33333333-3333-3333-3333-333333333333', 'porky.pig', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT', 'Porky Pig', 'porky.pig@looneytunes.com', NOW(), NOW()),
  ('44444444-4444-4444-4444-444444444444', 'tweety.bird', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT', 'Tweety Bird', 'tweety.bird@looneytunes.com', NOW(), NOW()),
  ('55555555-5555-5555-5555-555555555555', 'sylvester.cat', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT', 'Sylvester Cat', 'sylvester.cat@looneytunes.com', NOW(), NOW()),
  ('66666666-6666-6666-6666-666666666666', 'road.runner', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT', 'Road Runner', 'road.runner@looneytunes.com', NOW(), NOW()),
  ('77777777-7777-7777-7777-777777777777', 'yosemite.sam', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN','Yosemite Sam', 'yosemite.sam@looneytunes.com', NOW(), NOW()),
  ('88888888-8888-8888-8888-888888888888', 'wile.coyote', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT','Wile E. Coyote', 'wile.coyote@looneytunes.com' , NOW(), NOW()),
  ('99999999-9999-9999-9999-999999999999', 'marvin.martian', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT', 'Marvin the Martian', 'marvin.martian@looneytunes.com', NOW(), NOW()),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'tasmanian.devil', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT', 'Tasmanian Devil', 'taz@looneytunes.com', NOW(), NOW());
