-- 办公室管理相关表结构

USE smart_office;

-- ====================================
-- 1. 办公室表
-- ====================================
CREATE TABLE IF NOT EXISTS office (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '办公室ID',
    office_code VARCHAR(50) NOT NULL UNIQUE COMMENT '办公室编号（如201、202、203）',
    office_name VARCHAR(100) NOT NULL COMMENT '办公室名称',
    description TEXT COMMENT '办公室描述',
    floor INTEGER COMMENT '楼层',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '办公室状态：ACTIVE-启用, INACTIVE-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_office_code (office_code),
    INDEX idx_floor (floor),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='办公室表';

-- ====================================
-- 2. 办公区表
-- ====================================
CREATE TABLE IF NOT EXISTS work_area (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '办公区ID',
    area_code VARCHAR(50) NOT NULL COMMENT '办公区编号（如A区、B区、C区）',
    area_name VARCHAR(100) NOT NULL COMMENT '办公区名称',
    office_id BIGINT NOT NULL COMMENT '所属办公室ID',
    description TEXT COMMENT '办公区描述',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '办公区状态：ACTIVE-启用, INACTIVE-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_area_code (area_code),
    INDEX idx_office_id (office_id),
    INDEX idx_status (status),
    FOREIGN KEY (office_id) REFERENCES office(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='办公区表';

-- ====================================
-- 3. 更新设备信息表，添加办公室和办公区关联
-- ====================================
ALTER TABLE device_info 
ADD COLUMN office_id BIGINT COMMENT '所属办公室ID',
ADD COLUMN work_area_id BIGINT COMMENT '所属办公区ID',
ADD INDEX idx_office_id (office_id),
ADD INDEX idx_work_area_id (work_area_id);

-- 添加外键约束
ALTER TABLE device_info 
ADD CONSTRAINT fk_device_office FOREIGN KEY (office_id) REFERENCES office(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_device_work_area FOREIGN KEY (work_area_id) REFERENCES work_area(id) ON DELETE SET NULL;

-- ====================================
-- 4. 插入示例数据
-- ====================================

-- 插入办公室数据
INSERT INTO office (office_code, office_name, description, floor) VALUES
('201', '办公室201', '研发部办公室', 2),
('202', '办公室202', '市场部办公室', 2),
('203', '办公室203', '财务部办公室', 2),
('301', '办公室301', '人事部办公室', 3),
('302', '办公室302', '总经理办公室', 3);

-- 插入办公区数据
INSERT INTO work_area (area_code, area_name, office_id, description) VALUES
-- 办公室201的办公区
('A区', 'A区工作区', 1, '研发部A区工作区域'),
('B区', 'B区工作区', 1, '研发部B区工作区域'),
('C区', 'C区工作区', 1, '研发部C区工作区域'),
-- 办公室202的办公区
('A区', 'A区工作区', 2, '市场部A区工作区域'),
('B区', 'B区工作区', 2, '市场部B区工作区域'),
-- 办公室203的办公区
('A区', 'A区工作区', 3, '财务部A区工作区域'),
-- 办公室301的办公区
('A区', 'A区工作区', 4, '人事部A区工作区域'),
('B区', 'B区工作区', 4, '人事部B区工作区域'),
-- 办公室302的办公区
('A区', 'A区工作区', 5, '总经理办公室A区');

-- 更新现有设备的办公室和办公区信息
UPDATE device_info SET office_id = 1, work_area_id = 1 WHERE device_id = 'W601_001';
UPDATE device_info SET office_id = 1, work_area_id = 2 WHERE device_id = 'W601_002';

-- ====================================
-- 5. 创建视图：设备与办公室办公区关联信息
-- ====================================
CREATE OR REPLACE VIEW v_device_office_info AS
SELECT 
    d.id,
    d.device_id,
    d.device_name,
    d.device_type,
    d.location,
    d.status,
    d.online_status,
    d.last_online_time,
    o.office_code,
    o.office_name,
    wa.area_code,
    wa.area_name,
    d.create_time,
    d.update_time
FROM device_info d
LEFT JOIN office o ON d.office_id = o.id
LEFT JOIN work_area wa ON d.work_area_id = wa.id;

SELECT '办公室管理表结构创建完成！' AS message;