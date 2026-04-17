-- 删除成就模块中不需要的字段
-- competition_id: 卡片不可点击，无需跳转
-- winner_count: 可通过关联用户表查得，是冗余字段

-- 1. 删除相关索引
DROP INDEX IF EXISTS idx_achievement_competition_id;

-- 2. 删除字段
ALTER TABLE tb_achievement 
DROP COLUMN IF EXISTS competition_id,
DROP COLUMN IF EXISTS winner_count;
