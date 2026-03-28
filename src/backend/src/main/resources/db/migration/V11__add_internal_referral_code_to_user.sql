-- 添加内推码字段到 tb_user 表
ALTER TABLE tb_user ADD COLUMN internal_referral_code VARCHAR(8);

-- 添加部分唯一索引（只对非 NULL 的内推码生效）
CREATE UNIQUE INDEX idx_user_internal_referral_code
ON tb_user(internal_referral_code)
WHERE internal_referral_code IS NOT NULL;

-- 为现有团队成员生成内推码（角色为成员、方向负责人、超级管理员）
UPDATE tb_user
SET internal_referral_code = UPPER(SUBSTRING(MD5(RANDOM()::TEXT || id::TEXT) FROM 1 FOR 8))
WHERE role_id IN (SELECT id FROM tb_role WHERE name IN ('成员', '方向负责人', '超级管理员'))
  AND internal_referral_code IS NULL;

-- 添加列注释
COMMENT ON COLUMN tb_user.internal_referral_code IS '内推码，8位大写字母+数字，用于内部推荐';
