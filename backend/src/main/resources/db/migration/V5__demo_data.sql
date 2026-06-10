-- =============================================
-- V5__demo_data.sql - 丰富的演示数据
-- 所有密码均为 admin123
-- =============================================

-- 1. 添加更多用户
-- Lab Manager
INSERT INTO users (username, password_hash, real_name, email, role, phone, is_active)
VALUES ('labmanager1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王芳', 'wangfang@lab.com', 'lab_manager', '13800000002', true);

INSERT INTO users (username, password_hash, real_name, email, role, phone, is_active)
VALUES ('labmanager2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '陈静', 'chenjing@lab.com', 'lab_manager', '13800000003', true);

-- Researchers
INSERT INTO users (username, password_hash, real_name, email, role, phone, is_active)
VALUES ('researcher2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李明', 'liming@lab.com', 'researcher', '13800000004', true);

INSERT INTO users (username, password_hash, real_name, email, role, phone, is_active)
VALUES ('researcher3', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张伟', 'zhangwei@lab.com', 'researcher', '13800000005', true);

INSERT INTO users (username, password_hash, real_name, email, role, phone, is_active)
VALUES ('researcher4', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '刘洋', 'liuyang@lab.com', 'researcher', '13800000006', true);

INSERT INTO users (username, password_hash, real_name, email, role, phone, is_active)
VALUES ('researcher5', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '赵敏', 'zhaomin@lab.com', 'researcher', '13800000007', true);

-- 2. 添加更多设备到现有房间
-- 101 房间 - 化学实验室
INSERT INTO equipment (name, model, serial_number, room_id, category, status, manufacturer, purchase_date, notes)
SELECT '高效液相色谱仪', 'LC-2030C', 'SN-004', r.id, 'analyzer', 'available', 'Shimadzu', '2025-06-15', '用于有机化合物分析'
FROM rooms r WHERE r.room_number = '101';

INSERT INTO equipment (name, model, serial_number, room_id, category, status, manufacturer, purchase_date, notes)
SELECT '紫外可见分光光度计', 'UV-1900i', 'SN-005', r.id, 'spectrometer', 'available', 'Shimadzu', '2025-03-20', '常规光谱分析'
FROM rooms r WHERE r.room_number = '101';

INSERT INTO equipment (name, model, serial_number, room_id, category, status, manufacturer, purchase_date, notes)
SELECT '分析天平', 'ME204E', 'SN-006', r.id, 'other', 'available', 'Mettler Toledo', '2024-11-10', '精度0.0001g'
FROM rooms r WHERE r.room_number = '101';

-- 102 房间 - 物理实验室
INSERT INTO equipment (name, model, serial_number, room_id, category, status, manufacturer, purchase_date, notes)
SELECT 'X射线衍射仪', 'D8 Advance', 'SN-007', r.id, 'analyzer', 'in_use', 'Bruker', '2025-01-15', '晶体结构分析'
FROM rooms r WHERE r.room_number = '102';

INSERT INTO equipment (name, model, serial_number, room_id, category, status, manufacturer, purchase_date, notes)
SELECT '傅里叶红外光谱仪', 'Nicolet iS50', 'SN-008', r.id, 'spectrometer', 'available', 'Thermo Fisher', '2025-04-22', '材料结构表征'
FROM rooms r WHERE r.room_number = '102';

-- 201 房间 - 生物实验室
INSERT INTO equipment (name, model, serial_number, room_id, category, status, manufacturer, purchase_date, notes)
SELECT '荧光显微镜', 'Eclipse Ni-U', 'SN-009', r.id, 'microscope', 'available', 'Nikon', '2025-02-28', '细胞荧光成像'
FROM rooms r WHERE r.room_number = '201';

INSERT INTO equipment (name, model, serial_number, room_id, category, status, manufacturer, purchase_date, notes)
SELECT 'PCR仪', 'ProFlex', 'SN-010', r.id, 'other', 'maintenance', 'Applied Biosystems', '2024-09-15', '等待校准维护'
FROM rooms r WHERE r.room_number = '201';

INSERT INTO equipment (name, model, serial_number, room_id, category, status, manufacturer, purchase_date, notes)
SELECT '超速离心机', 'Optima XPN-100', 'SN-011', r.id, 'centrifuge', 'available', 'Beckman Coulter', '2025-05-10', '最高转速100,000 rpm'
FROM rooms r WHERE r.room_number = '201';

-- 3. 添加更多项目
INSERT INTO projects (name, code, description, status, start_date, end_date, owner_id)
SELECT '蛋白质组学研究', 'PROJ-2026-002', '利用质谱技术研究细胞蛋白质表达谱', 'active', '2026-02-01', '2026-12-31', u.id
FROM users u WHERE u.username = 'labmanager1';

INSERT INTO projects (name, code, description, status, start_date, end_date, owner_id)
SELECT '新型催化剂开发', 'PROJ-2026-003', '开发高效纳米催化剂用于绿色化学合成', 'active', '2026-03-01', NULL, u.id
FROM users u WHERE u.username = 'admin';

INSERT INTO projects (name, code, description, status, start_date, end_date, owner_id)
SELECT '半导体材料表征', 'PROJ-2025-008', '第三代半导体材料电学性能研究', 'completed', '2025-06-01', '2026-01-31', u.id
FROM users u WHERE u.username = 'labmanager2';

INSERT INTO projects (name, code, description, status, start_date, end_date, owner_id)
SELECT '生物膜结构分析', 'PROJ-2026-004', '利用冷冻电镜解析生物膜蛋白三维结构', 'paused', '2026-04-01', NULL, u.id
FROM users u WHERE u.username = 'labmanager1';

-- 4. 添加项目成员
INSERT INTO project_members (project_id, user_id, role_in_project)
SELECT p.id, u.id, 'lead' FROM projects p, users u WHERE p.code = 'PROJ-2026-002' AND u.username = 'labmanager1';

INSERT INTO project_members (project_id, user_id, role_in_project)
SELECT p.id, u.id, 'member' FROM projects p, users u WHERE p.code = 'PROJ-2026-002' AND u.username = 'researcher2';

INSERT INTO project_members (project_id, user_id, role_in_project)
SELECT p.id, u.id, 'member' FROM projects p, users u WHERE p.code = 'PROJ-2026-002' AND u.username = 'researcher3';

INSERT INTO project_members (project_id, user_id, role_in_project)
SELECT p.id, u.id, 'lead' FROM projects p, users u WHERE p.code = 'PROJ-2026-003' AND u.username = 'admin';

INSERT INTO project_members (project_id, user_id, role_in_project)
SELECT p.id, u.id, 'member' FROM projects p, users u WHERE p.code = 'PROJ-2026-003' AND u.username = 'researcher4';

INSERT INTO project_members (project_id, user_id, role_in_project)
SELECT p.id, u.id, 'member' FROM projects p, users u WHERE p.code = 'PROJ-2026-003' AND u.username = 'researcher5';

INSERT INTO project_members (project_id, user_id, role_in_project)
SELECT p.id, u.id, 'lead' FROM projects p, users u WHERE p.code = 'PROJ-2025-008' AND u.username = 'labmanager2';

INSERT INTO project_members (project_id, user_id, role_in_project)
SELECT p.id, u.id, 'member' FROM projects p, users u WHERE p.code = 'PROJ-2025-008' AND u.username = 'researcher1';

INSERT INTO project_members (project_id, user_id, role_in_project)
SELECT p.id, u.id, 'lead' FROM projects p, users u WHERE p.code = 'PROJ-2026-004' AND u.username = 'labmanager1';

INSERT INTO project_members (project_id, user_id, role_in_project)
SELECT p.id, u.id, 'member' FROM projects p, users u WHERE p.code = 'PROJ-2026-004' AND u.username = 'researcher2';

-- 5. 添加使用记录（最近30天内的历史数据）
-- 已完成的使用记录
INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-20 09:00:00', '2026-05-20 11:30:00', '纳米材料紫外吸收光谱测定', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-001' AND u.username = 'researcher1' AND p.code = 'PROJ-2026-001';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-20 14:00:00', '2026-05-20 16:45:00', '蛋白质样品离心分离', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-002' AND u.username = 'researcher2' AND p.code = 'PROJ-2026-002';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-21 08:30:00', '2026-05-21 12:00:00', '生物膜样品形貌观察', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-003' AND u.username = 'researcher2' AND p.code = 'PROJ-2026-004';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-22 10:00:00', '2026-05-22 15:30:00', '催化剂XRD结构表征', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-007' AND u.username = 'researcher4' AND p.code = 'PROJ-2026-003';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-23 09:15:00', '2026-05-23 11:45:00', '纳米材料元素组成分析', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-004' AND u.username = 'researcher1' AND p.code = 'PROJ-2026-001';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-24 13:00:00', '2026-05-24 17:00:00', '催化剂红外光谱分析', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-008' AND u.username = 'researcher5' AND p.code = 'PROJ-2026-003';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-25 08:00:00', '2026-05-25 12:30:00', '半导体材料晶体结构验证', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-007' AND u.username = 'researcher1' AND p.code = 'PROJ-2025-008';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-26 14:30:00', '2026-05-26 16:00:00', '蛋白质样品称量', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-006' AND u.username = 'researcher2' AND p.code = 'PROJ-2026-002';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-27 09:00:00', '2026-05-27 11:00:00', '细胞荧光标记观察', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-009' AND u.username = 'researcher2' AND p.code = 'PROJ-2026-004';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-28 10:00:00', '2026-05-28 14:00:00', '纳米催化剂形貌分析', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-003' AND u.username = 'researcher4' AND p.code = 'PROJ-2026-003';

-- 更多近期记录
INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-29 08:30:00', '2026-05-29 10:30:00', '催化剂样品紫外光谱测定', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-005' AND u.username = 'researcher5' AND p.code = 'PROJ-2026-003';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-29 14:00:00', '2026-05-29 17:30:00', '蛋白质超速离心纯化', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-011' AND u.username = 'researcher3' AND p.code = 'PROJ-2026-002';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-30 09:00:00', '2026-05-30 12:00:00', '纳米材料光谱分析', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-001' AND u.username = 'researcher3' AND p.code = 'PROJ-2026-001';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-05-31 10:00:00', '2026-05-31 15:00:00', '半导体材料XRD测试', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-007' AND u.username = 'researcher1' AND p.code = 'PROJ-2025-008';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-06-01 08:00:00', '2026-06-01 11:30:00', '催化剂HPLC纯度分析', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-004' AND u.username = 'researcher4' AND p.code = 'PROJ-2026-003';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-06-01 14:00:00', '2026-06-01 16:30:00', '蛋白质样品电镜观察', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-003' AND u.username = 'researcher2' AND p.code = 'PROJ-2026-002';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, logout_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-06-02 09:00:00', '2026-06-02 12:00:00', '生物膜蛋白荧光成像', 'completed'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-009' AND u.username = 'researcher2' AND p.code = 'PROJ-2026-004';

-- 当前使用中的记录
INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-06-03 09:00:00', '纳米材料结构表征', 'active'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-001' AND u.username = 'researcher3' AND p.code = 'PROJ-2026-001';

INSERT INTO usage_records (equipment_id, user_id, project_id, login_time, purpose, status)
SELECT e.id, u.id, p.id, '2026-06-03 10:30:00', '催化剂晶体结构分析', 'active'
FROM equipment e, users u, projects p
WHERE e.serial_number = 'SN-007' AND u.username = 'researcher5' AND p.code = 'PROJ-2026-003';

-- 6. 添加数据记录
INSERT INTO data_records (equipment_id, usage_record_id, project_id, user_id, data_type, file_name, file_path, file_size, description, metadata_json)
SELECT e.id, ur.id, p.id, u.id, 'spectrum', 'UV_nanomaterial_001.csv', '/data/UV_nanomaterial_001.csv', 245760, '纳米材料紫外吸收光谱数据', '{"wavelength_range":"200-800nm","resolution":"1nm"}'
FROM equipment e, usage_records ur, projects p, users u
WHERE e.serial_number = 'SN-001' AND ur.purpose = '纳米材料紫外吸收光谱测定' AND p.code = 'PROJ-2026-001' AND u.username = 'researcher1';

INSERT INTO data_records (equipment_id, usage_record_id, project_id, user_id, data_type, file_name, file_path, file_size, description, metadata_json)
SELECT e.id, ur.id, p.id, u.id, 'image', 'SEM_nanocatalyst_001.tiff', '/data/SEM_nanocatalyst_001.tiff', 5242880, '纳米催化剂扫描电镜形貌图', '{"magnification":"50000x","voltage":"15kV"}'
FROM equipment e, usage_records ur, projects p, users u
WHERE e.serial_number = 'SN-003' AND ur.purpose = '纳米催化剂形貌分析' AND p.code = 'PROJ-2026-003' AND u.username = 'researcher4';

INSERT INTO data_records (equipment_id, usage_record_id, project_id, user_id, data_type, file_name, file_path, file_size, description, metadata_json)
SELECT e.id, ur.id, p.id, u.id, 'measurement', 'XRD_catalyst_001.dat', '/data/XRD_catalyst_001.dat', 1048576, '催化剂XRD衍射数据', '{"scan_range":"10-80deg","scan_speed":"2deg/min"}'
FROM equipment e, usage_records ur, projects p, users u
WHERE e.serial_number = 'SN-007' AND ur.purpose = '催化剂XRD结构表征' AND p.code = 'PROJ-2026-003' AND u.username = 'researcher4';

INSERT INTO data_records (equipment_id, usage_record_id, project_id, user_id, data_type, file_name, file_path, file_size, description, metadata_json)
SELECT e.id, ur.id, p.id, u.id, 'spectrum', 'HPLC_protein_001.csv', '/data/HPLC_protein_001.csv', 327680, '蛋白质HPLC纯化分析数据', '{"column":"C18","flow_rate":"1.0ml/min"}'
FROM equipment e, usage_records ur, projects p, users u
WHERE e.serial_number = 'SN-004' AND ur.purpose = '催化剂HPLC纯度分析' AND p.code = 'PROJ-2026-003' AND u.username = 'researcher4';

INSERT INTO data_records (equipment_id, usage_record_id, project_id, user_id, data_type, file_name, file_path, file_size, description, metadata_json)
SELECT e.id, ur.id, p.id, u.id, 'image', 'fluorescence_cell_001.tif', '/data/fluorescence_cell_001.tif', 8388608, '细胞荧光标记成像数据', '{"excitation":"488nm","emission":"520nm"}'
FROM equipment e, usage_records ur, projects p, users u
WHERE e.serial_number = 'SN-009' AND ur.purpose = '细胞荧光标记观察' AND p.code = 'PROJ-2026-004' AND u.username = 'researcher2';

INSERT INTO data_records (equipment_id, usage_record_id, project_id, user_id, data_type, file_name, file_path, file_size, description, metadata_json)
SELECT e.id, ur.id, p.id, u.id, 'spectrum', 'IR_catalyst_001.csv', '/data/IR_catalyst_001.csv', 163840, '催化剂红外光谱数据', '{"range":"400-4000cm-1","resolution":"4cm-1"}'
FROM equipment e, usage_records ur, projects p, users u
WHERE e.serial_number = 'SN-008' AND ur.purpose = '催化剂红外光谱分析' AND p.code = 'PROJ-2026-003' AND u.username = 'researcher5';

INSERT INTO data_records (equipment_id, usage_record_id, project_id, user_id, data_type, file_name, file_path, file_size, description, metadata_json)
SELECT e.id, ur.id, p.id, u.id, 'measurement', 'XRD_semiconductor_001.dat', '/data/XRD_semiconductor_001.dat', 2097152, '半导体材料XRD测试数据', '{"material":"GaN","scan_range":"5-120deg"}'
FROM equipment e, usage_records ur, projects p, users u
WHERE e.serial_number = 'SN-007' AND ur.purpose = '半导体材料晶体结构验证' AND p.code = 'PROJ-2025-008' AND u.username = 'researcher1';

INSERT INTO data_records (equipment_id, usage_record_id, project_id, user_id, data_type, file_name, file_path, file_size, description, metadata_json)
SELECT e.id, ur.id, p.id, u.id, 'image', 'SEM_biofilm_001.tiff', '/data/SEM_biofilm_001.tiff', 4194304, '生物膜样品电镜形貌图', '{"magnification":"10000x","voltage":"10kV"}'
FROM equipment e, usage_records ur, projects p, users u
WHERE e.serial_number = 'SN-003' AND ur.purpose = '生物膜样品形貌观察' AND p.code = 'PROJ-2026-004' AND u.username = 'researcher2';

INSERT INTO data_records (equipment_id, usage_record_id, project_id, user_id, data_type, file_name, file_path, file_size, description, metadata_json)
SELECT e.id, ur.id, p.id, u.id, 'measurement', 'centrifuge_protein_001.csv', '/data/centrifuge_protein_001.csv', 81920, '蛋白质离心分离数据', '{"speed":"15000rpm","time":"30min","temperature":"4C"}'
FROM equipment e, usage_records ur, projects p, users u
WHERE e.serial_number = 'SN-002' AND ur.purpose = '蛋白质样品离心分离' AND p.code = 'PROJ-2026-002' AND u.username = 'researcher2';

INSERT INTO data_records (equipment_id, usage_record_id, project_id, user_id, data_type, file_name, file_path, file_size, description, metadata_json)
SELECT e.id, ur.id, p.id, u.id, 'spectrum', 'UV_catalyst_001.csv', '/data/UV_catalyst_001.csv', 204800, '催化剂紫外光谱数据', '{"wavelength_range":"200-800nm","concentration":"0.1mg/ml"}'
FROM equipment e, usage_records ur, projects p, users u
WHERE e.serial_number = 'SN-005' AND ur.purpose = '催化剂样品紫外光谱测定' AND p.code = 'PROJ-2026-003' AND u.username = 'researcher5';

INSERT INTO data_records (equipment_id, usage_record_id, project_id, user_id, data_type, file_name, file_path, file_size, description, metadata_json)
SELECT e.id, ur.id, p.id, u.id, 'measurement', 'ultracentrifuge_protein_001.csv', '/data/ultracentrifuge_protein_001.csv', 122880, '蛋白质超速离心纯化数据', '{"speed":"80000rpm","time":"4h","gradient":"sucrose"}'
FROM equipment e, usage_records ur, projects p, users u
WHERE e.serial_number = 'SN-011' AND ur.purpose = '蛋白质超速离心纯化' AND p.code = 'PROJ-2026-002' AND u.username = 'researcher3';
