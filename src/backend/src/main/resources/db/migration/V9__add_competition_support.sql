-- 竞赛管理功能数据库迁移脚本
-- 创建竞赛表，修改介绍图片表支持竞赛关联

-- 1. 创建竞赛表
CREATE TABLE tb_competition (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    short_name VARCHAR(50),
    logo_file_id BIGINT,
    summary VARCHAR(500),
    detail TEXT,
    sort_order INTEGER DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tb_competition IS '竞赛表，存储竞赛基本信息';
COMMENT ON COLUMN tb_competition.id IS '竞赛ID';
COMMENT ON COLUMN tb_competition.name IS '竞赛名称';
COMMENT ON COLUMN tb_competition.short_name IS '竞赛简称';
COMMENT ON COLUMN tb_competition.logo_file_id IS 'Logo文件ID，关联tb_file.id';
COMMENT ON COLUMN tb_competition.summary IS '竞赛简介（简短）';
COMMENT ON COLUMN tb_competition.detail IS '竞赛详细介绍';
COMMENT ON COLUMN tb_competition.sort_order IS '排序权重，越大越靠前';
COMMENT ON COLUMN tb_competition.enabled IS '是否启用';
COMMENT ON COLUMN tb_competition.created_at IS '创建时间';
COMMENT ON COLUMN tb_competition.updated_at IS '更新时间';

-- 2. 为tb_introduce_image表添加竞赛关联字段
ALTER TABLE tb_introduce_image
ADD COLUMN competition_id BIGINT,
ADD COLUMN sort_order INTEGER DEFAULT 0;

COMMENT ON COLUMN tb_introduce_image.competition_id IS '竞赛ID，仅在type=competition时有效';
COMMENT ON COLUMN tb_introduce_image.sort_order IS '排序权重，越大越靠前';

-- 3. 创建索引
CREATE INDEX idx_competition_enabled_sort ON tb_competition(enabled, sort_order DESC);
CREATE INDEX idx_introduce_image_competition ON tb_introduce_image(competition_id);
