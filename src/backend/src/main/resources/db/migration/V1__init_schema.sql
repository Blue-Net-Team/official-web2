-- 蓝网官方网站数据库初始化脚本
-- 创建18张表：角色权限、用户管理、报名系统、文件存储、考核系统、审计日志
-- 注意：所有表关系在应用层维护，不使用外键约束

-- 1. 角色表
CREATE TABLE tb_role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. 权限表
CREATE TABLE tb_permission (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    value VARCHAR(100) NOT NULL UNIQUE,
    url VARCHAR(255),
    method VARCHAR(10)
);

-- 3. 角色权限关联表
CREATE TABLE tb_role_permission (
    id SERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE(role_id, permission_id)
);

-- 4. 学院表
CREATE TABLE tb_college (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- 5. 用户表
CREATE TABLE tb_user (
    id SERIAL PRIMARY KEY,
    student_id VARCHAR(13) NOT NULL UNIQUE,
    email VARCHAR(100),
    role_id BIGINT,
    password VARCHAR(255),
    username VARCHAR(50),
    nickname VARCHAR(50),
    college_id BIGINT,
    major VARCHAR(100),
    direction VARCHAR(50),
    job VARCHAR(50),
    avatar_id BIGINT,
    disable BOOLEAN DEFAULT FALSE,
    wechat_qrcode VARCHAR(255),
    github_id VARCHAR(100),
    github_username VARCHAR(100)
);

-- 6. 用户经历表
CREATE TABLE tb_user_experience (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50),
    title VARCHAR(200),
    content TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP
);

-- 7. 成就表
CREATE TABLE tb_achievement (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    type VARCHAR(50),
    relate_to VARCHAR(200),
    achieve_at INTEGER
);

-- 8. 用户成就关联表
CREATE TABLE tb_user_achievement (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    UNIQUE(user_id, achievement_id)
);

-- 9. 文件表
CREATE TABLE tb_file (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    url VARCHAR(500) NOT NULL
);

-- 10. 介绍图片表
CREATE TABLE tb_introduce_image (
    id SERIAL PRIMARY KEY,
    type VARCHAR(50),
    description VARCHAR(500)
);

-- 11. 报名表
CREATE TABLE tb_enroll (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50),
    student_id VARCHAR(13) NOT NULL UNIQUE,
    password VARCHAR(255),
    internal_referral_code VARCHAR(50),
    college_id BIGINT,
    major VARCHAR(100),
    grade INTEGER,
    direction VARCHAR(50),
    avatar_id BIGINT,
    status VARCHAR(50) DEFAULT 'pending'
);

-- 12. 验证码表
CREATE TABLE tb_verify_code (
    id SERIAL PRIMARY KEY,
    target VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    expire_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    ip_address VARCHAR(50)
);

-- 13. 消息模板表
CREATE TABLE tb_message_template (
    id SERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100),
    subject VARCHAR(200),
    content TEXT,
    description VARCHAR(500),
    enabled BOOLEAN DEFAULT TRUE
);

-- 14. 考核时间表
CREATE TABLE tb_evaluation_time (
    id SERIAL PRIMARY KEY,
    direction VARCHAR(50),
    epoch INTEGER,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    time_limit BOOLEAN DEFAULT FALSE,
    time_limit_minutes INTEGER
);

-- 15. 考核题目表
CREATE TABLE tb_evaluation_question (
    id SERIAL PRIMARY KEY,
    evaluation_time_id BIGINT NOT NULL,
    question_no INTEGER NOT NULL,
    question_type VARCHAR(50),
    title VARCHAR(500),
    content JSONB,
    attachment_id BIGINT,
    score DECIMAL(10, 2)
);

-- 16. 考核答案表
CREATE TABLE tb_evaluation_answer (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    content TEXT,
    language VARCHAR(50),
    file_id BIGINT,
    submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, question_id)
);

-- 17. 评论表
CREATE TABLE tb_comment (
    id SERIAL PRIMARY KEY,
    answer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT,
    score DECIMAL(5, 2),
    comment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 18. 审计日志表
CREATE TABLE tb_audit (
    id SERIAL PRIMARY KEY,
    action VARCHAR(200) NOT NULL,
    action_arg JSONB,
    action_user_id BIGINT,
    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    remarks VARCHAR(500),
    success_state BOOLEAN
);

-- 创建索引（无外键约束，仅用于查询优化）
CREATE INDEX idx_user_student_id ON tb_user(student_id);
CREATE INDEX idx_user_email ON tb_user(email);
CREATE INDEX idx_user_role_id ON tb_user(role_id);
CREATE INDEX idx_user_college_id ON tb_user(college_id);
CREATE INDEX idx_user_direction ON tb_user(direction);

CREATE INDEX idx_enroll_student_id ON tb_enroll(student_id);
CREATE INDEX idx_enroll_college_id ON tb_enroll(college_id);
CREATE INDEX idx_enroll_direction ON tb_enroll(direction);
CREATE INDEX idx_enroll_status ON tb_enroll(status);

CREATE INDEX idx_user_experience_user_id ON tb_user_experience(user_id);

CREATE INDEX idx_verify_code_target ON tb_verify_code(target);
CREATE INDEX idx_verify_code_expire_at ON tb_verify_code(expire_at);

CREATE INDEX idx_eval_time_direction ON tb_evaluation_time(direction);
CREATE INDEX idx_eval_time_epoch ON tb_evaluation_time(epoch);

CREATE INDEX idx_eval_question_time_id ON tb_evaluation_question(evaluation_time_id);
CREATE INDEX idx_eval_question_type ON tb_evaluation_question(question_type);

CREATE INDEX idx_eval_answer_user_id ON tb_evaluation_answer(user_id);
CREATE INDEX idx_eval_answer_question_id ON tb_evaluation_answer(question_id);

CREATE INDEX idx_comment_answer_id ON tb_comment(answer_id);
CREATE INDEX idx_comment_user_id ON tb_comment(user_id);

CREATE INDEX idx_audit_action_time ON tb_audit(action_time);
CREATE INDEX idx_audit_action_user_id ON tb_audit(action_user_id);
