-- FUCarRentingSystem Database Initialization
-- Creates all three schemas and grants privileges to the fucar user

CREATE DATABASE IF NOT EXISTS customer_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS car_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS renting_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON customer_db.* TO 'fucar'@'%';
GRANT ALL PRIVILEGES ON car_db.* TO 'fucar'@'%';
GRANT ALL PRIVILEGES ON renting_db.* TO 'fucar'@'%';
FLUSH PRIVILEGES;
