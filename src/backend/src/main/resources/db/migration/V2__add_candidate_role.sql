-- 添加CANDIDATE角色
INSERT INTO tb_role (name)
SELECT 'CANDIDATE'
WHERE NOT EXISTS (
    SELECT 1 FROM tb_role WHERE name = 'CANDIDATE'
);

-- 确保所有角色都存在
INSERT INTO tb_role (name) VALUES
    ('SUPER_ADMIN'),
    ('DIRECTION_ADMIN'),
    ('MEMBER'),
    ('CANDIDATE')
ON CONFLICT (name) DO NOTHING;
