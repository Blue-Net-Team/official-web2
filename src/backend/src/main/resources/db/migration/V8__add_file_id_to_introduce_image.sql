-- 为 tb_introduce_image 表添加 file_id 列和 direction 列
-- 用于关联文件表和方向筛选

-- 添加 file_id 列
ALTER TABLE tb_introduce_image
ADD COLUMN file_id BIGINT;

-- 添加 direction 列（用于方向介绍类型的图片筛选）
ALTER TABLE tb_introduce_image
ADD COLUMN direction VARCHAR(50);

-- 添加列注释
COMMENT ON COLUMN tb_introduce_image.file_id IS '关联的文件ID，逻辑外键引用 tb_file.id';
COMMENT ON COLUMN tb_introduce_image.direction IS '方向，仅在 type=direction 时有效，可选值：computer_vision, structural_design, embedded';

-- 添加索引以优化查询性能
CREATE INDEX idx_introduce_image_type ON tb_introduce_image(type);
CREATE INDEX idx_introduce_image_direction ON tb_introduce_image(direction);
CREATE INDEX idx_introduce_image_file_id ON tb_introduce_image(file_id);
