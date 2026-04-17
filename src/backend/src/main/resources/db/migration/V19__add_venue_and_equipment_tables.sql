-- 场地和设备管理功能数据库迁移脚本
-- 创建场地表和设备表，用于实验室环境展示

-- 1. 创建场地表
CREATE TABLE tb_venue (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    subtitle VARCHAR(100),
    description TEXT,
    image_file_id BIGINT,
    sort_order INTEGER DEFAULT 0
);

COMMENT ON TABLE tb_venue IS '场地表，存储实验室场地信息';
COMMENT ON COLUMN tb_venue.id IS '场地ID';
COMMENT ON COLUMN tb_venue.name IS '场地名称';
COMMENT ON COLUMN tb_venue.subtitle IS '场地副标题';
COMMENT ON COLUMN tb_venue.description IS '场地描述';
COMMENT ON COLUMN tb_venue.image_file_id IS '图片文件ID，关联tb_file.id';
COMMENT ON COLUMN tb_venue.sort_order IS '排序权重，越大越靠前';

-- 2. 创建设备表
CREATE TABLE tb_equipment (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    brand VARCHAR(100),
    description TEXT,
    image_file_id BIGINT,
    sort_order INTEGER DEFAULT 0
);

COMMENT ON TABLE tb_equipment IS '设备表，存储实验室设备信息';
COMMENT ON COLUMN tb_equipment.id IS '设备ID';
COMMENT ON COLUMN tb_equipment.name IS '设备名称';
COMMENT ON COLUMN tb_equipment.brand IS '设备品牌';
COMMENT ON COLUMN tb_equipment.description IS '设备描述';
COMMENT ON COLUMN tb_equipment.image_file_id IS '图片文件ID，关联tb_file.id';
COMMENT ON COLUMN tb_equipment.sort_order IS '排序权重，越大越靠前';

-- 3. 创建索引
CREATE INDEX idx_venue_sort_order ON tb_venue(sort_order DESC);
CREATE INDEX idx_equipment_sort_order ON tb_equipment(sort_order DESC);
