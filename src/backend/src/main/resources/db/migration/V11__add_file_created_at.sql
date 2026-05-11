-- 为 tb_file 表新增 created_at 字段，用于支持孤儿文件清理中的 PENDING 超时判定
ALTER TABLE tb_file ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();

COMMENT ON COLUMN tb_file.created_at IS '文件记录创建时间';
