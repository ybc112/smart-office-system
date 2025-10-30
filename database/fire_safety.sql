-- 消防安全管理数据库表

-- 消防水带表
CREATE TABLE fire_hose (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '消防水带编号',
    name VARCHAR(100) NOT NULL COMMENT '消防水带名称',
    location VARCHAR(200) NOT NULL COMMENT '安装位置',
    last_check_time DATETIME COMMENT '上次检查时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-停用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='消防水带管理表';

-- 灭火器表
CREATE TABLE fire_extinguisher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '灭火器编号',
    name VARCHAR(100) NOT NULL COMMENT '灭火器名称',
    location VARCHAR(200) NOT NULL COMMENT '安装位置',
    last_pressure_check_time DATETIME COMMENT '上次压力检查时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-停用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='灭火器管理表';

-- 插入示例数据
INSERT INTO fire_hose (code, name, location, last_check_time) VALUES
('FH001', '消防水带-1号', '1楼大厅东侧', '2024-01-15 10:00:00'),
('FH002', '消防水带-2号', '1楼大厅西侧', '2024-01-10 14:30:00'),
('FH003', '消防水带-3号', '2楼走廊中央', '2024-01-20 09:15:00'),
('FH004', '消防水带-4号', '2楼安全出口', '2023-12-25 16:45:00');

INSERT INTO fire_extinguisher (code, name, location, last_pressure_check_time) VALUES
('FE001', '干粉灭火器-1号', '1楼办公室201门口', '2024-01-18 11:00:00'),
('FE002', '干粉灭火器-2号', '1楼办公室202门口', '2024-01-12 15:20:00'),
('FE003', '泡沫灭火器-1号', '2楼办公室203门口', '2024-01-22 08:30:00'),
('FE004', '二氧化碳灭火器-1号', '2楼办公室204门口', '2023-12-20 13:10:00');