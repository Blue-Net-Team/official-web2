-- 删除考核评判表中的 comment 字段，评语功能已迁移至 Comment 系统
ALTER TABLE tb_assessment_judgement DROP COLUMN comment;
