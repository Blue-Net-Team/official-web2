-- 移除竞赛表的启用状态字段（竞赛不再需要启停用控制）
ALTER TABLE tb_competition DROP COLUMN IF EXISTS enabled;
