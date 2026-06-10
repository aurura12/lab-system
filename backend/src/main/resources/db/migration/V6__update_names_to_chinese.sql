-- =============================================
-- V6__update_names_to_chinese.sql
-- 将 V2 中的英文数据更新为中文
-- =============================================

-- 更新实验室名称
UPDATE labs SET name = '主楼实验中心', description = '主要实验楼，配备各类大型仪器设备', address = '科学大道1号' WHERE name = 'Main Research Building';

-- 更新楼层名称
UPDATE floors SET name = '一层' WHERE name = '1F';
UPDATE floors SET name = '二层' WHERE name = '2F';

-- 更新房间名称
UPDATE rooms SET name = '化学实验室' WHERE room_number = '101' AND name = 'Chemistry Lab';
UPDATE rooms SET name = '物理实验室' WHERE room_number = '102' AND name = 'Physics Lab';
UPDATE rooms SET name = '生物实验室' WHERE room_number = '201' AND name = 'Biology Lab';

-- 更新设备名称
UPDATE equipment SET name = '紫外可见分光光度计' WHERE serial_number = 'SN-001';
UPDATE equipment SET name = '台式高速离心机' WHERE serial_number = 'SN-002';
UPDATE equipment SET name = '扫描电子显微镜' WHERE serial_number = 'SN-003';

-- 更新用户姓名
UPDATE users SET real_name = '系统管理员' WHERE username = 'admin';
UPDATE users SET real_name = '张伟' WHERE username = 'researcher1';

-- 更新项目名称
UPDATE projects SET name = '纳米材料表征', description = '纳米材料性能表征与分析研究' WHERE code = 'PROJ-2026-001';
