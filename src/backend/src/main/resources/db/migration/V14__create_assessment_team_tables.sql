-- 创建考核队伍表和队伍成员表

CREATE TABLE tb_assessment_team (
    id SERIAL PRIMARY KEY,
    assessment_time_id BIGINT NOT NULL,
    leader_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    invite_code VARCHAR(10) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tb_assessment_team IS '考核队伍表';
COMMENT ON COLUMN tb_assessment_team.id IS '队伍ID';
COMMENT ON COLUMN tb_assessment_team.assessment_time_id IS '所属考核时间ID';
COMMENT ON COLUMN tb_assessment_team.leader_id IS '队长用户ID';
COMMENT ON COLUMN tb_assessment_team.name IS '队伍名称';
COMMENT ON COLUMN tb_assessment_team.invite_code IS '邀请码，唯一';
COMMENT ON COLUMN tb_assessment_team.status IS '队伍状态：ACTIVE/DISBANDED';
COMMENT ON COLUMN tb_assessment_team.created_at IS '创建时间';

CREATE INDEX idx_asm_team_time_id ON tb_assessment_team(assessment_time_id);
CREATE INDEX idx_asm_team_leader_id ON tb_assessment_team(leader_id);
CREATE INDEX idx_asm_team_invite_code ON tb_assessment_team(invite_code);

CREATE TABLE tb_assessment_team_member (
    id SERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(team_id, user_id)
);

COMMENT ON TABLE tb_assessment_team_member IS '考核队伍成员表';
COMMENT ON COLUMN tb_assessment_team_member.id IS '成员记录ID';
COMMENT ON COLUMN tb_assessment_team_member.team_id IS '所属队伍ID';
COMMENT ON COLUMN tb_assessment_team_member.user_id IS '成员用户ID';
COMMENT ON COLUMN tb_assessment_team_member.joined_at IS '加入时间';

CREATE INDEX idx_asm_team_member_team_id ON tb_assessment_team_member(team_id);
CREATE INDEX idx_asm_team_member_user_id ON tb_assessment_team_member(user_id);
