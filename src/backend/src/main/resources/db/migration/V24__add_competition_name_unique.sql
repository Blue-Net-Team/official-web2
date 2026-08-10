-- 为竞赛名称添加唯一约束，作为成就与竞赛之间的稳定关联键
-- 注意：执行前请确保 tb_competition 表中不存在重复 name，否则迁移会失败

ALTER TABLE tb_competition ADD CONSTRAINT uk_competition_name UNIQUE (name);
