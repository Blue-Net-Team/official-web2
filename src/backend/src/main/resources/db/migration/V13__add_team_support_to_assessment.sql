-- 为考核系统添加组队支持
-- 1. 考核时间表增加 allow_team 字段，标识该考核是否允许组队
-- 2. 答案表增加 team_id 字段，用于关联队伍答案

ALTER TABLE tb_assessment_time
    ADD COLUMN allow_team BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN tb_assessment_time.allow_team IS '是否允许组队，默认 false';

ALTER TABLE tb_assessment_answer
    ADD COLUMN team_id BIGINT;

COMMENT ON COLUMN tb_assessment_answer.team_id IS '队伍ID，组队题时关联到队伍';

CREATE INDEX idx_asm_answer_team_id ON tb_assessment_answer(team_id);
