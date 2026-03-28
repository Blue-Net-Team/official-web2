-- 为 tb_user 表添加 bio 字段
-- 用于存储用户的个人简介信息

-- 添加 bio 列
ALTER TABLE tb_user
ADD COLUMN bio TEXT;

-- 添加列注释
COMMENT ON COLUMN tb_user.bio IS '个人简介';
