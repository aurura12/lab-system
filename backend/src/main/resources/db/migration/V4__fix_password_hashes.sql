-- Fix incorrect BCrypt hashes from V2 seed data
-- The original hashes did not match the documented passwords
-- Correct BCrypt hash of "admin123"
UPDATE users SET password_hash = '$2a$10$ngQnsz7JxFk.AujyWoO/BuALYFFXuU1yDXKqgdd2cGL./lZO0z4si';
