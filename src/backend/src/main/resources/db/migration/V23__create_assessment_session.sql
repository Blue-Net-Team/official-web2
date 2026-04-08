CREATE TABLE tb_assessment_session (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    assessment_time_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    deadline TIMESTAMP NOT NULL,
    CONSTRAINT uk_session_user_time UNIQUE (user_id, assessment_time_id)
);

COMMENT ON TABLE tb_assessment_session IS '考核会话表，记录用户进入限时考核的开始时间和截止时间';
COMMENT ON COLUMN tb_assessment_session.id IS '会话ID，主键，自增';
COMMENT ON COLUMN tb_assessment_session.user_id IS '用户ID，关联 tb_user.id';
COMMENT ON COLUMN tb_assessment_session.assessment_time_id IS '考核时间ID，关联 tb_assessment_time.id';
COMMENT ON COLUMN tb_assessment_session.start_time IS '用户首次查看考题列表的时间';
COMMENT ON COLUMN tb_assessment_session.deadline IS '考核截止时间 = min(start_time + timeLimitMinutes, endTime)';
