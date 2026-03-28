-- 为 tb_enroll 表添加 email 和 introduction 字段
-- 用于保存报名者的邮箱和自我介绍信息

-- 添加 email 列
ALTER TABLE tb_enroll
ADD COLUMN email VARCHAR(100);

-- 添加 introduction 列
ALTER TABLE tb_enroll
ADD COLUMN introduction TEXT;

-- 添加列注释
COMMENT ON COLUMN tb_enroll.email IS '报名者邮箱，用于接收通知';
COMMENT ON COLUMN tb_enroll.introduction IS '自我介绍，100-500字';

-- 创建索引优化查询
CREATE INDEX idx_enroll_email ON tb_enroll(email);
