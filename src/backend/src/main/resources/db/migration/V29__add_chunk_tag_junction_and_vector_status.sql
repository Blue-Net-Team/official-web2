-- 知识库分片-标签中间表 + 分片向量状态
-- 中间表无物理外键，关系由应用层维护（项目规范）

-- ============================================
-- 分片-标签关联中间表
-- ============================================
CREATE TABLE tb_rag_chunk_tags (
    chunk_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (chunk_id, tag_id)
);

CREATE INDEX idx_rag_chunk_tags_tag ON tb_rag_chunk_tags(tag_id);

COMMENT ON TABLE tb_rag_chunk_tags IS 'RAG 分片-标签关联中间表（应用层维护关系）';
COMMENT ON COLUMN tb_rag_chunk_tags.chunk_id IS '分段 ID';
COMMENT ON COLUMN tb_rag_chunk_tags.tag_id IS '标签 ID';

-- ============================================
-- 分片向量同步状态
-- ============================================
ALTER TABLE tb_rag_chunks
    ADD COLUMN IF NOT EXISTS vector_status VARCHAR(32) NOT NULL DEFAULT 'synced';

COMMENT ON COLUMN tb_rag_chunks.vector_status IS '向量同步状态：synced(已同步)、embedding(向量化中)';

-- ============================================
-- 存量数据回填：tags 名字数组 → 中间表
-- 名字在 tb_rag_tags 中无对应记录的标签跳过（记日志由迁移审查替代，数据量小可人工核对）
-- ============================================
INSERT INTO tb_rag_chunk_tags (chunk_id, tag_id)
SELECT c.id, t.id
FROM tb_rag_chunks c
CROSS JOIN LATERAL unnest(c.tags) AS u(tag_name)
JOIN tb_rag_tags t ON t.tag_name = u.tag_name
ON CONFLICT DO NOTHING;
