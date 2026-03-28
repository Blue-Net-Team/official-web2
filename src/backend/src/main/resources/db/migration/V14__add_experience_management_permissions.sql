-- 添加用户经历管理权限
INSERT INTO tb_permission (name, value, url, method)
VALUES
    ('创建用户经历', 'user:experience:create', '/api/v1/user/experiences', 'POST'),
    ('更新用户经历', 'user:experience:update', '/api/v1/user/experiences/*', 'PUT'),
    ('删除用户经历', 'user:experience:delete', '/api/v1/user/experiences/*', 'DELETE')
ON CONFLICT (value) DO NOTHING;

-- 为MEMBER角色分配经历管理权限
INSERT INTO tb_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM tb_role r
CROSS JOIN tb_permission p
WHERE r.name IN ('MEMBER', 'DIRECTION_ADMIN', 'SUPER_ADMIN')
  AND p.value IN ('user:experience:create', 'user:experience:update', 'user:experience:delete')
  AND NOT EXISTS (
      SELECT 1 FROM tb_role_permission rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
