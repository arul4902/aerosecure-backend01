-- ============================================
-- AeroSecure - Database Schema
-- Aircraft Registration & Fleet Management
-- ============================================

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- Aircraft table for fleet management
CREATE TABLE IF NOT EXISTS aircraft (
    aircraft_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);
