-- 成就成员关联改造：
-- 1. 新建成就外部协作者表，存放非系统用户的合作成员姓名
-- 2. 竞赛经历统一迁移到成就系统，物理删除用户自维护的竞赛经历数据
-- 3. 为 tb_user_achievement.achievement_id 添加索引，支持按成就查询关联成员

CREATE TABLE tb_achievement_external_member (
    id SERIAL PRIMARY KEY,
    achievement_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_achievement_external_member_achievement_id ON tb_achievement_external_member(achievement_id);

COMMENT ON TABLE tb_achievement_external_member IS '成就外部协作者表';
COMMENT ON COLUMN tb_achievement_external_member.achievement_id IS '成就ID';
COMMENT ON COLUMN tb_achievement_external_member.name IS '外部协作者姓名';
COMMENT ON COLUMN tb_achievement_external_member.display_order IS '展示顺序';

DELETE FROM tb_user_experience WHERE type = 'COMPETITION';

CREATE INDEX IF NOT EXISTS idx_user_achievement_achievement_id ON tb_user_achievement(achievement_id);
