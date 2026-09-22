-- 知识库标签向量同步状态（与 tb_rag_chunks.vector_status 同模式）
-- 存量行默认 synced：现有标签在创建时已由 ai-service 完成嵌入，无需回填

ALTER TABLE tb_rag_tags
    ADD COLUMN IF NOT EXISTS vector_status VARCHAR(32) NOT NULL DEFAULT 'synced';

COMMENT ON COLUMN tb_rag_tags.vector_status IS '标签向量同步状态：synced(已同步)、embedding(向量化中)';
