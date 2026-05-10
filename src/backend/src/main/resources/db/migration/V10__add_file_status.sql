-- 为 tb_file 表新增 status 字段，用于跟踪预签名上传的中间状态
ALTER TABLE tb_file ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'active';

COMMENT ON COLUMN tb_file.status IS '文件状态：pending/待上传, active/已激活, rejected/已拒绝';
