-- RAG 向量存储表（pgvector）
-- 由 ai-service 的 PgVectorStore 使用，Schema 由主 API 服务 Flyway 管理

-- 启用 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================
-- RAG 标签表
-- ============================================
CREATE TABLE tb_rag_tags (
    id BIGSERIAL PRIMARY KEY,
    tag_name VARCHAR(128) NOT NULL UNIQUE,
    tag_vector VECTOR(1024) NOT NULL,
    tag_description VARCHAR(512) DEFAULT '',
    chunks_count INT DEFAULT 0
);

CREATE INDEX idx_rag_tags_name ON tb_rag_tags(tag_name);

-- ============================================
-- RAG 分段表
-- ============================================
CREATE TABLE tb_rag_chunks (
    id BIGSERIAL PRIMARY KEY,
    doc_id BIGINT,
    chunk_vector VECTOR(1024) NOT NULL,
    content TEXT DEFAULT '',
    tags VARCHAR(128)[],
    source VARCHAR(64) DEFAULT ''
);

CREATE INDEX idx_rag_chunks_doc_id ON tb_rag_chunks(doc_id);
CREATE INDEX idx_rag_chunks_source ON tb_rag_chunks(source);
CREATE INDEX idx_rag_chunks_tags ON tb_rag_chunks USING GIN(tags);

-- ============================================
-- RAG 文档表
-- ============================================
CREATE TABLE tb_rag_docs (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT,
    title VARCHAR(512) DEFAULT '',
    source VARCHAR(64) DEFAULT '',
    metadata JSONB
);

CREATE INDEX idx_rag_docs_source ON tb_rag_docs(source);
CREATE INDEX idx_rag_docs_file_id ON tb_rag_docs(file_id);

-- ============================================
-- 向量索引（pgvector HNSW，cosine 距离）
-- ============================================
CREATE INDEX idx_rag_tags_vector ON tb_rag_tags USING hnsw (tag_vector vector_cosine_ops);
CREATE INDEX idx_rag_chunks_vector ON tb_rag_chunks USING hnsw (chunk_vector vector_cosine_ops);

-- ============================================
-- 表注释
-- ============================================
COMMENT ON TABLE tb_rag_tags IS 'RAG 标签向量表';
COMMENT ON COLUMN tb_rag_tags.id IS '标签 ID，主键';
COMMENT ON COLUMN tb_rag_tags.tag_name IS '标签名称';
COMMENT ON COLUMN tb_rag_tags.tag_vector IS '标签向量（维度 1024）';
COMMENT ON COLUMN tb_rag_tags.tag_description IS '标签描述';
COMMENT ON COLUMN tb_rag_tags.chunks_count IS '关联分段数量';

COMMENT ON TABLE tb_rag_chunks IS 'RAG 文档分段向量表';
COMMENT ON COLUMN tb_rag_chunks.id IS '分段 ID，主键';
COMMENT ON COLUMN tb_rag_chunks.doc_id IS '所属文档 ID';
COMMENT ON COLUMN tb_rag_chunks.chunk_vector IS '分段向量（维度 1024）';
COMMENT ON COLUMN tb_rag_chunks.content IS '分段内容';
COMMENT ON COLUMN tb_rag_chunks.tags IS '标签数组';
COMMENT ON COLUMN tb_rag_chunks.source IS '数据来源';

COMMENT ON TABLE tb_rag_docs IS 'RAG 文档向量表';
COMMENT ON COLUMN tb_rag_docs.id IS '文档 ID，主键';
COMMENT ON COLUMN tb_rag_docs.file_id IS '关联文件 ID';
COMMENT ON COLUMN tb_rag_docs.title IS '文档标题';
COMMENT ON COLUMN tb_rag_docs.source IS '数据来源';
COMMENT ON COLUMN tb_rag_docs.metadata IS '扩展元数据（JSON）';
