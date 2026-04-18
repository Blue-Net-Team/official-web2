-- 为权限表添加访问级别字段
ALTER TABLE tb_permission ADD COLUMN access_level VARCHAR(20) NOT NULL DEFAULT 'PROTECTED';

COMMENT ON COLUMN tb_permission.access_level IS '访问级别: PUBLIC(公开), AUTHENTICATED(需登录), PROTECTED(需权限)';
