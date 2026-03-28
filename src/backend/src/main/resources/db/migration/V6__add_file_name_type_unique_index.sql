-- 为 tb_file 表添加 name 和 type 的组合唯一索引
-- 确保同一类型的文件不能有重复的名称

ALTER TABLE tb_file
ADD CONSTRAINT uk_file_name_type UNIQUE (name, type);
