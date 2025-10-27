-- 智慧办公楼系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_office DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smart_office;

-- ====================================
-- 1. 用户信息表
-- ====================================
CREATE TABLE IF NOT EXISTS user_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN-管理员, USER-普通用户',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 插入默认管理员账号（密码：admin123，明文存储）
INSERT INTO user_info (username, password, real_name, role)
VALUES ('admin', 'admin123', '系统管理员', 'ADMIN');

-- 插入普通用户（密码：user123，明文存储）
INSERT INTO user_info (username, password, real_name, role)
VALUES ('user', 'user123', '普通员工', 'USER');

-- ====================================
-- 2. 设备信息表
-- ====================================
CREATE TABLE IF NOT EXISTS device_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    device_id VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号（如W601_001）',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(50) NOT NULL COMMENT '设备类型：SENSOR-传感器, ACTUATOR-执行器',
    location VARCHAR(100) COMMENT '设备位置',
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE' COMMENT '设备状态：ONLINE-在线, OFFLINE-离线, FAULT-故障',
    online_status TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态：0-离线, 1-在线',
    last_online_time DATETIME COMMENT '最后在线时间',
    firmware_version VARCHAR(50) COMMENT '固件版本',
    description TEXT COMMENT '设备描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_device_id (device_id),
    INDEX idx_device_type (device_type),
    INDEX idx_status (status),
    INDEX idx_online_status (online_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备信息表';

-- 插入默认W601设备
INSERT INTO device_info (device_id, device_name, device_type, location, description)
VALUES
('W601_001', 'W601智能开发板', 'SENSOR', '办公室A区', '集成光照、温湿度传感器及RGB灯、蜂鸣器'),
('W601_002', 'W601智能开发板', 'SENSOR', '办公室B区', '集成光照、温湿度传感器及RGB灯、蜂鸣器');

-- ====================================
-- 3. 传感器数据表
-- ====================================
CREATE TABLE IF NOT EXISTS sensor_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据ID',
    device_id VARCHAR(50) NOT NULL COMMENT '设备编号',
    light DECIMAL(10,2) COMMENT '光照强度（lux）',
    temperature DECIMAL(5,2) COMMENT '温度（℃）',
    humidity DECIMAL(5,2) COMMENT '湿度（%）',
    flame TINYINT COMMENT '火焰检测：0-无火焰, 1-检测到火焰',
    rgb_status TINYINT COMMENT 'RGB灯状态：0-关闭, 1-开启',
    data_time DATETIME NOT NULL COMMENT '数据采集时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    INDEX idx_device_id (device_id),
    INDEX idx_data_time (data_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传感器数据表';

-- ====================================
-- 4. 告警日志表
-- ====================================
CREATE TABLE IF NOT EXISTS alarm_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '告警ID',
    device_id VARCHAR(50) NOT NULL COMMENT '设备编号',
    alarm_type VARCHAR(50) NOT NULL COMMENT '告警类型：FIRE-火灾, TEMP-温度异常, HUMIDITY-湿度异常, LIGHT-光照异常',
    alarm_level VARCHAR(20) NOT NULL COMMENT '告警级别：INFO-提示, WARNING-警告, CRITICAL-严重',
    alarm_message TEXT NOT NULL COMMENT '告警消息',
    alarm_value VARCHAR(50) COMMENT '告警触发值',
    threshold_value VARCHAR(50) COMMENT '阈值',
    status VARCHAR(20) NOT NULL DEFAULT 'UNHANDLED' COMMENT '处理状态：UNHANDLED-未处理, HANDLING-处理中, HANDLED-已处理, IGNORED-已忽略',
    handler_id BIGINT COMMENT '处理人ID',
    handle_time DATETIME COMMENT '处理时间',
    handle_remark TEXT COMMENT '处理备注',
    alarm_time DATETIME NOT NULL COMMENT '告警时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    INDEX idx_device_id (device_id),
    INDEX idx_alarm_type (alarm_type),
    INDEX idx_alarm_level (alarm_level),
    INDEX idx_status (status),
    INDEX idx_alarm_time (alarm_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警日志表';

-- ====================================
-- 5. 设备控制日志表
-- ====================================
CREATE TABLE IF NOT EXISTS control_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    device_id VARCHAR(50) NOT NULL COMMENT '设备编号',
    action VARCHAR(50) NOT NULL COMMENT '控制动作',
    params TEXT COMMENT '控制参数（JSON格式）',
    trigger_type VARCHAR(20) NOT NULL COMMENT '触发方式：AUTO-自动, MANUAL-手动',
    operator_id BIGINT COMMENT '操作人ID（手动触发时）',
    result VARCHAR(20) NOT NULL COMMENT '执行结果：SUCCESS-成功, FAILED-失败',
    error_message TEXT COMMENT '错误信息',
    control_time DATETIME NOT NULL COMMENT '控制时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    INDEX idx_device_id (device_id),
    INDEX idx_action (action),
    INDEX idx_trigger_type (trigger_type),
    INDEX idx_control_time (control_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备控制日志表';

-- ====================================
-- 6. 系统配置表
-- ====================================
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型：THRESHOLD-阈值, SYSTEM-系统配置',
    description VARCHAR(255) COMMENT '配置描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key),
    INDEX idx_config_type (config_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入默认阈值配置
INSERT INTO system_config (config_key, config_value, config_type, description)
VALUES
('light.threshold.low', '300', 'THRESHOLD', '光照阈值下限（低于此值开灯）'),
('light.threshold.high', '350', 'THRESHOLD', '光照阈值上限（高于此值关灯）'),
('temperature.threshold.low', '18', 'THRESHOLD', '温度阈值下限'),
('temperature.threshold.high', '28', 'THRESHOLD', '温度阈值上限'),
('humidity.threshold.low', '40', 'THRESHOLD', '湿度阈值下限'),
('humidity.threshold.high', '70', 'THRESHOLD', '湿度阈值上限'),
('mqtt.broker.url', 'tcp://localhost:1883', 'SYSTEM', 'MQTT服务器地址'),
('mqtt.client.id', 'smart-office-server', 'SYSTEM', 'MQTT客户端ID'),
('alarm.email.enable', 'false', 'SYSTEM', '邮件告警开关'),
('alarm.sms.enable', 'false', 'SYSTEM', '短信告警开关'),
('data.collect.interval', '5', 'SYSTEM', '数据采集间隔（秒）'),
('data.retention.days', '30', 'SYSTEM', '数据保留天数');

-- ====================================
-- 7. 操作日志表
-- ====================================
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    method VARCHAR(200) COMMENT '方法名',
    params TEXT COMMENT '请求参数',
    result TEXT COMMENT '返回结果',
    ip VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态：0-失败, 1-成功',
    error_message TEXT COMMENT '错误信息',
    execute_time BIGINT COMMENT '执行时长（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ====================================
-- 视图：最新传感器数据
-- ====================================
CREATE OR REPLACE VIEW v_latest_sensor_data AS
SELECT
    d.device_id,
    d.device_name,
    d.location,
    d.online_status,
    s.light,
    s.temperature,
    s.humidity,
    s.flame,
    s.rgb_status,
    s.data_time,
    s.create_time
FROM device_info d
LEFT JOIN (
    SELECT device_id, light, temperature, humidity, flame, rgb_status, data_time, create_time
    FROM sensor_data s1
    WHERE data_time = (
        SELECT MAX(data_time)
        FROM sensor_data s2
        WHERE s2.device_id = s1.device_id
    )
) s ON d.device_id = s.device_id;

-- ====================================
-- 视图：未处理告警统计
-- ====================================
CREATE OR REPLACE VIEW v_alarm_statistics AS
SELECT
    alarm_type,
    alarm_level,
    COUNT(*) as alarm_count,
    COUNT(CASE WHEN status = 'UNHANDLED' THEN 1 END) as unhandled_count,
    COUNT(CASE WHEN status = 'HANDLED' THEN 1 END) as handled_count,
    MAX(alarm_time) as latest_alarm_time
FROM alarm_log
WHERE alarm_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY alarm_type, alarm_level;

-- ====================================
-- 完成提示
-- ====================================
SELECT '数据库初始化完成！' AS message;
SELECT '默认管理员账号: admin / admin123' AS info;
SELECT '默认普通用户账号: user / user123' AS info;
