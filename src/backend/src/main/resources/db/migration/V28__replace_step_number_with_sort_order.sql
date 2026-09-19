-- ============================================
-- V28: 学习步骤 step_number 替换为无语义的排序列 sort_order
-- 序号降级为视图：后端只负责顺序，展示编号由前端按位置派生
-- ============================================

-- 1. 新增排序列
ALTER TABLE tb_direction_learning_step ADD COLUMN sort_order INT NOT NULL DEFAULT 0;

-- 2. 用既有步骤序号回填，保证相对顺序不变
UPDATE tb_direction_learning_step SET sort_order = step_number;

-- 3. 移除方向内序号唯一约束（排序值允许空洞，重排时允许瞬时重复）
ALTER TABLE tb_direction_learning_step DROP CONSTRAINT uk_direction_step;

-- 4. 删除人工输入的步骤序号列
ALTER TABLE tb_direction_learning_step DROP COLUMN step_number;

COMMENT ON COLUMN tb_direction_learning_step.sort_order IS '展示排序值，数值越小越靠前；无唯一约束，允许空洞';

-- ============================================
-- 回滚参考（不随 Flyway 自动执行，仅在需要回退时手工执行）
-- ============================================
-- ALTER TABLE tb_direction_learning_step ADD COLUMN step_number INT;
-- UPDATE tb_direction_learning_step t
--    SET step_number = r.rn
--   FROM (SELECT id, row_number() OVER (PARTITION BY direction ORDER BY sort_order ASC, id ASC) AS rn
--           FROM tb_direction_learning_step) r
--  WHERE t.id = r.id;
-- ALTER TABLE tb_direction_learning_step ALTER COLUMN step_number SET NOT NULL;
-- ALTER TABLE tb_direction_learning_step ADD CONSTRAINT uk_direction_step UNIQUE (direction, step_number);
-- ALTER TABLE tb_direction_learning_step DROP COLUMN sort_order;
