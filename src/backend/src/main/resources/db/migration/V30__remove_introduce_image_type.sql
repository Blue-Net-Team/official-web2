-- 移除介绍图片表的类型字段（竞赛图片改为直接使用 tb_competition.cover_file_id）
ALTER TABLE tb_introduce_image DROP COLUMN IF EXISTS type;
