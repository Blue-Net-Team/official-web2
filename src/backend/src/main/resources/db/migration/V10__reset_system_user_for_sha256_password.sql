-- 删除现有系统用户，以便重新初始化（使用新的密码格式：SHA-256 + BCrypt）
-- 系统用户学号为 000000000000（12个0）
-- 部署后 SystemUserInitializer 会自动重新创建系统用户

DELETE FROM tb_user WHERE student_id = '000000000000';
