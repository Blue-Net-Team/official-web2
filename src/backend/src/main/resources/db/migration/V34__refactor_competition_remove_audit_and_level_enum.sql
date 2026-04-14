-- 1. 将现有中文级别值转换为英文枚举值
UPDATE tb_competition SET level = 'national' WHERE level = '国家级';
UPDATE tb_competition SET level = 'provincial' WHERE level = '省级';
UPDATE tb_competition SET level = 'school' WHERE level = '校级';

-- 2. 修改 level 列默认值为英文枚举值
ALTER TABLE tb_competition ALTER COLUMN level SET DEFAULT 'provincial';

-- 3. 删除审计字段
ALTER TABLE tb_competition DROP COLUMN created_at;
ALTER TABLE tb_competition DROP COLUMN updated_at;

-- 4. 更新列注释
COMMENT ON COLUMN tb_competition.level IS '竞赛级别，对应 AwardLevel 枚举值：national/provincial/school';

-- 5. 删除不再需要的索引（原 enabled 列的索引包含了已删除的 enabled 列）
DROP INDEX IF EXISTS idx_competition_enabled_sort;
