-- 团队成就页面功能数据库迁移脚本
-- 扩展 tb_achievement 表，添加竞赛获奖相关字段

-- 1. 修改 achieve_at 字段类型从 INTEGER 改为 DATE
ALTER TABLE tb_achievement 
ALTER COLUMN achieve_at TYPE DATE USING to_date(achieve_at::text, 'YYYY');

COMMENT ON COLUMN tb_achievement.achieve_at IS '获奖日期';

-- 2. 添加竞赛获奖相关字段
ALTER TABLE tb_achievement 
ADD COLUMN competition_id BIGINT,
ADD COLUMN award_level VARCHAR(20),
ADD COLUMN award_name VARCHAR(50),
ADD COLUMN winner_count INTEGER DEFAULT 0;

COMMENT ON COLUMN tb_achievement.competition_id IS '关联竞赛ID，关联tb_competition.id，仅type=COMPETITION时有效';
COMMENT ON COLUMN tb_achievement.award_level IS '奖项级别：national/provincial/school，仅type=COMPETITION时有效';
COMMENT ON COLUMN tb_achievement.award_name IS '奖项名称：一等奖/二等奖/三等奖，仅type=COMPETITION时有效';
COMMENT ON COLUMN tb_achievement.winner_count IS '获奖人数，仅type=COMPETITION时有效';

-- 3. 创建索引
CREATE INDEX idx_achievement_competition_id ON tb_achievement(competition_id);
CREATE INDEX idx_achievement_award_level ON tb_achievement(award_level);
CREATE INDEX idx_achievement_achieve_at ON tb_achievement(achieve_at DESC);
CREATE INDEX idx_achievement_type ON tb_achievement(type);
