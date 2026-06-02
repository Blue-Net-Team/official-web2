-- 为知识库管理扩展 tb_rag_docs 表结构

-- 添加解析状态字段
ALTER TABLE tb_rag_docs
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) DEFAULT 'pending',
    ADD COLUMN IF NOT EXISTS chunk_count INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS error_message TEXT DEFAULT '',
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 为 status 创建索引以便快速查询解析状态
CREATE INDEX IF NOT EXISTS idx_rag_docs_status ON tb_rag_docs(status);

-- 为 created_at 创建索引以便分页排序
CREATE INDEX IF NOT EXISTS idx_rag_docs_created_at ON tb_rag_docs(created_at);

-- 表注释更新
COMMENT ON COLUMN tb_rag_docs.status IS '解析状态：pending(待解析)、parsing(解析中)、completed(已完成)、failed(失败)、canceling(取消中)、canceled(已取消)';
COMMENT ON COLUMN tb_rag_docs.chunk_count IS '关联分段数量';
COMMENT ON COLUMN tb_rag_docs.error_message IS '解析失败时的错误信息';
COMMENT ON COLUMN tb_rag_docs.created_at IS '记录创建时间';
COMMENT ON COLUMN tb_rag_docs.updated_at IS '记录更新时间';
