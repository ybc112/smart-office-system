-- ====================================
-- 更新用户密码为明文
-- ====================================
-- 用于将现有数据库中的BCrypt加密密码更新为明文密码
-- 执行此脚本前请确保已备份数据库

USE smart_office;

-- 更新管理员密码为明文
UPDATE user_info SET password = 'admin123' WHERE username = 'admin';

-- 更新普通用户密码为明文
UPDATE user_info SET password = 'user123' WHERE username = 'user';

-- 验证更新结果
SELECT id, username, password, role FROM user_info;
