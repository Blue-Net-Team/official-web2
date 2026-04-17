-- 蓝网官方网站数据库初始化脚本
-- 所有表关系在应用层维护，不使用外键约束
-- 由 V1~V33 迁移脚本合并而来

-- ============================================
-- 1. 角色与权限
-- ============================================

CREATE TABLE tb_role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tb_permission (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    value VARCHAR(100) NOT NULL UNIQUE,
    url VARCHAR(255),
    method VARCHAR(10)
);

CREATE TABLE tb_role_permission (
    id SERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE(role_id, permission_id)
);

-- 初始化角色
INSERT INTO tb_role (name) VALUES
    ('SUPER_ADMIN'),
    ('DIRECTION_ADMIN'),
    ('MEMBER'),
    ('CANDIDATE')
ON CONFLICT (name) DO NOTHING;

-- ============================================
-- 2. 学院
-- ============================================

CREATE TABLE tb_college (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- ============================================
-- 3. 用户
-- ============================================

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
    gender VARCHAR(20) NOT NULL DEFAULT 'unknown',
    job VARCHAR(50),
    avatar_id BIGINT,
    disable BOOLEAN DEFAULT FALSE,
    qrcode_id BIGINT,
    github_id VARCHAR(100),
    github_username VARCHAR(100),
    internal_referral_code VARCHAR(8),
    bio TEXT
);

CREATE TABLE tb_user_experience (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50),
    title VARCHAR(200),
    content TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP
);

-- ============================================
-- 4. 成就
-- ============================================

CREATE TABLE tb_achievement (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    type VARCHAR(50),
    relate_to VARCHAR(200),
    achieve_at DATE,
    award_level VARCHAR(20),
    award_name VARCHAR(50),
    file_id BIGINT
);

CREATE TABLE tb_user_achievement (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    UNIQUE(user_id, achievement_id)
);

-- ============================================
-- 5. 文件
-- ============================================

CREATE TABLE tb_file (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    url VARCHAR(500) NOT NULL,
    CONSTRAINT uk_file_name_type UNIQUE (name, type)
);

-- ============================================
-- 6. 报名
-- ============================================

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
    status VARCHAR(50) DEFAULT 'pending',
    email VARCHAR(100),
    introduction TEXT
);

-- ============================================
-- 7. 验证码与消息模板
-- ============================================

CREATE TABLE tb_verify_code (
    id SERIAL PRIMARY KEY,
    target VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    expire_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    scene VARCHAR(50) NOT NULL DEFAULT 'login'
);

CREATE TABLE tb_message_template (
    id SERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100),
    subject VARCHAR(200),
    content TEXT,
    description VARCHAR(500),
    enabled BOOLEAN DEFAULT TRUE
);

-- ============================================
-- 8. 考核系统
-- ============================================

CREATE TABLE tb_assessment_time (
    id SERIAL PRIMARY KEY,
    direction VARCHAR(50),
    epoch INTEGER,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    time_limit BOOLEAN DEFAULT FALSE,
    time_limit_minutes INTEGER,
    grade INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uk_assessment_time_direction_epoch_grade UNIQUE (direction, epoch, grade)
);

CREATE TABLE tb_assessment_question (
    id SERIAL PRIMARY KEY,
    assessment_time_id BIGINT NOT NULL,
    question_no INTEGER NOT NULL,
    question_type VARCHAR(50),
    title VARCHAR(500),
    content JSONB,
    attachment_id BIGINT,
    score DECIMAL(10, 2)
);

CREATE TABLE tb_assessment_answer (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    content TEXT,
    language VARCHAR(50),
    file_id BIGINT,
    submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, question_id)
);

CREATE TABLE tb_assessment_session (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    assessment_time_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    deadline TIMESTAMP NOT NULL,
    CONSTRAINT uk_session_user_time UNIQUE (user_id, assessment_time_id)
);

CREATE TABLE tb_comment (
    id SERIAL PRIMARY KEY,
    answer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT,
    score DECIMAL(5, 2),
    comment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 9. 审计日志
-- ============================================

CREATE TABLE tb_audit (
    id SERIAL PRIMARY KEY,
    action_arg JSONB,
    action_user_id BIGINT,
    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    success_state BOOLEAN,
    request_method VARCHAR(10),
    request_uri VARCHAR(500),
    http_status INTEGER,
    response_message VARCHAR(500),
    stack_trace TEXT,
    duration_ms BIGINT,
    request_uri_pattern VARCHAR(500)
);

-- ============================================
-- 10. 二维码
-- ============================================

CREATE TABLE tb_qrcode (
    id SERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL,
    type VARCHAR(50),
    epoch INT,
    direction VARCHAR(50),
    is_shared BOOLEAN DEFAULT FALSE
);

-- ============================================
-- 11. 竞赛
-- ============================================

CREATE TABLE tb_competition (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    short_name VARCHAR(50),
    logo_file_id BIGINT,
    summary VARCHAR(500),
    sort_order INTEGER DEFAULT 0,
    level VARCHAR(20) NOT NULL DEFAULT 'provincial',
    month VARCHAR(10),
    organizer VARCHAR(200),
    cover_file_id BIGINT
);

-- ============================================
-- 12. 方向学习路径
-- ============================================

CREATE TABLE tb_direction_learning_step (
    id BIGSERIAL PRIMARY KEY,
    direction VARCHAR(50) NOT NULL,
    step_number INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    video_url VARCHAR(500),
    CONSTRAINT uk_direction_step UNIQUE (direction, step_number)
);

-- 初始化默认学习步骤
INSERT INTO tb_direction_learning_step (direction, step_number, title, video_url) VALUES
('computer_vision', 1, 'Python基础', NULL),
('computer_vision', 2, 'OpenCV入门', NULL),
('computer_vision', 3, '深度学习基础', NULL),
('computer_vision', 4, '项目实战', NULL),
('embedded', 1, 'C语言基础', NULL),
('embedded', 2, '单片机入门', NULL),
('embedded', 3, 'RTOS实时操作系统', NULL),
('embedded', 4, '项目实战', NULL),
('structural_design', 1, 'CAD基础', NULL),
('structural_design', 2, 'SolidWorks入门', NULL),
('structural_design', 3, '仿真分析', NULL),
('structural_design', 4, '项目实战', NULL);

-- ============================================
-- 13. 场地与设备
-- ============================================

CREATE TABLE tb_venue (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    subtitle VARCHAR(100),
    description TEXT,
    image_file_id BIGINT,
    sort_order INTEGER DEFAULT 0
);

CREATE TABLE tb_equipment (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    brand VARCHAR(100),
    description TEXT,
    image_file_id BIGINT,
    sort_order INTEGER DEFAULT 0
);

-- ============================================
-- 索引
-- ============================================

-- tb_user
CREATE INDEX idx_user_student_id ON tb_user(student_id);
CREATE INDEX idx_user_email ON tb_user(email);
CREATE INDEX idx_user_role_id ON tb_user(role_id);
CREATE INDEX idx_user_college_id ON tb_user(college_id);
CREATE INDEX idx_user_direction ON tb_user(direction);
CREATE UNIQUE INDEX idx_user_internal_referral_code ON tb_user(internal_referral_code) WHERE internal_referral_code IS NOT NULL;

-- tb_user_experience
CREATE INDEX idx_user_experience_user_id ON tb_user_experience(user_id);

-- tb_achievement
CREATE INDEX idx_achievement_award_level ON tb_achievement(award_level);
CREATE INDEX idx_achievement_achieve_at ON tb_achievement(achieve_at DESC);
CREATE INDEX idx_achievement_type ON tb_achievement(type);
CREATE INDEX idx_achievement_file_id ON tb_achievement(file_id);

-- tb_enroll
CREATE INDEX idx_enroll_student_id ON tb_enroll(student_id);
CREATE INDEX idx_enroll_college_id ON tb_enroll(college_id);
CREATE INDEX idx_enroll_direction ON tb_enroll(direction);
CREATE INDEX idx_enroll_status ON tb_enroll(status);
CREATE INDEX idx_enroll_email ON tb_enroll(email);

-- tb_verify_code
CREATE INDEX idx_verify_code_target ON tb_verify_code(target);
CREATE INDEX idx_verify_code_expire_at ON tb_verify_code(expire_at);
CREATE INDEX idx_verify_code_scene ON tb_verify_code(scene);
CREATE INDEX idx_verify_code_target_scene ON tb_verify_code(target, scene);

-- tb_assessment_time
CREATE INDEX idx_asm_time_direction ON tb_assessment_time(direction);
CREATE INDEX idx_asm_time_epoch ON tb_assessment_time(epoch);

-- tb_assessment_question
CREATE INDEX idx_asm_question_time_id ON tb_assessment_question(assessment_time_id);
CREATE INDEX idx_asm_question_type ON tb_assessment_question(question_type);

-- tb_assessment_answer
CREATE INDEX idx_asm_answer_user_id ON tb_assessment_answer(user_id);
CREATE INDEX idx_asm_answer_question_id ON tb_assessment_answer(question_id);

-- tb_comment
CREATE INDEX idx_comment_answer_id ON tb_comment(answer_id);
CREATE INDEX idx_comment_user_id ON tb_comment(user_id);

-- tb_audit
CREATE INDEX idx_audit_action_time ON tb_audit(action_time);
CREATE INDEX idx_audit_action_user_id ON tb_audit(action_user_id);
CREATE INDEX idx_audit_request_uri ON tb_audit(request_uri);
CREATE INDEX idx_audit_http_status ON tb_audit(http_status);
CREATE INDEX idx_audit_uri_pattern ON tb_audit(request_uri_pattern);

-- tb_direction_learning_step
CREATE INDEX idx_direction_learning_step_direction ON tb_direction_learning_step(direction);

-- tb_venue
CREATE INDEX idx_venue_sort_order ON tb_venue(sort_order DESC);

-- tb_equipment
CREATE INDEX idx_equipment_sort_order ON tb_equipment(sort_order DESC);

-- ============================================
-- 表与列注释
-- ============================================

-- 角色权限
COMMENT ON TABLE tb_role IS '角色表，定义系统中的用户角色';
COMMENT ON COLUMN tb_role.id IS '角色ID，主键，自增';
COMMENT ON COLUMN tb_role.name IS '角色唯一标识，如 SUPER_ADMIN, DIRECTION_ADMIN, MEMBER, CANDIDATE';

COMMENT ON TABLE tb_permission IS '权限表，定义系统中的操作权限，与角色进行多对多关联';
COMMENT ON COLUMN tb_permission.id IS '权限ID，主键，自增';
COMMENT ON COLUMN tb_permission.name IS '权限名称，用于外部展示';
COMMENT ON COLUMN tb_permission.value IS '权限值，唯一标识，如 user:create';
COMMENT ON COLUMN tb_permission.url IS '操作的URL路径';
COMMENT ON COLUMN tb_permission.method IS 'HTTP请求方法';

COMMENT ON TABLE tb_role_permission IS '角色权限关联表';
COMMENT ON COLUMN tb_role_permission.role_id IS '角色ID';
COMMENT ON COLUMN tb_role_permission.permission_id IS '权限ID';

-- 学院
COMMENT ON TABLE tb_college IS '学院表';
COMMENT ON COLUMN tb_college.name IS '学院名称';

-- 用户
COMMENT ON TABLE tb_user IS '用户表，存储系统用户信息';
COMMENT ON COLUMN tb_user.student_id IS '学号，用户唯一凭证';
COMMENT ON COLUMN tb_user.email IS '用户邮箱';
COMMENT ON COLUMN tb_user.role_id IS '角色ID';
COMMENT ON COLUMN tb_user.password IS '密码哈希值';
COMMENT ON COLUMN tb_user.username IS '真实姓名';
COMMENT ON COLUMN tb_user.nickname IS '昵称';
COMMENT ON COLUMN tb_user.college_id IS '学院ID';
COMMENT ON COLUMN tb_user.major IS '专业';
COMMENT ON COLUMN tb_user.direction IS '方向';
COMMENT ON COLUMN tb_user.gender IS '性别：male/female/unknown';
COMMENT ON COLUMN tb_user.job IS '细化职责';
COMMENT ON COLUMN tb_user.avatar_id IS '头像文件ID';
COMMENT ON COLUMN tb_user.disable IS '账号封禁标识';
COMMENT ON COLUMN tb_user.qrcode_id IS '微信二维码ID';
COMMENT ON COLUMN tb_user.github_id IS 'GitHub用户ID';
COMMENT ON COLUMN tb_user.github_username IS 'GitHub用户名';
COMMENT ON COLUMN tb_user.internal_referral_code IS '内推码，8位大写字母+数字';
COMMENT ON COLUMN tb_user.bio IS '个人简介';

COMMENT ON TABLE tb_user_experience IS '用户经历表';
COMMENT ON COLUMN tb_user_experience.user_id IS '用户ID';
COMMENT ON COLUMN tb_user_experience.type IS '经历类型：竞赛、项目、实习';
COMMENT ON COLUMN tb_user_experience.title IS '标题';
COMMENT ON COLUMN tb_user_experience.content IS '详细内容';
COMMENT ON COLUMN tb_user_experience.start_time IS '开始时间';
COMMENT ON COLUMN tb_user_experience.end_time IS '结束时间';

-- 成就
COMMENT ON TABLE tb_achievement IS '成就表';
COMMENT ON COLUMN tb_achievement.title IS '成就标题';
COMMENT ON COLUMN tb_achievement.type IS '类型：论文、专利、竞赛';
COMMENT ON COLUMN tb_achievement.relate_to IS '相关竞赛或期刊名称';
COMMENT ON COLUMN tb_achievement.achieve_at IS '获奖日期';
COMMENT ON COLUMN tb_achievement.award_level IS '奖项级别：national/provincial/school';
COMMENT ON COLUMN tb_achievement.award_name IS '奖项名称';
COMMENT ON COLUMN tb_achievement.file_id IS '关联文件ID';

COMMENT ON TABLE tb_user_achievement IS '用户成就关联表';

-- 文件
COMMENT ON TABLE tb_file IS '文件表，存储文件元信息';
COMMENT ON COLUMN tb_file.name IS '文件名';
COMMENT ON COLUMN tb_file.type IS '文件类型，如 avatar, normal_img, assessment_attachment, work, qrcode';
COMMENT ON COLUMN tb_file.url IS '文件访问URL';

-- 报名
COMMENT ON TABLE tb_enroll IS '报名表';
COMMENT ON COLUMN tb_enroll.student_id IS '学号';
COMMENT ON COLUMN tb_enroll.internal_referral_code IS '内推码';
COMMENT ON COLUMN tb_enroll.college_id IS '学院ID';
COMMENT ON COLUMN tb_enroll.grade IS '年级';
COMMENT ON COLUMN tb_enroll.direction IS '报名方向';
COMMENT ON COLUMN tb_enroll.avatar_id IS '头像文件ID';
COMMENT ON COLUMN tb_enroll.status IS '报名状态：pending/approved/rejected';
COMMENT ON COLUMN tb_enroll.email IS '报名者邮箱';
COMMENT ON COLUMN tb_enroll.introduction IS '自我介绍';

-- 验证码
COMMENT ON TABLE tb_verify_code IS '验证码表';
COMMENT ON COLUMN tb_verify_code.target IS '目标标识（邮箱）';
COMMENT ON COLUMN tb_verify_code.code IS '验证码';
COMMENT ON COLUMN tb_verify_code.expire_at IS '过期时间';
COMMENT ON COLUMN tb_verify_code.used_at IS '使用时间';
COMMENT ON COLUMN tb_verify_code.scene IS '场景，如 login';

-- 消息模板
COMMENT ON TABLE tb_message_template IS '消息模板表';
COMMENT ON COLUMN tb_message_template.code IS '模板编码';
COMMENT ON COLUMN tb_message_template.name IS '模板名称';
COMMENT ON COLUMN tb_message_template.subject IS '邮件主题';
COMMENT ON COLUMN tb_message_template.content IS '模板内容';
COMMENT ON COLUMN tb_message_template.description IS '模板说明';
COMMENT ON COLUMN tb_message_template.enabled IS '是否启用';

-- 考核
COMMENT ON TABLE tb_assessment_time IS '考核时间表';
COMMENT ON COLUMN tb_assessment_time.direction IS '考核方向';
COMMENT ON COLUMN tb_assessment_time.epoch IS '考核轮次';
COMMENT ON COLUMN tb_assessment_time.start_time IS '开始时间';
COMMENT ON COLUMN tb_assessment_time.end_time IS '结束时间';
COMMENT ON COLUMN tb_assessment_time.time_limit IS '是否限时';
COMMENT ON COLUMN tb_assessment_time.time_limit_minutes IS '限时分钟数';
COMMENT ON COLUMN tb_assessment_time.grade IS '入学年份（如 2024、2025）';

COMMENT ON TABLE tb_assessment_question IS '考核题目表';
COMMENT ON COLUMN tb_assessment_question.assessment_time_id IS '所属考核时间ID';
COMMENT ON COLUMN tb_assessment_question.question_no IS '题目序号';
COMMENT ON COLUMN tb_assessment_question.question_type IS '题目类型：SINGLE_CHOICE/MULTIPLE_CHOICE/FILE_UPLOAD/ALGORITHM';
COMMENT ON COLUMN tb_assessment_question.title IS '考题标题';
COMMENT ON COLUMN tb_assessment_question.content IS '题目内容（JSON）';
COMMENT ON COLUMN tb_assessment_question.attachment_id IS '附件文件ID';
COMMENT ON COLUMN tb_assessment_question.score IS '满分分值';

COMMENT ON TABLE tb_assessment_answer IS '考核答案表';
COMMENT ON COLUMN tb_assessment_answer.user_id IS '考生用户ID';
COMMENT ON COLUMN tb_assessment_answer.question_id IS '题目ID';
COMMENT ON COLUMN tb_assessment_answer.content IS '答案内容';
COMMENT ON COLUMN tb_assessment_answer.language IS '编程语言';
COMMENT ON COLUMN tb_assessment_answer.file_id IS '文件答案ID';
COMMENT ON COLUMN tb_assessment_answer.submit_time IS '提交时间';

COMMENT ON TABLE tb_assessment_session IS '考核会话表';
COMMENT ON COLUMN tb_assessment_session.user_id IS '用户ID';
COMMENT ON COLUMN tb_assessment_session.assessment_time_id IS '考核时间ID';
COMMENT ON COLUMN tb_assessment_session.start_time IS '首次查看考题时间';
COMMENT ON COLUMN tb_assessment_session.deadline IS '考核截止时间';

COMMENT ON TABLE tb_comment IS '评论表';
COMMENT ON COLUMN tb_comment.answer_id IS '关联的答案ID';
COMMENT ON COLUMN tb_comment.user_id IS '评论者ID';
COMMENT ON COLUMN tb_comment.content IS '评论内容';
COMMENT ON COLUMN tb_comment.score IS '评分';
COMMENT ON COLUMN tb_comment.comment_time IS '评论时间';

-- 审计
COMMENT ON TABLE tb_audit IS '审计日志表';
COMMENT ON COLUMN tb_audit.action_arg IS '应用层传参（JSON）';
COMMENT ON COLUMN tb_audit.action_user_id IS '操作人ID';
COMMENT ON COLUMN tb_audit.action_time IS '操作时间';
COMMENT ON COLUMN tb_audit.ip_address IS '客户端IP';
COMMENT ON COLUMN tb_audit.user_agent IS '客户端User-Agent';
COMMENT ON COLUMN tb_audit.success_state IS '操作状态';
COMMENT ON COLUMN tb_audit.request_method IS 'HTTP请求方法';
COMMENT ON COLUMN tb_audit.request_uri IS '请求URI';
COMMENT ON COLUMN tb_audit.http_status IS 'HTTP状态码';
COMMENT ON COLUMN tb_audit.response_message IS '响应消息';
COMMENT ON COLUMN tb_audit.stack_trace IS '异常堆栈';
COMMENT ON COLUMN tb_audit.duration_ms IS '执行耗时（毫秒）';
COMMENT ON COLUMN tb_audit.request_uri_pattern IS 'URI模式（用于统计聚合）';

-- 二维码
COMMENT ON TABLE tb_qrcode IS '二维码表';
COMMENT ON COLUMN tb_qrcode.file_id IS '关联文件ID';
COMMENT ON COLUMN tb_qrcode.type IS '二维码类型';
COMMENT ON COLUMN tb_qrcode.epoch IS '考核轮次（仅ASSESSMENT类型）';
COMMENT ON COLUMN tb_qrcode.direction IS '方向（仅ASSESSMENT类型）';
COMMENT ON COLUMN tb_qrcode.is_shared IS '是否三方向共用';

-- 竞赛
COMMENT ON TABLE tb_competition IS '竞赛表';
COMMENT ON COLUMN tb_competition.name IS '竞赛名称';
COMMENT ON COLUMN tb_competition.short_name IS '竞赛简称';
COMMENT ON COLUMN tb_competition.logo_file_id IS 'Logo文件ID';
COMMENT ON COLUMN tb_competition.summary IS '竞赛简介';
COMMENT ON COLUMN tb_competition.sort_order IS '排序权重，越大越靠前';
COMMENT ON COLUMN tb_competition.level IS '竞赛级别：national/provincial/school';
COMMENT ON COLUMN tb_competition.month IS '举办月份';
COMMENT ON COLUMN tb_competition.organizer IS '主办单位';
COMMENT ON COLUMN tb_competition.cover_file_id IS '封面文件ID';

-- 方向学习路径
COMMENT ON TABLE tb_direction_learning_step IS '方向学习步骤表';
COMMENT ON COLUMN tb_direction_learning_step.direction IS '方向：computer_vision/embedded/structural_design';
COMMENT ON COLUMN tb_direction_learning_step.step_number IS '步骤序号';
COMMENT ON COLUMN tb_direction_learning_step.title IS '步骤标题';
COMMENT ON COLUMN tb_direction_learning_step.video_url IS '视频链接URL';

-- 场地
COMMENT ON TABLE tb_venue IS '场地表';
COMMENT ON COLUMN tb_venue.name IS '场地名称';
COMMENT ON COLUMN tb_venue.subtitle IS '副标题';
COMMENT ON COLUMN tb_venue.description IS '描述';
COMMENT ON COLUMN tb_venue.image_file_id IS '图片文件ID';
COMMENT ON COLUMN tb_venue.sort_order IS '排序权重';

-- 设备
COMMENT ON TABLE tb_equipment IS '设备表';
COMMENT ON COLUMN tb_equipment.name IS '设备名称';
COMMENT ON COLUMN tb_equipment.brand IS '品牌';
COMMENT ON COLUMN tb_equipment.description IS '描述';
COMMENT ON COLUMN tb_equipment.image_file_id IS '图片文件ID';
COMMENT ON COLUMN tb_equipment.sort_order IS '排序权重';
