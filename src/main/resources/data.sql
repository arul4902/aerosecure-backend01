-- ============================================
-- AeroSecure - Data Seed File
-- Aircraft Registration & Fleet Management
-- ============================================
-- This file auto-seeds the database on first run.
-- All teammates can use this - just change MySQL
-- credentials in application.properties and run.
-- ============================================

-- ============================================
-- USERS (BCrypt hashed passwords)
-- admin    -> password: admin123   (ROLE_ADMIN - Full CRUD access)
-- engineer -> password: engineer123 (ROLE_ENGINEER - Read-only access)
-- ============================================
INSERT IGNORE INTO users (username, password, role) VALUES
('admin', '$2a$10$2P4rn0/5QVAW4yz/7rD2sOjJ6yruspTqY3Rm1yel3LwZ4AN0bmgXS', 'ROLE_ADMIN');

INSERT IGNORE INTO users (username, password, role) VALUES
('engineer', '$2a$10$fzp5KySqmp4y3/Ymw39w5.0/FbEcMxloOBy/hW19HbWLcvZoqioTi', 'ROLE_ENGINEER');

-- ============================================
-- AIRCRAFT FLEET DATA
-- ============================================
INSERT IGNORE INTO aircraft (aircraft_id, model, manufacturer, status, created_at, updated_at) VALUES
(1, 'Boeing 737-800', 'Boeing', 'ACTIVE', NOW(), NOW());

INSERT IGNORE INTO aircraft (aircraft_id, model, manufacturer, status, created_at, updated_at) VALUES
(2, 'Airbus A320neo', 'Airbus', 'ACTIVE', NOW(), NOW());

INSERT IGNORE INTO aircraft (aircraft_id, model, manufacturer, status, created_at, updated_at) VALUES
(3, 'Boeing 777-300ER', 'Boeing', 'UNDER_MAINTENANCE', NOW(), NOW());

INSERT IGNORE INTO aircraft (aircraft_id, model, manufacturer, status, created_at, updated_at) VALUES
(4, 'Embraer E195-E2', 'Embraer', 'ACTIVE', NOW(), NOW());

INSERT IGNORE INTO aircraft (aircraft_id, model, manufacturer, status, created_at, updated_at) VALUES
(5, 'Bombardier CRJ-900', 'Bombardier', 'RETIRED', NOW(), NOW());

INSERT IGNORE INTO aircraft (aircraft_id, model, manufacturer, status, created_at, updated_at) VALUES
(6, 'Airbus A350-1000', 'Airbus', 'ACTIVE', NOW(), NOW());

INSERT IGNORE INTO aircraft (aircraft_id, model, manufacturer, status, created_at, updated_at) VALUES
(7, 'Boeing 787 Dreamliner', 'Boeing', 'UNDER_MAINTENANCE', NOW(), NOW());

INSERT IGNORE INTO aircraft (aircraft_id, model, manufacturer, status, created_at, updated_at) VALUES
(8, 'ATR 72-600', 'ATR', 'ACTIVE', NOW(), NOW());
