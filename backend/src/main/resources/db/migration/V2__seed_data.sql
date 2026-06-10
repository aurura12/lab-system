-- =============================================
-- V2__seed_data.sql
-- =============================================

-- Default admin user (password: admin123)
-- BCrypt hash of "admin123"
INSERT INTO users (username, password_hash, real_name, email, role, phone, is_active)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Admin', 'admin@lab.com', 'admin', '13800000000', true);

-- Sample lab
INSERT INTO labs (name, description, address) VALUES
('Main Research Building', 'Primary laboratory building', 'No.1 Science Road');

-- Sample floors
INSERT INTO floors (lab_id, floor_number, name)
SELECT l.id, 1, '1F' FROM labs l WHERE l.name = 'Main Research Building';

INSERT INTO floors (lab_id, floor_number, name)
SELECT l.id, 2, '2F' FROM labs l WHERE l.name = 'Main Research Building';

-- Sample rooms
INSERT INTO rooms (floor_id, room_number, name, area_sqm)
SELECT f.id, '101', 'Chemistry Lab', 80.00 FROM floors f WHERE f.floor_number = 1;

INSERT INTO rooms (floor_id, room_number, name, area_sqm)
SELECT f.id, '102', 'Physics Lab', 65.00 FROM floors f WHERE f.floor_number = 1;

INSERT INTO rooms (floor_id, room_number, name, area_sqm)
SELECT f.id, '201', 'Biology Lab', 90.00 FROM floors f WHERE f.floor_number = 2;

-- Sample equipment
INSERT INTO equipment (name, model, serial_number, room_id, category, status, manufacturer)
SELECT 'UV-Vis Spectrophometer', 'UV-2600', 'SN-001', r.id, 'spectrometer', 'available', 'Shimadzu'
FROM rooms r WHERE r.room_number = '101';

INSERT INTO equipment (name, model, serial_number, room_id, category, status, manufacturer)
SELECT 'Centrifuge', '5425R', 'SN-002', r.id, 'centrifuge', 'available', 'Eppendorf'
FROM rooms r WHERE r.room_number = '101';

INSERT INTO equipment (name, model, serial_number, room_id, category, status, manufacturer)
SELECT 'Scanning Electron Microscope', 'SU5000', 'SN-003', r.id, 'microscope', 'in_use', 'Hitachi'
FROM rooms r WHERE r.room_number = '201';

-- Sample researcher
INSERT INTO users (username, password_hash, real_name, email, role, phone, is_active)
VALUES ('researcher1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Zhang Wei', 'zhangwei@lab.com', 'researcher', '13800000001', true);

-- Sample project
INSERT INTO projects (name, code, description, status, start_date, owner_id)
SELECT 'Nanomaterial Characterization', 'PROJ-2026-001', 'Study of nanomaterial properties', 'active', '2026-01-01', u.id
FROM users u WHERE u.username = 'admin';
